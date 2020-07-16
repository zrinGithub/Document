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

 

#### <? super E>和<? extends E>的区别

指定的基类（E）都可以接受。

`<? super E>`接收E的父类，既然父类也可以，子类也包含对应变量方法，所以子类其实也可以。

`<? extends E>`只接收E的子类

```java
class Parent {
}

class Base extends Parent {
}

class Sub extends Base {
}

class Acceptor<E> {
    public void sExtends(Collection<? extends E> c) {
    }

    public void sSuper(Collection<? super E> c) {
    }
}

public class TestGeneric {
    public static void main(String[] args) {
        final Acceptor<Base> acceptor = new Acceptor<>();
        //基类都可以满足
        acceptor.sExtends(Arrays.asList(new Base()));
        acceptor.sSuper(Arrays.asList(new Base()));
        //<? extends E>只能适用子类
        acceptor.sExtends(Arrays.asList(new Sub()));
//        acceptor.sExtends(Arrays.asList(new Parent()));
        //<? super E>可以使用所用相关
        acceptor.sSuper(Arrays.asList(new Sub()));
        acceptor.sSuper(Arrays.asList(new Parent()));
    }

}
```





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



在看其他源码前，这三个操作为空：

```java
	// Callbacks to allow LinkedHashMap post-actions
	void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }
```





put：

```java
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

	//获取hash值
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        //如果长度为0，进行初始化
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        //如果指定位置还没有对应元素，直接插入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            //数组位置已经有元素了，则需要在链表后面插入
            Node<K,V> e; K k;
            //如果是指定的桶里面第一个元素hash和key相等，也就是需要修改的位置，直接指定e=第一个元素
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            //如果第一个元素不相等，需要判断是不是已经转换为树结构了
            //需要在树里面做操作
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                //如果是链表，需要在里面进行遍历，找到尾结点进行插入
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        //如果长度已经大于8了，开始转换结构为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //如果找到key一样的数据，也就是修改的位置，直接返回
                    //注意在前面循环判断里面已经设定了(e = p.next)
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    //继续向后面遍历
                    p = e;
                }
            }
            //找到了需要修改的位置
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                //onlyIfAbsent=false的情况下或者存储的为空值（视为absent）的情况下修改值
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        //fail-fast机制
        ++modCount;
        //调整结构
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

这里**先进行插入再进行扩容**。



get：

```java
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

	final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        //校验数组不是空，长度大于0，指定位置有元素
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            //就是第一个元素，直接返回
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                //如果是红黑树，在树里面搜索
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                //如果是链表，在链表里面遍历
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
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







#### ConcurrentHashMap源码解析

主要特点：线程安全。

但是性能比HashTable（全部synchronized同步）



使用分段锁来提高性能，锁粒度更加细化。





**jdk1.7和jdk1.8里面ConcurrentHashMap实现的区别**：



JDK8之前，ConcurrentHashMap使用锁分段技术，将数据分成一段段存储，每个数据段配置一把锁，即segment类，这个类继承ReentrantLock来保证线程安全
技术点：**Segment**+**HashEntry**



JKD8的版本取消Segment这个分段锁数据结构，底层也是使用Node数组+链表+红黑树，从而实现对每一段数据就行加锁，也减少了并发冲突的概率，CAS(读)+Synchronized(写)
技术点：**Node+Cas+Synchronized**





**源码**：

```java
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        //重哈希，减少碰撞的概率
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            //tab懒加载
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            //tabAt使用Unsafe volatile语义从对象的指定偏移获取引用，
            //这里就是指hash值对应位置的节点
            //如果该位置为空，cas初始化节点
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // no lock when adding to empty bin
            }
            //需要扩容
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            //不需要扩容，检测是红黑树还是链表进行处理
            else {
                V oldVal = null;
                //锁住指定的节点f来进行处理
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                //如果hash值和键值都相等替换
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                //如果没有对应的key且到达链表尾部，直接创建新的节点
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        //红黑树处理
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
```







## 三. 并发编程

