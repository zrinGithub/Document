# Java知识点整理

整理的时候源码大多数是看jdk13的代码。



[TOC]



## 一. 知识点分析

### 1. 介绍

- 需要掌握的知识点：

  - 掌握大厂简历投递技巧和流程，校招+社招
  - 学会编写一份适合自己的简历，中高级工程师
  - 掌握常见的HR人事必问面试题
  - 掌握新版javase核心面试题
  - 掌握JDK 集合框架核心源码+连环问面试题
  - 掌握多线程底层源码/AQS/锁设计/线程池+连环问面试题
  - 掌握消息队列连环问面试题
  - 掌握数据库索引、设计、集群搭建、分库分表等高级面试题
  - 远不止，还有更多....

  

   

## 二. 编程基础

### 1. 运算符



#### 运算符 &和&&、|和||的区别吗？



& 按位与操作：只有对应的两个二进制数为1时，结果位才为1

& 两边都运算，而 && 先算 && 左侧，若左侧为false 那么右侧就不运算，判断语句中推荐使用 &&，效率更高

也就是 && 为短路运算符，提高判断性能

```shell
1&1 = 1
1&0 = 0
0&1 = 0
0&0 = 0
```



| 按位或操作：有一个为1的时候，结果位就为1

|要对所有的条件进行判断。||只要满足第一个条件，后面的条件就不再判断。

```shell
1|1 = 1
1|0 = 1
0|1 = 1
0|0 = 0
```



#### 用最有效率的方法计算2乘以8



原理：**将一个数左移n位，相当于乘以2的n次方，位运算是CPU直接支持的，所以效率高**
答案：2<<3



例子：

常见的JDK源码里面HashMap的默认容量16
int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

直接是二进制操作了，表示1左移4位，变成10000，转为10进制也就是16, 直接以二进制形式去运行，效率更高



#### 位移

<< 左移，x<<1相当于x乘以2

\>>右移，x<<1相当于x除以以2

\>>>无符号右移，忽略符号位，空位补0



#### 传递两个非0的int数值进去，实现变量交换的方式

- 基础：临时变量

- 替换

  ```java
  public static void swap(int a, int b){
      System.out.printf("a=%d, b=%d",a,b);
      a = a + b;
      b = a - b ;
      a = a - b;
      System.out.printf("\na=%d, b=%d",a,b);
  }
  ```

  

- 异或运算 

  **一个数与另一个数异或两次是其本身， 一个数和自身异或结果是0，一个数和0异或得到本身** 

  即：`x^y^y=x x^x=0 x^0=x`
  
  ```java
public static void swap2(int a, int b){
      System.out.printf("a=%d, b=%d",a,b);
      a = a^b;   
      b = b^a;   // newB = a^b^b = a
  	a = a^b;   // newA = a^b^a = b
      System.out.printf("\na=%d, b=%d",a,b);
}
  ```







### 2. 关于数据类型

#### java数据类型分类

- 基础数据类型：byte、short、int、long、float、double、char、boolean
- 引用数据类型：其他都是引用类型
- String和Enum分别是什么类型：引用类型

 



#### ++i与i++

  ```java
  int i = 5;
  //返回5
  //return i++; 
  //返回6
  return ++i; 
  ```



#### `==` 和`equals()`的区别

  - 基本数据类型比较 要用==判断是否相等
  - 引用数据类型： **==比较的是内存地址是否一样**，不同对象的内存地址不一样，equals比较的是具体的内容， 也可以让开发者去定义什么条件去判断两个对象是否一样

 

### 3. try-catch-finally

#### try-catch-finally里面都有return，会返回什么？

在执行try、catch中的**return之前一定会执行finally中的代码**（如果finally存在），如果finally中有return语句，就会直接执行finally中的return方法，所以finally中的return语句一定会被执行的。

执行流程：finally执行前的代码里面有包含return，则会先确定return返回值，然后再执行finally的代码，最后再执行return

**尽量不要在真实的项目里面这么写。**



示例代码：

