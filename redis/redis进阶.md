[TOC]



## 一. 分布式锁

相关博客：

 pjmike_pj [基于Redis的分布式锁实现](https://juejin.im/post/5cc165816fb9a03202221dd5) 



参考代码：https://github.com/zrinGithub/redis-demo.git

分支： redisLock



### 1. 分布式环境遇到的问题

展现需要分布式锁的场景：

- springboot定时任务配置，定时任务配置步骤

  - 开启任务调度@EnableScheduling

  ```java
  @SpringBootApplication
  @EnableScheduling
  public class RedisDemoApplication {
  ......
  }
  ```

  

  - 设置执行方法

  ```java
  @Service
  @Slf4j
  public class NoLockTest {
      @Scheduled(cron = "0/5 * * * * ?")
      public void printDate() {
          log.info("---------Date:" + LocalDateTime.now() + "---------");
      }
  }
  ```

  

  - 启动多个服务，此时定时任务会多次打印。在集群环境下，当我们希望的**分布式任务是定时只调用一次**，那么这样的情况就不满足。

   ```shell
  nohup java -jar demo.jar --server.port=XXXX &
   ```





### 2. 基础概念和实现思路

- 分布式锁
  - 分布式锁是控制分布式系统或不同系统之间共同**访问共享资源**的一种锁实现
  - 如果不同的系统或同一个系统的不同主机之间共享了某个资源时，往往通过**互斥**（mutex）来防止彼此干扰。
- 要求
  - 可重入锁（避免死锁）
  - 具有高可用的获取锁和释放锁功能
  - 这把锁获取锁和释放锁的性能要好…
- 实现方案
  - 获取锁的时候，使用 setnx，锁的 value 值为当前占有锁服务器内网IP编号+任务标识
  - 获取锁后，使用 expire 命令为锁添 加一个超时时间，超过该时间则自动释放锁。
  - 释放锁的时候，判断是不是该锁（即Value为当前服务器内网IP编号拼接任务标识），若是该锁，则执行 delete 进行锁释放

- 命令说明

  ```shell
  #key不存在，操作成功返回1
  #如果key已经存在，则不做任何操作->操作返回0，即 SET if Not exists
  SETNX key value
  
  
  #设置键对应的值以及过期时间，对应命令
  SETEX key seconds value
  
  #相当于
  SET key value
  EXPIRE key seconds 
  ```

  

### 3. lua脚本学习

[Lua: 给 Redis 用户的入门指导](https://www.oschina.net/translate/intro-to-lua-for-redis-programmers?print)

[redis官网 EVAL说明]( https://redis.io/commands/eval )

[Lua 教程]( https://www.runoob.com/lua/lua-tutorial.html )

- 从 Redis 2.6.0 版本开始，通过内置的 Lua 解释器，可以使用 EVAL 命令对 Lua 脚本进行求值。
- Redis 使用单个 Lua 解释器去运行所有脚本，并且， Redis 也保证脚本会以**原子性**(atomic)的方式执行：当某个脚本正在运行的时候，不会有其他脚本或 Redis 命令被执行。这和使用 MULTI / EXEC 包围的事务很类似。在其他别的客户端看来，脚本的效果(effect)要么是不可见的(not visible)，要么就是已完成的(already completed)。



示例：

```lua
--编辑脚本
--1.注意KEY和ARGV两个lua表参数从1开始，
--2.表中不能有nil值。如果一个操作表中有[1, nil, 3, 4]，那么结果将会是[1]——表将会在第一个nil截断。
local link_id = redis.call("INCR", KEY[1])
redis.call("HSET", KEYS[2], link_id, ARGV[1])
return link_id

--调用
--scriptName.lua		脚本文件
--2 					需要传入的KEY的个数
--links:counter			KEY[1]
--links:urls 			KEY[2]
--http://xxx.com/		ARGV[1]
redis-cli EVAL "$(cat scriptName.lua)" 2 links:counter links:urls http://xxx.com/
```



>  `redis.call()` is similar to `redis.pcall()` the only difference is that if a Redis command call will result in an error, `redis.call()` will raise a Lua error that in turn will force [EVAL](https://redis.io/commands/eval) to return an error to the command caller, while `redis.pcall` will trap the error and return a Lua table representing the error. 



### 3. 加锁

加锁需要考虑一些问题：

- setNX和setEx的两步必须为**原子操作**，因为如果在setNx时抛出异常会导致锁不会过期。



------



这里redisTemplate已经提供setNx+setEx的操作，可以直接调用：

```java
public boolean getLock(String key, String value, long timeout) {
	return redisTemplate.opsForValue()
        .setIfAbsent(key, value, Duration.ofSeconds(timeout));
}
```



------



也可以使用lua脚本实现：

脚本 getLock.lua ：

```lua
--定义变量
local lockKey = KEYS[1]
local lockValue = ARGV[1]
local timeout = ARGV[2]

--setNx
if redis.call('setnx', lockKey, lockValue) == 1
then redis.call('expire', lockKey, timeout)
return 1
else
return 0
end
```





另一个版本的写法：

 从 Redis 2.6.12 版本开始， `SET` 命令的行为可以通过一系列参数来修改，命令只在设置操作成功完成时才返回 `OK` ： 

参考： http://redisdoc.com/string/set.html 

`SET key value [EX seconds] [PX milliseconds] [NX|XX]`

- `EX seconds` ： 将键的过期时间设置为 `seconds` 秒。 执行 `SET key value EX seconds` 的效果等同于执行 `SETEX key seconds value` 。
- `PX milliseconds` ： 将键的过期时间设置为 `milliseconds` 毫秒。 执行 `SET key value PX milliseconds` 的效果等同于执行 `PSETEX key milliseconds value` 。
- `NX` ： 只在键不存在时， 才对键进行设置操作。 执行 `SET key value NX` 的效果等同于执行 `SETNX key value` 。
- `XX` ： 只在键已经存在时， 才对键进行设置操作。



可以这么写：

```lua
return redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2], 'NX')
```



测试脚本：

`src/redis-cli -a 123456 EVAL "$(cat script/getLock.lua)" 1 name JACK 11`



### 4. 解锁

解锁考虑的问题

- 保证任务结束（正常或是异常）后，**锁必须释放**。
- **锁的value唯一保证释放的正确性**，如果不能保证，考虑服务A由于执行时间过长而导致锁已经过期，服务B拿到锁开始执行任务，服务A任务完成后因为判定此时锁value相同，不知道是自己持有的锁还是B的锁，如果直接释放会导致后面的运行问题（锁在没有过期情况下被释放），这一点可以用UUID来实现。



解锁的lua实现（保证原子性）：

```lua
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end
```



### 5. 使用锁

使用jedis可以直接调用 `jedis.eval` 来调用脚本

spring环境下可以写：

```java
private DefaultRedisScript<Boolean> script;
//使用lua脚本获取锁
public boolean getLockWithLuaScript(String key, String value, long timeout) {
	script = new DefaultRedisScript<>();
    script.setScriptSource(new ResourceScriptSource(
        new ClassPathResource("getLock.lua")));
	script.setResultType(Boolean.class);

    return (Boolean) redisTemplate.execute(script, Collections.singletonList(key), value, timeout);
}
```

对应的解锁脚本也一样



测试代码：

```java
@Scheduled(cron = "0/5 * * * * ?")
public void printDate() {
    String value = UUID.randomUUID().toString();
    try {
        if (getLockWithLuaScript(printDateRedisKey, value, 10L)) {
            log.info("---------Date:" + LocalDateTime.now() + "---------");
        }
    } catch (Exception e) {
        log.error(e.getMessage());
    } finally {
        releaseLockWithLuaScript(printDateRedisKey, value);
    }
}
```



 ### 6. 考虑redis集群的情况-官方的实现方法

官网说明：[Distributed locks with Redis]( https://redis.io/topics/distlock )



在redis集群中，可能出现服务A通过master获取锁后，在传输到slave之前crash，slave升级为master，服务B拿到锁，此时AB就都拿到锁了。

所以必须考虑一个能够**在所有实例获取释放锁**的算法，也就是官网的Redlock，官网的介绍比较详细，也可以配合下面的博客来理解其实现：

 阿飞的博客 :[Redlock：Redis分布式锁最牛逼的实现]( https://mp.weixin.qq.com/s?__biz=MzU5ODUwNzY1Nw==&mid=2247484155&idx=1&sn=0c73f45f2f641ba0bf4399f57170ac9b&scene=21#wechat_redirect )



java的实现是Redisson：

- pom导入依赖：

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.10.6</version>
</dependency>
```



- 使用：

参考这篇文章吧：

[Redisson实现Redis分布式锁的N种姿势]()

```java
// 1. 配置文件
Config config = new Config();
config.useSingleServer()
        .setAddress("redis://127.0.0.1:6379")
        .setPassword(RedisConfig.PASSWORD)
        .setDatabase(0);
//2. 构造RedissonClient
RedissonClient redissonClient = Redisson.create(config);

//3. 设置锁定资源名称
RLock lock = redissonClient.getLock("redlock");
lock.lock();
try {
    System.out.println("获取锁成功，实现业务逻辑");
    Thread.sleep(10000);
} catch (InterruptedException e) {
    e.printStackTrace();
} finally {
    lock.unlock();
}
```



这里没有试验过，以后搭建了集群试一下吧。



## 二. 持久化

方案：

1. RDB快照
2. AOF命令日志
3. 同时使用以上两种，这样重启服务以后，AOF用于



## 三. redis命令

### SAVE&BGSAVE

SAVE会阻塞Redis进程进行RDB创建

`save <seconds> <changes>`表示在seconds秒内，至少有changes次变化，就会自动触发`gbsave`命令



BGSAVE在后台异步保存当前数据库数据到磁盘

BGSAVE执行之后马上返回OK，然后fork出一个新子进程，原来的父进程继续处理客户端请求，而子进程负责把数据保存到磁盘。

异步的结果可以通过LASTSAVE查看



### BGREWRITEAOF

AOF文件重写，重写会创建一个当前 AOF 文件的体积优化版本。





## 三. redis集群

redis安装参考redis入门文档。

相关博客：https://zhuanlan.zhihu.com/p/72056688



### 1. 创建集群文件夹

```shell
cd /usr/local/
mkdir redis_cluster
cd redis_cluster
mkdir 7000 7001 7002 7003 7004 7005
cp /usr/local/redis-4.0.6/redis.conf  /usr/local/redis_cluster/7000
```



### 2. 修改配置文件

每个节点都需要修改

```shell
daemonize    yes               //redis后台运行
port  7000                     //端口7000,7002,7003,7004,7005
cluster-enabled  yes            //开启集群  把注释#去掉
cluster-config-file  nodes.conf   //集群的配置,首次启动自动生成 
cluster-node-timeout  5000     //请求超时  设置5秒够了
appendonly  yes               //aof日志开启，它会操作日志，如果使用rdb就是用dbfilename "xx.rdb" 与  dir ./指定工作目录
bind 127.0.0.1 172.16.244.133(Redis安装的这台机的内网IP,命令：ip addr) //添加一个内网IP，这部不操作的话会导致内网其他节点无妨访问你的集群

#配置到每一个节点
cp /usr/local/redis_cluster/7000/redis.conf /usr/local/redis_cluster/7001
```



  `cluster-enable`与`cluster-config-file`

与集群相关。



我们可以调用`redis-cli info Server`看到集群当前的模式（`redis_mode:standalone`或者`redis_mode:cluster`）

`cluster-config-file`指定集群配置文件的位置，每个节点运行都会维护一个集群配置文件（redis自己维护），每次重启会重新读取文件获得集群的信息。



### 3. 启动集群

**节点握手**：

`redis-server redis.conf`启动集群后，

可以通过`redis-cli -p 7000 cluster nodes`查看节点的情况，第一项就是id，这个id会保存到集群的配置文件，也就是即使重启也不会变。



节点启动以后需要进行节点握手：

进入redis客户端以后，调用命令`cluster meet ip port`，之后再调用`redis-cli -p 7000 cluster nodes`就可以看到加入节点的信息。



**分配槽**：

redis集群使用槽来完成数据分区，当所有16384个槽都分配了节点以后，集群才处于上线状态。

使用`redis-cli -p 7000 cluster info`可以看到当前集群的状态，分配槽以前`cluster_state:fail`



使用`cluster addslots`分配槽：

```sh
redis-cli -p 7000 cluster addslots {0..5461}
redis-cli -p 7001 cluster addslots {5462..10922}
redis-cli -p 7002 cluster addslots {10923..16383}
```



**指定主从关系**：

分配节点作为指定master的slave节点。最后的参数是master id，使用`cluster nodes`获取

```sh
redis-cli -p 8000 cluster replicate be816eba968bc16c884b963d768c945e86ac51ae
redis-cli -p 8001 cluster replicate 788b361563acb175ce8232569347812a12f1fdb4
redis-cli -p 8002 cluster replicate a26f1624a3da3e5197dde267de683d61bb2dcbf1
```



### 4. 集群的基本原理

