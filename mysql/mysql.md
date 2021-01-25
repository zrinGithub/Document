# Mysql 笔记

```sh
mysql> select version();
+-----------+
| version() |
+-----------+
| 5.7.26    |
+-----------+
1 row in set (0.06 sec)
```



### 长语句分割

```sql
-- 自定义语句结束的符号是//
delimiter //
```





## DCL数据控制

DCL：Data Control language 数据控制语言

用于设置更改数据库用户、角色权限

GRANT、DENY、REVOKE





### 限定root账户指定IP登录

```sql
-- 查看、修改root在哪台机器可以登录
select user,host from mysql.user where user='root';
update mysql.user set host='localhost' where user='root';

-- 刷新权限
flush privileges;
```



### 用户密码

修改用户密码：

```sql
-- set password for 用户@ip = password('密码');
set password for root@localhost = password('root');

-- mysqladmin -u用户 -p旧密码 password 新密码;
mysqladmin -uroot -proot password 123;

-- update mysql.user set authentication_string=password('密码') where user='用户' and host='ip';
update mysql.user set authentication_string=password('root') where user='root' and host='localhost';
```



### 免密登录

1. 修改my.cnf 在[mysqld]下面添加 `skip-grant-tables`

2. 重启mysql服务

3. mysql -uroot -p 直接免密登录

4. 修改密码

   



### 创建用户

```sql
create user 'username'@'host' identified by 'password';

username：你将创建的用户名

host：指定该用户在哪个主机上可以登陆，如果是本地用户可用localhost，如果想让该用户可以从任意远程主机    登陆，可以使用通配符%,如create user 'user1'@'%' identified by '123456';

password：该用户的登陆密码，密码可以为空，如果为空则该用户可以不需要密码登陆服务器
```



同样的，可以创建用户指定网段登录：

```sql
-- 用户名user1，密码为空，指定在120的网段登录
create user 'user1'@'120.%.%.%' identified by '';
```



### 查看用户权限

```sql
   select * from mysql.user where user='pig'\G
    mysql> show grants for 'pig'@'%';
    +---------------------------------+
    | Grants for pig@%                |
    +---------------------------------+
    | GRANT USAGE ON *.* TO 'pig'@'%' |
    +---------------------------------+
    USAGE：无权限的意思
    mysql> show grants for 'root'@'localhost';
    +---------------------------------------------------------------------+
    | Grants for root@localhost                                           |
    +---------------------------------------------------------------------+
    | GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION |
    +---------------------------------------------------------------------+
    WITH GRANT OPTION:表示这个用户拥有grant权限，即可以对其他用户授权
```





### 删除用户语法

```sql
   drop user 'pig'@'%';
   delete from mysql.user where user='pig';
```





### 库表权限的授权与回收

授权语法：grant

```sql
grant 权限1,权限2..... on 数据库对象 to '用户'@'host' identified by 'password';

对现有用户进行授权：对现有用户pig授予所有库所有表所有权限（因为是现有的，所以不用写登录IP与密码）。
grant all privileges on *.*  to 'pig';

对没有的用户进行授权：创建一个新用户dog授予XD库的所有权限，登录密码123456，任何一台主机登录
grant all privileges on XD.* to 'dog'@'%' identified by '123456';

对没有的用户进行授权：创建一个新用户cat授予XD库的employee表 查与修改权限，登录密码123456，任何一台主机登录
grant select,update on XD.employee to 'cat'@'%' identified by '123456'

对没有的用户进行授权：对用户cat授予XD库的employee表insert 权限，登录密码123456，任何一台主机登录
grant insert on XD.employee to 'cat'@'%' identified by '123456';
```



回收语法：revoke

```sql
回收pig用户的所有权限（注意：并没有回收它的登录权限）
revoke all privileges on *.*  from 'pig' @ '%';
flush privileges;

回收pig用户的所有权限（并回收它的登录权限）
delete from mysql.user where user='pig';
flush privileges;

回收cat用户对XD库的employee的查与修改权限
revoke select,update on XD.employee from 'cat'@'%';
flush privileges;
```





## 事务、视图、触发器、存储过程



### 事务

事务存在的意义：提供数据库从失败中恢复到正常的方法、数据库在异常状态下仍能保持一致性，

