使用版本3.7.4

# 环境



安装

https://www.python.org/

有三种安装包：

web-based需要联网

executable installer可执行文件

embeddable zip 嵌入式版本，可以集成到其他开发应用中



ide: 

- pycharm



# 数据类型

## 数字

整型：可正可负，不带小数点，在Python3里面没有大小限制，可以存储长整型

浮点型：可正可负，带小数点，可以使用科学计数法表示  1.1e2 = 110

复数：实数与虚数组合 a+bj，或者complex(a,b) 其中a,b都是浮点类型



可以使用type()查看：

```python
print(type(1))
print(type(-1.23))
print(type(complex(1, 2)))

结果
<class 'int'>
<class 'float'>
<class 'complex'>
```



重新赋值则内存地址改变：

```python
a = 1
print(id(a))
a = 2
print(id(a))

结果
140709488718080
140709488718112
```



## 进制转换



进制表示：

二进制：0b1010

八进制：0o101

十六进制：0x101



进制转换：

```python
转换为二进制：
print(bin(2))  # 0b10
print(bin(0x10))  # 0b10000

对应8进制函数oct
对应10进制int
对应16进制hex
```



## 布尔

首字母必须大写

```python
a = True
a = False 
```



这里的类型：

```python
a = True
print(isinstance(a, int))
print(isinstance(a, bool))
#数字只要不是0就是True
```



## 字符串

### 引号的作用

```python
#单引号和双引号
print("I'm sss")
print('"tk" sdds')

'''
结果：
I'm sss
"tk" sdds
'''

# 三引号的作用
print("""aaa
ddd
ccc""")
print("aaa\n"
      "ddd\n"
      "ccc")
'''
结果：
aaa
ddd
ccc
aaa
ddd
ccc
'''
```



### 字符串操作

截取：

```python
a = "0123456789"
print(a[0])  # 0
print(a[-1])  # 9
print(a[0:-1])  # 012345678 左闭右开
print(a[0:])  # 0123456789
print(a[-2:])  # 99
```



格式化：

```python
print("use %s get %d" % ("netty", 2))
'''
%c 字符
%s
%d
'''
```



也可以直接拼接：

```python
print("my name is", "aaa", "age:", 2)  # my name is aaa age: 2
```



# 运算符

基本：   +       -       *       /     % 

比较：==       >       <          !=

赋值：=       +=    -=      *=     /=   //=     &=    

还有：

// 整除 

```
>>> 10//3
3
```



幂

```
>>> 3**3
27
```





位运算：& |   <<     >>    ~     ^    

逻辑运算：and 	or	not

成员运算符：   in     not in（用于字符串或者列表）

身份运算符：  is      is not  （用于判定引用的位置）

```python
>>> a="change"
>>> b="ch"
>>> b+="ange"
>>> a==b
True
>>> a is b
False
```



# 条件语句

if-else

```python
if a>b:
    xxx
elif a<b:
    xxx
else:
    xxx  
```



三元表达式

`c = a if a>b else b`



for

```python
res = 0
for i in range(10):  # 0-9
    res += i
    print(i)
else:
    print(res)
```





while:

```python
i = 0
while i < 100:
    i += 1
else:
    print(i)
```





循环嵌套：

```python
for i in range(3):
    for j in range(2):
        print(i, j)
        
"""
0 0
0 1
1 0
1 1
2 0
2 1
"""
```



 此外还有break、continue



# 数据结构

## 列表

```python
array = [0, 1, 2, 3]
array.append(4)  # 添加
del array[2]  # 删除 [0, 1, 3, 4]
print(len(array))  # 长度 4
print(array[-2])  # 索引 3
print(array.index(3))  # 定位 2
array.insert(1, 90)  # 指定位置插入 [0, 90, 1, 3, 4]
a = array.pop(0)  # 删除指定位置 [90, 1, 3, 4]
```



## 集合

初始化：

```python
a = {1, 2, 3}
print(type(a))
a = {}  # 不能这样初始化空集合，这是字典的方式
print(type(a))
a = set()
print(type(a))

"""
<class 'set'>
<class 'dict'>
<class 'set'>
"""
```



操作：

```python
a = {1, 2, 3}
a.add(3)  # 排除重复元素 {1, 2, 3}
a.add(5)  # {1, 2, 3, 5}
a.update({9, 10})  # 添加过个元素 {1, 2, 3, 5, 9, 10}

a.remove(2)  # 删除 {1, 3, 5, 9, 10}
# a.remove(100)  # 不存在元素会报错
a.discard(100)  # 不会报错
pop = a.pop()  # 取元素 {3, 5, 9, 10}
a.remove(10)  # 取指定元素 {3, 5, 9}
a.clear()  # 清空 set()

a1 = {1, 2, 3}
a2 = {1, 3, 5}
union = a1.union(a2)  # 合集 union ={1, 2, 3, 5}
```

这里容易有误解，集合是无序的，它符合bag这种数据结构，为什么上面的示例pop的总是元素呢？因为本身数字的hash正好满足一个有序的状态，而set本身是hash寻址之后进行存储的。当然不是数字都有序，因为存储的hash桶数量固定。



## 元组（tuple）

元组的数据不能修改

```python
t = (1,)  # 单一表示元组的时候，需要加逗号，否则被认为是int
t = (1, 2, 3, ["aa", "cc"])

print(t[-1])  # 使用下标访问 ['aa', 'cc']
# 不能修改元素（列表那种里面的值可以改）
t[-1][1] = "sss"  # (1, 2, 3, ['aa', 'sss'])

t2 = t + t  # 创建新的(1, 2, 3, ['aa', 'sss'], 1, 2, 3, ['aa', 'sss'])

del t2  # 只能删除整个元组
```



## 字典（dict）





