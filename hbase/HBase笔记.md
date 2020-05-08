# Hbase笔记



## 逻辑存储结构

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



### Region Server 和 Region 的关系 

- 一个 `Region Server` 就是一个机器节点(服务器)

- 一个 `Region Server` 包含着多个 `Region`

- 一个 `Region` 包含着多个列簇 `(CF)`

- 一个 `Region Server` 中可以有多张 `Table`，一张 `Table` 可以有多个 `Region`


![](.\image\RegionServer和Region的关系.png)

## 架构

![hbase细化架构](.\image\hbase细化架构.png)

HMaster是Master Server实现，用于监控RegionServer实例。

HRegionServer是RegionServer实现，管理Regoins。

Region有多个Store。

