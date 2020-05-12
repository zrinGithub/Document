# Hbase笔记

## 基本结构

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

## 架构

![hbase细化架构](.\image\hbase细化架构.png)

HMaster是Master Server实现，用于监控RegionServer实例，同事负责Region在RegionServer中的负载均衡。

HRegionServer是RegionServer实现，管理Regoins。HBase客户端的读写直接与HRegionServer交互。

Region有多个Store。



## HBase Shell

启动HBase进入客户端:

```shell
bin/start-hbase.sh

bin/hbase shell
```

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
enable ‘s_behavior’
```



### DML

数据操纵语言（Data Manipulation Language, DML）包括数据的修改、查询、删除等操作。



#### Put



