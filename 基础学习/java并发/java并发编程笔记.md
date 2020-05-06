# Java并发编程笔记

## 一. 并发编程基础

### 进程与线程

多个线程共享进程的堆和方法区资源

每个线程有自己的程序计数器和栈区域。



### 线程编程

#### 创建运行

- 继承Thread，实现`run()`方法
- 实现Runnable接口，实现`run()`方法
- 实现Callable接口的`call()`方法，使用FutureTask包装执行，可以等待线程执行返回值



#### 通知与等待

- 等待：当一个线程调用一个共享变量的wait()方法时，该线程会被阻塞挂起，如果中断该线程会抛出异常。方法可以有参数，表示最多等待多久。
- 虚假唤醒：通过不断检验唤醒条件来判断。
- 唤醒：`notify()`、`notifyAll()`



#### 等待线程执行终止

调用`join()`方法等待线程终止才返回

调用之后当前线程会阻塞，直到join的线程返回



实际上调用`CoutDownLatch`更方便



#### 线程休眠

调用`sleep()`使当前线程休眠，但是锁不会释放。如果其他线程中断了休眠线程，会抛出中断异常。





#### 线程让出CPU执行权

调用`yield()`让出当前线程的CPU执行权，也就是剩下的时间片不再使用。

这个时候并不是阻塞挂起，而是处于就绪状态。



#### 线程中断

- `void interrupt()`：设置线程的中断标志位为true，如果当前线程处于wait(),join(),sleep()被阻塞，则会抛出异常。
- `boolean isInterrupted`返回boolean值确定当前线程是否被中断
- `boolean interrupted`这个是静态方法，所以实际上是确定**当前线程**的中断标志位，并且在调用后如果发现线程被中断，则会清除当前的中断标志位。



因为中断只是设置了标志位，所以必须手动检测：

```java
public void run(){
    //Thread.currentThread().isInterrupted()
    while(!Thread.currentThread().interruted()){
        
    }
}
```



#### 线程死锁

多个线程的资源请求形成了环路等待。



避免死锁的方法：

- 有序的申请资源





#### 守护线程与用户线程

- 守护线程：主线程结束后，守护线程退出。`thread.setDaemon(true)`
- 用户线程：JVM等待用户线程结束才会正常退出。



#### 线程本地变量ThreadLocals与InheritableThreadLocal

创建了ThreadLocal变量后，每个线程都有改变量的一个副本，也就是每个线程操作该变量都是在操作自己本地内存的数据。

```java
//创建
ThreadLocal<String> localVal = new ThreadLocal<>();
//设置
localVal.set("123");
//获取
localVal.get();
```

ThreadLocal没有继承性（因为主线程和子线程本变量存储的是自己内存）

如果有用到继承性，使用InheritableThreadLocal



**实现原理：**

Thread里面包含两个ThreadLocalMap的变量，结构类似定制化的HashMap：

```java
    /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
```



ThreadLocal是一个工具类，包装了对threadLocals的操作。

同理InheritableThreadLocal对于inheritableThreadLocals也是一样。



------



**InheritableThreadLocal 怎么实现继承性的？**

InheritableThreadLocal继承了ThreadLocal，重新实现了childValue(),getMap(),createMap()方法。

```java
public class InheritableThreadLocal<T> extends ThreadLocal<T> {
    protected T childValue(T parentValue) {
        return parentValue;
    }

    ThreadLocalMap getMap(Thread t) {
       return t.inheritableThreadLocals;
    }

    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
    }
}
```

其中getMap在ThreadLocal的get()方法中调用，createMap在set()的时候用于初始化。



子线程继承变量是怎么实现的？

追踪Thread的初始化方法，直到：

```java
   private Thread(ThreadGroup g, Runnable target, String name,
                   long stackSize, AccessControlContext acc,
                   boolean inheritThreadLocals) {
       //......
       Thread parent = currentThread();	
       //......
       //inheritThreadLocals默认是true
       if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
       //......
   }
```

可以看到，在创建线程时会将当前线程（也就是父线程）的inheritableThreadLocals赋值给子线程的inheritThreadLocals变量。





### 可见性的问题

线程使用变量的时候，经过了私有内存-》主内存的操作

一般是：每个核心的以及缓存-》CPU共享的二级缓存-》主内存



多线程编程会出现多个线程使用不同CPU，每次单个线程都是在本地缓存操作。可能A、B线程都在向变量写数据，读取数据的时候，A仍读取本地数据。



下面是一些解决的方法：

#### sychronized原子内置锁

使用sychronized锁，变量会从本地内存清除，直接在主内存中获取变量值在退出锁的时候把修改刷新到主内存。

会引起线程上下文切换带来的调度开销。



#### volatile关键字

写入数据直接刷新到主内存

读取数据直接从主内存获取

因为采用的是非阻塞算法，不会造成线程上下文切换的开销。

但是volatile不能保证原子性，也就是只能用于写入时不依赖当前变量值。因为如果进行读取-写入的操作不是原子性的。





### 原子性问题

在进行一系列操作的时候，如果不能保证全部执行（或者不执行），会出现线程安全问题。

比如常见的++i操作，实际上汇编源码解析（`javap -c`）为获取-计算-赋值三个操作。



解决的方式：

#### sychronized独占锁

参考签名的说明

#### 原子性操作类（AtomicXxx）

这个在CAS里面详细说明



### CAS操作

java 的 Unsafe类提供了一系列的cas的方法，

```java
    @ForceInline
    public final boolean compareAndSwapInt(Object o, long offset,
                                           int expected,
                                           int x) {
        return theInternalUnsafe.compareAndSetInt(o, offset, expected, x);
    }
```

第一个参数对象内存位置，第二个参数变量偏移量、第三个参数预期值、第四个参数新的值



#### ABA问题

CAS操作的ABA问题：

- 线程1、2都取变量val值为A
- 线程2设值val为B，后又设置为A
- 线程1进行CAS操作，判断可以修改。

ABA 的后果：

如果仅仅是对数值进行简单的赋值操作的话（比如库存量，统计数据之类的），实际上没有什么问题。

但是如果附带判定条件的话会出问题。

如：

- 数据栈A-B
- 线程1取元素A，线程2取元素A
- 线程2弹出A、B，压入D、C、A，此时结构为A-C-D
  - 线程1 CAS 操作，判断可以操作

此时线程1和线程2的栈是完全不同的结构。



JDK使用AtomicStampedReference来给每个变量配置一个时间戳用于区分ABA问题中两次不同的A。



### Unsafe类

几个关键的方法：

```java
//指定变量在类中的内存偏移地址
long objectFieldOffset(Field f);
//数组第一个元素的地址
int arrayBaseOffset(Class<?> arrayClass);
//数组一个元素占用字节
int arrayIndexScale(Class<?> arrayClass);

```

