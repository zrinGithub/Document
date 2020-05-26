# SpringCloud笔记

开发环境：Idea+jdk8



## 一. 介绍

### 应用背景

单机应用：启动时间长，内存设定和jvm gc的矛盾

微服务：服务解耦，独立部署。但是微服务也引入了新的问题：分布式事务、服务管理治理。

了解一下 高可用 LVS+keepalive 



### 基础概念

- **服务消费者**：consumer
- **服务提供者**：provider

- **网关**：路由转发 + 过滤器
  			/api/v1/pruduct/      商品服务
        			/api/v1/order/        订单服务
        			/api/v1/user/   	  用户服务

- **服务注册发现（注册中心）**：调用和被调用方的信息维护

- **配置中心**：管理配置，动态更新 `application.properties`/`application.yml`

- **链路追踪**：分析调用链路耗时
  	例子：下单-》查询商品服务获取商品价格-》查询用户信息-》保存数据库

- **负载均衡器**：分发负载

- **熔断**：保护自己和被调用方



### 常见微服务框架

- Dubbo：
  - zookeeper + dubbo + springmvc/springboot
  - 官方地址：http://dubbo.apache.org/#!/?lang=zh-cn
  - 通信方式：rpc
  - 注册中心：zookeeper/redis
  - 配置中心：diamond
- SpringCloud：
  - 官网地址：http://projects.spring.io/spring-cloud/
  - 通信方式：http restful
  - 注册中心：eruka/consul
  - 配置中心：config
  - 断路器：hystrix
  - 分布式追踪系统：sleuth+zipkin







## 二. 注册中心



### 说明

核心是服务注册表，心跳机制动态维护（检测有效服务）。



服务提供者`Provider`：启动时向注册中心上报自己的网络信息。

服务消费者`Consumer`：启动时向注册中心上报自己的网络信息，并且拉去Provider的相关信息。



调用方希望能够知道对应接口的地址，如果仅仅是使用配置文件，不利于动态新增。



主流的注册中心包括：zookeeper，Eureka，etcd。



### CAP

CAP指的是在一个分布式系统中，`Consistency`（一致性）、 `Availability`（可用性）、`Partition tolerance`（分区容错性），三者不可同时获得。



- 一致性（C）：在分布式系统中的所有数据备份，在同一时刻是否同样的值。（所有节点在同一时间的数据完全一致，越多节点，数据同步越耗时）
- 可用性（A）：负载过大后，集群整体是否还能响应客户端的读写请求。（服务一直可用，而且是正常响应时间）
- 分区容错性（P）：分区容忍性，就是高可用性，一个节点崩了，并不影响其它的节点（100个节点，挂了几个，不影响服务，越多机器越好）



CAP理论就是说在分布式存储系统中，最多只能实现上面的两点。而由于当前的网络硬件肯定会出现延迟丢包等问题，所以分区容忍性是我们必须需要实现的。所以我们只能在一致性和可用性之间进行权衡



**C A 满足的情况下，P不能满足的原因**：

数据同步(C)需要时间，也要正常的时间内响应(A)，那么机器数量就要少，所以P就不满足
    

**C P 满足的情况下，A不能满足的原因**：

数据同步(C)需要时间, 机器数量也多(P)，但是同步数据需要时间，所以不能再正常时间内响应，所以A就不满足
    

**A P 满足的情况下，C不能满足的原因**：

机器数量也多(P)，正常的时间内响应(A)，那么数据就不能及时同步到其他节点，所以C不满足



**注册中心选择**：

- Zookeeper：CP设计，保证了一致性，集群搭建的时候，某个节点失效，则会进行选举行的leader，或者半数以上节点不可用，则无法提供服务，因此可用性没法满足。

- Eureka：AP原则，无主从节点，一个节点挂了，自动切换其他节点可以使用，去中心化。

分布式系统中P,肯定要满足，所以只能在CA中二选一

没有最好的选择，最好的选择是根据业务场景来进行架构设计

如果要求一致性，则选择zookeeper，如金融行业

如果要去可用性，则Eureka，如电商系统



### Eureka介绍

官网：https://spring.io/projects/spring-cloud-netflix

文档：https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.2.2.RELEASE/reference/html/

虽然Erueka2.X闭源，但是1.9系列仍然在维护。



Eureka提供Netfilx用于服务发现的客户端和服务端，服务可以使用高可用部署，Eureka可以记录服务都元数据（端口号、地址、健康信息）。



### 搭建EurekaServer

- `@EnableEurekaServer`注解

- 依赖：

  ```xml
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
          </dependency>
  ```

- 配置：

  ```yaml
  
  ```

  















