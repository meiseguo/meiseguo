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
        res = requests.post(url, headers=headers, timeout=5)
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
        res = requests.post(url, headers=headers, timeout=5)
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

async def main():
    print('operator running...')
    while True:
        argsPublic = assetList()
        wsPublic = WsPublicAsync(url="wss://ws.okx.com:8443/ws/v5/public")
        try:
            await wsPublic.start()
            await wsPublic.subscribe(argsPublic, publicCallback)
            await asyncio.Event().wait()
        except Exception as e:
            print("connection lost, reconnect in 5s...", e)
        finally:
            try:
                await wsPublic.unsubscribe(argsPublic, callback=publicCallback)
            except Exception as e:
                print("fail to unsubscribe", e)
            print('operator stopped, reconnecting...')
        await asyncio.sleep(5)

if __name__ == '__main__':
    asyncio.run(main())