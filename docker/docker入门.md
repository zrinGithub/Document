# Docker笔记

## 一.  环境安装：

- 环境说明：

系统：centos7

- docker版本：
  - docker EE 企业版本
  - docker CE 社区版本
- 安装：

```shell
 # 安装wget
 yum install wget
 
 # 下载阿里云docker社区版yum源
 cd /etc/yum.repos.d/
 wget http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
 
 # 查看docker安装包
 yum list | grep docker
 
 # 安装docker ce社区版本
 yum install -y docker-ce.x86_64
 
 # 设置开机启动
 systemctl enable docker
 
 # 更新xfsprogs
 yum -y update xfsprogs
 
 # 启动docker
 systemctl start docker

 # 查看版本
 docker version
 
 # 查看详细信息
 docker info
```



## 二. Docker基本操作

### 1. 镜像的搜索下载、查看删除

```shell
# 查看本地镜像
docker images

# 搜索镜像
docker search centos

# 搜索并过滤出官方镜像
docker search --filter "is-official=true" centos

# 搜索并过滤大于多少颗星的镜像
docker search --filter starts=10 centos

# 下载centos镜像
docker pull centos:7

# 修改本地镜像名字（小写）
docker tag centos:7 mycentos:1

# 删除本地镜像
docker rmi centos:7
```



### 2. 配置阿里云镜像加速

- 阿里云镜像加速器配置地址： https://cr.console.aliyun.com/cn-shenzhen/instances/mirrors 



- 配置：

```shell
# 编辑后台启动配置
vim /etc/docker/daemon.json
{
  "registry-mirrors": ["https://002ovtdy.mirror.aliyuncs.com"]
}	

# 重启
systemctl daemon-reload && systemctl restart docker
```



### 3. docker的容器与镜像

- 一个镜像可以启动无数的容器
- 容器与容器互相不影响，处于隔离的环境



### 4. docker容器构建

```shell
# 构建容器：
docker run -itd --name=mycentos centos:7
-i:交互模式运行
-d:后台运行容器，返回容器id
-t:为容器重新分配一个伪输入终端
--name:为容器指定名称

# 查看本地所有容器：
docker ps -a

# 查看本地正在运行的容器
docker ps

# 停止容器
docker stop CONTAINER_ID/CONTAINER_NAME 

# 一次性停止所有容器
docker stop ${docker ps -a -q}

# 启动容器
docker start CONTAINER_ID/CONTAINER_NAME

# 重启容器
docker restart CONTAINER_ID/CONTAINER_NAME

# 删除容器
docker rm CONTAINER_ID/CONTAINER_NAME

# 强制删除容器
docker rmi -f CONTAINER_ID/CONTAINER_NAME

# 查看容器详细信息
docker inspect CONTAINER_ID/CONTAINER_NAME

# 进入容器
docker exec -it CONTAINER_ID/CONTAINER_NAME /bin/bash
```



### 5. 容器的文件复制与挂载

```shell
# 将宿主机复制到容器
docker cp 宿主机本地地址 CONTAINER_ID/CONTAINER_NAME:容器地址
docker cp /root/a.txt mycentos:/home

# 容器复制到宿主机
docker cp CONTAINER_ID/CONTAINER_NAME:容器地址 宿主机本地地址 

# 宿主机文件挂载到
docker run -itd -v 宿主机地址：容器路径 镜像
```



## 三. 自定义镜像

### 1. 基于docker commit制作镜像

容器的修改和保存：

```shell
# 启动并进入容器
docker run -it centos:7 bin/bash

# 在/home路径下创建文件夹
mkdir /home/test

# 安装ifconfig
yum -y install net-tools

# 重启容器
docker restart CONTAINER_ID/CONTAINER_NAME 

# 删除容器并在此启动容器
docker rm CONTAINER_ID/CONTAINER_NAME && docker run -it centos:7 bin/bash

# 构建容器
docker commit CONTAINER_ID/CONTAINER_NAME
docker commit -a "author" -m "comment" CONTAINER_ID/CONTAINER_NAME
-a:标注作者
-m:说明注释

# 查看详细信息
docker inspect CONTAINER_ID/CONTAINER_NAME

# 启动容器
docker run -itd CONTAINER_ID/CONTAINER_NAME /bin/bash

# 进入容器查看
docker exec -it CONTAINER_ID/CONTAINER_NAME /bin/bash
```



### 2. 基于dockerFile制作镜像

- DockerFile构建镜像

```shell
# this is a dockerfile
FROM centos:7
MAINTAINER AUTHOR 123@qq.com
RUN echo "正在构建镜像------"
WORKDIR /home/zr
COPY /root/a.txt /home/zr
RUN yum install -y net-tools
```

