# Kafka笔记

笔记参考《Kafka入门与实践》（牟大恩著）



## 一. 基础知识

### 1. 基本结构和概念

- 基础结构图：

![](.\images\kafka结构.png)

- 基本概念：

  - topic: 一组消息归纳为一个主题，生产者将消息发送到特定主题，消费者订阅主题或主题的某些分区进行消费。

  - message: java客户端成为recoed

  - partition: 每个主题又被分成一个或多个分区，每个分区在物理上对应一个文件夹，命名规则为主题名称后接“一”连接符+分区编号（0开始）。每个分区又有一至多个
    副本（ Replica ），分布在不同代理提高可用性。

    分区副本抽象为日志（ Log ）对象，即分区的副本与日志对象是一一对应的。

  - Leader 副本和Follower 副本：只有Leader 副
    本（只有一个）才负责处理客户端读／写请求， Follower 副本从Leader 副本同步数据。客户端只需与Leader 副本进行交互。

  - 偏移：消息追加到日志文件（分区目录下以＂ .log ”为文件名后缀的数据文件〉的尾部，而每条消息在日志文件中的位置都会对应一个按序递增的偏移量。

  - LogSegment ：一个日志又被划分为多个日志段（ LogSegment ），日志段是Kafka 日志对象分片的最小单位。

  - Broker:集群中每一个实例

  - 



## 二. kafka安装

### 1. 基础环境

- 说明

centos7 64位



- 网络配置

```shell
# 编辑
vi /etc/sysconfig/network-scripts/ifcfg-ens33 

......
BOOTPROTO=static
NAME=ens33
DEVICE=ens33
ONBOOT=yes
IPADDR=192.168.199.200
NETMASK=255.255.255.0
GATEWAY=192.168.199.2
DNS1=119.29.29.29

# 重启
service network restart

# 关闭防火墙并且禁止服务
systemctl stop firewalld.service
systemctl disable firewalld.service

# 安装文件传输
yum install -y lrzsz
```



### 2. 安装jdk

```shell
# 解压
tar -xzvf jdk-8u211-linux-x64.tar.gz -C /usr/local/java8

# 编辑环境变量
vim /etc/profile

export JAVA_HOME=/usr/local/java8/jdk1.8.0_211
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

# 重启生效
source /etc/profile

# 测试 
java -version
```



### 3. ssh安装

```shell
# 安装OpenSSH服务
yum install openssh-server -y

# 配置（使用默认配置也可以）
vim /etc/ssh/sshd_config

port=22 SSH默认的端口是22端，但是为了安全，通常将端口设置大一些如 2000
Protocol 2  启用SSH版本2协议
ListenAddress 192.168.0.222  设置服务监听的地址
DenyUsers   user1 user2 foo  拒绝访问的用户(用空格隔开)
AllowUsers  root osmond vivek  允许访问的用户(用空格隔开)
PermitRootLogin  no  禁止root用户登陆
PermitEmptyPasswords no  用户登陆需要密码认证
PasswordAuthentication  yes  启用口令认证方式

# 重启ssh
service sshd restart

# 设置开机自启
chkconfig sshd on

# 测试连接
ssh username@hostname(hostIP)
eg.ssh root@192.168.199.101
```



### 4. ZooKeeper集群安装

zookeeper环境也需要java环境

集群安装：



- 配置集群机器

```shell
# 配置每台机器的host文件
vim /etc/hosts

192.168.199.200 server-1
192.168.199.201 server-2
192.168.199.202 server-3
```



- 安装zookeeper：

```shell
# 1.解压
tar -xzvf zookeeper-3.4.6.tar.gz -C /usr/local/zookeeper/

# 2.数据和日志目录
mkdir -p /opt/data/zookeeper/data
mkdir -p /opt/data/zookeeper/logs

# 3.修改一些需要的配置
cp zoo_sample.cfg zoo.cfg
vim zoo.cfg

dataDir=/opt/data/zookeeper/data
dataLogDir=/opt/data/zookeeper/logs
# 配置格式：server.n=server-domain:portl:port2
# port1表示服务器与集群中leader交换信息端口
# port2表示选举leader时服务器互相通信端口
server.1=server-1:2888:3888
server.2=server-2:2888:3888
server.3=server-3:2888:3888

# 4.在配置的dataDir路径下创建myid文件，
# 文件内容就是服务器的编号，也就是刚才server.n中的n
vim /opt/data/zookeeper/data/myid

# 三台机器分别是1、2、3
1

# 5.配置其他机器
# 可以使用scp直接把配置拷贝过去
scp zoo.cfg root@192.168.199.201:/usr/local/zookeeper/zookeeper-3.4.6/conf
scp zoo.cfg root@192.168.199.202:/usr/local/zookeeper/zookeeper-3.4.6/conf

# 同样的，所有机器都要创建myid文件

# 6. 配置zookeeper的环境变量
vim /etc/profile

export ZOOKEEPER_HOME=/usr/local/zookeeper/zookeeper-3.4.6
export PATH=$ZOOKEEPER_HOME/bin:$PATH 

source /etc/profile
```



