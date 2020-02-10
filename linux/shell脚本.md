# shell脚本编写

[TOC]



## 一. 基础知识

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

# 小数处理
echo "scale=2; 0.13 + 0.1" | bc | awk '{printf "%.4f\n", $0}'

-F #指定分割符
cat /etc/passwd | awk -F":" '{print $1}'

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
>$2 #代表第二列
>$0 #代表一整行



### 4. sed

