# HBase 笔记

此文档为[ HBase入门与实践 ]( https://www.epubit.com/bookDetails?id=N24662 )笔记

参考[hbase官网]( https://hbase.apache.org/ )



## 介绍

- 特点：灵活处理非结构化数据
- 应用场景：用户行为记录、交互数据。



## 安装

### 单机部署

单机版需要配置java环境，参考 https://hbase.apache.org/book.html#java 

| HBase Version | JDK 7 | JDK 8 |                       JDK 9 (Non-LTS)                        |                       JDK 10 (Non-LTS)                       |                            JDK 11                            |
| :-----------: | :---: | :---: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|     2.1+      |   N   |   Y   | [HBASE-20264](https://issues.apache.org/jira/browse/HBASE-20264) | [HBASE-20264](https://issues.apache.org/jira/browse/HBASE-20264) | [HBASE-21110](https://issues.apache.org/jira/browse/HBASE-21110) |
|     1.3+      |   Y   |   Y   | [HBASE-20264](https://issues.apache.org/jira/browse/HBASE-20264) | [HBASE-20264](https://issues.apache.org/jira/browse/HBASE-20264) | [HBASE-21110](https://issues.apache.org/jira/browse/HBASE-21110) |



这是测试机器上的java版本：

```shell
java -version
java version "1.8.0_211"
Java(TM) SE Runtime Environment (build 1.8.0_211-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)
```



在官网下载stable release，这里使用最新的为2.2.4版的**二进制包**（源码包需要先编译）来测试：

```shell
#解压文件
tar xzvf hbase-2.2.4-bin.tar.gz -C /usr/local/hbase

#修改配置
cd /usr/local/hbase/hbase-2.2.4/
vim conf/hbase-site.xml 

<configuration>
  <property>
    #hbase数据存储目录，file://表示本地存储地址
    #生产一般使用hadoop，即hdfs://ipaddr:port/xxx
    <name>hbase.rootdir</name>
    <value>file:///root/data/hbase</value>
  </property>
  <property>
    #zookeeper数据存储地址
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/root/data/zookeeper</value>
  </property>
</configuration>
```



启动hbase：

```shell
bin/start-hbase.sh 
```



查看：

ip:16010



基本shell指令

```shell
#进入客户端
bin/hbase shell

hbase(main):002:0> create 's_test',{NAME=>'cf'}
hbase(main):003:0> list
hbase(main):006:0> put 's_test' ,'rowkey1','cf:v','value1'

hbase(main):007:0> scan 's_test'
ROW                   COLUMN+CELL                                               
 rowkey1              column=cf:v, timestamp=1588305652690, value=value1        
1 row(s)
Took 0.0402 seconds

hbase(main):008:0> deleteall 's_test','rowkey1'
hbase(main):009:0> disable 's_test'
hbase(main):010:0> drop 's_test'
```



### 集群安装

Hadoop2.6.1+与Hadoop2.7.1+对HBase1.2X及以上的版本呢都支持，所以我们一般使用者两个版本的Hadoop。



使用虚拟机的linux环境（Centos7）

ip分别为192.168.199.200-202

| 进程      | server-1                                                     | server-2                                                     | server-3                                |
| --------- | ------------------------------------------------------------ | ------------------------------------------------------------ | --------------------------------------- |
| Hadoop    | NameNode<br />DataNode<br />DFSZKFailoverController<br />JournalNode | NameNode<br />DataNode<br />DFSZKFailoverController<br />JournalNode | -<br />DataNode<br />-<br />JournalNode |
| HBase     | HMaster<br />HRegionServer                                   | HMaster<br />HRegionServer                                   | -<br />HRegionServer                    |
| Zookeeper | QuorumPeerMain                                               | QuorumPeerMain                                               | QuorumPeerMain                          |



#### 1. 创建用户

为了避免权限混乱，创建一个hadoop群组下的hadoop用户。

```shell
#创建用户与用户组
groupadd hadoop
adduser -g hadoop -d /home/hadoop hadoop

#设置密码，这里统一设为123456
passwd hadoop

#创建用户目录并分配权限，一般来说不用做这一步，会自动创建的
mkdir /home/hadoop
sudo chown hadoop:hadoop /home/hadoop
```



#### 2. 修改host文件

将IP与名称映射写入hosts文件中

```shell
#三台机器都需要配置
vim /etc/hosts

192.168.199.200 server-1
192.168.199.201 server-2
192.168.199.202 server-3
```



#### 3. 开启ssh免密登录

在server-1上面配置免密登录，以后可以直接使用这台机器传输配置文件

```shell
su - hadoop
#产生密钥，-t指定密钥类型
ssh-keygen -t rsa
#公钥保存为authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

#把key文件复制到server-2、server-3，需要输入密码
#这里需要确认其他机器上有对应文件夹，可以在三台机器都生成密钥
scp ~/.ssh/authorized_keys hadoop@server-2:~/.ssh/
scp ~/.ssh/authorized_keys hadoop@server-3:~/.ssh/

#测试
ssh hadoop@server-2
ssh hadoop@server-3
```

