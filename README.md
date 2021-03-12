# HandlerIPC
通过 Handler  、Messenger、Message 实现跨进程通信（IPC）


### Messenger的优势：
- 实际传递的是Message，可以复用信息池
- 支持信息回调
- 不需要编写aidl

## Messenger IPC 架构
![](https://github.com/EmotionalRonanyg/HandlerIPC/blob/main/MessengerIPC.png)
