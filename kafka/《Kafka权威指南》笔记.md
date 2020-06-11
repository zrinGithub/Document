# Kafka权威指南

文档为 [《Kafka权威指南》](https://www.ituring.com.cn/book/2067) 笔记

参考源码：https://github.com/gwenshap/kafka-examples



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

官网有对应参数的说明：[Producer Configs](https://kafka.apache.org/documentation.html#producerconfigs)：



#### 保证消息顺序

`max.in.flight.requests.per.connection`参数表示生产者收到服务器响应前可以发送多少消息。

`retries`生产者可以重试发送消息的次数。

需要保证消息顺序，那么首先应该保证消息是否写入成功，`retries`不能为0，可以把`max.in.flight.requests.per.connection`设为1，这样生产者在尝试发送第一批消息的时候，不会有其他消息发送给broker。



#### 实现序列化器

实现`Serializer`接口，并在配置中指定



#### 分区器

键值为null并使用默认分区，则分区器使用轮询（Round）分布。

键值不为null使用默认分区，使用散列算法（Kafka实现，不会因为Java版本变化），同时，如果加了新的分区，对应键的分区会发生改变。

我们可以实现`Patition`接口实现自己的分区器：

```java
public class MyPartition implements Partitioner {
    @Override
    public int partition(String topic, Object key, 
                         byte[] keyBytes, Object value, 
                         byte[] valueBytes, Cluster cluster) {
        //获取指定主题的全部分区
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int partitionSize = partitions.size();

        if ((keyBytes == null) || !(key instanceof String)) {
			//键不是字符串的处理
        }

        if(((String)key).equals("AAA"))
            return partitionSize;   //放在最后一个分区
        return 0;//其他分配到分区0
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
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





## 4. 消费者

### 消费组

从**单个消费组**的角度来看，所有消费者订阅同一个主题，每个消费者接收该主题一部分分区的信息。

![](.\images\消费者与分区.png)

如果只有一个消费者，那么将接收全部的分区消息。

如果消费者过多，那么会有闲置的消费者。

因此，同一个消费组下，**不要让消费者数量超过主题分区的数量**。

------





如果有**多个消费组**，它们之间是互不影响的。

![](.\images\消费组与分区.png)



#### 分区再均衡

当分区所有权转移的行为被称为**再均衡**：

群组中新的消费者A加入的时候，开始读取原本是其他消费者读取的消息。

当消费者A关闭的时候，离开群组，A之前读取的分区开始由其他消费者读取。



当主题有变化（如新增分区），会发生**分区重分配**。



**再均衡**期间，群组会不可用，消费者的读取状态丢失（需要再次读取缓存）。



**消费者使用心跳方式证明自己活跃。**



### 创建消费者

```java
private void createConsumer() {
        Properties kafkaProps = new Properties();
        //指定broker地址，不需要提供所有的地址，生产者可以连接给定的broker找到其他地址信息
        //但是至少提供两个避免宕机
        kafkaProps.put("bootstrap.servers", "server-1:9092,server-2:9092");
        //指定消费组
        kafkaProps.put("group.id", "CountryCounter");
        //为键和值指定反序列化器：默认的可以看org.apache.kafka.common.serialization.Serializer的继承树
        //基本类型都实现了，不用自己写
        kafkaProps.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(kafkaProps);
    }
```

其余配置查看官方文档：[consumer configs](https://kafka.apache.org/documentation.html#consumerconfigs)



### 订阅主题

```java
//consumer.subscribe(Collections.singletonList("myTopics"));
//订阅相关主题
cosumer.subscribe("test.*");
```



### 轮询

消费者订阅主题之后，由轮询向服务器请求数据，轮询复制处理细节（群组协调、分区再平衡、发送心跳、获取数据）。

```java
    public void consume() {
        try {
            //无限循环->消费者本来就是一个长期运行的应用程序
            while (true) {
                //保持轮询，否则被认为已经死亡
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                //记录列表
                for (ConsumerRecord<String, String> record : records) {
                    log.debug("topic = %s, " +
                                    "partition = %s, " +
                                    "offset = %d, " +
                                    "customer = %s," +
                                    "country = %s\n",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.key(),
                            record.value());
                }
            }
        } finally {
            //关闭消费者
            consumer.close();
        }
    }
```



### 提交和偏移量

调用`poll()`方法来获取未消费的记录，在Kafka中不需要得到消费者的确认。



消费者可以使用Kafka追踪消息的偏移量，也就是消息在分区的位置。



如果提交的偏移量小于客户端处理的最后一个消息偏移量，那么中间的消息就会重复处理。

![](.\images\偏移量1.png)









提交量大于客户端处理最后一个消息的偏移量，那么中间的消息将会丢失。

![](.\images\偏移量2.png)





#### 自动提交

消费者可以自动提交偏移量，只需要加上配置`enable.auto.commit`为true（默认值）。

提交的时间间隔是由配置`auto.commit.interval.ms`来设定的。

默认这个值是5000，也就是每过5s，消费者会自动把从`poll()`方法接收到的最大偏移量提交。

自动提交也是在轮询里面进行的。



同时，也存在一些问题：

1. 5s内产生在**再均衡，偏移量未提交**，新接管的消费者会对消息重复消费，可以减少`auto.commit.interval.ms`提交间隔，但是无法完全避免。

2. 同时每次轮询都会把上次调用返回的偏移量提交，需要自己在调用之前**确保所有返回的消息都处理完成**。



#### 同步手动提交偏移量

设置`enable.auto.commit`为false来手动提交偏移量。

```java
		//关闭自动提交偏移量
        kafkaProps.put("enable.auto.commit", "false");
```



`commitSync()`会提交由`poll()`返回的最新偏移量，所以在处理完消息后调用。没有不可恢复的错误发生，`commitSync()`会一直尝试提交。

```java
        while (true) {
            //保持轮询，否则被认为已经死亡
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            //处理记录
            for (ConsumerRecord<String, String> record : records) {
                //
            }
            try {
                //提交当前偏移量
                consumer.commitSync();
            } catch (CommitFailedException e) {
                log.error("commit failed", e);
            }
        }
```

**如果发生了再均衡，还是会有重复消费的情况产生。**



#### 异步提交

同步提交`commitSync()`会导致broker在响应提交请求之前会**阻塞**。

如果我们降低提交的频率，再均衡发生会增加重复消费的消息数量。

------



使用异步提交无需等待响应。

```java
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                //处理消息
            }
            try {
                //异步提交
                consumer.commitAsync();
            } catch (CommitFailedException e) {
                log.error("commit failed", e);
            }
        }
