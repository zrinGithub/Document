# 自签发证书在nginx开启https支持



1）上面的命令利用openssl库生成了相关的证书和key，有效期设置为了100年


> sudo openssl req -x509 -nodes -days 36500 -newkey rsa:2048 -keyout /etc/ssl/private/nginx-selfsigned.key -out /etc/ssl/certs/nginx-selfsigned.crt
> 

2）修改nginx配置文件中，针对https的特性，增加了几点优化配置



> server {
>     listen 8443 ssl;
>     keepalive_timeout 70;
>     server_name localhost;
>
> ​    ssl_certificate /etc/ssl/certs/nginx-selfsigned.crt;
> ​    ssl_certificate_key /etc/ssl/private/nginx-selfsigned.key;
>
> ​    ssl_session_cache shared:SSL:10m;
> ​    ssl_session_timeout 10m;
>
> ​    ssl_ciphers HIGH:!aNULL:!MD5;
> ​    ssl_prefer_server_ciphers on;
>
> ​    location / {
> ​        root html;
> ​        index index.html index.htm;
> ​    }
> }
