# shell脚本编写

[TOC]



## 一. 基础知识

可参考教程： https://linuxtools-rst.readthedocs.io/zh_CN/latest/index.html 

### 1. linux基础操作：

-  cd命令
  切换目录。
  到根目录 ：cd /
  
-  ls命令
  列出目录内容。
  列出/var目录的文件和目录的信息 ：ls –l /var；最常用方式 ls –ltr
  
- cat命令
  查看小文件内容。
  查看test.txt 文件内容 ：cat test.txt
  
-  chmod命令
  修改文件或目录权限。
  修改test.sh 为自己可执行：chmod u+x test.sh
  
- chown命令
  变更文件或目录的拥有者或所属群组。
  修改test.txt 属主为mysql ：chown mysql:mysql test.txt
  
- cp命令
  拷贝文件。
  拷贝文件test.sh 为 test.sh_bak：cp test.sh test.sh_bak
  
- diff命令
  对比文件差异。
  对比文件test.sh test.sh_bak 是否有差异diff test.sh test.sh_bak
  
- find命令
  查询文件。
  查询本目录下面的test.txt：find ./ -name test.txt
  
- mv命令
  移动或更名现有的文件或目录。
  移动 test.sh到/bin目录下：mv test.sh /bin/
  
- rm命令
  删除文件或目录。
  删除文件test.sh ：rm test.sh
  
- touch命令
  创建一个空文件。
  创建一个空的test.txt文件：touch test.txt
  
- which命令
  环境变量$PATH设置的目录里查找符合条件的文件。
  查询find命令在那个目录下面：which find
  
- ssh命令
  远程安全登录方式。
  登录到远程主机：ssh ${IP}
  
- grep命令
  查找文件里符合条件的字符串。
  从test.txt文件中查询test的内容：grep test test.txt
  
- wc命令
  统计。
  统计test.txt文件有多少行：wc -l test.txt
  
- date命令
  查询主机当前时间。
  查询主机当前时间：date
  
- exit命令
  退出命令。
  退出主机登录：exit
  
- kill命令
  杀进程。
  杀掉test用户下面的所有进程：ps -ef | awk ‘$1==”test” {print $2}’ | xargs kill -9
  
- id命令
  查看用户。
  查看当前用户：id ；查询主机是否有test用户：id test
  
- ps命令
  查询进程情况。
  查询test.sh进程：ps -ef | grep test.sh
  
- sleep命令
  休眠时间。
  休眠60秒 ：sleep 60
  
- uname命令
  查询主机信息。
  查询主机信息：uname -a
  
- passwd命令
  修改用户密码。
  使用root修改test用户的密码：passwd test
  
- ping命令
  查看网络是否通。
  查询本主机到远程IP的网络是否通：ping ${IP}
  
- df命令
  查看磁盘空间使用情况。
  查看主机的空间使用情况 ：df -h
  
- echo命令
  标准输出命令。
  对变量test进行输出：echo $test
  
- pwd命令
  查询所在目录。
  查询当前所在目录：pwd
  
- head命令
  查看文件的前面N行。
  查看test.txt的前10行：head -10 test.txt
  
- tail命令
  查看文件的后面N行。
  查看test.txt的后10行：tail -10 test.txt
  
- mkdir命令
  创建目录。
  创建test目录：mkdir test
  
- nl命令
  
  显示行号
  
  nl /etc/passwd
  
-  echo $LANG

   查看字符集
   
-  basename 取得文件文档名

   dirname 取得文件的目录名

   ```shell
   basename /etc/passwd
   passwd
   
   dirname /etc/passwd    
   /etc
   ```

   



### 2. vi基础操作：

- 命令行模式

```shell
h/j/k/l		方位移动
home/end	行首/行尾
G			尾行
gg			首行
num G 		跳至num行
o			新增一行插入
x			删除一个字符
dd			删除一整行
u			退回修改
i			进入编辑模式
```

- 底层操作

```shell
:q				退出
:q!				丢弃缓存退出
:w filename		保存到另一个文件
:wq				缓存保存到文件
:set nu			显示行数
/				搜索内容
```



### 3. 解释器

\#!/bin/bash（默认）

\#!/bin/ksh

\#!/bin/bsh

\#!/bin/sh