- 启动

```shell
# 启动服务
zkServer.sh start

# 查看这三台服务器的状态
zkServer.sh status
```



### 5. Kafka集群安装

```shell
# 1.解压
tar -xzvf kafka_2.11-2.1.1.tgz -C /usr/local

# 2.配置环境变量
vim /etc/profile

export KAFKA_HOME=/usr/local/kafka_2.11-2.1.1
export PATH=$KAFKA_HOME/bin:$PATH

# 3.配置修改
mkdir -p /opt/data/kafka/logs
vim config/server.properties

# 指定代理的id，需要保证同一个集群下面的broker.id唯一
# 这里直接和zookeeper的myid保持一致
broker.id=1
# 指定日志存储路径
log.dirs=/opt/data/kafka/logs
# 连接到zookeeper集群
zookeeper.connect=server-1:2181,server-2:2181,server-3:2181

# 4.验证
kafka-server-start.sh -daemon config/server.properties

# 查看java进程
jps

1718 Kafka			
1356 QuorumPeerMain #zookeeper

# 进入zookeeper
zkCli.sh -server server-1:2181
# 查看已经启动的代理节点
ls /brokers/ids
```

 



## 三. kafka源码环境搭建

### 1. scala安装

[下载地址]( https://www.scala-lang.org/download/ )

```shell
#下载scala-2.13.1.msi后直接安装

# 配置环境变量，在path中添加%SCALA_HOME%\bin
SCALA_HOME=

```



### 2. gradle安装



### 3. 下载编译源码

github地址：https://github.com/apache/kafka.git



## 四. kafka基本操作

### 1. 直接启动查看节点

kafka-server-start.sh具体脚本解析可以查看[shell脚本](https://github.com/zrinGithub/Document/blob/master/linux/shell脚本.md) 

核心的部分为：`exec $base_dir/kafka-run-class.sh $EXTRA_ARGS kafka.Kafka "$@"`

也就是执行kafka-run-class.sh

```shell
# 三个节点启动kafka
kafka-server-start.sh -daemon ../config/server.properties

# 进入zookeeper查看
# 查看节点
ls /brokers/ids
[1, 2, 3]

# 查看控制器，这里leader为1号节点,杀掉1号进程会自动选举leader
get /controller
{"version":1,"brokerid":1,"timestamp":"1582399363346"}
cZxid = 0x300000018
ctime = Sun Feb 23 03:22:43 CST 2020
mZxid = 0x300000018
mtime = Sun Feb 23 03:22:43 CST 2020
pZxid = 0x300000018
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x2706dd82d790000
dataLength = 54
numChildren = 0

# 查看节点信息
# "jmx_port":-1，这里如果需要开启JMX监控，可以在启动脚本增加配置
# export JMX_PORT=9999
# 或者启动时指定
# JMX_PORT=9999 kafka-server-start.sh -daemon ../config/server.properties
get /brokers/ids/1
{"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://server-1:9092"],"jmx_port":-1,"host":"server-1","timestamp":"1582399362931","port":9092,"version":4}
cZxid = 0x300000017
ctime = Sun Feb 23 03:22:42 CST 2020
mZxid = 0x300000017
mtime = Sun Feb 23 03:22:42 CST 2020
pZxid = 0x300000017
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x2706dd82d790000
dataLength = 186
numChildren = 0
```



### 2. 启动集群的脚本

```shell
#!/bin/bash
# 这些是/etc/hosts里面配置完成的
brokers="server-1 server-2 server-3"
KAFKA_HOME=" /usr/local/kafka_2.11-2.1.1"

echo "INFO:BEGAIN to start kafka cluster------"

for broker in $brokers
do
	echo "INFO:Start kafka on ${broker}------"
	ssh $broker -C "source /etc/profile;sh ${KAFKA_HOME}/bin/kafka-server-start.sh -daemon ${KAFKA_HOME}/config/server.properties"
	if [ $? -eq 0 ];then
		echo "INFO:${broker} Start successfully"
	fi
done

echo "INFO:Kafka cluster starts successfully!"
```



### 3. 关闭集群脚本

```shell
#!/bin/bash
# 这些是/etc/hosts里面配置完成的
brokers="server-1 server-2 server-3"
KAFKA_HOME=" /usr/local/kafka_2.11-2.1.1"

echo "INFO:BEGAIN to shut down kafka cluster------"

for broker in $brokers
do
	echo "INFO:Shut down kafka on ${broker}------"
	ssh $broker -C "sh ${KAFKA_HOME}/bin/kafka-server-stop.sh"
	if [ $? -eq 0 ];then
		echo "INFO:${broker} Shut down successfully"
	fi
done

echo "INFO:Kafka cluster shut down successfully!"
```



### 4. 主题管理