```java
public class ExceptionReturnDemo {
    public static void main(String[] args) {
        System.out.println("test1():" + test1());
        System.out.println("test2():" + test2());
    }

    public static int test1() {
        int a = 1;
        try {
            System.out.println(a / 0);
            a = 2;
        } catch (ArithmeticException e) {
            a = 3;
            return a;
        } finally {
            a = 4;
        }
        return a;
    }

    public static int test2() {
        int a = 1;
        try {
            System.out.println(a / 0);
            a = 2;
        } catch (ArithmeticException e) {
            a = 3;
            return a;
        } finally {
            a = 4;
            return a;
        }
    }
}
/**
 * main() output:
 * test1():3
 * test2():4
 */
```



#### try-with-resource

JDK7以前需要自己关闭资源：

```java
OutputStream fileOutputStream = new FileOutputStream(path);
try {
	fileOutputStream.write("test".getBytes());
} catch (Exception e) {
	e.printStackTrace();
} finally {
	try {
        //手动关闭资源
    	fileOutputStream.close();
	} catch (Exception e) {
    	e.printStackTrace();
	}
}
```





JDK7就出现了try-with-resource的写法，也就是try(...)在括号声明的资源可以自动关闭。

需要关闭的资源只要实现了`java.lang.AutoCloseable`（这个类的version可以看到从1.7开始），就可以⾃动被关闭

示例代码：

```java
//可以放置多个对象用分号隔开，越早声明，越晚关闭
 try (
FileInputStream fis = new FileInputStream(path);
BufferedInputStream bis = new BufferedInputStream(fis);
FileOutputStream fos = new FileOutputStream(path);
BufferedOutputStream bos = new BufferedOutputStream(fos);
 	  ) {
	int size;
    byte[] buf = new byte[1024];
	while ((size = bis.read(buf)) != -1) {
    	bos.write(buf, 0, size);
	}
} catch (Exception e) {
	e.printStackTrace();
}
```



JDK9之后进行了进一步的优化：

资源可以在外部进行初始化，只需要在try(...)括号里面引用即可。

```java
OutputStream outputStream = new FileOutputStream(path);
OutputStream outputStream2 = new FileOutputStream(path);
try (outputStream; outputStream2) {
	outputStream.write("电话交换机".getBytes());
} catch (Exception e) {
	e.printStackTrace();
}
```



### 4. 文件API和递归

#### 找出目录下的所有子目录以及子文件

```java
public static void main(String[] args) {
    //找出某目录下的所有子目录以及子文件并打印到控制台上
    List<String> paths = new ArrayList<>();
    
    getAllFilePaths(new File("/dir/path"), paths);

    for (String path : paths) {
        System.out.println(path);
    }
}

private static void getAllFilePaths(File filePath, List<String> paths) {
    File[] files = filePath.listFiles();
    if (files == null) {
        return;
    }
    for (File f : files) {
        if (f.isDirectory()) {
            paths.add(f.getPath());
            getAllFilePaths(f, paths);
        } else {
            paths.add(f.getPath());
        }
    }
}
```



### 5. 字符串



#### String str = new String("ASD"); 创建了几个对象



答案：

首先去常量池找“ASD”这个对象，如果没有这个对象则会创建一个，堆里面也会创建一个。如果常量池存在的话就只会在堆里面创建一个。



#### 字符串比较

下面是比较什么？输出结果是什么？为什么是这样的结果 

```java
String str1= new String("ASD"); 
String str2= "ASD"; 
String str3= "ASD"; 
System.out.println(str1 == str2); //false
System.out.println(str2 == str3); //true
```

==实在比较内存地址

str1是new String开辟的新的地址，str2是把常量池存在的对象地址返回，所以不一样。

str2与str3都是比较常量池对象地址。





 写出下面代码的各个结果？如果需要两个都为true，应该怎么修改

```java
String s1 = "xdclass";
String s2 = s1 + ".net";  //变量 + 常量 = 来自堆

String s3 = "xdclass" + ".net";  //常量 + 常量 = 来自常量池
System.out.println(s2 == "xdclass.net"); //false
System.out.println(s3 == "xdclass.net"); //true
```

 

答案：
第一条语句打印的结果为false

s2 = s1 + ".net",   //变量+常量=堆
构建了一个新的string对象，并将对象引用赋予s2变量，常量池中的地址不一样，但是值一样。



第二条语句打印的结果为true，javac编译可以对【字符串常量】直接相加的表达式进行优化，不用等到运行期再去进行加法运算处理，而是直接将其编译成一个这些常量相连的结果.