```

`commitAsync()`不会进行重试（如果重试，可能新的偏移量都提交成功了，出现再均衡就会重复消费）



和所有异步工作方式一样，`commitAsync()`提供回调函数，可以用于记录错误、生成度量指标。

```java
consumer.commitAsync(((offsets, exception) -> {
                    if (exception != null)
                        log.error("error", offsets, exception);
                    //offsets的key就是主题分区，value是偏移量和元数据
                    log.debug(offsets.keySet().toString());
                }));
```



**重试异步提交**：

我们可以维护一个递增序列号，提交偏移量后递增，重试之前检查序列号，如果相同则可以安全重试，如果序列号更大，说明有新的提交了，则应该停止重试。





#### 同步异步组合提交

因为临时问题导致偶尔的提交失败，可以不进行重试（后续会有成功的）。

但是如果是**关闭消费者或者再均衡前最后一次提交**，需要保证提交成功。



这里使用同步异步组合提交解决关闭消费者前的情况（发生再均衡前也可以提交偏移量，后面[再均衡监听器](#再均衡监听器)会有讲到）：

```java
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    //处理消息
                }
                try {
                    //这里使用异步提交，比较阻塞，速度更快
                    consumer.commitAsync();
                } catch (CommitFailedException e) {
                    log.error("commit failed", e);
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            try {
                //关闭消费者之前，没有下一次提交来保障，所以必须使用同步，不断重试直到成功
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
```



#### 提交特定的偏移量

无论是同步还是异步，之前讲到的`commitAsync()`、`commitSync()`都是提交`poll()`的最后一个偏移量，也就是每次提交批次的偏移量。



如果`poll()`获取的数据太多，我们希望在中间提交偏移量怎么办？

因为消费者可能不仅读取一个分区，所以我们需要提供指定的主题分区。

```java
 	//记录偏移量
    private Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    int count = 0;
	......
        
        
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    //......处理消息
                    currentOffset.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1, "metadata"));
                    //10000条数据异步提交一次
                    if (count % 1000 == 0)
                        consumer.commitAsync(currentOffset, null);//不提供回调函数
                    count++;
                }
            }
