# Docker笔记

[TOC]



## 印象

Docker 		搬运工

Container	集装箱



与虚拟机的区别：

虚拟机是构建在物理CPU和内存上面，属于硬件级别。

Docker构建在操作系统层面，利用操作系统的容器化技术。



Docker Engine是c/s架构的应用程序，我们使用docker client交互，底层使用Docker Damon，Registry存储镜像，默认是从Docker Hub(hub.docker.com)公共镜像进行拉取。

Image 就是自定义镜像，可以通过镜像创建Container.



相关博客：https://www.qikqiak.com/k8s-book/ 

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

### 1. 镜像的搜索下载、查看、删除、重命名

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



### 4. docker容器构建（centos和nginx示例）

```shell
# 构建容器：
docker run -itd --name=mycentos centos:7
-i:交互模式运行
-d:后台运行容器，返回容器id
-t:为容器重新分配一个伪输入终端
--name:为容器指定名称

docker run -it --rm ubuntu:16.04 /bin/bash
--rm 退出终端就自动删除
需要使用exit退出

docker run --name nginx81 -d -p 80:80 -v /usr/docker/nginx/html:/usr/share/nginx/html nginx
-p前面是本机端口，后面是容器端口
-v前面是本地文件，后面是nginx容器默认主页
顺便说一句默认nginx配置文件在容器里的/etc/nginx里面

# 查看本地所有容器（两种方式都可以）：
docker ps -a
docker container ls --all

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



### 6. 打印容器操作系统中的说明

```shell
[root@localhost ~]# docker run -it --name=mycentos centos:7 /bin/bash
[root@43544a8a4a65 /]# cat /etc/os-release 
NAME="CentOS Linux"
VERSION="7 (Core)"
ID="centos"
ID_LIKE="rhel fedora"
VERSION_ID="7"
PRETTY_NAME="CentOS Linux 7 (Core)"
ANSI_COLOR="0;31"
CPE_NAME="cpe:/o:centos:centos:7"
HOME_URL="https://www.centos.org/"
BUG_REPORT_URL="https://bugs.centos.org/"

CENTOS_MANTISBT_PROJECT="CentOS-7"
CENTOS_MANTISBT_PROJECT_VERSION="7"
REDHAT_SUPPORT_PRODUCT="centos"
REDHAT_SUPPORT_PRODUCT_VERSION="7"
```



### 7.查看空间和容器启动日志

```shell
[root@localhost ~]# docker system df
TYPE                TOTAL               ACTIVE              SIZE                RECLAIMABLE
Images              11                  2                   2.159GB             2.159GB (99%)
Containers          2                   0                   27B                 27B (100%)
Local Volumes       0                   0                   0B                  0B
Build Cache         0                   0                   0B                  0B

[root@localhost ~]# docker logs CONTAINER_ID/CONTAINER_NAME
可以加-f跟踪，-t时间
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

# 重启进入容器，此时之前创建的文件夹还在,安装的网络工具使用ifconfig也存在
docker restart CONTAINER_ID/CONTAINER_NAME 
docker exec -it CONTAINER_ID/CONTAINER_NAME /bin/bash

# 查看容器修改
docker diff CONTAINER_ID/CONTAINER_NAME

# 删除容器（这里-f是强制删除，否则需要先暂停容器）并再次启动容器
# 此时新建的文件和yum安装的工具都不见了
docker rm -f CONTAINER_ID/CONTAINER_NAME && docker run -it centos:7 /bin/bash

# 构建容器，这里使用容器来构建自定义镜像，这样容器里面做的修改就能保存下来
docker commit CONTAINER_ID/CONTAINER_NAME 自定义命名
docker commit -a "author" -m "comment" CONTAINER_ID/CONTAINER_NAME
-a:标注作者
-m:说明注释

如：
docker commit -a "zr" -m "centos test" 96cef324046e mycentos:v1

# 查看历史记录（有点像git的提交记录，因为你修改了镜像，所以之前提交的和原镜像差不多）
docker history NEW_IMAGE_ID/NEW_IMAGE_NAME:TAG
如：
docker history mycentos:v1

# 查看详细信息
docker inspect CONTAINER_ID/CONTAINER_NAME

# 启动容器
docker run -itd IMAGE_ID/IMAGES_NAME /bin/bash

# 进入容器查看
docker exec -it CONTAINER_ID/CONTAINER_NAME /bin/bash
```



### 2. 基于dockerFile制作镜像

- Dockerfile构建镜像（COPY只能用相对路径）

```shell
vim Dockerfile

