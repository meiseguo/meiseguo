### Design of SafeMoney

Join **meiseguo** ✨ https://t.me/meiseguo ✨
Here are some ideas to get you started:

- 🌱 Account
- 🌱 Safety
- 🌱 Asset
- 🌱 Alarm
- 🌱 Alert
- 🌱 Action
- 🌱 Result
- 🌱 Report
- 🌱 Strategy
- 🌱 Status
- 🌱 Operator
- 🌱 Mode

'''mermaid
graph TD
A[开始] --> B[获取当前价格]
B --> C{条件Strategy}
C --> |Strategy=0.0| D[不动]
C --> |Strategy=1.0| E[加仓]
C --> |Strategy=0.5| F[减仓]
D --> B
'''
