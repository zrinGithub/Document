# 死信队列与延时队列

[TOC]

## TTL与超时

https://www.rabbitmq.com/ttl.html



## 死信队列

https://www.rabbitmq.com/dlx.html

死信队列 (dead-letter) 将下面情况的消息重新发布到exchange：

- 消息通过消费者使用`basic.reject` `basic.nack` 否定确认，并且`requeue`参数设置为`false`
- 已经超时（设置了TTL参数），队列过期不会使里面的消息成为死信
- 消息数量超多最大设置



死信配置的exchange就是普通的exchange。







## 延时队列