如果需要第一个输出为true，只需要把变量改为常量即可 `final String s1 = "xdclass";`
不管是`new String("XXX")`和直接常量赋值, 都会在字符串常量池创建.只是`new String("XXX")`方式会在堆中创建一个对象去指向常量池的对象, 普通的常量赋值是直接赋值给变量





#### String、StringBuffer与StringBuilder的区别与应用

- 相同点

**三者都是final， 不允许被继承**

**底层都是char[]字符数组实现**



- 不同点

可变：

String、StringBuffer与StringBuilder中，**String是不可变对象，另外两个是可变的**



并发：

StringBuilder 效率更快，因为它不需要加锁，不具备多线程安全

StringBuffer里面操作方法用**synchronized** ，效率相对更低,是线程安全的；



应用场景：
	操作少量的数据用String，但是常改变内容且操作数据多情况下最好不要用 String ，因为每次生成中间对象性能会降低

​	单线程下操作大量的字符串用StringBuilder，虽然线程不安全但是不影响

​	多线程下操作大量的字符串，且需要保证线程安全 则用StringBuffer

 

### 6. 面向对象

#### 面向对象的四大特性

**抽象**
关键词`abstract`声明的类叫作抽象类，`abstract`声明的⽅法叫抽象⽅法

⼀个类⾥包含了⼀个或多个抽象⽅法，类就必须指定成抽象类
抽象⽅法属于⼀种特殊⽅法，只含有⼀个声明，没有方法体

应用：抽象工厂、支付下的多平台
		
	
**封装**
封装是把过程和数据设定访问权限，对数据的访问只能通过已定义的接口或⽅法

在java中通过关键字private，protected和public实现封装。

封装把对象的所有组成部分组合在⼀起，封装定义程序如何引⽤对象的数据，

封装实际上使用方法将类的数据隐藏起来，控制⽤户对类的修改和访问数据的程度。 适当的
封装可以让代码更容易理解和维护，也加强了代码的安全性

类封装、⽅法封装



**继承**

⼦类继承⽗类的特征和⾏为，使得⼦类对象具有⽗类的⽅法和属性，⽗类也叫基类，具有公共的方法和属性  	



**多态**	
同⼀个⾏为具有多个不同表现形式的能⼒

优点：减少耦合、灵活可拓展

⼀般是继承类或者重写方法实现

 

#### Overload和Override的区别？

重载**Overload**：表示同一个类中可以有多个名称相同的方法，但这些方法的参数列表各不相同，参数个数或类型不同

重写**Override**：表示子类中的方法可以与父类中的某个方法的名称和参数完全相同

 

#### 接口是否可以继承接口？接口是否支持多继承？类是否支持多继承？接口里面是否可以有方法实现

- 接⼝⾥可以有静态⽅法和⽅法体
- 接⼝中所有的⽅法必须是抽象⽅法（JDK8之后就不是）
- 接⼝不是被类继承了，⽽是要被类实现
- 接⼝⽀持多继承, 类不⽀持多个类继承

⼀个类只能继承⼀个类，但是能实现多个接⼝,接⼝能继承另⼀个接⼝，接⼝的继承使⽤extends关键字，和类继承⼀样

 

#### JDK8里面接口新特性

- interface中可以有static⽅法，但必须有⽅法实现体，该⽅法只属于该接⼝，接⼝名直接调⽤ 该⽅法
- 接⼝中新增default关键字修饰的⽅法，default⽅法只能定义在接⼝中，可以在⼦类或⼦接⼝ 中被重写default定义的⽅法必须有⽅法体
- ⽗接⼝的default⽅法如果在⼦接⼝或⼦类被重写，那么⼦接⼝实现对象、⼦类对象，调⽤该 ⽅法，以重写为准
- 本类、接⼝如果没有重写⽗类（即接⼝）的default⽅法，则在调⽤default⽅法时，使⽤⽗类（接口） 定义的default⽅法逻辑

 

### 7. List

#### Vector和ArrayList、LinkedList说明

答案：

- 线程安全
  - ArrayList：底层是`数组实现`，线程不安全，查询和修改非常快，但是增加和删除慢
  - LinkedList: 底层是`双向链表`，线程不安全，查询和修改速度慢，但是增加和删除速度快
  - Vector: 底层是`数组实现`，线程安全的，操作的时候使用`synchronized`进行加锁
