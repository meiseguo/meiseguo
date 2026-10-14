import sys
import time
import json
import asyncio
import aiohttp
import okx.Account as Account
from okx.websocket.WsPrivateAsync import WsPrivateAsync

flag = "0"
api_key = sys.argv[1]
secret_key = sys.argv[2]
passphrase = sys.argv[3]
account = sys.argv[4]
headers = {'Content-type': 'application/json'}
api = "http://meiseguo:9990/"
accountAPI = Account.AccountAPI(api_key, secret_key, passphrase, False, flag)
print(f"start account {account}")

session = None
last_data_time = time.time()

async def init_session():
    global session
    session = aiohttp.ClientSession()

async def updateOrder(order):
    global last_data_time
    last_data_time = time.time()
    print(f"[{account}] update order", order["ordId"])
    try:
        async with session.post(api + "report/" + account, json=order, timeout=10) as resp:
            if resp.status != 200:
                return
            result = await resp.json()
            if result["state"] == 200:
                print(f"[{account}] updated", order["ordId"])
    except Exception as ex:
        print(f"[{account}] fail to report", order, ex)

async def check():
    global last_data_time
    last_data_time = time.time()
    try:
        result = await asyncio.to_thread(accountAPI.get_positions)
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
                    async with session.post(url, timeout=10) as resp:
                        if resp.status != 200:
                            print(f"[{account}] fail to report danger, status:", resp.status)
                except Exception as e:
                    print(f"[{account}] fail to report danger", e)
    except Exception as e:
        print(f"[{account}] fail to get positions", e)

def privateCallback(message):
    global last_data_time
    last_data_time = time.time()
    try:
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
                asyncio.create_task(updateOrder(order))
        elif channel == "balance_and_position":
            asyncio.create_task(check())
    except Exception as e:
        print(f"[{account}] fail to parse message", e)


async def main():
    print(f'[{account}] client running')
    await init_session()
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

            # 不需要 watchdog，SDK 内部处理心跳和重连
            # 只需要等待连接断开，SDK 会抛异常
            while True:
                await asyncio.sleep(10)  # 每醒来一次，检查连接状态
                if wsPrivate.websocket is None:
                    print(f"[{account}] connection closed, reconnecting...", flush=True)
                    break
                # 检查底层连接状态
                try:
                    if wsPrivate.websocket.connection is None or wsPrivate.websocket.connection.closed:
                        print(f"[{account}] connection closed, reconnecting...", flush=True)
                        break
                except:
                    break

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