可以在并发访问中提供隔离的方法避免操作干扰。



ACID：

- Atomicity：原子操作
- Consistency：数据一致合理
- Isolation：各个事务之间不会相互干扰
- Durability：持久化

mysql的默认引擎就是InnoDB，也就是支持事务。



### 事务的操作

开启：begin;start transaction;

提交：commit

回滚：rollback



开启自动提交：

```sql
-- OFF（0）：表示关闭 ON （1）：表示开启
set autocommit=0;

-- 
show variables like 'autocommit';
```





### 视图

展示表经过处理的部分数据

```sql
-- 创建的基本语法是：
create view <视图名称> as select 语句;
create view <视图名称> (字段) as select 语句;
create or replace view <视图名称>;
    
-- 修改的语法是：
alter view <视图名称> as select 语句;
	
-- 视图删除语法：
drop view <视图名称> ;
	

```



### 触发器

```sql
-- after/before:可以设置为事件发生前或后
-- insert/update/delete:它们可以在执行insert、update或delete的过程中触发
-- for each row:每隔一行执行一次动作
create trigger 触发器名称  after/before   insert/update/delete on 表名  
        for each row
        begin
        sql语句;
        end
        
-- 删除触发器
drop trigger 触发器名称;
```



### 存储过程

脚本

```sql
create procedure 名称 (参数....)
        begin
         过程体;
         过程体;
         end
   
参数：in|out|inout 参数名称 类型（长度）
        in：表示调用者向过程传入值（传入值可以是字面量或变量）
        out：表示过程向调用者传出值(可以返回多个值)（传出值只能是变量）
        inout：既表示调用者向过程传入值，又表示过程向调用者传出值（值只能是变量）
        
声明变量：declare 变量名 类型(长度) default 默认值;
给变量赋值：set @变量名=值;
调用存储命令：call 名称(@变量名);
删除存储过程命令：drop procedure 名称;
查看创建的存储过程命令：show create procedure 名称\G;

```



```sql
创建一个简单的存储过程：
    mysql> delimiter //
    mysql> create procedure  name(in n int)
        ->             begin
        ->             select * from employee limit n;
        ->             end
        -> //
    Query OK, 0 rows affected (0.00 sec)

    mysql> set @n=5;
        -> //
    Query OK, 0 rows affected (0.00 sec)

    mysql> 
    mysql> call name(@n);
```





## 索引、存储引擎

### 引擎操作

数据库引擎就是底层软件组件，设计存储机制、索引技巧、锁定水平。

```sql
如何查看数据库支持的引擎
show engines;

查看当前数据的引擎：
show create table 表名\G

查看当前库所有表的引擎：
show table status\G

建表指定引擎
create table talName (id int,name varchar(20)) engine='InnoDB';

修改表引擎
alter table 表名 engine='MyiSAm';

修改默认引擎
•    vi /etc/my.cnf
•    [mysqld]下面
•    default-storage-engine=MyIsAM
•    记得保存后重启服务
```



### InnoDB MyISAM区别

MyISAM：

- 支持全文索引（full text）
- 不支持事务，不支持外键（含有外键的InnoDB表转MYISAM会失败）
- 表级锁，更新语句会直接锁表
- 会保存表的具体行数，也就是`select count(*) from aaa`不需要全表扫描
- 奔溃后恢复不好



InnoDB

- 支持事务
- 5.6以后开始支持全文索引，
- 行级锁（并非绝对，当执行sql语句时不能确定范围时，也会进行锁全表例如： update table set id=3 where name like 'a%';）;
- 不保存表的具体行数，也就是`select count(*) from aaa`要全表扫描
- 奔溃恢复好；





### 索引相关

索引是单独存储的数据库结构，包含了对数据库所有记录的引用指针，使用索引可以快速找到某列或者多列中有特定值的行。

https://tech.meituan.com/2014/06/30/mysql-index.html

https://bbs.huaweicloud.com/blogs/169243

索引常见的有：

- index 普通索引：普通、允许空值null
- unique 唯一索引：不能相同，允许空值null
- primary key 主键索引
- foreign key 外键索引
- full text 全文索引
- 组合索引