# this is a dockerfile
FROM centos:7
MAINTAINER AUTHOR 123@qq.com
RUN echo "正在构建镜像------"
WORKDIR /home/zr
COPY a.txt /home/zr
RUN yum install -y net-tools
```



- 构建（-t表示tag，最后的.表示当前路径）

```shell
docker build -t zrcentos:v1 .
```



- 查看

```shell
docker images
```



- 验证

进入查看文件以及插件是否存在。



### 3. 镜像分层结构



![](.\images\镜像分层结构.png)

- 共享资源
- 对容器的任何修改都是发生在容器层的
- 容器可读可写，镜像层只读



### 4. Dockerfile的基础指令

- FROM
  - 基于哪一个镜像
- MAINTAINER
  - 作者
- COPY
  - 复制文件到镜像中（只能用相对路径，否则找不到）
- ADD
  - 复制文件进入镜像（如果文件是.tar.gz会解压）
- WORKDIR
  - 指定工作目录（不存在会自动创建）
- ENV
  - 设置环境变量
- EXPOSE
  - 暴露容器的端口
- RUN
  - 在构建镜像的时候执行，用于镜像层面
- ENTRYPOINT
  - 在容器启动的时候执行，作用于容器层，dockerfile里有多条指令时只允许执行最后一条。
- CMD
  - 在容器启动的时候执行，作用于容器层，dockerfile里有多条指令时只允许执行最后一条。
  - 容器启动后执行默认的命令或者参数，允许被修改。
- 命令格式：
  - shell命令格式：RUN yum install -y net-tools
  - exec命令格式：RUN [ "yum","install" ,"-y" ,"net-tools"]
- Dockerfile示例

```dockerfile
#第一个
#构建显示：
#images building!
#容器启动后显示:
#container starting ！！！ echo container starting...
FROM centos:7
RUN echo "images building!"
CMD ["echo","container","starting..."]
ENTRYPOINT ["echo","container","starting ！！！"]
#第二个
#构建显示：
#Using cache--和第一个相同
#容器启动后显示:
#container2 starting ！！！ echo container2 starting...
FROM centos:7
RUN echo "images building!"
CMD ["echo","containe1r","starting..."]
CMD ["echo","container2","starting..."]
ENTRYPOINT ["echo","container","starting ！！！"]
ENTRYPOINT ["echo","container2","starting ！！！"]
#第三个
#容器启动后显示ps -ef效果
FROM centos:7
CMD ["-ef"]
ENTRYPOINT ["ps"]
```



## 四. 自定义环境实战

官方网址： https://hub.docker.com/ 



### 1. Java网站镜像

- 本地宿主机配置jdk

```shell
# 新增环境变量
vi /etc/profile
export JAVA_HOME=/usr/local/jdk
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

# 生效
source /etc/profile

# 检验
java -version
```



- 使用Dockerfile配置

```dockerfile
# 编辑Dockerfile
vi Dockerfile

FROM centos:7
ADD jdk-8u211-linux-x64.tar.gz /usr/local
RUN mv /usr/local/jdk1.8.0_211 /usr/local/jdk
ENV JAVA_HOME=/usr/local/jdk
ENV JRE_HOME=$JAVA_HOME/jre
ENV CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
ENV PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
ADD apache-tomcat-8.5.35.tar.gz /usr/local
RUN mv /usr/local/apache-tomcat-8.5.35 /usr/local/tomcat
EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/catalina.sh","run"]

# 当前目录下构建镜像
docker build -t zrcentos:jdk .

# 启动容器
docker run -itd -p 80:8080 -v /root/test/ROOT:/usr/local/tomcat/webapps/ROOT zrcentos:jdk /bin/bash

# 进入容器
docker exec -it CONTAINER_ID /bin/bash

# 网站访问
http://ip:80
```

可以在挂载的路径加上文件index.html这样就能访问。



### 2.构建nginx镜像

```dockerfile
# 编辑Dockerfile
vi Dockerfile

FROM centos:7
ADD nginx-1.16.0.tar.gz /usr/local
COPY nginx_install.sh /usr/local
RUN sh /usr/local/nginx_install.sh
EXPOSE 80

# 编辑nginx的安装脚本
vi nginx_install.sh

#!/bin/bash
yum install -y gcc gcc-c++ make pcre pcre-devel zlib zlib-devel
cd /usr/local/nginx-1.16.0
./configure --prefix=/usr/local/nginx && make && make install

# 制作镜像
docker build -t zrcentos:nginx .

# 这里容器nginx是以daemon方式启动，退出容器，nginx也会停止
/usr/local/nginx/sbin/nginx
# 使用前台方式启动
/usr/local/nginx/sbin/nginx -g "daemon off;"

# 启动容器
docker run -itd -p 80:80 zrcentos:nginx /usr/local/nginx/sbin/nginx -g "daemon off;"
```



### 3. redis镜像

```dockerfile
# 编辑Dockerfile
FROM centos:7
ADD redis-4.0.9.tar.gz /home
COPY redis_install.sh /home
RUN sh /home/redis_install.sh
ENTRYPOINT /usr/local/redis/bin/redis-server /usr/local/redis/conf/redis.conf

# 编辑redis安装脚本
#!/bin/bash
yum install -y gcc gcc-c++ make openssl openssl-devel
cd /home/redis-4.0.9
make && make PREFIX=/usr/local/redis install
mkdir -p /usr/local/redis/conf/
cp /home/redis-4.0.9/redis.conf /usr/local/redis/conf/
sed -i '69s/127.0.0.1/0.0.0.0/' /usr/local/redis/conf/redis.conf
sed -i '88s/protected-mode yes/protected-mode no/' /usr/local/redis/conf/redis.conf

# 制作镜像
docker build -t zrcentos:redis .

# 启动容器
docker run -itd -p 6380:6379 zrcentos:redis

# 进入容器
docker exec -it CONTAINER_ID /bin/bash

# 容器内连接
/usr/local/redis/bin/redis-cli -p 6380
```



### 4. mysql镜像

```dockerfile
# 拉取镜像
docker pull mysql:5.7

# 启动镜像，参考:https://hub.docker.com/_/mysql
docker run --name some-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7

# 进入镜像
docker exec -it CONTAINER_ID env LANG=C.UTF-8 /bin/bash

# 编辑Dockerfile
# init.sql 可以在初始化时候运行
FROM mysql:5.7
WORKDIR /docker-entrypoint-initdb.d
ENV LANG=C.UTF-8
ADD init.sql  .

# 测试sql语句
mysql -uroot -p123456
```



## 五. 网络模式与特权指令