```



#### 再均衡监听器

消费者在退出和进行分区再均衡之前，需要做一系列清理工作。

在`subscribe()`方法传入`ConsumerRebalanceListener`监听各个状态分别处理：

```java
public interface ConsumerRebalanceListener {
    //再均衡开始之前和消费者停止读取消息之后，这里提交了偏移量，那么接管分区的消费者不会重复消费
    void onPartitionsRevoked(Collection<TopicPartition> partitions);
    
    //重新分配分区之后和消费者开始读取消息之前。
    void onPartitionsAssigned(Collection<TopicPartition> partitions);
    
    default void onPartitionsLost(Collection<TopicPartition> partitions) {
        onPartitionsRevoked(partitions);
    }	
}
```



我们可以在再均衡发生前提交偏移量，这里我们提交的是最近处理的偏移量，而不是还在处理的最后一个偏移量（分区可能在处理消息的时候被撤回）：

```java
    //记录偏移量
    private Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    int count = 0;

    private class HandleRebalance implements ConsumerRebalanceListener {
        /**
         * @param partitions
         */
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            log.debug("Lost partitions in rebalance . Committing current offset:" + currentOffset);
            consumer.commitSync(currentOffset);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        }
    }

    //提交特定偏移量
    public void commitMsg() {
        try {
            consumer.subscribe(Collections.singletonList("myTopics"), new HandleRebalance());

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    //......处理消息
                    currentOffset.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1, "metadata"));
                }
                consumer.commitAsync(currentOffset, null);
            }
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
```





#### 从特定的偏移量处理数据

从分区的起始位置开始读取消息：

`seekToBeginning(Collection<TopicPartition> tp)`



分区末尾开始读取：

`seekToEnd(Collection<TopicPartition> tp)`



`seek()`指定偏移位置

------



**如果我们从Kafka读取数据，然后将消息入库，不想丢失任何数据，也不希望保存重复的结果。**



如下的代码，即使我们处理一条数据就提交一次（牺牲吞吐量），记录入库之后以及偏移量提交前发生错误会导致重复的记录：

```java
while (true) {
	ConsumerRecords<String, String> records = consumer.poll(100);
	for (ConsumerRecord<String, String> record : records){
        currentOffsets.put(new TopicPartition(record.topic(),
        record.partition()),
        new OffsetAndMetadata(record.offset()+1);
        //处理数据
        processRecord(record);
		//入库
        storeRecordInDB(record);
		//提交偏移量
        consumer.commitAsync(currentOffsets);
    }
}
```



我们希望入库和提交偏移量是一个原子操作避免重复提交：

在同一个事务把消息和偏移量都加到数据库里面（相当于版本号的乐观锁）

偏移量保存到数据库而不是Kafka的时候，消费者得到新的分区时需要使用`seek()`方法从数据库的偏移量开始处理数据。

```java
   //记录偏移量
    private Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    int count = 0;

    private class SaveOffsetOnRebalance implements ConsumerRebalanceListener {
     	//再均衡开始之前和消费者停止读取消息之后，这里提交了偏移量，那么接管分区的消费者不会重复消费
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            //提交数据库事务
            commitDBTransaction();
        }
		//重新分配分区之后和消费者开始读取消息之前。
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            for (TopicPartition partition : partitions)
                consumer.seek(partition, getOffsetFromDB(partition));
        }

    }

    //从数据库获取偏移量
    private long getOffsetFromDB(TopicPartition partition) {
        return 0;
    }

    //提交数据库事务
    private void commitDBTransaction() {
    }

    //提交特定偏移量
    public void commitMsg() {
        try {
            consumer.subscribe(Collections.singletonList("myTopics"), new SaveOffsetOnRebalance());
            //调用poll之后让消费者加入到群组里面分配分区
            consumer.poll(Duration.ofMillis(0));
            //获取分区的偏移量，seek之后获取位置，下一次调用poll才会开始读取
            for (TopicPartition partition : consumer.assignment())
                consumer.seek(partition, getOffsetFromDB(partition));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    //1. 处理消息
                    //2. 消息入库
                    //3. 偏移量入库
                }
                //4. 提交数据库事务
                commitDBTransaction();
            }
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
```



#### 如何退出循环

在另外一个线程调用`consumer.wakeup()`方法来退出循环（如果是主线程，调用ShutdownHook）



调用`wakeup()`可以退出`poll()`并抛出`WakeupException`异常。

不过，还是要保证`consumer.close()`，这是提交没有提交的东西，并且向群组协调器发送消息。

```java
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Starting exit...");
                consumer.wakeup();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        try {
            movingAvg.consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                ConsumerRecords<String, String> records = movingAvg.consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    //......
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            movingAvg.consumer.close();
            System.out.println("Closed consumer and we are done");
        }
    }