shell脚本可以不需要解释器



### 4. 文件权限和脚本执行

- 文件权限操作

文件权限：- rw- r-- r--
目录权限：drw-r--r--
分三列：每三个为一列，分别是所有者(owner)，所属组(group)，其他(others)

rwx r:4 w:2 x:1



- 脚本执行

  - 增加权限后执行

    - `chmod +x xxx.sh`

    - ./xxx.sh

  - 直接执行

    - `sh xxx.sh`或者`bash xxx.sh`或者`source xxx.sh`



## 二. 变量与常见符号

### 1. 变量

不需要提前声明变量

```shell
# b=123
# echo $b
123
# 加了{}能够限定内容
# echo ${b}
123
# echo ${b}hello
123hello

# 使用$()把执行结果返回给变量
date1=$(date --date='2 days ago' +%Y-%m-%d)

# $?获取函数返回值上一个命令的退出状态
#echo $?
0

# 测试脚本
vim test.sh
#!/bin/bash
#by zr 2020-02-09
#test for variable
echo "参数1：$1"
echo "参数2：$2"
echo "参数总数：$#"
echo "所有参数值：$*"
echo "脚本名称：$0"

# 测试
# sh test.sh jack 1 2 io
参数1：jack
参数2：1
参数总数：4
所有参数值：jack 1 2 io
脚本名称：test.sh
```



注意：**$*和$@的区别**

 $* 和 $@ 都表示传递给函数或脚本的所有参数 

```shell
vim test.sh

#!/bin/bash
echo "print each param from \"\$*\""
for var in "$*"
do
    echo "$var"
done

echo "print each param from \"\$@\""
for var in "$@"
do
    echo "$var"
done

# 执行
sh test.sh a b c d

print each param from "$*"
a b c d
print each param from "$@"
a
b
c
d
```



### 2. 符号

```shell
>	覆盖内容
cat > xxx.txt
>> 	追加内容

;	执行多条命令
cat xxx.txt;ls

|	管道符号
cat xxx.txt | grep keywords

&&	与，命令1执行成功才会执行命令2
命令1 && 命令2

||	或，命令1错误才执行命令2
命令1 || 命令2

""	输出变量值
# echo "b = $b"
b = 123

''	输出本身值
# echo 'b = $b' 
b = $b

``	输出命令的执行结果
# echo `uname`
Linux

2>/dev/null	错误输出
1>/dev/null	正确输出

cat not_exist.txt 2>/dev/null
```



### 3. 运算符

- 整数

```shell
加：
expr 12 + 6 expr $a + $b
echo $[12 + 6] echo $[a + b]
echo $((12 + 6)) echo $((a + b))

减：
expr 12 - 6 expr $a - $b
echo $[12 - 6] echo $[a - b]
echo $((12 - 6)) echo $((a - b))

乘：
expr 12 \* 6 expr $a \* $b
echo $[12 * 6] echo $[a * b]
echo $((12 * 6)) echo $((a * b))

除：
expr 12 / 6 expr $a / $b
echo $((12 / 6)) echo $((a / b))
echo $[12 / 6] echo $[a / b]

求余：
expr 12 % 6 expr $a % $b
echo $((12 % 6)) echo $((a % b))
echo $[12 % 6] echo $[a % b]
```

- 小数

```shell
# 使用bc计算器
yum -y install bc

# 进入交互界面
bc

# 管道
echo "1.2+1.3" | bc

# 设置精度，scale对加减无效，加减可以再除以1
echo "scale=2;10/3"|bc
3.33
echo "scale=2;1.2+1.3" | bc
2.5
echo "scale=2;(1.2+1.3)/1" | bc
2.50
```



### 4. 条件判断

- 文件

```shell
-e 目标是否存在（exist）
-d 是否为路径（directory）
-f 是否为文件（file）

#判断当前目录下是否有foer.sh这个文件，假如没有就创建出 xxx.sh
[ -e foer.sh ] || touch xxx.sh 
```



- 权限

```shell
-r 是否有读取权限（read）
-w 是否有写入权限（write）
-x 是否有执行权限（excute）

[ -x 123.txt ] && echo '有执行权限'
```



- 整数值（int型）

