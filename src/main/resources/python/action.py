import sys
import json
import time
import asyncio
import requests
import okx.Trade as Trade

flag = "0"
headers = {'Content-type': 'application/json'}
api = "http://localhost:9990/"
accounts = {}

def loadData():
    print("load accounts", accounts)
    url = api + "accounts"
    res = requests.post(url, headers=headers)
    if res.status_code != 200:
        return
    result = json.loads(res.text)
    if result["state"] == 200:
        data = result["data"]
        for account in data:
            accounts[account["account"]] = Trade.TradeAPI(account['apikey'], account['secretkey'], account['passphrase'], False, flag)

def updateOrder(order, state):
    url = api + "update/" + order["ordId"] + "/" + order["clOrdId"] + "/" + state
    res = requests.post(url, headers=headers)
    if res.status_code != 200:
        return

    result = json.loads(res.text)
    if result["state"] == 200:
        print("updated", order["ordId"])


def placeOrder(action):
    size = float(action["amount"]) / float(action["zhang"])
    sz = "%.2f" % size
    ccy = action["ccy"].split('-')[0]
    print("place order", action["side"], action["ccy"], action["tdMode"], sz)
    trade = accounts[action["account"]]
    result = trade.place_order(
        instId=action["ccy"],
        tdMode=action["tdMode"],
        side=action["side"],
        ordType="limit",
        posSide="net",
        px=action["price"],
        ccy=ccy,
        sz=sz,
        clOrdId=action["sn"]
    )
    print("place order", result)
    if result["code"] == "0":
        updateOrder(result["data"][0], "live")
    else:
        updateOrder({"ordId":result["data"][0]["sMsg"], "clOrdId":action["sn"]}, "error")


def actions(account):
    url = api + "actions/" + account
    res = requests.post(url, headers=headers)
    if res.status_code != 200:
        return

    result = json.loads(res.text)
    if result["state"] == 200:
        data = result["data"]
        for action in data:
            try:
                if action["status"] == "cancel":
                    trade = accounts[action["account"]]
                    result = trade.cancel_order(instId=action["ccy"], ordId=action["order"])
                    print("cancel order", result)
                    if result["code"] == "0":
                        updateOrder({"ordId":action["order"], "clOrdId":action["sn"]}, "canceled")
                    else:
                        updateOrder({"ordId":action["order"] + result["data"][0]["sMsg"], "clOrdId":action["sn"]}, "canceled")
                else:
                    placeOrder(action)
            except Exception as e:
                print("fail to place order : " + account, e)



async def main():
    print('operator running...')
    loadData()
    while True:
        for account in accounts:
            try:
                actions(account)
            except Exception as e:
                print("fail to ask actions for account", account)
        await asyncio.sleep(1)
    print("operator stopped")


if __name__ == '__main__':
    asyncio.run(main())