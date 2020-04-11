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