```shell
-eq 等于（equal）
-ne 不等于(not equal)
-gt 大于（greater than）
-lt 小于（lesser than）
-ge 大于或者等于（greater or equal）
-le 小于或者等于（lesser or equal）

[ 9 -gt 8 ] && echo '大于'
```



- 小数（浮点型）

```shell
# 使用bc计算器
[ `echo '1.2 < 1.3' | bc` -eq 1 ] && echo '小于'
```



- 字符串

```shell
= 相等
!= 不相等

[ 'kkkkk' != 'kkkk' ] && echo '不等于'
```



## 三. 脚本输入与输出

### 1. read 脚本输入

语法：**read -参数**

-p：给出提示符。默认不支持"\n"换行
-s：隐藏输入的内容
-t：给出等待的时间（s），超时会退出read
-n：限制读取字符的个数，触发到临界值会自动执行



示例

```shell
#!/bin/bash
read -p "`echo -e "\033[36;5m input your name \033[0m"` : " name
read -n 1 -t 5 -s -p "pass : "  pass
echo -e "\n"
echo "hello $name $pass"
```

>man echo
>
>-e     enable interpretation of backslash escapes



### 2. 输出上色

语法: echo -e "\033[字背景颜色;字体颜色;特效字符串\033[关闭属性"

- 字体色范围：30-37

```shell
echo -e "\033[30m 黑色字 \033[0m"
echo -e "\033[31m 红色字 \033[0m"
echo -e "\033[32m 绿色字 \033[0m"
echo -e "\033[33m 黄色字 \033[0m"
echo -e "\033[34m 蓝色字 \033[0m"
echo -e "\033[35m 紫色字 \033[0m"
echo -e "\033[36m 天蓝字 \033[0m"
echo -e "\033[37m 白色字 \033[0m"
```



- 字背景颜色范围：40-47

```shell
echo -e "\033[40;37m 黑底白字 \033[0m"
echo -e "\033[41;30m 红底黑字 \033[0m"
echo -e "\033[42;34m 绿底蓝字 \033[0m"
echo -e "\033[43;34m 黄底蓝字 \033[0m"
echo -e "\033[44;30m 蓝底黑字 \033[0m"
echo -e "\033[45;30m 紫底黑字 \033[0m"
echo -e "\033[46;30m 天蓝底黑字 \033[0m"
echo -e "\033[47;34m 白底蓝字 \033[0m"
```



- 特效范围

```shell
echo -e "\033[0m 无任何特效 \033[0m"
echo -e "\033[1m 高亮度 \033[0m"
echo -e "\033[4m 下划线 \033[0m"
echo -e "\033[5m 闪烁 \033[0m"
```



