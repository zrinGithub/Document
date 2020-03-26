# redis入门笔记

[TOC]



## 一. 应用说明



### 1. 介绍

- NoSQL

  - NoSQL是不同于传统的关系数据库的数据库管理系统的统称。其两者最重要的区别是NoSQL不使用SQL作为查询语言。 NoSQL数据存储可以不需要固定的表格模式。NoSQL是基于键值对的，可以想象成表中的主键和值的对应关系。

  - NoSQL：redis、memcached、mongodb、guava（loadingCache）

     

- redis的定义：

  - Redis 是一个开源（BSD许可）的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。 它支持多 种类型的数据结构，如 字符串（strings）、散列（hashes）、 列表（lists）、 集合（sets）、 有序集合（sorted sets）等。

     

- mysql与redis

  - 概念

    关系型数据库的一个常见用法是存储长期的报告数据，并将这些报告数据用作固定时间范围内的聚合数据。收集聚合数据的常见做法是：先将各个行插入一个报告表里面， 之后再通过扫描这些行来收集聚合数据， 并更新聚合表中巳有的那些行。

  - 图解剖析mysql的执行过程

     ![mysql执行步骤图](.\images\mysql执行步骤图.png)

    ![mysql执行细节](.\images\mysql执行细节.png)

 

 

 

### 2. Redis和memcached和mysql区别 

- 作为同款功能的内存缓存产品，redis和memcached各有什么优势

  - 内存管理机制

    - Memcached默认使用Slab Allocation机制管理内存，其主要思想是按照预先规定的大小， 将分配的内存分割成特定长度的块 以存储相应长度的key-value数据记录，以完全解决内存碎 片问题。空闲列表进行判断存储状态,【类似于Java虚拟机对象的分配，空闲列表】

    ![memcached空闲分配](.\images\memcached空闲分配.jpg)

    - Redis使用现场申请内存的方式来存储数据，并且很少使用free-list等方式来优化内存分配，会在一定程度上存在内存碎片,【CPU内存是连续，类似于Java虚拟机对象的分配，直接内存分配（指针碰撞）】

      ![redis数据存储](.\images\redis数据存储.png)

  - 数据持久化方案

    - memcached不支持内存数据的持久化操作，所有的数据都以in-memory的形式存储。
    - redis支持持久化操作。redis提供了两种不同的持久化方法来讲数据存储到硬盘里面， 第一种是rdb形式，一种是aof形式
      - rdb：属于全量数据备份，备份的是数据
      - aof：append only if,增量持久化备份，备份的是指令

  - 缓存数据过期机制

    - 概念：key，设计一个小时之后过期，超过一个小时查数据就会查不到
    - Memcached 在删除失效主键时也是采用的消极方法，即 Memcached 内部也不会监视主键是否失效，而是在通过 Get 访问主键时才会检查其是否已经失效
    - Redis 定时、定期等多种缓存失效机制，减少内存泄漏

  - 支持的数据类型

    - Memcached支持单一数据类型,[k,v]
    - redis支持五种数据类型

   

 

### 3. redis作为数据库和作为缓存的选择

- redis作为数据库的使用有什么优缺点

  - 优点

    - 没有Scheme约束，数据结构的变更相对容易，一开始确定数据类型， 抗压能力强，性能极高，10万/qps

  - 缺点

    - 没有索引，没有外键，缺少int/date等基本数据类型，多条件查询需要通过集合内联(sinter,zinterstore) 和连接间接实现开发效率低，可维护性不佳

    ![redis与其他数据库区别](.\images\redis与其他数据库区别.jpg)

