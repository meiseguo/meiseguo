import sys
import json
import asyncio
import requests
import okx.Account as Account
from okx.websocket.WsPrivateAsync import WsPrivateAsync

flag = "0"  # live trading: 0, demo trading: 1
api_key = sys.argv[1]
secret_key = sys.argv[2]
passphrase = sys.argv[3]
account = sys.argv[4]
headers = {'Content-type': 'application/json'}
api = "http://localhost:9990/"
accountAPI = Account.AccountAPI(api_key, secret_key, passphrase, False, flag)
print(f"start account {account}")

def updateOrder(order):
    print("update order", order["ordId"])
    try:
        res = requests.post(api + "report/" + account, headers=headers, json=order)
        print(res.status_code)
        if res.status_code != 200:
            return

        result = json.loads(res.text)
        if result["state"] == 200:
            print("updated", order["ordId"])
    except Exception as ex:
        print(ex)
        print('fail to report', order)


def check():
    result = accountAPI.get_positions()
    if "data" not in result:
        return

    for item in result["data"]:
        if "avgPx" in item:
            liqPx = "0.0"
            if "liqPx" in item:
                liqPx = item["liqPx"]
            if len(liqPx) < 1:
                liqPx = "0.0"
            avgPx = item["avgPx"]
            url = api + "danger/" + account + "/" + item["instId"] + "/" + item["instType"] + "/" + item["mgnMode"] + "/" + liqPx + "/" + avgPx
            print(url)
            res = requests.post(url, headers=headers)
            if res.status_code != 200:
                return


def privateCallback(message):
    print("private Callback")
    obj = json.loads(message)
    print(obj)
    if ("event" in obj) and (obj["event"] == "subscribe" or obj["event"] == "login"):
        return
    if ("arg" not in obj):
        return
    channel = obj["arg"]["channel"]
    if channel == "orders":
        orders = obj["data"]
        for order in orders:
            try:
                updateOrder(order)
            except Exception as e:
                print("fail to update order")

    elif channel == "balance_and_position":
        try:
            check()
        except Exception as e:
            print(e)
            print("fail to check")


async def main():
    print(f' client {account} running')
    argsPrivate = [{"channel": "orders", "instType": "ANY"}, {"channel": "balance_and_position"}]
    wsPrivate = WsPrivateAsync(
        apiKey=api_key,
        passphrase=passphrase,
        secretKey=secret_key,
        url="wss://ws.okx.com:8443/ws/v5/private",
        useServerTime=False
    )
    try:
        await wsPrivate.start()
        await wsPrivate.subscribe(argsPrivate, callback=privateCallback)
        # wait forever
        print(f"client {account} start listening")
        await asyncio.Event().wait()
    finally:
        await wsPrivate.unsubscribe(argsPrivate, callback=privateCallback)
        print(f' client {account} stopped')

if __name__ == '__main__':
    asyncio.run(main())