> 可以使用命令``包含
>
> read -p "`echo -e "\033[36m input your name \033[0m"` : " name



## 四. 数据处理

### 1. grep

- 作用：对数据进行 行的提取

- 语法：grep [选项]...[内容]...[file]

  -v 		#对内容进行取反提取
  -n 		#对提取的内容显示行号
  -w 		#精确匹配
  -i 		#忽略大小写
  ^ 		#匹配开头行首
  -E 		#正则匹配

- 示例：`grep -n test a.txt`

### 2. cut

- 作用：对数据进行 列的提取

- 语法：cut [选项]...[file]

  -d #指定分割符
  -f #指定截取区域
  -c #以字符为单位进行分割

- 示例：

```shell
# 以':'为分隔符，截取出/etc/passwd的第一列跟第三列
cut -d ':' -f 1,3 /etc/passwd

# 第一列到第三列
cut -d ':' -f 1-3 /etc/passwd

# 第二列到最后一列
cut -d ':' -f 2- /etc/passwd

# 第二个字符到第九个字符
cut -c 2-9 /etc/passwd

# 所有可登陆普通用户
# /bin/bash代表可登录用户 /sbin/nologin 不可登录
grep '/bin/bash' /etc/passwd | cut -d ':' -f 1 | grep -v root
```

​	

### 3. awk

- 作用：对数据进行 列的提取
- 语法：
  - awk '条件 {执行动作}' 文件名
  - awk '条件1 {执行动作} 条件2 {执行动作} ...' 文件名
  - awk [选项] '条件1 {执行动作} 条件2 {执行动作} ...' 文件名
- print：

```shell
printf #格式化输出，不会自动换行。
%ns：字符串型，n代表有多少个字符；
%ni：整型，n代表输出几个数字；
%.nf：浮点型，n代表的是小数点后有多少个小数

print #打印出内容，默认会自动换行 只有awk里面有

\t #制表符
\n #换行符

printf '%s\t%s\t%s\n' 1 2 3 4 5 6
```



- 示例：

```shell
# 存储情况查询
df -h |grep /dev/sda1 | awk '{printf "/dev/sda1的使用率是："} {print $5 }'

# 查看进程的pid
jps | grep -i KAFKA | awk -F" " '{print $1}'

# 小数处理
echo "scale=2; 0.13 + 0.1" | bc | awk '{printf "%.4f\n", $0}'

-F #指定分割符
cat /etc/passwd | awk -F ":" '{print $1}'

BEGIN #在读取所有行内容前就开始执行，常常被用于修改内置变量的值
FS #BEGIN时定义分割符
cat /etc/passwd | awk 'BEGIN {FS=":"} {print $1}'

END #结束的时候 执行
NR #行号
cat /etc/passwd | awk -F":" '{print $1} END {print "END"}'
df -h | awk 'NR==2 {print $5}'
awk -F ":" '(NR>=2 && NR<=3) {print $1}' /etc/passwd
df -h | awk 'NR>=2 && NR<=5' /etc/passwd
```

>$1 #代表第一列
>
>$2 #代表第二列
>
>$0 #代表一整行



### 4. sed

- 作用：对数据进行处理（选取，新增，替换，删除，搜索）

- 语法：sed \[选项][动作] 文件名

  常见的选项与参数：

```shell
-n #把匹配到的行输出打印到屏幕
p #以行为单位进行查询，通常与-n一起使用
df -h | sed -n '2p'

d #删除
sed '2d' df.txt	# 只是输出删除了，不会对源文件删除	
#文件来源： df -h > df.txt

a #在行的下面插入新的内容
sed '2a 1234567890' df.txt

i #在行的上面插入新的内容
sed '2i 1234567890' df.txt

c #替换
sed '2c 1234567890' df.txt

s/要被取代的内容/新的字符串/g #指定内容进行替换
sed 's/0%/100%/g' df.txt

-i #对源文件进行修改(高危操作，慎用，用之前需要备份源文件)
sed -i 's/0%/100%/g' df.txt

# 搜索
cat -n df.txt | sed -n '/100%/p'

-e #表示可以执行多条动作
cat -n df.txt | sed -n -e 's/100%/100%-----100%/g' -e '/100%-----100%/p'
```



## 五.  循环控制

### 1. if

- 语法：

```shell
# if
if [ 条件判断 ]
	then
	执行动作
fi

# if-else
if [ 条件判断 ]
	then
	执行动作
else
	执行动作
fi

# if-else-if
if [条件判断]
    then
    执行动作
elif [条件判断]
    then
    执行动作
elif [条件判断]
    then
    执行动作
fi

# 示例
#!/bin/bash
#判断输入的数字是否大于10
read -p "请输入数字：" number

if [ $number -eq 10 ]
        then
        echo '等于10'
elif [ $number -lt 10 ]
        then
        echo '小于10'
elif [ $number -gt 10 ]
        then
        echo '大于10'
fi
```



### 2. for

- 语法：

```shell
for 变量名 in 值1 值2 值3
    do
    执行动作
    done
   
for 变量名 in `命令`   
	do
    执行动作
    done
   
for ((条件))
	do
    执行动作
    done
    
# 示例

#!/bin/bash
for i in 1 2 3 0 9
do
echo $i
done

#!/bin/bash
for i in `seq 4 14`
do
echo $i
done

#!/bin/bash
for ((i=1;i<11;i++))
do
echo $i
done

#!/bin/bash
for i in $(cat a.txt)
do
ping -c 2 $i
done

cat > a.txt
www.baidu.com
www.taobao.com
```



### 3. case

- 语法

```shell
case 变量 in
值1 )
执行动作1
;;
值2 )
执行动作2
;;
值3 )
执行动作3
;;
....
esac

# 示例
#!/bin/bash
read -p "input your city:" city
case $city in
'A')
echo 'A:1'
;;
'B')
echo 'B:2'
;;
'C')
echo 'C:3'
;;
*)
echo 'cannot recognize'
esac
```



### 4. while

- 语法：

```shell
while [ 条件判断式 ]
do
执行动作
done
```

- 示例：

```shell
vim whileTest.sh

#!/bin/bash
i=0
sum=0
while [ $i -lt $1 ]
do
sum=$(($sum+$i))
i=$(($i+1))
done
echo "the sum is : $sum"

sh whileTest.sh 101
the sum is : 5050
```



## 六. 常用脚本

### 1. 选择脚本 

```shell
vim cmdChoose.sh

#!/bin/bash
while [ 1 ]
do
# 这里的EOF只要前后一直就可以了，可以改为AA、BB
cat << EOF
**********************************
1. 计算你输入的目录下有多少文件		
2. 计算从0加到输入的数字
3. 批量创建用户
4. 测试用户名密码是否匹配
5. 测试ip
6. 巡检内存使用率
7. 数据库查询学生成绩
q. 退出
**********************************
EOF

echo "输入选项："
read key
case $key in
# 计算你输入的目录下有多少文件
1 )
clear
sh countFiles.sh
;;

# 计算从0加到输入的数字
2 )
clear
sh countNums.sh
;;

# 批量创建用户
3 )
clear
sh batchCreateUser.sh
;;

# 测试用户名密码是否匹配
4 )
clear
sh testPassword.sh
;;

# 测试ip
5 )
clear
sh testIp.sh
;;

# 巡检内存使用率
6 )
clear
sh testDf.sh
;;

# 数据库查询学生成绩
7 )
clear
sh selectScores.sh
;;

# 退出
q )
clear
echo '-------quit-------'
break
;;
esac
done
```

### 2. 计算输入的目录有多少文件

```shell
vim countFiles.sh

#!/bin/bash
# 计算输入的目录有多少文件
# 直接输出
echo "统计当前目录下文件的个数（包括子文件夹）："`ls -lR $1|grep "^-"|wc -l`
# 赋值给变量
dNum=`ls -l $1|grep "^d"|wc -l`
echo "当前目录下文件夹的个数：$dNum"
echo "当前目录下文件的个数："`ls -l $1|grep "^-"|wc -l`
```

### 3. 计算从0加到输入的数字

```shell
vim countFiles.sh

#!/bin/bash
# 计算从0加到输入的数字
i=0
sum=0
while [ $i -le $1 ]
do
sum=$(($sum+$i))
i=$(($i+1))
done
echo "结果是 ： $sum"
```

### 4. 批量创建用户

```shell
vim batchCreateUser.sh

#!/bin/bash
# 批量创建用户
read -p '用户名称：' name
read -p '用户数量：' number
for(( i=1;i<=$number;i++ ))
do
	cat /etc/passwd | grep "${name}$i" 1>/dev/null
	# 查看是否存在用户
	if_exist=`echo $?`
	if [ $if_exist -eq 1 ]
		then
		# 创建用户
		useradd ${name}$i 2>/dev/null && echo "创建用户${name}$i成功！"
		
		# 生成随机密码
		password=`head -2 /dev/urandom | md5sum | cut -c 1-8`
		# 给新用户设置密码并把用户名密码放在文本中
		echo $password | passwd --stdin ${name}$i
		echo "${name}$i:$password" >> /home/shell/newuser_passwd.txt
	else
    	echo "${name}$i用户已经存在"
	fi
done
```

> 删除用户以及主目录：
>
>  userdel -r username



### 5. 验证用户名密码

```shell
vim testPassword.sh

#!/bin/bash
# 参考数据库查询脚本
```

### 6. 测试ip

```shell
vim testIp.sh

#!/bin/bash
# 参考for里面的脚本
```

### 7. 巡检内存使用率

```shell
vim testDf.sh

#!/bin/bash
# 巡检内存使用率
mem_total=`free -m | sed -n '2p' | awk {'print $2'}`
mem_used=`free -m | sed -n '2p' | awk {'print $3'}`
mem_free=`free -m | sed -n '2p' | awk {'print $4'}`
Percent_mem_used=`echo "scale=2; $mem_used/$mem_total*100" | bc`
Percent_mem_free=`echo "scale=2; $mem_free/$mem_total*100" | bc`
now_time=`date "+%Y-%m-%d %H-%M-%S 星期%w"`
echo -e "\n"
echo -e "$now_time\n内存的使用率是：$Percent_mem_used%"
echo -e "内存还剩：$Percent_mem_free%"

# 检查负载是否有压力(超过1M)
if [ $mem_used -gt 1 ]
	then
	echo -e "\033[31m 内存使用率已经超过负载能力，目前使用率达到：$Percent_mem_used% \033[0m"
else
	echo '目前内存负载正常'
fi

echo -e "\n"
```

### 8. 数据库查询学生成绩

```shell
vim selectScores.sh

#!/bin/bash
# 数据库查询学生成绩
read -p '输入学生姓名：' sname
read -s -p '输入数据库用户：' user
echo -e "\n"
read -s -p '输入数据库密码：' pass
sql="select * from student.user where name='${sname}';"
# -e 表示非交互执行sql，也就是在shell中直接执行sql
mysql -u${user} -p -e "${sql}"
exit
```



## 七. 登录其他机器

```shell
# 记录地址
vim ip.txt

A|192.168.199.200
B|192.168.199.201
C|192.168.199.202

# 登录脚本
#!/bin/bash
# 登录其他机器
RegionIp=`cat /root/ip.txt | grep $1 | awk -F "|" '{print $2}'`
ssh ${RegionIp}
```



## 八. 常用脚本解析

### 1. kafka脚本

- 启动脚本

```shell
# 一般使用方式
kafka-server-start.sh -daemon config/server.properties

vim kafka-server-start.sh

# 判定参数，$#表示参数数量
if [ $# -lt 1 ];
then
        echo "USAGE: $0 [-daemon] server.properties [--override property=value]*
"
        exit 1
fi

# $0脚本名称，这里base_dir=脚本路径
base_dir=$(dirname $0)

# $KAFKA_LOG4J_OPTS如果为空，则添加日志配置
if [ "x$KAFKA_LOG4J_OPTS" = "x" ]; then
    export KAFKA_LOG4J_OPTS="-Dlog4j.configuration=file:$base_dir/../config/log4
j.properties"
fi

# $KAFKA_HEAP_OPTS为空，则设置堆内存为1G
if [ "x$KAFKA_HEAP_OPTS" = "x" ]; then
    export KAFKA_HEAP_OPTS="-Xmx1G -Xms1G"
fi

# 配置参数
EXTRA_ARGS=${EXTRA_ARGS-'-name kafkaServer -loggc'}

# 获取后台执行参数
COMMAND=$1
case $COMMAND in
  -daemon)
    EXTRA_ARGS="-daemon "$EXTRA_ARGS
    # 这里使用shift吧-daemon参数略过了，所以最后$@不会重复带这个参数
    shift
    ;;
  *)
    ;;
esac

# 执行目录下的kafka-run-class.sh脚本
exec $base_dir/kafka-run-class.sh $EXTRA_ARGS kafka.Kafka "$@"
```





- 关闭脚本

```shell
vim kafka-server-stop.sh

SIGNAL=${SIGNAL:-TERM}
PIDS=$(ps ax | grep -i 'kafka\.Kafka' | grep java | grep -v grep | awk '{print $
1}')

if [ -z "$PIDS" ]; then
  echo "No kafka server to stop"
  exit 1
else
  kill -s $SIGNAL $PIDS
fi
```



其实可以改为：

```shell
PIDS=$(jps | grep -i KAFKA | awk -F" " '{print $1}')
```



### 2. 查看本机TCP连接数

```shell
#对包含TCP的内容进行统计
#/^tcp/使用正则确定位置
#$NF最后一个字段
#++S[$NF]就是对最后一个字段进行字典统计
netstat -n | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'
```

 

## 九. 工具插件

### 1. lsof（ list open files）

lsof是查看当前系统文件的工具  https://linuxtools-rst.readthedocs.io/zh_CN/latest/tool/lsof.html 。

```shell
 #安装
 yum install -y lsof
 
 #列出谁在使用6379端口
 lsof -i:6379
 
 #列出所有tcp 网络连接信息
 lsof -i tcp
```