- redis作为缓存的使用，搭配数据库使用的两种方案

  - jedis整合使用方案 set key,value ["11","22"] 第一层在缓存进行查询，如果得到数据则直接返回， 第二层在数据库进行查询，并且刷新缓存，方便下次查询 ["33,"44"]
  - 作为mybatis/hibernate二级缓存使用方案，一级缓存：sqlSession，进程缓存，单次链接有效

- 图解分析加redis前后的架构区别

   ![NOSQL带来的改变](.\images\NOSQL带来的改变.jpg)



 

 

 

## 二. Redis安装启动

环境：

centOs7 64位

 

### 1. redis安装

[官网说明](https://redis.io/download)

```shell
# 使用wget下载
yum install wget

wget http://download.redis.io/releases/redis-5.0.7.tar.gz

# 解压
tar -xvzf redis-5.0.7.tar.gz -C /usr/local/redis

# 进入目录开始编译
cd /usr/local/redis/redis-5.0.7.tar.gz/

make

# 启动
src/redis-server
src/redis-cli
```



### 2. 启动方式

- 直接启动：

```shell
src/redis-server
```



- 通过制定配置文件启动

```shell
# 修改配置文件
vim redis.conf

daemonize yes

# 指定配置文件启动
src/redis-server redis.conf
```



- 启动脚本设置开机自启动

```shell
# 按照脚本配置路径
mkdir /etc/redis
cp redis.conf /etc/redis/6379.conf
cp utils/redis_init_script /etc/init.d/redisd

# 修改脚本（开头添加），修改命令路径
#!/bin/sh
# chkconfig:   2345 90 10

# 设置为开机自启动
chkconfig redisd on
# 打开服务
service redisd start
# 关闭服务
service redisd stop
```



### 3. 开启远程访问

```shell
vim redis.conf

# bind指定能够访问的ip
# bind 192.168.1.100 10.0.0.1	指定多个地址
# bind 127.0.0.1

protected-mode no

# 或者不改protected-mode,设置密码即可
requirepass 123456
```

>redis-cli要用密码登录的方法：
>
> redis-cli -h 服务ip -p 端口 -a  密码
>
>或者登录后使用： auth 密码	登录



## 三. Redis数据类型和消息订阅



### 1. String

- set/get
  - 设置key对应的值为String类型的value
  - 获取key对应的值 
- mget
  
- 批量获取多个key的值，如果可以不存在则返回nil
  
- incr && incrby

  - incr对key对应的值进行加加操作，并返回新的值;incrby加指定值

- incr && incrby

  - incr对key对应的值进行加加操作，并返回新的值;incrby加指定值

- setnx

  - 设置key对应的值为String类型的value，如果key已经存在则返回0

- setex

  - 设置key对应的值为String类型的value，并设定有效期

- 其他命令

  - getrange 获取key对应value的子字符串

  - mset 批量设置多个key的值，如果成功表示所有值都被设置，否则返回0表示没有任何值被设置

  - msetnx，同mset，不存在就设置，不会覆盖已有的key

  - getset 设置key的值，并返回key旧的值

  - append：给指定key的value追加字符串，并返回新字符串的长度

     

 

### 2. Hash

- Hash是一个String类型的field和value之间的映射表

- redis的Hash数据类型的key（hash表名称）对应的value实际的内部存储结构为一个HashMap

- Hash特别适合存储对象

- 相对于把一个对象的每个属性存储为String类型，将整个对象存储在Hash类型中会占用更少内存。

- 所存储的成员较少时数据存储为zipmap，当成员数量增大时会自动转成真正的HashMap,此时encoding为ht。

- 运用场景： 如用一个对象来存储用户信息，商品信息，订单信息等等。

-  

- Hash命令讲解

  - hset——设置key对应的HashMap中的field的value

  - hget——获取key对应的HashMap中的field的value

  - hgetall——获取key对应的HashMap中的所有field的value

  - hlen--返回key对应的HashMap中的field的数量

     

### 3. List

- lpush——在key对应的list的头部添加一个元素
- lrange——获取key对应的list的指定下标范围的元素，-1表示获取所有元素
- lpop——从key对应的list的尾部删除一个元素，并返回该元素
- rpush——在key对应的list的尾部添加一个元素
- rpop——从key对应的list的尾部删除一个元素，并返回该元素

 

### 4. Set

- sadd——在key对应的set中添加一个元素
- smembers——获取key对应的set的所有元素
- spop——随机返回并删除key对应的set中的一个元素
- suion——求给定key对应的set并集
- sinter——求给定key对应的set交集

 

 

### 5. SortSet

- zadd ——在key对应的zset中添加一个元素
- zrange——获取key对应的zset中指定范围的元素，-1表示获取所有元素
- zrem——删除key对应的zset中的一个元素
- zrangebyscore——返回有序集key中，指定分数范围的元素列表,排行榜中运用
- zrank——返回key对应的zset中指定member的排名。其中member按score值递增(从小到大）； 排名以0为底，也就是说，score值最小的成员排名为0,排行榜中运用
- set是通过hashmap存储，key对应set的元素，value是空对象 sortset是怎么存储并实现排序的呢，hashmap存储，还加了一层跳跃表 跳跃表：相当于双向链表，在其基础上添加前往比当前元素大的跳转链接

 



### 6. 消息订阅发布

- 作用：发布订阅类似于信息管道，用来进行系统之间消息解耦，类似于mq，rabbitmq、rocketmq、kafka、activemq主要有消息发布者和消息订阅者。比如运用于：订单支付成功，会员系统加积分、钱包进行扣钱操作、发货系统（下发商品）

   

- PUBLISH 将信息message发送到指定的频道channel。返回收到消息的客户端数量

- SUBSCRIBE 订阅给指定频道的信息

- UNSUBSCRIBE 取消订阅指定的频道，如果不指定，则取消订阅所有的频道。

- redis的消息订阅发布和mq对比？ 答：redis发布订阅功能比较薄弱但比较轻量级，mq消息持久化，数据可靠性比较差，无后台功能可msgId、msgKey进行查询消息

 

 









## 四. 传统关系型数据库事务与Redis事务

 

### 1. 传统关系型数据库事务

- 一个数据库事务通常包含了一个序列的对数据库的读/写操作。它的存在包含有以下两个目的：

  - 为数据库操作序列提供了一个从失败中恢复到正常状态的方法，同时提供了数据库即使在异常状态下仍能保持一致性的方法。

  - 当多个应用程序在并发访问数据库时，可以在这些应用程序之间提供一个隔离方法，以防止彼此的操作互相干扰。

     

- 事务的ACID四大特性

  - 原子性（Atomicity）：事务作为一个整体被执行，包含在其中的对数据库的操作要么全部被执行，要么都不执行
  - 一致性（Consistency）：事务应确保数据库的状态从一个一致状态转变为另一个一致状态。一致状态的含义是数据库中的数据应满足完整性约束
  - 隔离性（Isolation）：多个事务并发执行时，一个事务的执行不应影响其他事务的执行
  - 持久性（Durability）：已被提交的事务对数据库的修改应该永久保存在数据库中

 

- 事务隔离机制

  - 语法：

  ```mysql
  -- 会话隔离级别
  set session transaction isolatin level repeatable read;
  -- 系统隔离级别
  set global transaction isolation level read uncommitted;
  ```

  

  - 查看：

  ```mysql
  -- 会话隔离级别
  select @@tx_isolation;
  -- 系统隔离级别
  select @@global.tx_isolation;
  ```

  

  - 种类：
    - read uncommitted
    - read committed
    - repeatable read
    - serializable

 

 

### 2. mysql事务隔离机制和MVCC

- redis事务隔离机制可重复读讲解（repeatable read）

- InnoDB MVCC多版本并发控制功能讲解

  - 在每一行数据中额外保存两个隐藏的列：当前行创建时的版本号和删除时的版本号（可能为空，其实还有一列称为回滚指针，用于事务回滚，不在本文范畴）。这里的版本号并不是实际的时间值，而是系统版本号。每开始新的事务，系统版本号都会自动递增。事务开始时刻的系统版本号会作为事务的版本号， 用来和查询每行记录的版本号进行比较

- 图解InnoDB MVCC的组成和原理

  ![mvcc](.\images\mvcc.png)

 

### 3. redis事务机制

- MULTI 与 EXEC命令
  - 以 MULTI 开始一个事务，然后将多个命令入队到事务中， 最后由 EXEC 命令触发事务， 一并执行事务中的所有命令

 

- DISCARD命令
  - DISCARD 命令用于取消一个事务， 它清空客户端的整个事务队列， 然后将客户端从事务状态调整回非事务状态， 最后返回字符串 OK 给客户端， 说明事务已被取消
  - 
- WATCH命令
  - WATCH 命令用于在事务开始之前监视任意数量的键： 当调用 EXEC 命令执行事务时， 如果任意一个被监视的键已经被其他客户端修改了， 那么整个事务不再执行， 直接返回失败。
- 图解redis执行事务过程原理

 ![redis事务状态](.\images\redis事务状态.png)

![redis客户端服务器处理事务命令](.\images\redis客户端服务器处理事务命令.png)

 





### 4. redis事务与传统关系型事务的比较

- 原子性（Atomicity）
  - 单个 Redis 命令的执行是原子性的，但 Redis 没有在事务上增加任何维持原子性的机制，所以 Redis 事务的执行并不是原子性的。如果一个事务队列中的所有命令都被成功地执行，那么称这个事务执行成功
- 一致性（Consistency）
  - 入队错误
    - 在命令入队的过程中，如果客户端向服务器发送了错误的命令，比如命令的参数数量不对，等等， 那么服务器将向客户端返回一个出错信息， 并且将客户端的事务状态设为 REDIS_DIRTY_EXEC 。
  - 执行错误
    - 如果命令在事务执行的过程中发生错误，比如说，对一个不同类型的 key 执行了错误的操作， 那么 Redis 只会将错误包含在事务的结果中， 这不会引起事务中断或整个失败，不会影响已执行事务命令的结果，也不会影响后面要执行的事务命令， 所以它对事务的一致性也没有影响
  - 隔离性（Isolation）
    - WATCH 命令用于在事务开始之前监视任意数量的键： 当调用 EXEC 命令执行事务时， 如果任意一个被监视的键已经被其他客户端修改了， 那么整个事务不再执行， 直接返回失败
  - 持久性（Durability）
    - 因为事务不过是用队列包裹起了一组 Redis 命令，并没有提供任何额外的持久性功能，所以事务的持久性由 Redis 所使用的持久化模式决定

 







## 五. Redis结合springboot

 项目地址： https://github.com/zrinGithub/redis-demo 

### 1. 简单读写文件

- pom文件：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

- application.properties：

```properties
#spring.redis.database=0这是默认值
spring.redis.host=192.168.199.101
spring.redis.port=6379
spring.redis.password=123456
```

- RedisTemplate

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
```



- 测试读写：

```java
@Resource
private RedisTemplate<String, String> redisTemplate;

@GetMapping("/test/redis")
public String testRedis() {
	redisTemplate.opsForValue().set("name","jerry");
    return redisTemplate.opsForValue().get("name");
}
```



 

### 2. RedisTemplate API构建工具类

- opsForValue 操作String，Key，Value，包含过期key，setBit位操作等
- opsForSet 操作set
- opsForHash 操作hash
- opsForZset 操作SortSet
- opsForList 操作list队列
- 工具类：com.zr.redisdemo.util.RedisUtil



### 3. redis缓存数据

```java
//首先从redis中取 
Player player = (Player) redisUtil.get(PREFIX_PLAYER + ":" + id);
 if (player == null) {
    //数据不存在，从数据库中取并保存在redis中
 	player = dao.selectById(id);
    if (player != null) {
    	redisUtil.set(PREFIX_PLAYER + ":" + id, player);
        return player;
	}
}
return player;
```



 

### 4. mybatis的一级缓存与二级缓存

- mybatis一级缓存： 是指Session缓存。一级缓存的作用域默认是一个SqlSession。Mybatis默认开启一级缓存。  同一个SqlSession中，执行相同的查询SQL，第一次会去数据库进行查询，并写到缓存中；第二次以后是直接去缓存中取。

  当Mybatis整合Spring后，直接通过Spring注入Mapper的形式，如果不是在同一个事务中每个Mapper的每次查询操作都对应一个全新的SqlSession实例，这个时候就不会有一级缓存的命中，但是在同一个事务中时共用的是同一个SqlSession。 

  也就是service中同一个方法会使用一个sqlSession：

  ```java
   public void testCache(Integer id) {
  	Player player1 = dao.selectById(id);
      System.out.println(JSON.toJSONString(player1));
      //日志只会查询一次数据库
      Player player2 = dao.selectById(id);
      System.out.println(JSON.toJSONString(player2));
  }
  ```

  

- Mybatis的二级缓存是指mapper映射文件。二级缓存的作用域是同一个namespace下的mapper映射文件内容，多个SqlSession共享。Mybatis需要手动设置启动二级缓存。

  也就是只要是mapper下面同一个查询，只会到数据库查询一次。 

```yml
# 开启缓存，默认就是true
mybatis:
   configuration:
      cache-enabled: true
# 在mapper的xml文件中，开启缓存，注意，类必须要序列化
<cache/>
```



### 5.springboot缓存

- springboot cache的整合步骤

  - 引入pom.xml依赖

    ```xml
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    ```

    

  - 开启缓存注解：

    ```java
    @EnableCaching
    @SpringBootApplication
    public class RedisDemoApplication {
    ......
    }
    ```

    

  - 在方法上面加入SpEL表达式

    - @CacheConfig(cacheNames="userInfoCache") 
      - 在同个redis里面必须唯一
    - @Cacheable(查) ： 
      - 划分可缓存的方法 - 即，结果存储在缓存中的方法，以便在后续调用（具有相同的参数）时，返回缓存中的值而不必实际执行该方法
    - @CachePut（修改、增加） ：
      -  当需要更新缓存而不干扰方法执行时，可以使用@CachePut注释。也就是说，始终执行该方法并将其结果放入缓存中（根据@CachePut选项）
    - @CacheEvict（删除）： 
      - 对于从缓存中删除陈旧或未使用的数据非常有用，指示缓存范围内的驱逐是否需要执行而不仅仅是一个条目驱逐

    ```java
    //查询
    // @Cacheable 会先查询缓存，如果缓存中存在，则不执行方法
    @Cacheable(key = "#p0", unless = "#result == null || #result.isEmpty()")
    public Player selectById(Integer id) {
    	System.out.printf("查询id=s%的运动员", id);
        Assert.notNull(id, "id不能为空");
        return dao.selectById(id);
    }
    ```

    

- springboot cache 存在什么问题

  - 第一，生成key过于简单，容易冲突userCache::3
  - 第二，无法设置过期时间，默认过期时间为永久不过期
  - 第三，配置序列化方式，默认的是序列化JDKSerialazable

- springboot cache自定义项

  - 自定义KeyGenerator
  - 自定义cacheManager，设置缓存过期时间
  - 自定义序列化方式，Jackson

 **参考redisAsMybatisCache分支代码的com.zr.redisdemo.config.RedisConfig**





**压力测试redis缓存和数据库的对比级别**

- apache abtest

  - ab是Apache HTTP server benchmarking tool，可以用以测试HTTP请求的服务器性能

  - 安装：

    ```shell
    yum install -y httpd-tools
    # 检验
    ab -v 
    ```
    
    
    
     

- 使用： 

  ```shell
  # n:requests总数 c:concurrency并发数 
  # 统计qps：qps即每秒并发数，request per second
  ab -n1000 -c10 http://192.168.0.103:8080/cache/select/1 
  ab -n1000 -c10 http://192.168.0.103:8080/cache/selectWithOutCache/1
  ```




### 6. redis实现分布式集群环境session共享

**集群：多机器部署同一套服务（代码），性能更好，更承受更高的用户并发**



- cookie与session

  - Cookie是什么？ Cookie 是一小段文本信息，伴随着用户请求和页面在 Web 服务器和浏览器之间传递。Cookie 包含每次用户访问站点时 Web 应用程序都可以读取的信息，我们可以看到在服务器写的cookie，会通过响应头Set-Cookie的方式写入到浏览器

  - HTTP协议是无状态的，并非TCP一样进行三次握手，对于一个浏览器发出的多次请求，WEB服务器无法区分是不是来源于同一个浏览器。所以服务器为了区分这个过程会通过一个 sessionid来区分请求，而这个sessionid是怎么发送给服务端的呢。cookie相对用户是不可见的，用来保存这个sessionid是最好不过了

  - 单机可以把session保存在内存中，分布式环境因为请求分发不一定每次都在同一台机器，所以不能保存在单台机器的内存中，这时候可以考虑保存在redis中。

     

     

- redis实现分布式集群配置过程

  见代码redisSession分支

  - pom引入session包

  ```xml
  <dependency>
  	<groupId>org.springframework.session</groupId>
      <artifactId>spring-session-data-redis</artifactId>
  </dependency>
  ```

  

  - @EnableRedisHttpSession 开启redis session缓存

  ```java
  @EnableCaching
  @SpringBootApplication
  //指定缓存的时间为150s->maxInactiveIntervalInSeconds
  @EnableRedisHttpSession(maxInactiveIntervalInSeconds=150)
  public class RedisDemoApplication {
  	......
  }
  ```

​	

- 验证

  - 运行两个端口不同浏览器查看
  
  ```java
  @RestController
  public class SessionController {
  
      @GetMapping("setSession")
      public String setSession(HttpServletRequest request) {
          Map<String, Object> map = new HashMap<>();
          HttpSession session = request.getSession();
          session.setAttribute("request Url", request.getRequestURL());
          session.setAttribute("token", UUID.randomUUID());
          return JSONObject.toJSONString(session);
      }
  
      @GetMapping("getSession")
      public String getSession(HttpServletRequest request) {
          return request.getSession().getAttribute("token").toString();
      }
  }
  ```
  
  

> 当然，一般使用的都是自己的同一登录系统，会自己手动把需要的账户信息在登录的时候填入redis中并设置过期时间。

 

 



## 六. redis项目实战之排行榜实现

 见代码分支：rankList

 

### 1. 功能

- 排行榜：
  - 排行榜功能是一个很普遍的需求。使用 Redis 中有序集合的特性来实现排行榜是又好又快的选择。 一般排行榜都是有实效性的，比如“用户积分榜”，游戏中活跃度排行榜，游戏装备排行榜等。
  - 面临问题：数据库设计复杂，**并发数**较高，数据要求**实时性**高
- redis实现排行榜api：ZSetOperations（sortedSet）

 

### 2. mysql数据库表设计



score_flow（积分流水表）查top100 

user_score（用户积分表总表）查用户的排名





- 表设计过程中应该注意的点：
  - 1）更小的通常更好 控制字节长度
  - 2）使用合适的数据类型： 如tinyint只占8个位，char(1024)与varchar(1024)的对比,char用于类似定长数据存储比varchar节省空间，如：uuid（32），可以用char(32).
  - 3）尽量避免NULL，建议使用NOT NULL DEFAULT ''
  - 4）NULL的列会让索引统计和值比较都更复杂。可为NULL的列会占据更多的磁盘空间，在Mysql中也需要更多复杂的处理程序

- 索引设计过程中应该注意的点

  - 选择唯一性索引：唯一性索引的值是唯一的，可以更快速的通过该索引来确定某条记录,保证物理上面唯一
  - 为经常需要排序、分组和联合操作的字段建立索引 ，经常需要ORDER BY、GROUP BY、DISTINCT和UNION等操作的字段，排序操作会浪费很多时间
  - 常作为查询条件的字段建立索引 如果某个字段经常用来做查询条件，那么该字段的查询速度会影响整个表的查询速度
  - 数据少的地方不必建立索引，例如状态这种字段的索引，因为值很少，会占用不必要的内存。



- sql优化：explain查看执行计划

  - 能用BETWEEN（取两个值之间）的时候不要用IN

  - 能用DISTINCT的时候不要用GROUP BY

  - 避免数据强制转换

  - 使用explain来查看执行计划（扫描行数会影响CPU运行，占用大量的内存）

```sql
EXPLAIN SELECT * FROM score_flow WHERE user_id=1;
    
id				1
select_type		SIMPLE				查询类型
table    		score_flow			表
partitions		ref					ref外键关联查询
type			ref
possible_keys	idx_userid
key				idx_userid
key_len			4			
ref				const
rows			1					扫描行数
filtered		100					
Extra			
```

​    




### 3. 排行榜接口

- 添加用户积分
- 获取top N 排行
- 排名范围：reverseZRankWithScore
  - 分数范围：reverseRangeByScore 
- 根据用户ID获取排行

  - reverseRank

 跟基础操作差不多，没写对应代码，参考com.zr.redisdemo.controller.RankListController。

 

### 4. springboot项目初始化加载讲解

考虑redis的持久化问题，可以考虑把数据保存在数据库（定时），每次springboot加载的时候同步数据到redis。



- springboot实现初始化加载配置

  - 实现ApplicationRunner

    ```java
    @FunctionalInterface
    public interface ApplicationRunner {
    	/**
    	 * Callback used to run the bean.
    	 */
    	void run(ApplicationArguments args) throws Exception;
    }
    ```

    

  - 实现InitializingBean，**初始化结束前，用户请求不会进来**

    ```java
    public interface InitializingBean {
        // 在对象的所有属性被初始化后之后才会调用afterPropertiesSet()方法
        // 相当于最后的验证以及初始化操作
        void afterPropertiesSet() throws Exception;
    }
    ```

  

 

 

## 七. Redis面试题 

### 1. 缓存的收益和成本

- 缓存带来的回报
  - 高速读写
    - 缓存加速读写速度：CPU L1/L2/L3 Cache、Linux page Cache加速硬盘读写、浏览器缓存、Ehcache缓存数据库结果
  - 降低后端负载
    - 后端服务器通过前端缓存降低负载： 业务端使用Redis降低后端MySQL负载等
- 缓存带来的代价
  - 数据不一致
    - 缓存层和数据层有时间窗口不一致，和更新策略有关
  - 代码维护成本
    - 原本只需要读写MySQL就能实现功能，但加入了缓存之后就要去维护缓存的数据，增加了代码复杂度。
  - 堆内缓存可能带来内存溢出的风险影响用户进程，如ehCache、loadingCache
  - 堆内缓存和远程服务器缓存redis的选择
    - 堆内缓存一般性能更好，远程缓存需要套接字传输
    - 用户级别缓存尽量采用远程缓存
    - 大数据量尽量采用远程缓存，服务节点化原则

 

### 2. 缓存雪崩

- 什么是缓存雪崩？你有什么解决方案来防止缓存雪崩？
  - 如果缓存集中在一段时间内失效，发生大量的缓存穿透，所有的查询都落在数据库上，造成了缓存雪崩。 由于原有缓存失效，新缓存未到期间所有原本应该访问缓存的请求都去查询数据库了，而对数据库CPU 和内存造成巨大压力，严重的会造成数据库宕机
  - 你有什么解决方案来防止缓存雪崩？
    - 加锁排队
      - key： whiltList value：1000w个uid 指定setNx whiltList value nullValue mutex互斥锁解决，Redis的SETNX去set一个mutex key， 当操作返回成功时，再进行load db的操作并回设缓存； 否则，就重试整个get缓存的方法
    - 数据预热
      - 缓存预热就是系统上线后，将相关的缓存数据直接加载到缓存系统。这样就可以避免在用户请求的时候，先查询数据库，然后再将数据缓存的问题!用户直接查询事先被预热的缓存数据!可以通过缓存reload机制，预先去更新缓存，再即将发生大并发访问前手动触发加载缓存不同的key
    - 双层缓存策略
      - C1为原始缓存，C2为拷贝缓存，C1失效时，可以访问C2，C1缓存失效时间设置为短期，C2设置为长期。
    - 定时更新缓存策略
      - 失效性要求不高的缓存，容器启动初始化加载，采用定时任务更新或移除缓存
    - 设置不同的过期时间，让缓存失效的时间点尽量均匀

 

 

### 3. 缓存穿透

- 什么是缓存穿透？你有什么解决方案来防止缓存穿透？
  - 缓存穿透是指用户查询数据，在数据库没有，自然在缓存中也不会有。这样就导致用户查询的时候， 在缓存中找不到对应key的value，每次都要去数据库再查询一遍，然后返回空(相当于进行了两次 无用的查询)。这样请求就绕过缓存直接查数据库
- 你有什么解决方案来防止缓存穿透？
  - 采用布隆过滤器BloomFilter
    - 将所有可能存在的数据哈 希到一个足够大的 bitmap 中，一个一定不存在的数据会被这个 bitmap 拦截掉，从而避免了对底层存储系统的查询压力
  - 缓存空值
    - 如果一个查询返回的数据为空(不管是数据不 存在，还是系统故障)我们仍然把这个空结果进行缓存，但它的过期时间会很短，最长不超过五分钟。 通过这个直接设置的默认值存放到缓存，这样第二次到缓冲中获取就有值了，而不会继续访问数据库

 

 

 

 

 

 

 

**小D课堂，愿景：让编程不在难学，让技术与生活更加有趣**

**相信我们，这个是可以让你学习更加轻松的平台，里面的课程绝对会让你技术不断提升**

**欢迎加小D讲师的微信： jack794666918**

我们官方网站：[https://xdclass.net](https://xdclass.net/)

**千人IT技术交流QQ群： 718617859**

**重点来啦：免费赠送你干货文档大集合**，包含前端，后端，测试，大数据，运维主流技术文档（持续更新）

https://mp.weixin.qq.com/s/qYnjcDYGFDQorWmSfE7lpQ

 