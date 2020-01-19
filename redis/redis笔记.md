# Redis 笔记



## 环境搭建

- 基本环境：centos7

- redis版本：



### 安装指令：

[官网说明](https://redis.io/download)

```shell
# 使用wget下载
yum install wget

wget http://download.redis.io/releases/redis-4.0.9.tar.gz

# 解压
tar xzf redis-4.0.9.tar.gz -C /usr/local/redis

# 进入目录开始编译
cd /usr/local/redis/redis-4.0.9/

make

# 启动
src/redis-server
src/redis-cli
```



### 启动方式：

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



