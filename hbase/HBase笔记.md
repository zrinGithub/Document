# Hbase笔记



此文档为[ HBase入门与实践 ]( https://www.epubit.com/bookDetails?id=N24662 )笔记

参考[hbase官网]( https://hbase.apache.org/ )



[TOC]



## 1. 介绍

- 特点：灵活处理非结构化数据
- 应用场景：用户行为记录、交互数据。



## 2. 安装

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





## 3. 基本结构

### 逻辑存储结构

![](.\image\Hbase逻辑存储结构.png)

- `Row key` ：表的主键，按照字典序排序。

- 列簇：在 `HBase` 中，列簇将表进行横向切割。

- 列：属于某一个列簇，在 `HBase` 中可以进行动态的添加。

- `Cell` : 是指具体的 `Value` 。

- `Version` ：在这张图里面没有显示出来，这个是指版本号，用时间戳（`TimeStamp` ）来表示。



### key的结构

`KEY` 的组成是以 `Row key` 、`CF(Column Family)` 、`Column` 和 `TimeStamp` 组成的。

![](.\image\HbaseKey结构.png)



### 物理结构

`Region`也就是分区，表按照行键的范围划分为不同的分区。

数据按照列族存储在`StoreFile`中。空白的列单元不会被存储。

一个分区下每个列族会有一个存储仓库（`Store`）。

每个`Store`有且仅有一个`MemStore`（内存仓库），当`MemStore`大小达到配置阈值或者集群中`MemStore`占据内存达到配置阈值百分比的时候，会把刷新`StoreFile`到磁盘存储。如果开启了压缩，则会按照`block`进行压缩。



- 一个 `Region Server` 就是一个机器节点(服务器)
- 一个 `Region Server` 包含着多个 `Region`
- 一个 `Region` 包含着多个列簇 `(CF)`
- 一个 `Region Server` 中可以有多张 `Table`，一张 `Table` 可以存在于多个 `Region`
  - 这句话有点拗口，实际上就是`Table`拆分成分区，分区名格式就是`<tableName,startRowkey,createTime>`


![](.\image\RegionServer和Region的关系.png)

## 4. 架构

![hbase细化架构](.\image\hbase细化架构.png)

HMaster是Master Server实现，用于监控RegionServer实例，同事负责Region在RegionServer中的负载均衡。

HRegionServer是RegionServer实现，管理Regoins。HBase客户端的读写直接与HRegionServer交互。

Region有多个Store。



## 5. HBase Shell

启动HBase进入客户端:

```shell
bin/start-hbase.sh

bin/hbase shell
```

### 示例表

![示例表](.\image\示例表.png)



### DDL

数据定义语言（Data Defination Language. DDL）包括数据库表的创建修改语句。



#### 创建表

```shell
#创建表s_behavior，包含两个列族pc和ph
create 's_behavior',{NAME=>'pc'},{NAME=>'ph'}
```



#### 查看所有表

```shell
list
```



#### 查看建表

```shell
describe 's_behavior'
```

这里可以看到Hbase默认设置的·一些属性：

```shell
#数据块编码，用类似于压缩算法的编码来节省存储空间，主要是针对行键。默认情况下不适用数据块编码。
DATA_BLOCK_ENCODING => 'NONE',、
#布隆过滤器
BLOOMFILTER => 'ROW',
#集群键数据复制开关为0表示不开启复制
REPLICATION_SCOPE => '0',
#数据版本
VERSIONS => '1',
#压缩方式
COMPRESSION => 'NONE',
#数据的有效时长：Time To Live
TTL => 'FOREVER',
#保留了删除的数据
KEEP_DELETED_CELLS => 'FALSE',
#读取数据的最小单元
BLOCKSIZE => '65536'
```



### 修改表

修改表的结构需要将表现下线，然后在执行修改的命令，再上线。

```shell
disable 's_behavior'
#将表修改为开启集群键复制
alter 's_behavior',{NAME=>'cf',REPLICATION_SCOPE=>'1',KEEP_DELETED_CELLS=>'TRUE'}
enable 's_behavior'
```



### DML

数据操纵语言（Data Manipulation Language, DML）包括数据的修改、查询、删除等操作。



#### Put

插入一行数据到HBase表：

```shell
#命令格式
put <table>,<rowkey>,<列族:列标识符>,<值>

#插入数据
put 's_behavior','12345_1516592489001_1','pc:v','1001'
put 's_behavior','12345_1516592489001_1','ph:o','1001'
```





#### Get

获取Hbase表的一条数据

```shell
#命令格式
get <table>,<rowkey>

#获取一条数据
get 's_behavior','12345_1516592489001_1'

#显示结果
COLUMN                                        CELL
pc:v                                         timestamp=1589360047222, value=1001         ph:o                                         timestamp=1589360053375, value=1001         1 row(s)
Took 0.0589 seconds 
```



`Get`可以通过**指定时间戳来获取**一行数据在某一个时刻的镜像

```shell
get 's_behavior','12345_1516592489001_1' , {TIMESTAMP => '1589360047222'}

#显示结果
COLUMN                                        CELL
pc:v                                         timestamp=1589360047222, value=1001         1 row(s)
Took 0.0165 seconds
```



`Get`也可以支持**获取数据的多个版本**，但是需要在建表语句中指定`VERSION`属性。

```shell
#修改表支持多个VERSION，这里的VERSIONS参数表示支持3个版本的数据
alter 's_behavior',NAME=>'pc',VERSIONS =>3

#在同一个cell传入新的数据
put 's_behavior','12345_1516592489001_1','pc:v','1002'

#此时直接查看行数据会得到最新的版本，也就是pc:v已经被修改为1002
get 's_behavior','12345_1516592489001_1'

COLUMN                                        CELL                                       pc:v                                         timestamp=1589360510067, value=1002         ph:o                                         timestamp=1589360053375, value=1001  

#查看多个版本的数据，VERSIONS参数表示显示多少个版本的数据
get 's_behavior','12345_1516592489001_1',{COLUMN => 'pc:v',VERSIONS=>2}

COLUMN                                        CELL                                       pc:v                                         timestamp=1589360510067, value=1002          pc:v                                         timestamp=1589360047222, value=1001 
```

继续向cell里面put数据，可以看到只能存储三个版本的数据。

而默认建表的时候，`VERSION`参数为1，表示只能存储一个版本的数据。



#### Scan

用于扫描表的数据，需要注意查询的数据量，避免数据过大导致HBase集群出现响应延迟。



##### 扫描表

```shell
#语法
scan <table>

#扫描表的数据
scan 's_behavior'

ROW                                           COLUMN+CELL                                 12345_1516592489001_1                        column=pc:v, timestamp=1589360765939, value=1004                                                                             12345_1516592489001_1                        column=ph:o, timestamp=1589360053375, value=1001
1 row(s)
```



##### 时间区域

```shell
scan 's_behavior', {TIMERANGE => [1589360053375,1589360510067]}

ROW                                           COLUMN+CELL                                 12345_1516592489001_1                        column=ph:o, timestamp=1589360053375, value=1001                                                                               1 row(s)
Took 0.0222 seconds
```



##### 多版本

```shell
#获取三个版本的数据
scan 's_behavior',{VERSIONS=>3}

ROW                                           COLUMN+CELL                                 12345_1516592489001_1                        column=pc:v, timestamp=1589360765939, value=1004                                                                               12345_1516592489001_1                        column=pc:v, timestamp=1589360746455, value=1003                                                                              12345_1516592489001_1                        column=pc:v, timestamp=1589360510067, value=1002                                                                               12345_1516592489001_1                        column=ph:o, timestamp=1589360053375, value=1001                                                                               1 row(s)
Took 0.0583 seconds
```



##### LIMIT限制行数查看

数据准备

```shell
#插入数据
put 's_behavior','12345_1516592489001_2','pc:v','1102'
put 's_behavior','12345_1516592489001_3','pc:v','1103'
put 's_behavior','12345_1516592489001_4','pc:v','1104'
put 's_behavior','123456_1516592489001_4','pc:v','1001'
```



数据查看：

```shell
#这里的FILTER用于过滤数据，这里取 ROW_KEY 为12345_XXX的数据。
#COLUMNS是指列族
#LIMIT指定限制数量
scan 's_behavior', {FILTER => "PrefixFilter('12345_')" ,COLUMNS => ['pc'],LIMIT=>3}

ROW                                           COLUMN+CELL                                 12345_1516592489001_1                        column=pc:v, timestamp=1589360765939, value=1004                                                                               12345_1516592489001_2                        column=pc:v, timestamp=1589362508962, value=1102                                                                               12345_1516592489001_3                        column=pc:v, timestamp=1589362514767, value=1103
3 row(s)
Took 0.0181 seconds
```



##### 指定开始行结束行范围

使用`STARTROW`和`STOPROW`指定需要查询的行范围

```shell
scan 's_behavior', {STARTROW => '12345_1516592489001' ,STOPROW=>'12345_1516592489002',COLUMNS => ['pc']}

ROW                                           COLUMN+CELL                                 12345_1516592489001_1                        column=pc:v, timestamp=1589360765939, value=1004                                                                               12345_1516592489001_2                        column=pc:v, timestamp=1589362508962, value=1102                                                                               12345_1516592489001_3                        column=pc:v, timestamp=1589362514767, value=1103                                                                               12345_1516592489001_4                        column=pc:v, timestamp=1589362521514, value=1104                                                                               4 row(s)
Took 0.0127 seconds 
```



##### 对Cell的值过滤查询

```shell
scan 's_behavior', FILTER=>"ValueFilter(=,'binary:1104')"

ROW                                           COLUMN+CELL                                 12345_1516592489001_4                        column=pc:v, timestamp=1589362521514, value=1104
1 row(s)
Took 0.6691 seconds 
```



##### SingleColumnValueFilter

`SingleColumnValueFilter`指定搜索的列限定符

```shell
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.util.Bytes;

#指定查询值为1001，列为ph:o的数据
scan 's_behavior',{FILTER => SingleColumnValueFilter.new(Bytes.toBytes('ph') ,Bytes.toBytes('o') ,CompareFilter::CompareOp.valueOf('EQUAL') ,BinaryComparator.new(Bytes.toBytes('1001'))),COLUMNS => ['ph:o']}
```



##### FILTER过滤条件

```shell
#KeyOnlyFilter让服务端只是返回数据的Row Key
scan 's_behavior', {FILTER => "PrefixFilter('12345') AND KeyOnlyFilter()" , LIMIT=>3}
```



### 删除数据

使用`Delete`，`DeleteAll`，`truncate`命令用来删除列、行、表数据。



##### 删除列

```shell
#语法
delete '<table>', '<rowkey>, '<列族:列标识符>', '[<time stamp>]'

#删除某列的数据
delete 's_behavior','12345_1516592489001_1' ,'ph:o'
```



##### 删除行

```shell
#语法
deleteall '<table>', '<rowkey>

#删除整行的数据
deleteall 's_behavior','12345_1516592489001_1'
```



##### 删除表

```shell
truncate 's_behavior'
```





#### 其他

##### 复制状态查看

HBase开启了集群键复制的时候，可以使用`status`查看复制的状态。

```shell
status 'replication'
```



##### 分区拆分

当`StoreFile`大小达到某个值以后需要手动或者使用自动化程序将分区进行拆分。

```shell
#因为Row Key的第一个字符在0~9之间，使用5进行拆分
split 's_behavior','5'
```



##### 分区主压缩

```shell
major_compact 's_behavior,,1511878479015.e933a5867bd5253211a4ef90e549192f.'
```



##### 开启关闭负载均衡

```shell
balance_switch true
balance_switch false
```



##### 分区手动迁移

```shell
#EncodedRegionName	是RegionName的后缀
#destSeverName		是HBase Web UI 上的分区服务器全名
move' <EncodedRegionName>',’<destSeverName>’

move 'e933a5867bd5253211a4ef90e549192f', 'master2,16020,1513049558323'
```



## 6. 模式设计

因为行键充当HBase表的一级索引，而且HBase没有二级索引机制，因此行键的设计非常重要。



### 行键设计

- 唯一原则
- 长度适中，建议定长方便提取。
- 散列原则：避免读写负载在热点分区

一般会考虑数据分析来组合行键。









## 7. 客户端API

不想写了，看书吧，比较清楚