- 使用场景
  - Vector已经很少用了
  - 增加和删除场景多则用LinkedList
  - 查询和修改多则用ArrayList

  

#### ArrayList应该怎么做才能保证线程安全

- 自己写个包装类，根据业务一般是对 add/update/remove加锁 

- `Collections.synchronizedList(new ArrayList<>());` 使用synchronized加锁包装返回：

```java
    public static <T> List<T> synchronizedList(List<T> list) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list) :
                new SynchronizedList<>(list));
    }
	//可以自己传入变量锁
    static <T> List<T> synchronizedList(List<T> list, Object mutex) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list, mutex) :
                new SynchronizedList<>(list, mutex));
    }
```



- `CopyOnWriteArrayList`（顾名思义写时复制，在修改、删除和新增的时候复制并对写操作加锁，因为先复制再操作，所以操作的是副本的内容能够保证一致性） 使用`ReentrantLock`加锁

 ```java
public CopyOnWriteArrayList(Collection<? extends E> c);
 ```



#### CopyOnWriteArrayList知识点

了解`CopyOnWriteArrayList`吗？和 `Collections.synchronizedList`实现线程安全有什么区别, 使用场景是怎样的？

- `CopyOnWriteArrayList`：修改会拷贝新的数组操作（add、set、remove等)，修改后指向新的集合，使用`ReentrantLock`保证不会有多个线程同时拷贝一份数组。

  - 场景：适用**读多写少**场景(读不加锁的，删除、增加、修改是需要加锁的,)

  

- 对比`Collections.synchronizedList`：每个方法中都使用了`synchronized`同步锁

  - 场景：写操作性能比`CopyOnWriteArrayList`好，读操作性能并不如`CopyOnWriteArrayList`

 

- `CopyOnWriteArrayList`的设计思想是怎样的,有什么缺点？

  设计思想：**读写分离**+**最终一致**

  缺点：内存占用问题，写时复制机制，内存里会同时驻扎两个对象的内存。如果旧的对象和新写入的对象大，则容易发生Yong GC和Full GC



#### ArrayList扩容机制

JDK1.7之前ArrayList默认大小是10，JDk1.7之后是0。若已经指定大小则集合大小为指定的。

```java
//jdk8源码
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
public ArrayList() {
	this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    }
}
```



新增元素的时候，会首先确保容量充足，然后把数据写入下一个位置：

```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}

//minCapacity = size + 1 也就是最小增容的情况应该为当前容量+1
private void ensureCapacityInternal(int minCapacity) {
	ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}
```



默认的大小是10，当需要扩容的时候，最小会扩容到这个默认值

ArrayList里面的容量和size不是一个概念，size是指有内容的大小，容量是指底层数组的大小

```java
private static final int DEFAULT_CAPACITY = 10;

//计算容量，这里如果当前容量为0，会取默认值（这里是10）和最小扩容量的最大值
//如果当前不是空列表，则直接返回最小扩容量
private static int calculateCapacity(Object[] elementData, int minCapacity) {
	if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
    	return Math.max(DEFAULT_CAPACITY, minCapacity);
	}
    return minCapacity;
}

//对于扩容量的边界检查
private void ensureExplicitCapacity(int minCapacity) {
	modCount++;
	// overflow-conscious code
    if (minCapacity - elementData.length > 0)
    	grow(minCapacity);
}
```



`ArrayList`的元素个数大于其容量，新的容量 = 原始大小+原始大小/2

对这个新的容量要进行两个条件验证：

1. 新的容量是否超出最大边界
2. 新的容量是否小于最小扩容量

```java
private void grow(int minCapacity) {
	// overflow-conscious code
	int oldCapacity = elementData.length;
	// 新的容量=原始大小+原始大小/2
	int newCapacity = oldCapacity + (oldCapacity >> 1);
	// 新设定容量小于最小扩容平量，则取最小扩容量
	if (newCapacity - minCapacity < 0)
		newCapacity = minCapacity;
    // 新设定容量大于最大值，超出了边界数据
	if (newCapacity - MAX_ARRAY_SIZE > 0)
		//hugeCapacity判断非负数以及超过int最大值
		newCapacity = hugeCapacity(minCapacity);
	// minCapacity is usually close to size, so this is a win:
	elementData = Arrays.copyOf(elementData, newCapacity);
}
```



