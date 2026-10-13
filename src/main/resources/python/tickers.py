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

def askStrategy(ticker):
    url = api + "ticker/" + ticker["instId"] + "/" + ticker["last"] + "/" + ticker["ts"]
    try:
        res = requests.post(url, headers=headers, timeout=10)  # 5秒改成10秒
        if res.status_code != 200:
            print("fail to report ticker", ticker, "status:", res.status_code)
    except Exception as e:
        print("fail to report ticker", ticker, "error:", e)

def publicCallback(message):
    try:
        obj = json.loads(message)
        if ("event" in obj) and (obj["event"] == "subscribe"):
            return
        if ("arg" not in obj):
            return
        channel = obj["arg"]["channel"]
        if channel == "tickers":
            tickers = obj["data"]
            for ticker in tickers:
                askStrategy(ticker)
    except Exception as e:
        print("fail to ask strategy", e)
        print('okx message: ', message)

def assetList():
    argsPublic = [{"channel":"tickers","instId":"DOGE-USDT-SWAP"}]
    print("load assets, default: ", argsPublic)
    url = api + "assets"
    try:
        res = requests.post(url, headers=headers, timeout=10)  # 5秒改成10秒
        if res.status_code != 200:
            print("fail to load assets, status:", res.status_code)
            return argsPublic
        result = json.loads(res.text)
        if result["state"] == 200:
            data = result["data"]
            argsPublic = [{"channel":"tickers","instId":x} for x in data]
            print("load assets, result: ", argsPublic)
    except Exception as e:
        print("fail to load assets", e)
    return argsPublic

async def heartbeat(ws, stop_evt):
    print("[Tickers] heartbeat STARTED", flush=True)
    while not stop_evt.is_set():
        try:
            await ws.websocket.send("ping")
            print(f"[Tickers] PING sent {time.strftime('%H:%M:%S')}", flush=True)
        except Exception as e:
            print(f"[Tickers] heartbeat FAIL: {e}", flush=True)
            stop_evt.set()
            break
        await asyncio.sleep(20)

async def main():
    print('operator running...')
    reconnect_delay = 5  # 初始重连延迟
    while True:
        argsPublic = assetList()
        wsPublic = WsPublicAsync(url="wss://ws.okx.com:8443/ws/v5/public")
        hb_task = None
        try:
            consume_task = await wsPublic.start()
            hb_task = asyncio.create_task(heartbeat(wsPublic))
            print("[Tickers] heartbeat TASK CREATED", flush=True)
            await wsPublic.subscribe(argsPublic, publicCallback)
            print("start listening...", flush=True)

            # 并发等待：接收任务 或 心跳任务，任一结束就退出
            done, pending = await asyncio.wait(
                {consume_task, hb_task},
                return_when=asyncio.FIRST_COMPLETED
            )
            # 如果心跳先失败，consume_task 还在跑，需要取消它
            if hb_task in done:
                print("[Tickers] heartbeat stopped, forcing reconnect", flush=True)
                consume_task.cancel()
            reconnect_delay = 5
        except Exception as e:
            print(f"connection lost, reconnect in {reconnect_delay}s...", e)
        finally:
            if hb_task and not hb_task.done():
                hb_task.cancel()
            # 只在连接还活着时尝试 unsubscribe，避免报错
            if wsPublic.websocket is not None:
                try:
                    await wsPublic.unsubscribe(argsPublic, callback=publicCallback)
                except Exception as e:
                    print("fail to unsubscribe (ignore):", e)
            print('operator stopped, reconnecting...')
        # 指数退避：5s -> 10s -> 20s -> 40s -> 60s（封顶）
        await asyncio.sleep(reconnect_delay)
        reconnect_delay = min(reconnect_delay * 2, 60)

if __name__ == '__main__':
    asyncio.run(main())