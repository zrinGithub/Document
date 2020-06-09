# Kafka权威指南

文档为 [《Kafka权威指南》](https://www.ituring.com.cn/book/2067) 笔记



## 1. 概念

特点：

- 数据按照一定顺序持久化，按需读取
- 分布式，具备数据故障保护和性能伸缩能力



### 消息与批次

**消息**中键是可选的元数据，可以**通过键控制消息写入不同分区**（比如散列来进行分布或者依据业务把相同类型数据放在同一个分区）。



**批次**就是一组消息，**同属于一个主题与分区**。为了提高效率，消息**分批次写入**Kafka。



批次越大，单个消息传输时间越长。批次小则会造成大量网络开销。



消息模式（Schema）:可以选用xml、json或者Apache Avro



### 主题与分区

一般把一类（业务需求）消息放在同一个主题（Topic），消息之于主题就像数据行与表。

主题有分为多个分区：

![](.\images\主题与分区.png)

每个分区都是一个队列（FIFO），单个分区可以保证消息顺序，但是在**主题范围无法保证消息顺序**。



**分区的意义在于：分布在不同服务器实现数据冗余和伸缩性。**



### 生产者与消费者

需要发布消息到制定的主题时：

1. 生产者将消息发布到分区（默认均衡，**可以通过消息键和分区器控制**）
2. 消费者**订阅主题**并按照消息生产顺序读取



------



关于消费者：

- 消费者统建检查消息**偏移量**来区分读取的消息

- 消费者将最后读取的偏移量保存到Zookeeper或者Kafka上面用于**保存读取记录**。



**消费组是实现单播放**的一种方法：

在同一个消费组中：

同一个分区只能被一个消费者使用

从消息的角度，每个消息只会处理一次。

![](.\images\消费组.png)



### Broker与集群

Broker：一个独立Kafka服务器

- 接收生产者的消息
- 设置偏移量
- 响应消费者读取请求



在集群中，选举一个broker作为控制器负责将分区分发到broker。

一个分区可以通过分配给多个broker（分区复制）来实现消息冗余。

![](.\images\分区与集群.png)

如图，如果Broker1失效之后，Broker2开始接管分区0，生产者消费者可以连接Broker2来继续操作。



Broker的保留策略包括限制时间和限制大小，**主题可以配置对应消息保留策略**。



### 多集群

Kafka提供MirrorMaker在集群间复制消息。

MirrorMaker包含一个生产者和一个消费者，消费者从一个集群读取消息，生产者把消息发送到另一个集群。



## 2. 安装

测试环境：Centos7 64位



安装过程参考另外一篇笔记：[Kafka笔记](https://github.com/zrinGithub/Document/blob/master/kafka/kafka%E7%AC%94%E8%AE%B0.md)



### broker配置

```shell
vim config/server.properties

#常用配置
#在集群里面需要保证唯一性
broker.id=1
port=
# 指定日志存储路径
log.dirs=/opt/data/kafka/logs
# 连接到zookeeper集群
zookeeper.connect=server-1:2181,server-2:2181,server-3:2181
```



### 使用

#### 加入broker

考虑下面这几个因素：



1. 数据大小

如果集群保存10T数据，单个broker存储2T，那么需要5个。考虑数据复制参数，如果为只复制一份，那么需要10个broker。



2. 集群处理请求的能力。

------

要把新的broker加入到集群里面需要修改配置：

- 配置相同的`zookeeper.connect`
- 为`broker.id`在集群内指定唯一值



### API说明

[官网说明](https://kafka.apache.org/documentation/#producerapi)



#### maven依赖

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.5.0</version>
</dependency>
```







## 3. 生产者

![](.\images\生产者.png)

生产者发送消息的流程包括：

- 组装`ProducerRecord`，包括主题与消息，分区和键也可以指定
- 序列化
- 如果制定了分区，那么分区控制器不载工作，否则控制分区选择
- 消息被添加到一个批次
- 该批次所有消息发送到指定主题和分区上
- Broker收到消息后返回响应，成功返回`RecordMetaData`记录了主题分区信息。



下面结合API看一下：

### 创建生产者



```java
    private void createProducer() {
        Properties kafkaProps = new Properties();
        //指定broker地址，不需要提供所有的地址，生产者可以连接给定的broker找到其他地址信息
        //但是至少提供两个避免宕机
        kafkaProps.put("bootstrap.servers", "server-1:9092,server-2:9092");
        //为键和值指定序列化器：
        //默认的可以看org.apache.kafka.common.serialization.Serializer的继承树
        //基本类型都实现了，不用自己写
        kafkaProps.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(kafkaProps);
    }
```



### 发送消息

发送分为3种方式：

1. 发送后忘记：发送后不再附加操作，虽然生产者会自动尝试重发，但是也可能丢失消息。
2. 同步发送：发送后返回`Future`对象，调用`get()`方法进行等待。
3. 异步发送：发送指定回调函数。

**生产者可以使用多线程来提高吞吐量**。



```java
    private void sendMsg() {
        //创建消息，指定主题、键值、消息内容
        ProducerRecord<String, String> record =
                new ProducerRecord<>("CustomerCountry", "Precision Products",
                        "France");
        try {
            //发送消息，用于不重要的消息日志
//            producer.send(record);
            //同步发送，拿到消息存储信息（偏移量、主题、分区）
//            RecordMetadata metadata = producer.send(record).get();
            //异步发送，指定回调函数
            producer.send(record, (recordMetadata, exception) -> {
                if (exception != null)
                    log.error(exception.getMessage());
                else
                    log.debug(recordMetadata.topic() + recordMetadata.offset() + recordMetadata.partition());
            });
        } catch (Exception e) {
            log.error("send error: " + e.getMessage());
        }
    }
```