### 8. Map

#### Map相关基础知识

Map的实现 

- `HashMap`、`Hashtable`、`LinkedHashMap`、`TreeMap`、`ConcurrentHashMap`



#### HashMap和Hashtable 的区别

  - `HashMap`：底层是基于数组+链表，非线程安全的，默认容量是16、允许有空的健和值
  - `Hashtable`：基于哈希表实现，线程安全的(加了`synchronized`)，默认容量是11，不允许有null的健和值

 

#### hashCode和equals

`hashcode()`

顶级类`Object`里面的方法，所有的类都是继承`Object`,返回是一个`int`类型的数。根据一定的hash规则(存储地址，字段，长度等)，映射成一个数组，即散列值。
	

`equals()`

顶级类Object里面的方法，所有的类都是继承Object,返回是一个boolean类型

根据自定义的匹配规则，用于匹配两个对象是否一样，一般逻辑如下：

- 判断地址是否一样
- 非空判断和Class类型判断
- 强转
- 对象里面的字段一一匹配

使用场景：对象比较、或者集合容器里面排重、比较、排序



编写一个User对象，重写里面的`hashcode()`和`equal()`方法

```java
@Data
public class User {
    private int age;
    private  String name;
    private Date time;

    @Override
    public int hashCode() {
        //int code = age/name.length()+time.hashCode();
        //return code
        return Objects.hash(age,name,time);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) 
            return false;
        User user = (User) obj;
        return age == user.age 
            && Objects.equals(name, user.name) 
            && Objects.equals(time, user.time);
    }
}
```

 

 

#### HashMap和TreeMap选择

`HashMap`: 散列桶(数组+链表)

可以实现快速的存储和检索，适用于在map中插入删除和定位元素



`TreeMap`:使用存储结构是一个平衡二叉树->红黑树，可以自定义排序规则，要实现`Comparator`接口

能便捷的实现内部元素的各种排序，但是一般性能比`HashMap`差，适用于需要排序的map结构（微信支付签名)



#### Set和Map的关系

`Set`核心就是不保存重复的元素，存储一组唯一的对象
`Set`每一种实现都是对应`Map`里面的一种封装，
`HashSet`对应的就是`HashMap`，`TreeSet`对应的就是`TreeMap`



#### 需要排序的Map

 `LinkedHashMap`：按照自然的添加顺序

`TreeMap`： 按照设定（`Comparator`接口实现）的排序规则

 

#### 线程安全的Map

多线程环境下可以：

- `concurrent`包下的`ConcurrentHashMap`，里面使用CAS来进行操作

- `Collections.synchronizedMap()`里面会在所有的操作外面包一层`synchronized`。

`ConcurrentHashMap`虽然是线程安全，但是他的效率比`Hashtable`（使用`synchronized`）要高很多



#### HashMap实现原理

HashMap底层结构：数组+链表+红黑树优化（jdk8才有红黑树）

链表越长，遍历就越消耗时间。红黑树能够把链表查询的O(n)优化到O(logn)

```java
	//Node数组组成的map表结构
	transient Node<K,V>[] table;
	
	//Node是一个链表的结构
	static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;
		
        //......
    }
	
```



在JDK1.8中，链表的长度大于8，链表会转换成红黑树

```java
if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
	treeifyBin(tab, hash);
```



#### 数据结构：树

二叉查找树在特殊情况下会变成一条线性结构，和原先的链表存在一样的深度遍历问题，查找性能就会慢，
使用红黑树主要是提升查找数据的速度，红黑树是平衡二叉树的一种，插入新数据后会通过左旋，右旋、变色等操作来保持平衡，解决单链表查询深度的问题

数据量少的时候操作数据，遍历线性表比红黑树所消耗的资源少，且前期数据少 平衡二叉树保持平衡是需要消耗资源的，所以前期采用线性表，等到一定数之后变换到红黑树



#### 处理Hash碰撞方法

- 开放地址法
  当发生地址冲突时，按照某种方法继续探测哈希表中的其他存储单元，直到找到空位置为止。

- 再哈希法

  取其他方式获取hash值，比如首字母失败就用第二个字母
- 链表法

  像`HashMap`这样优化结构，每个桶里面加入链表结构












