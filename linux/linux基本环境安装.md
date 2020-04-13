# linux基本环境安装

虚拟机镜像：CentOS-7-x86_64-Minimal-1908.iso



[TOC]



## java

- yum安装

```shell
#查看库
yum -y list java*

#安装
yum install -y java-1.8.0-openjdk-devel.x86_64

#测试
java -version
```

 yum安装的jdk在 `/usr/lib/jvm` 路径下。



- 使用安装包

地址：https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

```shell
#下载官网包，或者直接上传服务器
wget download_url

#解压
tar xzvf jdk-8u131-linux-x64.tar.gz -C /usr/local/java8

#配置环境变量
vim /etc/profile

export JAVA_HOME=/usr/local/java/jdk1.8.0_161
export PATH=$PATH:$JAVA_HOME/bin
export CLASSPATH=.:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar:$CLASSPATH

#刷新环境
source /etc/profile

#测试
java -version
```



## Maven

[下载地址]( https://maven.apache.org/download.cgi )

```shell
#解压
tar xzvf apache-maven-3.3.3-bin.tar.gz -C /usr/local/maven

#配置环境变量
vim /etc/profile

export MAVEN_HOME=/usr/local/maven/apache-maven-3.3.3
export PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin

#刷新环境
source /etc/profile

#测试
mvn -v
```



## Git

- yum安装

```shell
#安装
yum -y install git

#校验
git --version
```

安装地址在 /usr/libexec/git-core 



- 安装包

[下载地址]( https://github.com/git/git/releases )

```shell
#解压
tar xzvf git-2.26.0.tar.gz 

#安装编译依赖并把自动安装的git卸载
yum install -y curl-devel expat-devel gettext-devel openssl-devel zlib-devel gcc perl-ExtUtils-MakeMaker
yum remove git

#安装到指定路径
make prefix=/usr/local/git install

#配置环境变量
vim /etc/profile

export PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin:/usr/local/git/bin

#刷新环境
source /etc/profile

#测试
git --version
```



## SSH

测试的机器为三台：

server-1

server-2

server-3



使用免密登录就要把主要操作机器的公钥放在其他机器里面



```shell
#1.安装ssh
#一般环境都已经安装了
#使用 yum list installed|grep openssh查看
yum install openssh-server -y

#2.在三台机器上分别生成密钥
#产生密钥，-t指定密钥类型
ssh-keygen -t rsa
#使用默认地址，直接回车，后面的选择也是直接回城
Enter file in which to save the key (/root/.ssh/id_rsa): 

#完成后在~/.ssh/id_rsa下生成id_rsa、id_rsa.pub
#依次为私钥、公钥

#3.授权
#3.1在server-1操作
#公钥保存为authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

#把key文件复制到server-2，需要输入密码
scp ~/.ssh/authorized_keys root@server-2:~/.ssh/

#此时server-1可以免密登录server-2

#3.2在server-2操作
#追加本机公钥
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
#把key文件复制到server-3机器，需要输入密码
scp ~/.ssh/authorized_keys root@server-3:~/.ssh/

#3.3在server-2操作
#追加本机公钥
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

#至此可以用server-1免密登录所有机器
```

