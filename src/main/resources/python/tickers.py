import sys
import json
import time
import asyncio
import requests
import okx.Trade as Trade
from okx.websocket.WsPublicAsync import WsPublicAsync

flag = "0"
off = 1
on  = 0
headers = {'Content-type': 'application/json'}
api = "http://meiseguo:9990/"

last_tick_time = time.time()

async def askStrategy(ticker):
    global last_tick_time
    last_tick_time = time.time()
    url = api + "ticker/" + ticker["instId"] + "/" + ticker["last"] + "/" + ticker["ts"]
    try:
        res = await asyncio.to_thread(requests.post, url, headers=headers, timeout=10)
        if res.status_code != 200:
            print("fail to report ticker", ticker, "status:", res.status_code, flush=True)
    except Exception as e:
        print("fail to report ticker", ticker, "error:", e, flush=True)

def publicCallback(message):
    try:
        if isinstance(message, str) and message.strip().lower() == "pong":
            return
        obj = json.loads(message)
        if ("event" in obj) and (obj["event"] == "subscribe"):
            return
        if ("arg" not in obj):
            return
        channel = obj["arg"]["channel"]
        if channel == "tickers":
            tickers = obj["data"]
            for ticker in tickers:
                asyncio.create_task(askStrategy(ticker))
    except Exception as e:
        print("fail to ask strategy", e, flush=True)
        print('okx message: ', message, flush=True)

async def assetList():
    argsPublic = [{"channel":"tickers","instId":"DOGE-USDT-SWAP"}]
    print("load assets, default: ", argsPublic, flush=True)
    url = api + "assets"
    try:
        res = await asyncio.to_thread(requests.post, url, headers=headers, timeout=10)
        if res.status_code != 200:
            print("fail to load assets, status:", res.status_code, flush=True)
            return argsPublic
        result = json.loads(res.text)
        if result["state"] == 200:
            data = result["data"]
            argsPublic = [{"channel":"tickers","instId":x} for x in data]
            print("load assets, result: ", argsPublic, flush=True)
    except Exception as e:
        print("fail to load assets", e, flush=True)
    return argsPublic

async def watchdog(stop_evt):
    while not stop_evt.is_set():
        await asyncio.sleep(10)
        if time.time() - last_tick_time > 30:
            print(f"[Tickers] no tick for 30s, reconnecting...", flush=True)
            stop_evt.set()
            break

async def main():
    print('operator running...', flush=True)
    reconnect_delay = 5
    while True:
        argsPublic = await assetList()
        wsPublic = WsPublicAsync(url="wss://ws.okx.com:8443/ws/v5/public")
        wd_task = None
        stop_evt = asyncio.Event()
        try:
            consume_task = await wsPublic.start()
            await wsPublic.subscribe(argsPublic, publicCallback)
            print("start listening...", flush=True)
            wd_task = asyncio.create_task(watchdog(stop_evt))
            await stop_evt.wait()
            if consume_task and not consume_task.done():
                consume_task.cancel()
            reconnect_delay = 5
        except Exception as e:
            print(f"connection lost, reconnect in {reconnect_delay}s...", e, flush=True)
        finally:
            if wd_task and not wd_task.done():
                wd_task.cancel()
            if wsPublic.websocket is not None:
                try:
                    await wsPublic.unsubscribe(argsPublic, callback=publicCallback)
                except Exception as e:
                    print("fail to unsubscribe (ignore):", e, flush=True)
            print('operator stopped, reconnecting...', flush=True)
        await asyncio.sleep(reconnect_delay)
        reconnect_delay = min(reconnect_delay * 2, 60)

if __name__ == '__main__':
    asyncio.run(main())