[Java并发编程笔记](https://github.com/zrinGithub/Document/blob/master/%E5%9F%BA%E7%A1%80%E5%AD%A6%E4%B9%A0/java%E5%B9%B6%E5%8F%91/java%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B%E7%AC%94%E8%AE%B0.md)



### 1. 基础

#### 线程、进程、协程的区别

进程:本质上是一个独立执行的程序，进程是操作系统进行资源分配和调度的基本概念，操作系统进行资源分配和调度的一个独立单位



线程:是操作系统能够进行运算调度的最小单位。它被包含在进程之中，是进程中的实际运作单位。一个进程中可以并发多个线程，每条线程执行不同的任务，切换受系统控制。



协程: 又称为微线程，是一种用户态的轻量级线程，协程不像线程和进程需要进行系统内核上的上下文切换，协程的上下文切换是由用户自己决定的，有自己的上下文，所以说是轻量级的线程，也称之为用户级别的线程就叫协程，一个线程可以多个协程,线程进程都是同步机制，而协程则是异步 
Java的原生语法中并没有实现协程,目前python、Lua和GO等语言支持



关系：一个进程可以有多个线程，它允许计算机同时运行两个或多个程序。线程是进程的最小执行单位，CPU的调度切换的是进程和线程，进程和线程多了之后调度会消耗大量的CPU，CPU上真正运行的是线程，线程可以对应多个协程



#### 协程对于多线程有什么优缺点吗

优点：
    非常快速的上下文切换，不用系统内核的上下文切换，减小开销
    单线程即可实现高并发，单核CPU可以支持上万的协程
    由于只有一个线程，也不存在同时写变量的冲突，在协程中控制共享资源不需要加锁



缺点：
    协程无法利用多核资源，本质也是个单线程
    协程需要和进程配合才能运行在多CPU上
    目前java没成熟的第三方库，存在风险
    调试debug存在难度，不利于发现问题



#### 并发和并行的区别

**并发 concurrency**：
一台处理器上同时处理任务, 这个同时实际上是交替处理多个任务，程序中可以同时拥有两个或者多个线程，当有多个线程在操作时,如果系统只有一个CPU,则它根本不可能真正同时进行一个以上的线程,它只能把CPU运行时间划分成若干个时间段,再将时间段分配给各个线程执行

**并行 parallellism**：
    多个CPU上同时处理多个任务，一个CPU执行一个进程时，另一个CPU可以执行另一个进程，两个进程互不抢占CPU资源，可以同时进行
        

并发指在一段时间内宏观上去处理多个任务。  并行指同一个时刻，多个任务确实真的同时运行。    



#### 线程实现方式

- 继承Thread
- 实现Runnable
- 通过FutureTask包装Callable方式
- 线程池创建（submit包含返回值）



#### 线程状态

`Thread`类里面的`State`里面包含6种，JVM里面包含9种：

```java
    public enum State {
        /**
         * Thread state for a thread which has not yet started.
         */
        NEW,

        /**
         * Thread state for a runnable thread.  A thread in the runnable
         * state is executing in the Java virtual machine but it may
         * be waiting for other resources from the operating system
         * such as processor.
         */
        RUNNABLE,

        /**
         * Thread state for a thread blocked waiting for a monitor lock.
         * A thread in the blocked state is waiting for a monitor lock
         * to enter a synchronized block/method or
         * reenter a synchronized block/method after calling
         * {@link Object#wait() Object.wait}.
         */
        BLOCKED,

        /**
         * Thread state for a waiting thread.
         * A thread is in the waiting state due to calling one of the
         * following methods:
         * <ul>
         *   <li>{@link Object#wait() Object.wait} with no timeout</li>
         *   <li>{@link #join() Thread.join} with no timeout</li>
         *   <li>{@link LockSupport#park() LockSupport.park}</li>
         * </ul>
         *
         * <p>A thread in the waiting state is waiting for another thread to
         * perform a particular action.
         *
         * For example, a thread that has called <tt>Object.wait()</tt>
         * on an object is waiting for another thread to call
         * <tt>Object.notify()</tt> or <tt>Object.notifyAll()</tt> on
         * that object. A thread that has called <tt>Thread.join()</tt>
         * is waiting for a specified thread to terminate.
         */
        WAITING,

        /**
         * Thread state for a waiting thread with a specified waiting time.
         * A thread is in the timed waiting state due to calling one of
         * the following methods with a specified positive waiting time:
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
         *   <li>{@link #join(long) Thread.join} with timeout</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         * </ul>
         */
        TIMED_WAITING,

        /**
         * Thread state for a terminated thread.
         * The thread has completed execution.
         */
        TERMINATED;
    }
```



#### 线程状态转换

![](.\image\线程状态转换.png)

```
sleep
    属于线程Thread的方法
    让线程暂缓执行，等待预计时间之后再恢复
    交出CPU使用权，不会释放锁
    进入阻塞状态TIME_WAITGING，睡眠结束变为就绪Runnable
    
yield
    属于线程Thread的方法
    t1/t2/t3
    暂停当前线程的对象，去执行其他线程
    交出CPU使用权（让出当前时间片），不会释放锁，和sleep类似
    作用：让相同优先级的线程轮流执行，但是不保证一定轮流
    注意：不会让线程进入阻塞状态，直接变为就绪Runnable，只需要重新获得CPU使用权
    
    
join  
    属于线程Thread的方法
    在主线程上运行调用该方法，会让主线程休眠，不会释放已经持有的对象锁
    让调用join方法的线程先执行完毕，再执行当前线程（并没有什么先调用的先执行的说法，比较像barrier或者CountDownLatch，只是针对主线程和子线程顺序）
    
wait
    属于Object的方法
    当前线程调用对象的wait方法，会释放锁，进入线程的等待队列
    需要依靠notify或者notifyAll唤醒，或者wait(timeout)时间自动唤醒
    
notify
    属于Object的方法
    唤醒在对象监视器上等待的单个线程，选择是任意的
    
notifyAll
    属于Object的方法
    唤醒在对象监视器上等待的全部线程
```



### 2. 并发编程常见知识点

- 多线程的业务场景

  异步任务：用户注册、记录日志

  定时任务：定期备份日志、备份数据库

  分布式计算：Hadoop处理任务mapreduce，master-wark(单机单进程)

  服务器编程：Socket网络编程，一个连接一个线程

   

- 不是线程安全的数据结构

  HashMap、ArrayList、LinkedList

   

- 在Java中可以有哪些方法来保证线程安全

  加锁,比如synchronize/ReentrantLock

  使用volatile声明变量，轻量级同步，不能保证原子性(需要解释)

  使用线程安全类(原子类AtomicXXX，并发容器，同步容器 CopyOnWriteArrayList/ConcurrentHashMap等

  ThreadLocal本地私有变量/信号量Semaphore等



#### volatile

volatile保证了共享变量的**可见性**（数据立即写入内存，其他内核存储内存地址数据无效），避免**脏读**

经常放在一起说的：synchronized：保证可见性，也保证原子性



**使用场景**
1、不能保证原子性

2、禁止指令重排（内存屏障），JVM相关的优化没了，效率偏弱

-----



为什么会出现**脏读**？

JAVA内存模型简称 **JMM**
JMM规定所有的变量存在在主内存，每个线程有自己的工作内存,线程对变量的操作都在工作内存中进行，不能直接对主内存就行操作

volatile关键字修修饰的变量写入到主内存，读取也是主内存的值



#### 指令重排&happens-before

指令重排序：

```
指令重排序分两类 编译器重排序和运行时重排序

JVM在编译java代码或者CPU执行JVM字节码时，对现有的指令进行重新排序，主要目的是优化运行效率(不改变程序结果的前提)

int a = 3 //1
int b = 4 //2
int c =5 //3 
int h = a*b*c //4

定义顺序 1,2,3,4
计算顺序  1,3,2,4 和 2,1,3,4 结果都是一样


虽然指令重排序可以提高执行效率，但是多线程上可能会影响结果，有什么解决办法？
解决办法：内存屏障
解释：内存屏障是屏障指令，使CPU对屏障指令之前和之后的内存操作执行结果的一种约束
```



happens-before？

重排序的约束原则，参考博客[Java内存模型以及happens-before规则](https://juejin.im/post/5ae6d309518825673123fd0e)

```
先行发生原则，volatile的内存可见性就提现了该原则之一


例子：
//线程A操作
int k = 1;

//线程B操作
int j = k;

//线程C操作
int k = 2

分析：
假设线程A中的操作“k=1”先行发生于线程B的操作“j=k”，那确定在线程B的操作执行后，变量j的值一定等于1，依据有两个：一是先行发生原则，“k=1”的结果可以被观察到；二是第三者线程C还没出现，线程A操作结束之后没有其他线程会修改变量k的值。

但是考虑线程C出现了，保持线程A和线程B之间的先行发生关系，线程C出现在线程A和线程B的操作之间，但是线程C与线程B没有先行发生关系，那j的值会是多少？答案是1和2都有可能，因为线程C对变量k的影响可能会被线程B观察到，也可能不会，所以线程B就存在读取到不符合预期数据的风险，不具备多线程安全性


八大原则(对这个不理解，一定要去补充相关博文知识)
1、程序次序规则
2、管程锁定规则
3、volatile变量规则
4、线程启动规则
5、线程中断规则
6、线程终止规则
7、对象终结规则
8、传递性
```



#### 并发编程三要素

**原子性**

一个不可再被分割的颗粒，原子性指的是一个或多个操作要么全部执行成功要么全部执行失败，期间不能被中断，也不存在上下文切换，线程切换会带来原子性的问题

int num = 1; // 原子操作
num++; // 非原子操作，从主内存读取num到线程工作内存，进行 +1，再把num写到主内存, 除非用原子类，即java.util.concurrent.atomic里的原子变量类



解决办法是可以用synchronized 或 Lock(比如ReentrantLock) 来把这个多步操作“变成”原子操作。

-----

**有序性**

程序执行的顺序按照代码的先后顺序执行，因为处理器可能会对指令进行重排序
JVM在编译java代码或者CPU执行JVM字节码时，对现有的指令进行重新排序，主要目的是优化运行效率(不改变程序结果的前提)
int a = 3 //1
int b = 4 //2
int c =5 //3 
int h = a*b*c //4

上面的例子 执行顺序1,2,3,4 和 2,1,3,4 结果都是一样，指令重排序可以提高执行效率，但是多线程上可能会影响结果

假如下面的场景，正常是顺序处理
//线程1
before();//处理初始化工作，处理完成后才可以正式运行下面的run方法
flag = true; //标记资源处理好了，如果资源没处理好，此时程序就可能出现问题

//线程2
while(flag){
    run(); //核心业务代码
}



指令重排序后，导致顺序换了，程序出现问题，且难排查

//线程1
flag = true; //标记资源处理好了，如果资源没处理好，此时程序就可能出现问题
//线程2
while(flag){
    run(); //核心业务代码
}

//线程1

before();//处理初始化工作，处理完成后才可以正式运行下面的run方法

-----

**可见性**

一个线程A对共享变量的修改,另一个线程B能够立刻看到
// 线程 A 执行
int num = 0;
// 线程 A 执行
num++;
// 线程 B 执行
System.out.print("num的值：" + num);

线程A执行 i++ 后再执行线程 B，线程 B可能有2个结果，可能是0和1。





因为 i++ 在线程A中执行运算，并没有立刻更新到主内存当中，而线程B就去主内存当中读取并打印，此时打印的就是0；也可能线程A执行完成更新到主内存了,线程B的值是1。
所以需要保证线程的可见性
synchronized、lock和volatile能够保证线程可见性



#### 进程、线程间调度算法

进程间的调度：

```
先来先服务调度算法：
    按照作业/进程到达的先后顺序进行调度 ，即：优先考虑在系统中等待时间最长的作业
    缺点：排在长进程后的短进程的等待时间长，不利于短作业/进程

短作业优先调度算法：
    短进程/作业（要求服务时间最短）在实际情况中占有很大比例，为了使得它们优先执行
    缺点：对长作业不友好

高响应比优先调度算法: 
    在每次调度时，先计算各个作业的优先权：优先权=响应比=（等待时间+要求服务时间）/要求服务时间,
    因为等待时间与服务时间之和就是系统对该作业的响应时间，所以 优先权=响应比=响应时间/要求服务时间，选    择优先权高的进行服务需要计算优先权信息，缺点是增加了系统的开销
    
时间片轮转调度算法:
    轮流的为各个进程服务，让每个进程在一定时间间隔内都可以得到响应
    缺点：由于高频率的进程切换，会增加了开销，且不区分任务的紧急程度

优先级调度算法:
    根据任务的紧急程度进行调度，高优先级的先处理，低优先级的慢处理
    缺点：如果高优先级任务很多且持续产生，那低优先级的就可能很慢才被处理
```



线程间的调度算法：

```java
线程调度是指系统为线程分配CPU使用权的过程，主要分两种:

协同式线程调度(分时调度模式)：线程执行时间由线程本身来控制，线程把自己的工作执行完之后，要主动通知系统切换到另外一个线程上。最大好处是实现简单，且切换操作对线程自己是可知的，没啥线程同步问题。坏处是线程执行时间不可控制，如果一个线程有问题，可能一直阻塞在那里

抢占式线程调度：每个线程将由系统来分配执行时间，线程的切换不由线程本身来决定（Java中，Thread.yield()可以让出执行时间，但无法获取执行时间）。线程执行时间系统可控，也不会有一个线程导致整个进程阻塞。

Java线程调度就是抢占式调度,优先让可运行池中优先级高的线程占用CPU,如果可运行池中的线程优先级相同,那就随机选择一个线程。

所以我们如果希望某些线程多分配一些时间，给一些线程少分配一些时间，可以通过设置线程优先级来完成。
JAVA的线程的优先级，以1到10的整数指定。当多个线程可以运行时，VM一般会运行最高优先级的线程（Thread.MIN_PRIORITY至Thread.MAX_PRIORITY）

在两线程同时处于就绪runnable状态时，优先级越高的线程越容易被系统选择执行。但是优先级并不是100%可以获得，只不过是机会更大而已。

有人会说，wait,notify不就是线程本身控制吗？
其实不是，wait是可以让出执行时间，notify后无法获取执行时间，随机等待队列里面获取而已
```



#### 常用的锁

**悲观锁**：当线程去操作数据的时候，总认为别的线程会去修改数据，所以它每次拿数据的时候都会上锁，别的线程去拿数据的时候就会阻塞，比如synchronized
**乐观锁**：每次去拿数据的时候都认为别人不会修改，更新的时候会判断是别人是否回去更新数据，通过版本来判断，如果数据被修改了就拒绝更新，比如CAS是乐观锁，但严格来说并不是锁，通过原子性来保证数据的同步，比如说数据库的乐观锁，通过版本控制来实现，CAS不会保证线程同步，乐观的认为在数据更新期间没有其他线程影响
小结：悲观锁适合写操作多的场景，乐观锁适合读操作多的场景，乐观锁的吞吐量会比悲观锁多



-----



**公平锁**：指多个线程按照申请锁的顺序来获取锁，简单来说 如果一个线程组里，能保证每个线程都能拿到锁 比如ReentrantLock里面的FairSync(底层是同步队列FIFO:First Input First Output来实现)
**非公平锁**：获取锁的方式是随机获取的，保证不了每个线程都能拿到锁，也就是存在有线程饿死,一直拿不到锁，比如synchronized、ReentrantLock的NonfairSync
小结：非公平锁性能高于公平锁，更能重复利用CPU的时间



-----



**可重入锁**：也叫递归锁，在外层使用锁之后，在内层仍然可以使用，并且不发生死锁
**不可重入锁**：若当前线程执行某个方法已经获取了该锁，那么在方法中尝试再次获取锁时，就会获取不到被阻塞
小结：可重入锁能一定程度的避免死锁 synchronized、ReentrantLock 重入锁

​    private void meathA(){
​            //获取锁 TODO
​        meathB();
​    }

​    private void meathB(){
​            //获取锁 TODO
​            //其他操作
​    }



-----



**自旋锁**：一个线程在获取锁的时候，如果锁已经被其它线程获取，那么该线程将循环等待，然后不断的判断锁是否能够被成功获取，直到获取到锁才会退出循环,任何时刻最多只能有一个执行单元获得锁.
小结：不会发生线程状态的切换，一直处于用户态，减少了线程上下文切换的消耗，缺点是循环会消耗CPU
常见的自旋锁：TicketLock,CLHLock,MSCLock



-----



**共享锁**：也叫S锁/读锁，能查看但无法修改和删除的一种数据锁，加锁后其它用户可以并发读取、查询数据，但不能修改，增加，删除数据，该锁可被多个线程所持有，用于资源数据共享。比如`ReadWriteLock`的读锁支持多个线程同时进行读取操作。



**互斥锁**：也叫X锁/排它锁/写锁/独占锁/独享锁/ 该锁每一次只能被一个线程所持有,加锁后任何线程试图再次加锁的线程会被阻塞，直到当前线程解锁。例子：如果 线程A 对 data1 加上排他锁后，则其他线程不能再对 data1 加任何类型的锁,获得互斥锁的线程即能读数据又能修改数据



**死锁**：两个或两个以上的线程在执行过程中，由于竞争资源或者由于彼此通信而造成的一种阻塞的现象，若无外力作用，它们都将无法让程序进行下去。



-----



下面三种是Jvm为了提高锁的获取与释放效率而做的优化 针对内置锁**Synchronized**的锁升级，锁的状态是通过对象监视器在对象头中的字段来表明，是不可逆的过程。



**偏向锁**：一段同步代码一直被一个线程所访问，那么该线程会自动获取锁，获取锁的代价更低，
**轻量级锁**：当锁是偏向锁的时候，被其他线程访问，偏向锁就会升级为轻量级锁，其他线程会通过自旋的形式尝试获取锁，但不会阻塞，且性能会高点
**重量级锁**：当锁为轻量级锁的时候，其他线程虽然是自旋，但自旋不会一直循环下去，当自旋一定次数的时候且还没有获取到锁，就会进入阻塞，该锁升级为重量级锁，重量级锁会让其他申请的线程进入阻塞，性能也会降低

分段锁、行锁、表锁



#### synchronized

`synchronized`是解决线程安全的问题，常用在 同步普通方法、静态方法、代码块 中

**非公平、可重入**



每个对象有一个锁和一个等待队列，锁只能被一个线程持有，其他需要锁的线程需要阻塞等待。锁被释放后，对象会从队列中取出一个并唤醒，唤醒哪个线程是不确定的，不保证公平性



两种形式：

**方法**：生成的字节码文件中会多一个 **ACC_SYNCHRONIZED** 标志位，当一个线程访问方法时，会去检查是否存在**ACC_SYNCHRONIZED**标识，如果存在，执行线程将先获取**monitor**，获取成功之后才能执行方法体，方法执行完后再释放**monitor**。在方法执行期间，其他任何线程都无法再获得同一个**monitor**对象，也叫隐式同步

**代码块**：加了`synchronized`关键字的代码段，生成的字节码文件会多出 **monitorenter** 和 **monitorexit** 两条指令，每个**monitor**维护着一个记录着拥有次数的计数器, 未被拥有的**monitor**的该计数器为0，当一个线程获执行**monitorenter**后，该计数器自增1；当同一个线程执行**monitorexit**指令的时候，计数器再自减1。当计数器为0的时候，**monitor**将被释放，也叫显式同步。

两种本质上没有区别，底层都是通过**monitor**来实现同步, 只是方法的同步是一种隐式的方式来实现，无需通过字节码来完成。



```shell
javac XXX.java
查看字节码
javap -v XXX.class
```





#### CAS

全称是**Compare And Swap**，即比较再交换，是实现并发应用到的一种技术

底层通过Unsafe类实现原子性操作操作包含三个操作数 —— 内存地址（V）、预期原值（A）和新值(B)。 

如果内存位置的值与预期原值相匹配，那么处理器会自动将该位置值更新为新值 ，若果在第一轮循环中，a线程获取地址里面的值被b线程修改了，那么a线程需要自旋（这个看具体实现），到下次循环才有可能机会执行。

```java
	//这是Unsafe里面的实现，这里的while就是为了自旋
	@HotSpotIntrinsicCandidate
    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!weakCompareAndSetLong(o, offset, v, v + delta));
        return v;
    }
```





CAS这个是属于乐观锁，性能较悲观锁有很大的提高。

`AtomicXXX` 等原子类底层就是CAS实现，一定程度比`synchonized`好，因为后者是悲观锁

```java
public class TestUnSafe {
    static Unsafe unSafe;

    static Long stateOffset;

    private volatile long state = 0;

    static {
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            //因为是静态变量，直接取就可以
            unSafe = (Unsafe) field.get(null);
            //获取state变量在类TestUnSafe中的偏移位置
            stateOffset = unSafe.objectFieldOffset(TestUnSafe.class.getDeclaredField("state"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestUnSafe test = new TestUnSafe();
        boolean result = unSafe.compareAndSwapLong(test, stateOffset, 0, 1);
        System.out.println(result);
    }
}
```



cas问题：

- 自旋时间长CPU利用率增加，CAS里面是一个循环判断的过程，如果线程一直没有获取到状态，cpu资源会一直被占用
- ABA问题

#### ABA问题

如果一个变量V初次读取是A值，并且在准备赋值的时候也是A值，那就能说明A值没有被修改过吗？

其实是不能的，因为变量V可能被其他线程改回A值（A-B-A），结果就是会导致CAS操作误认为从来没被修改过，从而赋值给V，在一些压栈操作的时候会产生问题（栈顶的数据没变但是实际已经操作过了）。



-----



给变量加一个版本号即可，在比较的时候不仅要比较当前变量的值 还需要比较当前变量的版本号。



在java5中，已经提供了`AtomicStampedReference`来解决问题，检查当前引用是否等于预期引用，其次检查当前标志是否等于预期标志，如果都相等就会以原子的方式将引用和标志都设置为新值

```java
	//AtomicStampedReference cas源码
	public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }
```





### 3. 并发编程底层

#### AQS

AQS的全称为（**AbstractQueuedSynchronizer**），这个类在java.util.concurrent.locks包下面。它是一个Java提高的底层同步工具类，比如`CountDownLatch`、`ReentrantLock`，`Semaphore`，`ReentrantReadWriteLock`，`SynchronousQueue`，`FutureTask`等等皆是基于AQS的



简单来说：是用一个int类型的变量（`state`）表示同步状态，并提供了一系列的CAS操作来管理这个同步状态：

- state（用于计数器，类似gc的回收计数器）
- 线程标记（当前线程是谁加锁的），
- 阻塞队列（用于存放其他未拿到锁的线程)



例子：线程A调用了lock()方法，通过CAS将state赋值为1，然后将该锁标记为线程A加锁。如果线程A还未释放锁时，线程B来请求，会查询锁标记的状态，因为当前的锁标记为 线程A，线程B未能匹配上，所以线程B会加入阻塞队列，直到线程A触发了 unlock() 方法，这时线程B才有机会去拿到锁，但是不一定肯定拿到



-----

`acquire(int arg)` 

`tryAcquire()`尝试直接去获取资源，如果成功则直接返回,AQS里面未实现但没有定义成abstract，因为独占模式下只用实现tryAcquire-tryRelease，而共享模式下只用实现tryAcquireShared-tryReleaseShared，类似设计模式里面的适配器模式



`addWaiter()` 根据不同模式将线程加入等待队列的尾部，有Node.EXCLUSIVE互斥模式、Node.SHARED共享模式；如果队列不为空，则以通过compareAndSetTail方法以CAS将当前线程节点加入到等待队列的末尾。否则通过enq(node)方法初始化一个等待队列

​    

`acquireQueued()`使线程在等待队列中获取资源，一直获取到资源后才返回,如果在等待过程中被中断，则返回true，否则返回false

`release(int arg)`独占模式下线程释放指定量的资源，里面是根据tryRelease()的返回值来判断该线程是否已经完成释放掉资源了；在自义定同步器在实现时，如果已经彻底释放资源(state=0)，要返回true，否则返回false
    

unparkSuccessor方法用于唤醒等待队列中下一个线程

-----

AQS几种同步方式：

独占式: 比如`ReentrantLock`

共享式：比如`Semaphore`

存在组合：组合式的如`ReentrantReadWriteLock`，AQS为使用提供了底层支撑，使用者可以自由组装实现




1. `boolean tryAcquire(int arg) `
2. `boolean tryRelease(int arg) `
3. `int tryAcquireShared(int arg) `
4. `boolean tryReleaseShared(int arg) `
5. `boolean isHeldExclusively()`

不需要全部实现，根据获取的锁的种类可以选择实现不同的方法，比如
实现支持独占锁的同步器应该实现`tryAcquire`、`tryRelease`、`isHeldExclusively`
实现支持共享获取的同步器应该实现`tryAcquireShared`、`tryReleaseShared`、`isHeldExclusively`



#### ReentrantLock源码以及和Synchronized区别

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    abstract static class Sync extends AbstractQueuedSynchronizer {}
    
    static final class NonfairSync extends Sync {
        protected final boolean tryAcquire(int acquires){}
    }
    
    static final class FairSync extends Sync {
        protected final boolean tryAcquire(int acquires) {
    }
}
```



```java
Lock接口定义的方法：
    lock
    lockInterruptibly
    tryLock
    tryLock
    unlock
    newCondition
```



-----



ReentrantLock和synchronized使用的场景是什么，实现机制有什么不同？

```
ReentrantLock和synchronized都是独占锁

synchronized：
    1、是悲观锁会引起其他线程阻塞，java内置关键字
    2、无法判断是否获取锁的状态，锁可重入、不可中断、只能是非公平
    3、加锁解锁的过程是隐式的,用户不用手动操作,优点是操作简单但显得不够灵活
    4、一般并发场景使用足够、可以放在被递归执行的方法上,且不用担心线程最后能否正确释放锁
    5、synchronized操作的应该是对象头中mark word	

ReentrantLock：
    1、是个Lock接口的实现类，是乐观锁，
    2、可以判断是否获取到锁，可重入、可判断、可公平可不公平
    3、需要手动加锁和解锁,且 解锁的操作尽量要放在finally代码块中,保证线程正确释放锁
    4、在复杂的并发场景中使用在重入时要却确保重复获取锁的次数必须和重复释放锁的次数一样，否则可能导致       其他线程无法获得该锁。
    5、创建的时候通过传进参数true创建公平锁,如果传入的是false或没传参数则创建的是非公平锁
    6、底层不同是AQS的state和FIFO队列来控制加锁
```



#### ReentrantReadWriteLock

```
1、读写锁接口 ReadWriteLock 接口的一个具体实现，实现了读写锁的分离，
2、支持公平和非公平，底层也是基于AQS实现
3、允许从写锁降级为读锁
	流程：先获取写锁，然后获取读锁，最后释放写锁；但不能从读锁升级到写锁
        
4、重入：读锁后还可以获取读锁；获取了写锁之后既可以再次获取写锁又可以获取读锁
    核心：读锁是共享的，写锁是独占的。 读和读之间不会互斥，读和写、写和读、写和写之间才会互斥，主要是提升了读写的性能
    

ReentrantLock是独占锁且可重入的，相比synchronized而言功能更加丰富也更适合复杂的并发场景，但是也有弊端，假如有两个线程A/B访问数据，加锁是为了防止线程A在写数据， 线程B在读数据造成的数据不一致； 但线程A在读数据，线程C也在读数据，读数据是不会改变数据没有必要加锁，但是还是加锁了，降低了程序的性能，所以就有了ReadWriteLock读写锁接口


场景：读多写少，比如设计一个缓存组件 或 提高Collection的并发性
```





#### 并发编程里面解决生产消费者模型

核心：要保证生产者不会在缓冲区满时放入数据，消费者也不会在缓冲区空时消耗数据
常用的同步方法是采用信号或加锁机制

1. wait() / notify()方法

2. await() / signal()方法用ReentrantLock和Condition实现等待/通知模型

3. Semaphore信号量

4. BlockingQueue阻塞队列

   1. ArrayBlockingQueue

   2. LinkedBlockingQueue

      put方法用来向队尾存入元素，如果队列满，则阻塞
      take方法用来从队首取元素，如果队列为空，则阻塞



 

 

 #### 并发队列

常见的阻塞队列

`ArrayBlockingQueue`：基于数组实现的一个阻塞队列，需要指定容量大小，FIFO先进先出顺序

`LinkedBlockingQueue`：基于链表实现的一个阻塞队列，如果不指定容量大小，默认 Integer.MAX_VALUE, FIFO先进先出顺序，一个支持优先级的无界阻塞队列，默认情况下元素采用自然顺序升序排序，也可以自定义排序实现  java.lang.Comparable接口

`DelayQueue`：延迟队列，在指定时间才能获取队列元素的功能，队列头元素是最接近过期的元素，里面的对象必须实现 java.util.concurrent.Delayed 接口并实现CompareTo和getDelay方法



`BlockingQueue`：JUC提供了线程安全的队列访问的接口，并发包下很多高级同步类的实现都是基于阻塞队列实现的

1. 当阻塞队列进行插入数据时，如果队列已满，线程将会阻塞等待直到队列非满
2. 从阻塞队列读数据时，如果队列为空，线程将会阻塞等待直到队列里面是非空的时候

