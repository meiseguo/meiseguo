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
api = "http://meiseguo:9990/"
accountAPI = Account.AccountAPI(api_key, secret_key, passphrase, False, flag)
print(f"start account {account}")

def updateOrder(order):
    print(f"[{account}] update order", order["ordId"])
    try:
        res = requests.post(api + "report/" + account, headers=headers, json=order, timeout=10)
        if res.status_code != 200:
            return

        result = json.loads(res.text)
        if result["state"] == 200:
            print(f"[{account}] updated", order["ordId"])
    except Exception as ex:
        print(f"[{account}] fail to report", order, ex)

def check():
    try:
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
                try:
                    res = requests.post(url, headers=headers, timeout=10)
                    if res.status_code != 200:
                        print(f"[{account}] fail to report danger, status:", res.status_code)
                except Exception as e:
                    print(f"[{account}] fail to report danger", e)
    except Exception as e:
        print(f"[{account}] fail to get positions", e)

def privateCallback(message):
    try:
        print(f"[{account}] RAW message: {str(message)[:100]}")
        if not isinstance(message, str) or not message.startswith(("{", "[")):
            return
        obj = json.loads(message)
        if ("event" in obj) and (obj["event"] == "subscribe" or obj["event"] == "login"):
            return
        if ("arg" not in obj):
            return
        channel = obj["arg"]["channel"]
        if channel == "orders" and "data" in obj:
            orders = obj["data"]
            for order in orders:
                try:
                    asyncio.create_task(asyncio.to_thread(updateOrder, order))
                except Exception as e:
                    print(f"[{account}] fail to update order", e)
        elif channel == "balance_and_position":
            try:
                asyncio.create_task(asyncio.to_thread(check))
            except Exception as e:
                print(f"[{account}] fail to check", e)
    except Exception as e:
        print(f"[{account}] fail to parse message", e)

async def main():
    print(f'[{account}] client running')
    argsPrivate = [{"channel": "orders", "instType": "ANY"}, {"channel": "balance_and_position"}]
    while True:
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
            print(f"[{account}] start listening")
            # 关键：主动消费消息，异常会从这里抛出
            await wsPrivate.consume()
            print("end of private ws")
        except Exception as e:
            print(f"[{account}] connection lost: {e}")
        finally:
            try:
                await wsPrivate.unsubscribe(argsPrivate, callback=privateCallback)
            except Exception as e:
                print(f"[{account}] fail to unsubscribe: {e}")
            print(f"[{account}] stopped, reconnecting...")
        await asyncio.sleep(5)

if __name__ == '__main__':
    asyncio.run(main())