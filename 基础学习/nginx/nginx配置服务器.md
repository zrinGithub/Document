# OpenResty配置带权限图片服务器

## 安装OpenResty

linux安装OpenResty：

```shell
#添加仓库执行命令
yum install yum-utils
yum-config-manager --add-repo https://openresty.org/package/centos/openresty.repo

#执行安装，会安装在默认的路径下/usr/local/openresty
yum -y install openresty
```



## 配置基础的服务器

编辑`nginx.conf`:

```nginx
        location /image/ {
            root /usr/local/openresty/resFile/;
            autoindex on;
        }
```

`autoindex on`启动目录浏览

之后创建本地目录：/usr/local/openresty/resFile/image/在下面放置文件



## 加入lua脚本

