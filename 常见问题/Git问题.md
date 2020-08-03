# GitPushrejected解决方案

```shell
git pull origin master --allow-unrelated-histories
git pull origin master　
```



# SSL certificate problem

```shell
git config --global http.sslVerify false
```



# 上传用户名邮箱

如果只是当前文件夹配置可以不加`--global`

```shell
git config --global user.name 你的目标用户名；
git config --global user.email 你的目标用户名；
```