```



#### 反序列化

实现`Deserializer`接口或者使用`Avro`做反序列化



### 独立消费者

如果不需要消费者群组和处理再均衡，只需要把主题或者分区分配给独立的消费者即可。

```java
	List<PartitionInfo> partitionInfos = null;
	//请求主题可用的分区
	partitionInfos = consumer.partitionsFor("topic"); 
    
	if (partitionInfos != null) {
        for (PartitionInfo partition : partitionInfos)
        	partitions.add(new TopicPartition(partition.topic(), partition.partition()));
        //分配分区
        consumer.assign(partitions); 
        while (true) {
            //添加了新的分区，消费者不会收到通知
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record: records) {
            	System.out.println("topic = %s, partition = %s, offset = %d,
            	customer = %s, country = %s\n",
            	record.topic(), record.partition(), record.offset(),
            	record.key(), record.value());
        	}
        	consumer.commitSync();
    	}
	}
```



## 5. 深入

### 集群与zk节点

Zookeeper负责维护集群信息：

- broker启动。创建临时节点注册id到zk（/brokers/ids）
- kafka组件订阅该路径（/brokers/ids），当有broker加入或退出集群，能够得到通知。
- 如果broker id重复，注册失败（zk 节点唯一校验）
- broker需要关闭（停机、长时间无响应），zk上的临时节点被删除，zk通知kafka组件。



### 控制器

除了作为一般的broker功能外，控制器负责分区leader选举。

- 集群第一个启动的broker作为控制器在zk创建临时节点/controller
- 其他broker试图创建/controller节点失败后，创建zookeeper watch对象监控该节点的变更
- 控制器被关闭或断开zk连接，临时节点小时，其他broker通过watch得到通知并尝试成为控制器（先创建节点的成为控制器，同样的创建失败的在新的控制器节点创建watch）。

- 控制器通过zk节点知道有broker A离开集群后，A的分区如果是leader那么需要遍历这些分区，确定新的leader位置。然后向对应broker发送请求。新的分区leader开始处理来自生产者与消费者的请求，follower开始从新的leader那里复制消息。
- **控制器使用epoch（版本号与位移）来避免脑裂**。



### 复制

每个分区都有多个副本，只有一个leader副本来处理生产者与消费者的请求，其他都是follower副本只负责复制leader的消息。









