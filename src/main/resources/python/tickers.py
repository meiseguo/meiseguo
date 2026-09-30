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
api = "http://localhost:9990/"

def askStrategy(ticker):
    url = api + "ticker/" + ticker["instId"] + "/" + ticker["last"] + "/" + ticker["ts"]
    res = requests.post(url, headers=headers)
    if res.status_code != 200:
        print("fail to report ticker", ticker)


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
    res = requests.post(url, headers=headers)
    if res.status_code != 200:
        return argsPublic
    result = json.loads(res.text)
    if result["state"] == 200:
        data = result["data"]
        argsPublic = [{"channel":"tickers","instId":x} for x in data]
        print("load assets, result: ", argsPublic)
        return argsPublic

async def main():
    print('operator running...')
    argsPublic = assetList()
    wsPublic = WsPublicAsync(url="wss://ws.okx.com:8443/ws/v5/public")
    try:
        await wsPublic.start()
        await wsPublic.subscribe(argsPublic, publicCallback)
        await asyncio.Event().wait()
    except Exception as e:
        print("fail to subscribe", e)
    finally:
        await wsPublic.unsubscribe(argsPublic, callback=publicCallback)
        print('operator stopped')


if __name__ == '__main__':
    asyncio.run(main())