# Java并发编程笔记

该文档为《Java并发编程之美》笔记

部分算法图来自：[数据结构与算法系列](https://www.cnblogs.com/skywang12345/p/3603935.html)

[TOC]



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



#### 线程本地变量ThreadLocal与InheritableThreadLocal

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

Thread里面包含两个ThreadLocalMap的变量，结构类似定制化的HashMap，

这里为啥Thread维护的是一个map结构？实际上ThreadLocalMap的键类型为ThreadLocal，因为我们可能存在多个ThreadLocal值，所以使用Map。

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

举个例子：

```java
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }
```

这里先通过当前线程获取线程中的ThreadLocalMap变量

之后使用当前ThreadLocal对象作为键查找对应的数据

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
//指定变量在类中的内存偏移地址，因为取的是类中的相对位置，所以实际上是和类而非对象绑定的一项数据
long objectFieldOffset(Field f);
//数组第一个元素的地址
int arrayBaseOffset(Class<?> arrayClass);
//数组一个元素占用字节
int arrayIndexScale(Class<?> arrayClass);
//cas操作
@HotSpotIntrinsicCandidate
public final long getAndSetLong(Object o, long offset, long newValue) {
    long v;
    do {
        v = getLongVolatile(o, offset);
    } while (!weakCompareAndSetLong(o, offset, v, newValue));
    return v;
}
//weakCompareAndSetLong里面调用compareAndSetLong
native boolean compareAndSetLong(Object o, long offset,
                                                  long expected,
                                                  long x);


//支持volatile版本的：设置对象o中偏移量为offset类型为long的field值为x
public native void    putLongVolatile(Object o, long offset, long x);
//对应的不支持volatile版本的方法
public native void    putFloat(Object o, long offset, float x);

/** Ordered/Lazy version of {@link #putLongVolatile(Object, long, long)} */
@ForceInline
public void putOrderedLong(Object o, long offset, long x) {
	theInternalUnsafe.putLongRelease(o, offset, x);
}

/** Release version of {@link #putLongVolatile(Object, long, long)} */
@HotSpotIntrinsicCandidate
public final void putLongRelease(Object o, long offset, long x) {
	putLongVolatile(o, offset, x);
}

/**
 * Blocks current thread, returning when a balancing
 * {@code unpark} occurs, or a balancing {@code unpark} has
 * already occurred, or the thread is interrupted, or, if not
 * absolute and time is not zero, the given time nanoseconds have
 * elapsed, or if absolute, the given deadline in milliseconds
 * since Epoch has passed, or spuriously (i.e., returning for no
 * "reason"). Note: This operation is in the Unsafe class only
 * because {@code unpark} is, so it would be strange to place it
 * elsewhere.
 */
@HotSpotIntrinsicCandidate
public native void park(boolean isAbsolute, long time);

/**
 * Unblocks the given thread blocked on {@code park}, or, if it is
 * not blocked, causes the subsequent call to {@code park} not to
 * block.  Note: this operation is "unsafe" solely because the
 * caller must somehow ensure that the thread has not been
 * destroyed. Nothing special is usually required to ensure this
 * when called from Java (in which there will ordinarily be a live
 * reference to the thread) but this is not nearly-automatically
 * so when calling from native code.
 *
 * @param thread the thread to unpark.
 */
@HotSpotIntrinsicCandidate
public native void unpark(Object thread);
```

> 这些方法经常有`isAbsolute`的参数，如果为`true`表示设定的是绝对时间点，如果为`false`表示设定的是时长。



这里如果需要使用使用Unsafe类，需要使用反射的方法，因为在getUnsafe方法中，会判断是否是BootStrap类加载器：

```java
@CallerSensitive
public static Unsafe getUnsafe() {
	Class<?> caller = Reflection.getCallerClass();
    if (!VM.isSystemDomainLoader(caller.getClassLoader()))
    	throw new SecurityException("Unsafe");
	return theUnsafe;
}
```



下面是测试代码：

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



### MethodHandles与VarHandle

`VarHandle`是jdk9后面引进的变量句柄，这里记录一下常用的写法（`java.util.concurrent.locks.AbstractQueuedSynchronizer`里面的代码）：

```java
//这里的Lookup内部类是一个变量句柄的工厂类
MethodHandles.Lookup l = MethodHandles.lookup();
//findVarHandle  
//第一个参数变量所在类的类型
//第二个参数变量名称
//第三个参数变量类型
NEXT = l.findVarHandle(Node.class, "next", Node.class);
NEXT.compareAndSet(this, expect, update);
NEXT.set(this, new Node());
```

其他的很多和`Unsafe`差不多，比如`volatile`语义写、cas。







### 指令重排（有序性）

编译器和处理器会对没有数据依赖的指令进行重排序，在多线程的情况下，可能会出现：

- 线程1进行a,b赋值操作
- 线程2在a满足条件时打印b

对a,b的赋值操作的顺序实际上会影响我们预期的效果。



解决的办法：用锁可以把一系列操作作为事务处理。也可以使用volatile确保操作不会进行重排序。





### 伪共享

因为Cache是按行存储，可能出现一个缓存行包含多个变量，这样当一个线程操作某一个变量的时候，其他变量也受到了限制，会导致其他线程读取不到数据，直接访问一级缓存或者主内存。



解决的办法就是创建变量后填充缓存行，这样一个缓存行就只有一个变量。

可以看Thread类里面的应用：

```java
    /** The current seed for a ThreadLocalRandom */
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    /** Probe hash value; nonzero if threadLocalRandomSeed initialized */
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;

    /** Secondary seed isolated from public ThreadLocalRandom sequence */
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;
```





### 锁的分类

#### 乐观锁与悲观锁

- 悲观锁：处理数据前进行加锁，操作完再解锁。比如数据库的事务就是如此。

- 乐观锁：在写的时候通过比较版本信息（版本号、时间戳、原始数值）来进行CAS操作。



#### 公平锁与非公平锁

线程A持有数据锁的同时，B最先开始申请数据并阻塞，之后线程C也开始申请数据阻塞。

在A释放锁之后：

- 公平锁：线程B因为最先请求锁，所以获取
- 非公平锁：线程B、C都可能获取锁



#### 独占锁与共享锁

- 独占锁：只有一个线程可以获取锁，比如ReentrantLock

- 共享锁：多个线程持有，比如ReadWriteLock的读锁支持多个线程同时进行读取操作。（这实际是一种优化，因为读数据不会引起线程安全问题）



### 可重入锁

可重入锁会维护一个计数器以及线程标识（类似map<threadName,count>?），当获取锁会把对应的值加一，释放锁减一，只有计数到0才会完全释放这个锁。

synchronized是一个可重入锁。



### 自旋锁

获取锁失败后，不是马上阻塞挂起，而是多次尝试获取，如果次数以达到预设，则阻塞。

相当于通过消耗CPU时间来换取线程调度的开销。



## 二. 并发包中的主要组件实现原理

### ThreadLocalRandom

#### 已有的Random的并发处理

使用：

```java
//产生实例
Random random = new Random();
//获取随机数
random.nextInt(5);
```



`Random`类生成随机数：

```java
    public int nextInt(int bound) {

        //......
        int r = next(31);
        
        int m = bound - 1;
        //如果和减去一的数与为0，说明这个数是2的次方
        if ((bound & m) == 0)  // i.e., bound is a power of 2
            r = (int)((bound * (long)r) >> 31);
        else {
            for (int u = r;
                 u - (r = u % bound) + m < 0;
                 u = next(31))
                ;
        }
        return r;
    }
```



在`next()`函数里面，使用老的随机种子算出新种子，种子使用`AtomicLong`原子操作类（后面会详细分析）使用CAS操作来确保在多线程操作的情况下，只有一个线程可以操作变量seed：

```java
    protected int next(int bits) {
        long oldseed, nextseed;
        AtomicLong seed = this.seed;
        do {
            oldseed = seed.get();
            nextseed = (oldseed * multiplier + addend) & mask;
        } while (!seed.compareAndSet(oldseed, nextseed));
        return (int)(nextseed >>> (48 - bits));
    }
```

但是这里失败的线程使用`while`循环一直**自旋**来重新获取种子，这样降低了并发的性能。



#### ThreadLocalRandom的处理

使用方式：

```java
//获取实例
ThreadLocalRandom random = ThreadLocalRandom.current();
//获取随机数
random.nextInt(8);
```



其原理和ThreadLocal类似，也是每个线程根据自己的本地种子生成新的种子。

`Thread`维护了`threadLocalRandomSeed`、`threadLocalRandomProbe`、`threadLocalRandomSecondarySeed` 

`ThreadLocalRandom`作为操作的工具类，继承了`Random`类，重写了`nextInt()`等方法，每次计算种子的时候，会把线程本地的数据拿出来计算新的种子。



`ThreadLocalRandom`维护了`Thread`几个关键变量在类中的偏移量：

```java
    //unsafe实例
	private static final Unsafe U = Unsafe.getUnsafe();
	//threadLocalRandomSeed
    private static final long SEED = U.objectFieldOffset
            (Thread.class, "threadLocalRandomSeed");
    private static final long PROBE = U.objectFieldOffset
            (Thread.class, "threadLocalRandomProbe");
    private static final long SECONDARY = U.objectFieldOffset
            (Thread.class, "threadLocalRandomSecondarySeed");
```



`current()`方法：

```java
    public static ThreadLocalRandom current() {
        //threadLocalRandomProbe==0说明这是线程第一次调用ThreadLocalRandom实例
        //需要计算线程的初始化种子变量
        if (U.getInt(Thread.currentThread(), PROBE) == 0)
            localInit();
        return instance;
    }
	//初始化的时候
    static final void localInit() {
        int p = probeGenerator.addAndGet(PROBE_INCREMENT);
        int probe = (p == 0) ? 1 : p; // skip 0
        //计算初始化种子
        long seed = mix64(seeder.getAndAdd(SEEDER_INCREMENT));
        Thread t = Thread.currentThread();
		//变量设置到当前线程（threadLocalRandomSeed、threadLocalRandomProbe）
        U.putLong(t, SEED, seed);
        U.putInt(t, PROBE, probe);
    }
```



`nextInt()`会重新计算种子产生随机数：

```java
    public int nextInt() {
        return mix32(nextSeed());
    }
```



**`nextSeed()`重新计算种子：**

```java
    final long nextSeed() {
        Thread t; long r; // read and update per-thread seed
        //更新种子
        U.putLong(t = Thread.currentThread(), SEED,
                  r = U.getLong(t, SEED) + GAMMA);
        return r;
    }
```

`U.getLong(t, SEED) + GAMMA)`获取当前线程的`threadLocalRandomSeed`，加上`GAMMA`作为新的种子并且放入当前线程的`threadLocalRandomSeed`中。



### 原子操作类

JUC中的原子操作类，都是使用非阻塞算法CAS来实现。



#### 原子变量操作类

- `AtomicInteger`
- `AtomicLong`
- `AtomicBoolean`



##### 使用示例

```java
public class AtomicTest {
    //创建Long类型的原子计数器
    private static AtomicLong atomicLong = new AtomicLong();

    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(() -> {
            int count = 100;
            while (count-- > 0) {
                System.out.println(Thread.currentThread().getName() + " now value:" + atomicLong.incrementAndGet());
            }
        }, "A");
        Thread threadB = new Thread(() -> {
            int count = 100;
            while (count-- > 0) {
                System.out.println(Thread.currentThread().getName() + " now value:" + atomicLong.addAndGet(3));
            }
        }, "B");
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        System.out.println("main over , now atomicLong = " + atomicLong.get());
    }
}
/**
 * output:
 * ......
 * A now value:392
 * B now value:395
 * A now value:396
 * A now value:397
 * A now value:398
 * A now value:399
 * A now value:400
 * main over , now atomicLong = 400
 */
```

加大循环的次数，改为long会产生数据计算错误。



##### 源码

```java
public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;
    //JVM是否支持Long类型无锁CAS
	static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();
    private static native boolean VMSupportsCS8();
    //获取Unsafe实例
    private static final jdk.internal.misc.Unsafe U = jdk.internal.misc.Unsafe.getUnsafe();
    //获取变量value在类中的偏移量
    private static final long VALUE = U.objectFieldOffset(AtomicLong.class, "value");
	//volatile修饰的数据值
    private volatile long value;
 
    //读数据
    public final long get() {
        return value;
    }
    
    //写数据保持可见性
    public final void set(long newValue) {
        // See JDK-8180620: Clarify VarHandle mixed-access subtleties
        U.putLongVolatile(this, VALUE, newValue);
    }

    //底层Unsafe使用的是自旋CAS判断weakCompareAndSetLong
    public final long getAndSet(long newValue) {
        return U.getAndSetLong(this, VALUE, newValue);
    }
    
    //......
}
```



#### LongAdder（JDK8新增的原子操作类）

`AtomicLong`的源码可以看出来，涉及原子操作都是使用循环**自旋**的方式来竞争同一个原子变量。



`LongAdder`维护了多个Cell变量（原子性更新数组），每个Cell变量里面有初始值为0的long型变量。

多个线程如果争夺一个Cell的变量时失败，不会一直自旋CAS重试，而是会在其他Cell的变量上面进行CAS尝试。

获取值的时候，会把所有Cell变量的value值累加后再加上base基准值进行返回。



`LongAdder`对Cell是惰性加载的，并发较少的时候，直接操作base。

原子性数组使用`@sun.misc.Contended`对Cell进行了字节填充，占据了一个缓存行，避免了伪共享的情况。



##### 源码分析

`LongAdder`继承了`Striped64`

```java
@SuppressWarnings("serial")
abstract class Striped64 extends Number {
    //填充缓存行
    @jdk.internal.vm.annotation.Contended static final class Cell {
        //volatile修饰，保证数据的可见性
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            return VALUE.compareAndSet(this, cmp, val);
        }
        final void reset() {
            VALUE.setVolatile(this, 0L);
        }
        final void reset(long identity) {
            VALUE.setVolatile(this, identity);
        }
        final long getAndSet(long val) {
            return (long)VALUE.getAndSet(this, val);
        }

        // 以前的版本就是使用Unsafe来操作的
        // jdk9使用变量句柄VarHandle
        // VarHandle mechanics
        private static final VarHandle VALUE;
        static {
            try {
                MethodHandles.Lookup l = MethodHandles.lookup();
                VALUE = l.findVarHandle(Cell.class, "value", long.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
	transient volatile Cell[] cells;
    transient volatile long base;
    transient volatile int cellsBusy;
    //......
}
```

我使用的环境是jdk13，这里可以看到Cell里面的CAS操作发生了改变，不再使用`Unsafe`来进行操作，而是使用JDK9中的`java.lang.invoke.VarHandle`来进行操作（实际上作为`Unsafe`的替代者，在安全性与可用性上面更好->后面要是有时间加说明到后面）。



`LongAdder`

```java
public class LongAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    //获取当前的值
    public long longValue() {
        return sum();
    }
    
    //返回当前的值，就是base加上所有Cell内部的value值
	public long sum() {
        Cell[] cs = cells;
        long sum = base;
        if (cs != null) {
            for (Cell c : cs)
                if (c != null)
                    sum += c.value;
        }
        return sum;
    }
    
    //重置
    public void reset() {
        Cell[] cs = cells;
        base = 0L;
        if (cs != null) {
            for (Cell c : cs)
                if (c != null)
                    c.reset();
        }
    }
    
    //获取值并且重置
    public long sumThenReset() {
        Cell[] cs = cells;
        long sum = getAndSetBase(0L);
        if (cs != null) {
            for (Cell c : cs) {
                if (c != null)
                    sum += c.getAndSet(0L);
            }
        }
        return sum;
    }
    
    public void increment() {
        add(1L);
    }

    public void decrement() {
        add(-1L);
    }
    
    //......
}
```



把区别于`AtomicLong`的自旋CAS操作的add操作单独拿出来分析：

```java
    //这是核心代码，对比AtomicLong里面调用Unsafe的getAndAddLong、getAndSetLong使用自旋CAS操作
    public void add(long x) {
        Cell[] cs; long b, v; int m; Cell c;
        
        //如果cells是空->直接使用base值作为预期值进行cas操作判断结果
        //如果cells不为空且cas操作失败则进入判断体
        if ((cs = cells) != null || !casBase(b = base, b + x)) {
            boolean uncontended = true;
            //如果cells为空则通过getProbe() & m判断在哪一个cell操作
            //getProbe()是 threadLocalRandomProbe 的值
            //之后在选定的Cell里面进行cas操作
            //如果cell里面的cas还是失败，进入longAccumulate方法
            if (cs == null || (m = cs.length - 1) < 0 ||
                (c = cs[getProbe() & m]) == null ||
                !(uncontended = c.cas(v = c.value, v + x)))
                longAccumulate(x, null, uncontended);
        }
    }
```



`casBase()`是`Striped64`里面的方法：

```java
	//可以视为更安全的Unsafe操作类
	private static final VarHandle BASE;

	final boolean casBase(long cmp, long val) {
        return BASE.compareAndSet(this, cmp, val);
    }
```



`longAccumulate()`是`Striped64`里面的方法，这个太长也没看懂不贴了：

通过threadLocalRandomProbe判断是否第一次操作进行初始化

cells新建或扩容



#### LongAccumulator类（JDK8新增）

构造函数中：

`accumulatorFunction`是一个运算接口

`identity`作为运算的初始值。

```java
    public LongAccumulator(LongBinaryOperator accumulatorFunction,
                           long identity) {
        this.function = accumulatorFunction;
        base = this.identity = identity;
    }
```



第一个参数是一个双目运算接口：

```java
@FunctionalInterface
public interface LongBinaryOperator {

    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the operator result
     */
    long applyAsLong(long left, long right);
}
```



之前的`LongAdder`更像是一个特殊的`LongAccumulator`：

```java
new LongAccumulator(new LongBinaryOperator(){
    @Override
    public long applyAsLong(long left, long right){
        return left + right;
    }
},0);
```



可以看到：

这里调用`function.applyAsLong`来进行指定的计算方式

调用`longAccumulate`传入了`LongBinaryOperator`参数

```java
	public void accumulate(long x) {
        Cell[] cs; long b, v, r; int m; Cell c;
        if ((cs = cells) != null
            || ((r = function.applyAsLong(b = base, x)) != b
                && !casBase(b, r))) {
            boolean uncontended = true;
            if (cs == null
                || (m = cs.length - 1) < 0
                || (c = cs[getProbe() & m]) == null
                || !(uncontended =
                     (r = function.applyAsLong(v = c.value, x)) == v
                     || c.cas(v, r)))
                longAccumulate(x, function, uncontended);
        }
    }
```



### 并发包中的List

#### CopyOnWriteArrayList

使用了写时复制的策略和`ReentrantLocak`，每次的修改都是在底层一个复制的数组里面操作。



##### 构造器

```java
	private transient volatile Object[] array;

	//默认设置空的数组初始化
	public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

	final void setArray(Object[] a) {
        array = a;
    }

	//拷贝数组存储本地
	public CopyOnWriteArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }

	//如果参数是CopyOnWriteArrayList类型就直接获取数组
	//如果不是就复制数组，判断并处理为基类Object数组
	public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements;
        if (c.getClass() == CopyOnWriteArrayList.class)
            elements = ((CopyOnWriteArrayList<?>)c).getArray();
        else {
            elements = c.toArray();
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elements.getClass() != Object[].class)
                elements = Arrays.copyOf(elements, elements.length, Object[].class);
        }
        setArray(elements);
    }
```



##### 添加元素

```java
	final transient ReentrantLock lock = new ReentrantLock();

	public boolean add(E e) {
        //使用独占锁ReentrantLock
        final ReentrantLock lock = this.lock;
        //获取锁
        lock.lock();
        try {
            //获取当前数组
            Object[] elements = getArray();
            int len = elements.length;
            //拷贝一份数组
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            //添加新的元素
            newElements[len] = e;
            //设置当前数组为拷贝的数据
            setArray(newElements);
            return true;
        } finally {
            //释放锁
            lock.unloc();
        }
    }

	public void add(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            //边界判断
            if (index > len || index < 0)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+len);
            Object[] newElements;
            //获取需要移动的元素个数
            int numMoved = len - index;
            //如果是在末尾设置元素
            //直接在原数组扩容一位之后复制给新的数组
            if (numMoved == 0)
                newElements = Arrays.copyOf(elements, len + 1);
            else {
                //新数组要扩容一位接收新元素
                newElements = new Object[len + 1];
                //把设定位置之前的数据拷贝给新的数据，长度为index（也就是index位前面数据的大小）
                System.arraycopy(elements, 0, newElements, 0, index);
                //把设定位置之后数据拷贝给新的数组，长度为numMoved
                System.arraycopy(elements, index, newElements, index + 1,
                                 numMoved);
            }
            //在设定位置上设置新的元素
            newElements[index] = element;
            setArray(newElements);
        } finally {
            lock.unlock();
        }
    }

	//这里和add差不多，只是加入快照作为对比
	public boolean addIfAbsent(E e) {
        //获取当前元素数组作为快照记录
        Object[] snapshot = getArray();
        //检查元素是否存在，如果存在直接返回false，否则进入addIfAbbset的重载方法
        return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
            addIfAbsent(e, snapshot);
    }

	private boolean addIfAbsent(E e, Object[] snapshot) {
        final ReentrantLock lock = this.lock;
        //加锁
        lock.lock();
        try {
            // 重新获取当前的数组
            Object[] current = getArray();
            int len = current.length;
            // 如果当前的数组和之前的快照不一致
            // 说明有改动的内容
            if (snapshot != current) {
                // 这里取两个最小的长度，如果有元素变化或者已经有了这个元素，则返回false
                int common = Math.min(snapshot.length, len);
                for (int i = 0; i < common; i++)
                    if (current[i] != snapshot[i] && eq(e, current[i]))
                        return false;
                // 如果当前数组已经有这个元素，就返回false
                if (indexOf(e, current, common, len) >= 0)
                        return false;
            }
            // 拷贝一份n+1的数组，并把新加的元素加到最后一位
            Object[] newElements = Arrays.copyOf(current, len + 1);
            newElements[len] = e;
            // 设置新的数组
            setArray(newElements);
            return true;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
```

`addIfAbsent`实际上检查了三次元素是否存在，分别是在获取锁之前，获取锁之后比较当前和快照，比较当前是否存在。



##### 获取元素

```java
	public E get(int index) {
        return get(getArray(), index);
    }

	private E get(Object[] a, int index) {
        return (E) a[index];
    }

	final Object[] getArray() {
        return array;
    }
```

这里可以看到，因为没有加锁，可能线程A在读取数据的时候，已经把旧的array压栈，新线程B对数组进行了加锁修改，因为`CopyOnWriteArray`是复制后再操作的，所以线程A后面再改数据还是指向原来的数组。这里体现了`copy-on-write-list`的**弱一致性**，每次读取的不一定是最新的数据，



##### 修改指定元素

```java
	public E set(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            E oldValue = get(elements, index);

            if (oldValue != element) {
                int len = elements.length;
                //复制新的数组
                Object[] newElements = Arrays.copyOf(elements, len);
                newElements[index] = element;
                //修改数组
                setArray(newElements);
            } else {
                //array是volatile
                // Not quite a no-op; ensures volatile write semantics
                setArray(elements);
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }
```



##### 删除元素

删除的几个操作和新增类似，指定位置删除和指定位置新增差不多，指定对象删除和`addIfAbsebt`差不多。



##### 迭代器

使用迭代的时候，和读取数据一样，因为指向的是旧的数组，而其他线程的修改操作是复制后再操作的，所以在迭代的时候，对其他线程的修改是不可见的，也是*弱一致性*的体现。





### 锁原理

#### LockSupport工具类

`LockSupport`底层使用`Unsafe`来完成**挂起和唤醒**线程。

```java
private static final Unsafe U = Unsafe.getUnsafe();
```

用于创建锁和封装其他的同步类。



`LockSupport`与每个使用它的线程都会关联一个**许可证**

默认情况下调用`LockSupport`的线程是不持有**许可证**的



##### 源码分析

```java
	//如果当前线程已经有关联的许可证，name就会马上返回
	//否则会开始阻塞挂起	
	//注意，使用unpark、interrupt或者被虚假唤醒，也会返回，所以使用的时候要使用循环判断条件
	public static void park() {
        U.park(false, 0L);
    }

	//超时返回的版本
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            U.park(false, nanos);
    }

	//阻塞当前线程并且设置
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        //设置当前线程的parkBlocker变量
        setBlocker(t, blocker);
        //使用Unsafe挂起线程
        U.park(false, 0L);
        //线程被激活以后需要清除parkBlocker变量
        setBlocker(t, null);
    }

	//Thread中的变量 volatile Object parkBlocker;
	private static final long PARKBLOCKER = U.objectFieldOffset
            (Thread.class, "parkBlocker");

    private static void setBlocker(Thread t, Object arg) {
        // Even though volatile, hotspot doesn't need a write barrier here.
        U.putReference(t, PARKBLOCKER, arg);
    }

	//unpark的实际作用就是使参数线程获取 许可证 
	//也就是即使unpark在park前调用，调用park也会马上返回
    public static void unpark(Thread thread) {
        if (thread != null)
            U.unpark(thread);
    }
```

其他还有

`public static void parkNanos(Object blocker, long nanos);`

`public static void parkUntil(Object blocker, long deadline);`



调用`unpark()`之后，后面调用`park()`只能释放一次：

```java
public class LockSupportDemo2 {
    public static void main(String[] args) throws Exception {
        LockSupport.unpark(Thread.currentThread());
        LockSupport.park();
        System.out.println("end park1!");
        LockSupport.park();
        System.out.println("end park2!");
    }
}
/**
 * end park1!
 * jstack pid可以看到： java.lang.Thread.State: WAITING
 */
```



因为我们不知道`park()`返回的原因，所以使用循环判定：

比如：我们设定了只有中断才返回：

```java
public class LockSupportDemo {
    public static void main(String[] args) throws Exception {
        Thread A = new Thread(() -> {
            System.out.println("child");
            while (!Thread.currentThread().isInterrupted()){
                LockSupport.park();
            }
            System.out.println("child thread unpark");
        });
        A.start();
        Thread.sleep(1000);

        System.out.println("main thread begin unpark!");

        //中断子线程
        A.interrupt();
        //不会起作用，因为循环判定失败
//        LockSupport.unpark(A);
    }
}
```



##### 应用

使用LockSupport设计锁：

```java
public class FIFOMutex {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final Queue<Thread> waiters = new ConcurrentLinkedQueue<>();

    public void lock() {
        boolean wasInterrupted = false;
        Thread currentThread = Thread.currentThread();
        waiters.add(currentThread);

        //判断是否队首
        //如果当前线程不是队首的线程，把当前线程挂起
        //如果是，则cas加锁设定locked=true，cas失败则会继续挂起
        //
        while (waiters.peek() != currentThread || !locked.compareAndSet(false, true)) {
            LockSupport.park(this);
            //这里如果其他线程中断了这个线程，则需要保存标志位，在后面恢复标志位
            if (Thread.interrupted())
                wasInterrupted = true;
        }

        waiters.remove();
        if (wasInterrupted)
            currentThread.interrupt();
    }

    public void unlock() {
        locked.set(false);
        //释放队首元素
        LockSupport.unpark(waiters.peek());
    }
}
```





#### AQS

##### AbstractQueuedSynchronizer类结构

AQS即`AbstractQueuedSynchronizer`抽象同步队列。

![](.\image\AQS类图结构.png)

这张图是书里面截出来的，jdk13下面有点变化，但是主要的不变。



总结一下：

`AbstractQueuedSynchronizer`是一个FIFO的双向队列，关键的字段包括：

`state`同步的状态		

`tail`队尾元素

`head`队首元素

`xxxOffset`记录变量在类中的偏移位置



`state`作为同步状态信息

`private volatile int state;`

`ReentrantLock`用来计算可重入次数，`ReentrantReadWriteLock`使用高16位来标识读状态（读锁次数），低16位标识获取写锁的可重入次数。

对`state`的操作是同步的关键：

- 独占方式下获取、释放资源：`acquire(int arg)`、`acquireInterruptibly(int arg)`、`release(int arg)`
  - 如果其他线程尝试操作`state`的时候发现已经被锁定，进入阻塞状态。
  - `ReentrantLock`作为独占锁。使用CAS操作`state`实现重入效果（+1、-1）

- 共享方式下获取、释放资源：`acquireShared(int arg)`、`acquireSharedInterruptibly(int arg)`、`releaseShared(int arg)`
  - 可以共享获取`state`变量
  - `Semaphore`中，线程调用`acquire()`获取信号量的时候，会查看个数是否满足继续操作，满足则使用CAS自旋获取信号量。



`Node`作为队列的节点，在通用的构造里面，会把当前线程封装到节点里面：

```java
 		Node(Node nextWaiter) {
            this.nextWaiter = nextWaiter;
            //把当前线程作为参数封装
            THREAD.set(this, Thread.currentThread());
        }

		//实际上设置的是Node里面的thread变量
		private static final VarHandle THREAD;
		THREAD = l.findVarHandle(Node.class, "thread", Thread.class);
```



`SHARED`标记线程是获取共享资源的时候放到AQS队列

`EXCLUSIVE`标记线程是获取独占资源的时候放入AQS队列

`waitStatus`记录当前线程的等待状态：

正常的默认状态为0

- `CANCELLED:1` 线程被取消 
  - waitStatus value to indicate thread has cancelled.
- `SIGNAL:-1` 线程需要被唤醒 
  - waitStatus value to indicate successor's thread needs unparking.
- `CONDITION:-2` 线程在条件队列里面等待：
  - waitStatus value to indicate thread is waiting on condition.
- `PROPAGATE:-3` 释放共享资源需要通知其他节点 
  - waitStatus value to indicate the next acquireShared should unconditionally propagate.





AQS内部类`ConditionObject`包含了一个条件队列。



##### 获取、释放资源

```java
    public final void acquire(int arg) {
        //tryAcquire由具体实现类来完成
       	//如果获取失败则入队，这里根据acquireQueued的结果的保留了中断标志位
        //因为是独占锁，所以这里设置的mode=Node.EXLUSIVE
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
	
	//比起acquire,会检测中断标志位
	//如果被中断会直接抛出异常
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    public final boolean release(int arg) {
       	//tryRelease由具体锁实现
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
	
	//这个也需要在实现类里面做修改
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }
```





##### 入队操作

```java
	//线程获取锁失败以后转换为Node，插入到AQS阻塞队列中。
	private Node enq(Node node) {
        //循环，如果是空列则创建了在尾部插入
        for (;;) {
            Node oldTail = tail;
            if (oldTail != null) {
                //如果尾结点不是空，就把node的前置节点设为oldTail  
                //即：node.prev=oldTail
                //并使用cas设置node在尾部插入（设定为新的尾结点）
                node.setPrevRelaxed(oldTail);
                if (compareAndSetTail(oldTail, node)) {
                    oldTail.next = node;
                    return oldTail;
                }
            } else {
                //空队列初始化
                initializeSyncQueue();
            }
        }
    }

	//静态初始化：
	//MethodHandles.Lookup l = MethodHandles.lookup();
	//PREV = l.findVarHandle(Node.class, "prev", Node.class);
	//可以看到操作的是Node类中的prev变量
	private static final VarHandle PREV;
	final void setPrevRelaxed(Node p) {
    	PREV.set(this, p);
	}
	
	//private static final VarHandle HEAD;
	//HEAD = l.findVarHandle(AbstractQueuedSynchronizer.class, "head", Node.class);
	private final void initializeSyncQueue() {
        Node h;
        if (HEAD.compareAndSet(this, null, (h = new Node())))
            tail = h;
    }
```



##### 条件变量

条件变量使用`signal`与`await`配合锁来实现线程间同步的基础条件。

常见的例子：

```java
        ReentrantLock lock = new ReentrantLock();
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();

		Thread A = new Thread(()->{
            lock.lock();
            try{
                condition1.await();
                condition2.signal();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        });
```

实际上，这里底层就是返回一个`ConditionObject`。AQS没有提供`newCondition()`，这是由`Lock`来定义的。



------



可以看到，这里的`await()`、`signal()`和`synchronized`的`wait()`、`notify`相似，但是AQS可以对应多个条件变量。



我们看一下`AbstractQueuedSynchronizer`的内部类`ConditionObject`：

每个`ConditionObject`维护了一个条件队列，当调用`await()`的时候，线程被放到条件队列。

```java
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        /** 队首 */
        private transient Node firstWaiter;
        /** 队尾 */
        private transient Node lastWaiter;
        
        //调用await()方法时
		public final void await() throws InterruptedException {
            //检测中断位，对中断做出响应
            if (Thread.interrupted())
                throw new InterruptedException();
            //创建新的node节点，放置到条件队列尾
            Node node = addConditionWaiter();
            //释放当前锁
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            //阻塞当前线程
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }	
        
        //唤醒线程
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                //把条件队列队首位置的元素移动到AQS队列
                doSignal(first);
        }
    }
```



**阻塞队列**：

当多个线程同时使用`lock.lock()`获取锁的时候，只有一个线程能够拿到锁，其他线程被转换为Node节点插入AQS**阻塞队列**中，做CAS自旋尝试获取锁。



**条件队列**：

获取到锁的线程调用条件变量`await()`方法的时候，线程会释放获取的锁，转换为Node节点插入到条件变量对应的**条件队列**中。

这个时候**阻塞队列**中的一个线程可以获取锁。



如果一个线程调用了条件变量的`signal`或者`signalAll`方法，会把**条件队列**中的一个或者全部Node节点移动到AQS的**阻塞队列**中，尝试获取锁。



#### 使用AQS自定义独占锁

为了理解AQS在锁中的作用，这里展示基于AQS实现的不可重入的独占锁。

```java
public class NonReentrantLock implements Lock, Serializable {
    //内部工具类，用于维护AQS队列数据
    private static class Sync extends AbstractQueuedSynchronizer {
        //state=0的时候，尝试获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            assert arg == 1;
            if (compareAndSetState(0, 1)) {
                //设置锁的拥有者为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        //尝试释放锁，设置state=0
        @Override
        protected boolean tryRelease(int arg) {
            assert arg == 1;
            //如果当前的状态是0，表示没有占有锁
            //尝试释放锁抛出异常
            if (getState() == 0)
                throw new IllegalMonitorStateException();
            //解除当前线程对锁的占有
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        //锁是否已经被持有
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        //提供条件变量
        public Condition newCondition() {
            return new ConditionObject();
        }
    }

    //创建AQS变量操作类Sync
    private final Sync sync = new Sync();

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
```



可以看出来，主要需要实现的是`tryAcquire`、`tryRelease`、`isHeldExclusively`的实现，也就是对AQS中`state`变量的操作。





#### ReentrantLock原理



![ReentrantLock类图结构](.\image\ReentrantLock类图结构.png)



介绍一下这些类的作用（图从文档截下来的，与现在版本jdk13有点差异）：



`ReentrantLock`实现了`Lock`接口，主要用于直接给外部提供锁的功能。



`Sync`是`ReentrantLock`的内部类，实现了`AbstractQueuedSynchronizer`接口。

重写了释放锁的`tryRelease`方法（释放锁和公平策略没有关系，所以直接在这里实现）

这里对应AQS里面`state`表示线程获取锁的重入次数：

- 首次成获取锁cas设置`state=1`，**设置锁的持有者为当前线程**
- 该线程第二次获取锁，`state+=1`
- 释放锁cas设置`state-=1`，只有减一后为0才让当前线程获取锁



`NonfairSync`和`FairSync`分别实现了各自的`tryAcquire`方法，因为获取锁的时候考虑公平策略处理不同。



下面看一下对应的源码。



##### ReentrantLock构造器

默认构造器是非公平锁，可以设置fair参数。

```java
    public ReentrantLock() {
        sync = new NonfairSync();
    }
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
```



##### lock()获取锁

```java
	public void lock() {
        sync.acquire(1);
    }
```



`Sync`实现的AQS，因此这里实际上就是调用`AbstractQueuedSynchronizer`的`acquire`方法。

```java
	public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```



可以看到，具体获取锁的实现还是在`NonfairSync`和`FairSync`中的`tryAcquire`方法：



###### NonfairSync

```java
		//尝试获取锁
		protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }

		//非公平锁的设计很简单，就是设置state的变量
		@ReservedStackAccess
        final boolean nonfairTryAcquire(int acquires) {
            //获取当前线程
            final Thread current = Thread.currentThread();
            //获取state变量
            int c = getState();
            //state=0表示没有线程占有锁
            if (c == 0) {
                //cas设置state=1（这里的参数传过来是1）
                if (compareAndSetState(0, acquires)) {
                    //设置锁的持有者
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            //如果state!=0且当前线程就是锁的持有者，就把state变量增加后设置进去
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                //只有当前线程在这个分支里面操作，所以不用使用cas
                setState(nextc);
                return true;
            }
            return false;
        }
```



###### FairSync

```java
		@ReservedStackAccess
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            //这里和非公平锁不同的是要使用hasQueuedPredecessors判断当前线程是不是排在队首的
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
```



公平锁主要不同的实现在于`hasQueuedPredecessors()`，这个方法用于检测当前线程是否排在阻塞队列的队首，是在`AbstractQueuedSynchronizer`里面实现的:

```java
    public final boolean hasQueuedPredecessors() {
        Node h, s;
        //检查阻塞队列是不是空
        if ((h = head) != null) {
            if ((s = h.next) == null || s.waitStatus > 0) {
                s = null; // traverse in case of concurrent cancellation
                //这里之所以要求小于等于0才确定，是为了避免CANCELLED =  1这种情况
                for (Node p = tail; p != h && p != null; p = p.prev) {
                    if (p.waitStatus <= 0)
                        s = p;
                }
            }
            //验证排在最前面的节点
            if (s != null && s.thread != Thread.currentThread())
                return true;
        }
        return false;
    }
```



##### lockInterruptibly()获取锁

```java
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
```



`AbstractQueuedSynchronizer`：

```java
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        //如果先后才能被中断，直接抛出异常
        if (Thread.interrupted())
            throw new InterruptedException();
        //尝试获取资源
        if (!tryAcquire(arg))
            //调用AQS中的可以被中断的方法
            doAcquireInterruptibly(arg);
    }
```



##### tryLock()尝试获取锁

尝试获取锁。

如果当前锁没有被其他线程持有，则获取锁返回true，否则返回false。

**方法不会引起当前线程阻塞。**

```java
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }
```



这里调用的是`Sync`里面的方法，和前面非公平锁调用一致，区别在于`tryLock`直接返回`nonfairTryAcquire`的结果，而不会做后续的操作（把包含线程的节点加到阻塞队列里面去）：

```java
        @ReservedStackAccess
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            //当前没有线程持有锁
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            //持有锁的是当前线程，可重入直接操作state+1（acquire）
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
```



##### tryLock(long timeout, TimeUnit unit)

`ReentranLock`里面的方法：

```java
	//如果超时时间到了没有获取该锁则返回false
	public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }
```





##### unlock()释放锁

```java
	//释放锁
	public void unlock() {
        sync.release(1);
    }
	
	//AQS里面的方法
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            //释放完成之后，检查队列是否还有元素
            //如果有元素而且waitStatus!=0则唤醒该线程
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

		//释放可重入锁，需要把state-1，如果操作后为0,释放当前线程持有锁
		@ReservedStackAccess
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
```



### 并发队列

#### ConcurrentLinkedQueue

特点：使用cas非阻塞实现。



内部是一个单向的链表。



##### offer操作

```java
    public boolean offer(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            if (q == null) {
                // p is last node
                if (p.casNext(null, newNode)) {
                    // Successful CAS is the linearization point
                    // for e to become an element of this queue,
                    // and for newNode to become "live".
                    if (p != t) // hop two nodes at a time
                        casTail(t, newNode);  // Failure is OK.
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }
            else if (p == q)
                // We have fallen off list.  If tail is unchanged, it
                // will also be off-list, in which case we need to
                // jump to head, from which all live nodes are always
                // reachable.  Else the new tail is a better bet.
                p = (t != (t = tail)) ? t : head;
            else
                // Check for tail updates after two hops.
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }
```

注释还是比较详细，不一定每次操作结束head都指向头结点，tail都指向尾结点。

每次完成结点插入后，会判端`p!=t`然后更新尾结点。



`p==q`是在进行`poll`后面可能出现tail自己指向自己的结果（就是`tail.next=null`）,所以需要把p指向头结点head。

像`peek`这种操作只是获取队列头数据，和`poll`类似。



##### poll操作

```java
    public E poll() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;

                if (item != null && p.casItem(item, null)) {
                    // Successful CAS is the linearization point
                    // for item to be removed from this queue.
                    if (p != h) // hop two nodes at a time
                        updateHead(h, ((q = p.next) != null) ? q : p);
                    return item;
                }
                else if ((q = p.next) == null) {
                    updateHead(h, p);
                    return null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }
```





#### LinkedBlockingQueue

有界队列，使用单向链表来实现，使用两个Node存储头尾结点

```java
    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }
```





一些关键的变量写在下面：

```java
    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger();

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();

    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();
```

两个`ReentrantLock`负责出队（poll）和入队（offer）,各维护一个条件变量，用于入队的时候判断是否已满，出队的时候判断是否已空。也就是生产者-消费者的模型：





##### offer与put操作

`offer`是非阻塞的操作：

```java
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final AtomicInteger count = this.count;
        //如果已经超出容量，直接返回false
        if (count.get() == capacity)
            return false;
        final int c;
        final Node<E> node = new Node<E>(e);
        final ReentrantLock putLock = this.putLock;
        //获取入队锁
        putLock.lock();
        try {
            //再次判断是否超出容量
            if (count.get() == capacity)
                return false;
            //入队操作
            enqueue(node);
            //获取并将count值递增
            c = count.getAndIncrement();
            //递增之后还有容量，使用条件变量唤醒条件队列中的阻塞线程
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            //释放锁
            putLock.unlock();
        }
        //至少有一个元素（刚插入的），获取出队锁，使用条件变量唤醒阻塞线程
        if (c == 0)
            signalNotEmpty();
        return true;
    }

	private void enqueue(Node<E> node) {
        // assert putLock.isHeldByCurrentThread();
        // assert last.next == null;
        last = last.next = node;
    }

    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }
```

`offer`也有一个包含等待时长的重载，在队列已满的情况下不会马上返回，而是等待指定时间后再次判断：

```java
            while (count.get() == capacity) {
                if (nanos <= 0L)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
```





`put`操作与此类似，但是发现队列满会使用条件变量等待进入阻塞状态。

```java
			//循环判定避免虚假唤醒的情况
			while (count.get() == capacity) {
                notFull.await();
            }
```





##### poll、take、peek操作

`poll`操作也是非阻塞的:

```java
    public E poll() {
        final AtomicInteger count = this.count;
        //如果没有元素了，直接返回false
        if (count.get() == 0)
            return null;
        final E x;
        final int c;
        final ReentrantLock takeLock = this.takeLock;
        //获取出队锁
        takeLock.lock();
        try {
            //再次判断
            if (count.get() == 0)
                return null;
            //出队拿到旧元素
            x = dequeue();
            //获取并cas递减count
            c = count.getAndDecrement();
            //还有元素，条件变量唤醒阻塞线程
            if (c > 1)
                notEmpty.signal();
        } finally {
            //释放锁
            takeLock.unlock();
        }
        //队列已经不再是满的（上面的操作移除了一个），通知阻塞队列
        if (c == capacity)
            //这里就是获取入队锁然后唤醒notFull，代码不贴了
            signalNotFull();
        return x;
    }

    private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        // assert head.item == null;
        Node<E> h = head;
        Node<E> first = h.next;
        //自引用通知gc回收
        h.next = h; // help GC
        //头结点换位oldhead.next
        head = first;
        //这里的头结点是一个哨兵的概念，所以不存储值
        E x = first.item;
        first.item = null;
        return x;
    }
```

其中，`poll(long timeout, TimeUnit unit)`的重载在获取锁之后才进行循环判断：

```java
            while (count.get() == 0) {
                if (nanos <= 0L)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
```



`take`是阻塞版本的出队操作：

```java
    public E take() throws InterruptedException {
        final E x;
        final int c;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        //获取锁，会响应中断
        takeLock.lockInterruptibly();
        try {
            //循环判断避免虚假唤醒
            while (count.get() == 0) {
                notEmpty.await();
            }
            //出队操作
            x = dequeue();
            //获取并使用cas递减
            c = count.getAndDecrement();
            //如果还有元素（出队后count=1之后递减就是0）
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        //如果队列非空
        if (c == capacity)
            signalNotFull();
        return x;
    }
```



`peek`的操作只是取元素，所以比较简单：

```java
    public E peek() {
        final AtomicInteger count = this.count;
        if (count.get() == 0)
            return null;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            return (count.get() > 0) ? head.next.item : null;
        } finally {
            takeLock.unlock();
        }
    }
```



##### remove操作

`remove`操作比较特殊（实际上队列的特点本来就没有这种操作），因为需要保证操作过程没有其他的线程修改队列，所以需要获取入队锁和出队锁：

```java
    public boolean remove(Object o) {
        if (o == null) return false;
        fullyLock();
        try {
            for (Node<E> pred = head, p = pred.next;
                 p != null;
                 pred = p, p = p.next) {
                if (o.equals(p.item)) {
                    //找到元素后，把对应元素移除，
                    //因为是单向链表，所以这里需要传递前一个值
                    unlink(p, pred);
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    void unlink(Node<E> p, Node<E> pred) {
        // assert putLock.isHeldByCurrentThread();
        // assert takeLock.isHeldByCurrentThread();
        // p.next is not changed, to allow iterators that are
        // traversing p to maintain their weak-consistency guarantee.
        p.item = null;
        //改变执行
        pred.next = p.next;
        //如果需要修改尾结点
        if (last == p)
            last = pred;
        //如果非满，通知需要入队的阻塞线程
        if (count.getAndDecrement() == capacity)
            notFull.signal();
    }
```



#### ArrayBlockingQueue

`ArrayBlockingQueue`是一个有界数组实现的阻塞队列。



下面是一些重要的变量：

```java
    /** The queued items */
    final Object[] items;

    /** items index for next take, poll, peek or remove */
    int takeIndex;

    /** items index for next put, offer, or add */
    int putIndex;

    /** Number of elements in the queue */
    int count;

    /** Main lock guarding all access */
    final ReentrantLock lock;

    /** Condition for waiting takes */
    private final Condition notEmpty;

    /** Condition for waiting puts */
    private final Condition notFull;
```





构造器里面设定了范围，默认lock是非公平锁：

```java
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }
```



##### offer与put操作

`offer`非阻塞操作：

```java
    public boolean offer(E e) {
        Objects.requireNonNull(e);
        final ReentrantLock lock = this.lock;
        //获取锁
        lock.lock();
        try {
            //数组已满，直接返回
            if (count == items.length)
                return false;
            else {
                //入队操作
                enqueue(e);
                return true;
            }
        } finally {
            //释放锁
            lock.unlock();
        }
    }

	private void enqueue(E e) {
        // assert lock.isHeldByCurrentThread();
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        final Object[] items = this.items;
        //这里putIndex存储了入队的数组下标
        items[putIndex] = e;
        //入队下标+1，如果数组已满，入队下标置零
        if (++putIndex == items.length) putIndex = 0;
        //count记录元素个数
        count++;
        //非空通知
        notEmpty.signal();
    }
```

重载版本`offer(E e, long timeout, TimeUnit unit)`获取锁后，即使队列已满也会阻塞指定时间：

```java
		//因为是阻塞的，所以需要对中断做出响应
		lock.lockInterruptibly();
		......
		try {
            while (count == items.length) {
                if (nanos <= 0L)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
```







`put`阻塞操作：

```java
    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        final ReentrantLock lock = this.lock;
        //响应中断获取锁
        lock.lockInterruptibly();
        try {
           	//循环判定避免虚假唤醒
            while (count == items.length)
                //不满足条件一直阻塞，等待notFull.signal/signalAll
                notFull.await();
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }
```





##### poll、take、peek操作

`poll`非阻塞操作：

```java
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (count == 0) ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    private E dequeue() {
        // assert lock.isHeldByCurrentThread();
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        final Object[] items = this.items;
        //takeIndex作为出队的下标，可以直接返回指定位置的数据
        @SuppressWarnings("unchecked")
        E e = (E) items[takeIndex];
        //出队位置置空
        items[takeIndex] = null;
        //出队后下标递增，如果已经到了最大值，直接置零
        if (++takeIndex == items.length) takeIndex = 0;
        count--;
        if (itrs != null)
            itrs.elementDequeued();
        //出队后进行非满通知
        notFull.signal();
        return e;
    }
```



`take`阻塞操作：

```java
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        //阻塞操作要对中断做出响应
        lock.lockInterruptibly();
        try {
            //循环判定避免虚假唤醒
            while (count == 0)
                notEmpty.await();
            return dequeue();
        } finally {
            lock.unlock();
        }
    }
```



`peek`非阻塞操作：

```java
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return itemAt(takeIndex); // null when queue is empty
        } finally {
            lock.unlock();
        }
    }
```



#### PriorityBlockingQueue

`PriorityBlockingQueue`是带有**优先级**的**无界**、**阻塞**队列。



底层使用数组`queue`维护：

```java
	//底层数组
	private transient Object[] queue;

    //队列元素的个数
    private transient int size;

	private transient Comparator<? super E> comparator;

	private final ReentrantLock lock = new ReentrantLock();

	private final Condition notEmpty = lock.newCondition();
```

因为设计是无界的，所以这里没有之前类似`notFull`的条件变量。



如果需要有优先级，需要在构造器传递比较器（默认是null）。

```java
    public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        //传递的比较器，父类的比较器也可以传递进来
        this.comparator = comparator;
        //
        this.queue = new Object[Math.max(1, initialCapacity)];
    }
```



##### offer和put操作

`offer`因为是无界队列，所以是阻塞操作，`put`底层就是offer操作。

```java
 public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        //获取锁
        lock.lock();
        int n, cap;
        Object[] es;
        //元素个数大于当前队列长度
        while ((n = size) >= (cap = (es = queue).length))
            //尝试扩容
            tryGrow(es, cap);
        try {
            final Comparator<? super E> cmp;
            if ((cmp = comparator) == null)
                siftUpComparable(n, e, es);
            else
                siftUpUsingComparator(n, e, es, cmp);
            //队列元素增加1
            size = n + 1;
            //通知当前队列非空
            notEmpty.signal();
        } finally {
            //释放独占锁
            lock.unlock();
        }
        return true;
    }

	private static final VarHandle ALLOCATIONSPINLOCK;
	ALLOCATIONSPINLOCK = l.findVarHandle(PriorityBlockingQueue.class,
    	"allocationSpinLock",
        int.class);

    private void tryGrow(Object[] array, int oldCap) {
        //扩容比较小耗时间，所以先释放锁
        lock.unlock(); // must release and then re-acquire main lock
        Object[] newArray = null;
        
        //CAS成功则扩容
        if (allocationSpinLock == 0 &&
            ALLOCATIONSPINLOCK.compareAndSet(this, 0, 1)) {
            try {
                int newCap = oldCap + ((oldCap < 64) ?
                                       (oldCap + 2) : // grow faster if small
                                       (oldCap >> 1));
                if (newCap - MAX_ARRAY_SIZE > 0) {    // possible overflow
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError();
                    newCap = MAX_ARRAY_SIZE;
                }
                if (newCap > oldCap && queue == array)
                    newArray = new Object[newCap];
            } finally {
                allocationSpinLock = 0;
            }
        }
        //cas操作失败的线程会让出CPU
        if (newArray == null) // back off if another thread is allocating
            Thread.yield();
        //重新获取锁
        lock.lock();
        //如果扩容完成了，需要把queue指向新的数组
        if (newArray != null && queue == array) {
            queue = newArray;
            //数据拷贝
            System.arraycopy(array, 0, newArray, 0, oldCap);
        }
    }
```





底层是一个二叉堆的结构，所以每次需要和跟节点比较：

![最大堆的添加操作](.\image\最大堆的添加操作.jpg)

此图是最大堆的添加操作，这里是一个最小堆，也就是找到比现在插入节点数据小的位置就停止。

```java
   

	//传入的参数 k=size 队列元素个数，也就是队尾
	//x插入的元素
	//es = queue
    private static <T> void siftUpComparable(int k, T x, Object[] es) {
        //直接向上转型
        Comparable<? super T> key = (Comparable<? super T>) x;
        //指定位置>0
        while (k > 0) {
            //(k - 1) >>> 1 -> (k - 1)/2，得到父节点的位置
            int parent = (k - 1) >>> 1;
            //拿到父节点的数据
            Object e = es[parent];
            //如果满足位置（父节点小于当前节点），直接退出
            if (key.compareTo((T) e) >= 0)
                break;
            //如果需要调换位置，把当前位置的数据换为父节点，然后k指向父节点的位置
            es[k] = e;
            k = parent;
        }
        //在指定位置k设置数据
        es[k] = key;
    }

    private static <T> void siftUpUsingComparator(
        int k, T x, Object[] es, Comparator<? super T> cmp) {
        while (k > 0) {
            //(k - 1) >>> 1 -> (k - 1)/2
            int parent = (k - 1) >>> 1;
            Object e = es[parent];
            //包含了比较器的情况，直接调用比较器的方法
            if (cmp.compare(x, (T) e) >= 0)
                break;
            es[k] = e;
            k = parent;
        }
        es[k] = x;
    }
```



##### poll操作

获取根节点元素（最小的值）

```java
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return dequeue();
        } finally {
            lock.unlock();
        }
    }
```



```java
    private E dequeue() {
        // assert lock.isHeldByCurrentThread();
        final Object[] es;
        final E result;

        //直接获取队列中第一个位置
        if ((result = (E) ((es = queue)[0])) != null) {
            final int n;
            //数量-1，拿出末尾的元素，因为
            final E x = (E) es[(n = --size)];
            //末尾元素置空，实际上就是元素数组减少一个元素后大小减1
            es[n] = null;
            //如果还剩下元素，则需要进行最小堆的重新结构化
            if (n > 0) {
                final Comparator<? super E> cmp;
                if ((cmp = comparator) == null)
                    siftDownComparable(0, x, es, n);
                else
                    siftDownUsingComparator(0, x, es, n, cmp);
            }
        }
        return result;
    }
```



最小堆删除元素的时候，不能直接用子节点代替父节点，因为无法保证完成后依然是最小堆，

所以需要把末尾元素替换根节点，通过比较向下移动：

![](.\image\最大堆的删除操作.jpg)

这个图展示是最大堆的操作。



```java
	//k=0  	指向末尾元素的位置（一开始使用末尾的元素来代替被移除的根节点）
	//x->key 	末尾元素转换为key
	//es	移除根节点后的数组（这里状态只是删除了尾结点）
	//n		当前元素个数
	private static <T> void siftDownComparable(int k, T x, Object[] es, int n) {
        // assert n > 0;
        Comparable<? super T> key = (Comparable<? super T>)x;
        int half = n >>> 1;           // loop while a non-leaf
        while (k < half) {
            //查找左子节点，默认左边小
            int child = (k << 1) + 1; // assume left child is least
            Object c = es[child];
            //查找右子节点
            int right = child + 1;
            //right < n还没到尾结点
            //c.compareTo(es[right]) > 0 左子节点右子节点大，c指向右子节点
            if (right < n &&
                ((Comparable<? super T>) c).compareTo((T) es[right]) > 0)
                c = es[child = right];
            //如果子节点都比父节点大，找到了位置k
            if (key.compareTo((T) c) <= 0)
                break;
            //设定k指定位置数据为子节点数据
            es[k] = c;
            //k开始指向被交换的位置
            k = child;
        }
        //找到位置后设定新的值进去
        es[k] = key;
    }

	//区别就在于使用了设定的比较器
    private static <T> void siftDownUsingComparator(
        int k, T x, Object[] es, int n, Comparator<? super T> cmp) {
        // assert n > 0;
        int half = n >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = es[child];
            int right = child + 1;
            if (right < n && cmp.compare((T) c, (T) es[right]) > 0)
                c = es[child = right];
            if (cmp.compare(x, (T) c) <= 0)
                break;
            es[k] = c;
            k = child;
        }
        es[k] = x;
    }
```



##### take操作

`take`稍微和poll有点不同，是一个阻塞操作。

```java
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        //和所有的阻塞操作一样，都需要对中断做出响应
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null)
                notEmpty.await();
        } finally {
            lock.unlock();
        }
        return result;
    }
```




#### DelayQueue

`DelayQueue`并发队列是一个**无界、阻塞、延迟**队列。



```java
public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
    implements BlockingQueue<E> {

}

public interface Delayed extends Comparable<Delayed> {
    long getDelay(TimeUnit unit);
}
```

内部元素实现`Delayed`接口，因为内部使用`PriorityQueue`存放数据，所以要实现`Comparable`来确定优先级。



使用`ReentrantLock`实现线程同步。

```java
private final transient ReentrantLock lock = new ReentrantLock();
private final Condition available = lock.newCondition();

private final PriorityQueue<E> q = new PriorityQueue<E>();

private Thread leader;
```



##### offer操作

因为是无界的队列，所以每次offer都可以插入，没有必要阻塞。

```java
    public boolean offer(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            //向PriorityQueue队列插入元素，这里面也是向最小堆插入数据的操作
            q.offer(e);
            //说明当前插入的元素e是最先将要过期的
            if (q.peek() == e) {
                leader = null;
                //available条件变量通知队列有元素了。
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
```



##### take操作

获取并且移除队列里面延迟时间过期的元素，没有就阻塞等待。

```java
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                //获取首节点
                E first = q.peek();
                //没有就等待，外层是for循环阻塞
                if (first == null)
                    available.await();
                else {
                    //getDelay是Delayed的接口需要实现的
                    long delay = first.getDelay(NANOSECONDS);
					//如果延时已经到了，直接取出来
                    if (delay <= 0L)
                        return q.poll();
                    
                    //这里如果延时还没结束，那就会开始进入等待，把first置空通知gc回收
                    first = null; // don't retain ref while waiting
                    //leader线程不为空，进入条件队列
                    if (leader != null)
                        available.await();
                    else {
                        //获取当前线程，设置到leader里面
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            //等待对应的时间再返回
                            available.awaitNanos(delay);
                        } finally {
                            //等待时间到了，设置leader=null
                            if (leader == thisThread)
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            //整个操作完成，如果队列中还有元素而且没有线程锁定，则调用条件变量进行唤醒
            if (leader == null && q.peek() != null)
                available.signal();
            lock.unlock();
        }
    }
```

leader里面记录的是操作首节点的线程，如果检测到不为null，表示有其他线程在执行take，这个时候就需要进入条件队列等待，否则的话就可以设置leader为当前线程，调用`awaitNanos`等待对应的时间，在这期间释放了锁，所以其他线程仍然可以进行`offer`操作（也可以调用`take`，但因为当前leader不为空会被阻塞）。



##### poll操作

非阻塞的取操作

```java
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E first = q.peek();
            return (first == null || first.getDelay(NANOSECONDS) > 0)
                ? null
                : q.poll();
        } finally {
            lock.unlock();
        }
    }
```







### 线程池ThreadPoolExecutor原理

线程池的作用：

- 维护可复用的线程，优化性能
- 提供线程资源限制与管理，维护一些基本的统计数据（线程完成任务数）





<img src=".\image\Executors类图.png"  />

#### ThreadPoolExecutor基本参数



`ctl`用于记录**线程池状态**和**线程池中线程个数**

```java
//高3位用于记录线程池状态
//低29位记录线程个数
//当然不是所有平台int都是32位的，所以不一定是低29位，可以看掩码是-3来处理的
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
```



下面这些变量都是围绕`ctl`的：

```java
	//COUNT_MASK找到ctl记录线程个数的数值
	private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int COUNT_MASK = (1 << COUNT_BITS) - 1;

	//高3位用于记录线程池的状态
    // runState is stored in the high-order bits
	//RUNNING:		1110 0000 0000 0000 0000 0000 0000 0000  
    private static final int RUNNING    = -1 << COUNT_BITS;
	//SHUTDOWN:		0000 0000 0000 0000 0000 0000 0000 0000
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
	//STOP			0010 0000 0000 0000 0000 0000 0000 0000 
    private static final int STOP       =  1 << COUNT_BITS;	
	//TIDYING		0100 0000 0000 0000 0000 0000 0000 0000 
    private static final int TIDYING    =  2 << COUNT_BITS;
	//TERMINATED	0110 0000 0000 0000 0000 0000 0000 0000
    private static final int TERMINATED =  3 << COUNT_BITS;

	// Packing and unpacking 
	//获取运行状态
    private static int runStateOf(int c)     { return c & ~COUNT_MASK; }
	//获取线程数
    private static int workerCountOf(int c)  { return c & COUNT_MASK; }
	//rs表示运行状态，wc表示线程数量，或操作得到当前的ctl值，可以看ctl的构造器
    private static int ctlOf(int rs, int wc) { return rs | wc; }

	private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

	//不知道为啥这么写，好像只有RUNNING小于0
    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }
```



运行状态含义：

- `RUNNING`	接收新的任务，并且开始处理阻塞队列的任务
- `SHUTDOWN`  拒绝新的任务，但是处理阻塞队列的任务
- `STOP`  拒绝新的任务，抛弃阻塞队列任务，中断正在处理的任务
- `TIDYING` 所有任务（包括阻塞队列）执行完后当前线程活动线程数为0，将要调用terminated方法
- `TERMINATED` 终止状态





线程池相关的参数：

```java
	//线程池核心线程数
	private volatile int corePoolSize;
	
	//保存阻塞任务队列
	private final BlockingQueue<Runnable> workQueue;

	//线程池最大线程数量
	private volatile int maximumPoolSize;

	//创建线程的工厂
	private volatile ThreadFactory threadFactory;
	
	//饱和策略，当队列满或者线程个数到到maximumPoolSize之后的策略
	//ThreadPoolExecutor里面包含了四种实现，比较简单不贴代码了
	//CallerRunsPolicy/AbortPolicy/DiscardPolicy/DiscardOldestPolicy
	private volatile RejectedExecutionHandler handler;
	//默认的饱和策略是直接抛出异常
	private static final RejectedExecutionHandler defaultHandler =
    	new AbortPolicy();	

	/**
     * Timeout in nanoseconds for idle threads waiting for work.
     * Threads use this timeout when there are more than corePoolSize
     * present or if allowCoreThreadTimeOut. Otherwise they wait
     * forever for new work.
     */
	//存活时间，如果当前线程池中的线程数量>核心线程数，并且处于闲置状态。
	//这些闲置线程能够存活的最大时间。
    private volatile long keepAliveTime;

```



线程池中的锁：

```java
	//控制worker线程操作的原子性
	private final ReentrantLock mainLock = new ReentrantLock();
	
	//条件队列
	private final Condition termination = mainLock.newCondition();
```

工厂类：

```java
 	private volatile ThreadFactory threadFactory;
	
	//默认使用
	Executors.defaultThreadFactory();
```



#### Worker

`ThreadPoolExecutor`的内部类：

```java
	private final HashSet<Worker> workers = new HashSet<>();
	
	//不可重入锁
	private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
    	/** Thread this worker is running in.  Null if factory fails. */
    	//具体执行任务的线程
        final Thread thread;
        /** Initial task to run.  Possibly null. */
        //工作线程执行的第一个任务
        Runnable firstTask;
        /** Per-thread task counter */
        volatile long completedTasks;
        
    	//state=0锁没有被获取
    	//state=1锁已经被占有
    	//初始值为-1，避免运行runWorker的时候被中断
    	Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }
    	
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
		
        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }    
        
        public void run() {
            runWorker(this);
        }
    }
```





#### Executors构建线程池

为了方便说看参数，这里贴一下`ThreadPoolExecutor`构造器，主要的参数作用看`ThreadPoolExecutor`的说明

```java
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
```



这里的工厂类使用的`DefaultThreadFactory`：

```java
    private static class DefaultThreadFactory implements ThreadFactory {
        //线程工厂个数
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        //工厂创建了多少线程
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                //设置为用户线程
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
```



**newFixedThreadPool**：

```java
	//corePoolSize=maximumPoolSize=nThreads
	//keepAliveTime=0多余的闲置线程会马上回收
	//LinkedBlockingQueue是一个有界队列（生产者-消费者模型）
	public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

	//自己提供线程工厂类
	public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      threadFactory);
    }
```



**newSingleThreadExecutor**

```java
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```



**newCachedThreadPool**

```java
	//corePoolSize=0
	//maximumPoolSize=Integer.MAX_VALUE
	//提供60s空闲等待时间
	//SynchronousQueue同步队列只能有一个任务
	public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```





#### ThreadPoolExecutor.execute

`AbstractExecutorService`实现了默认的`submit`方法，就是使用execute来执行的：

```java
    public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }
```





提交任务到线程池进行执行，之后works会把执行对应的任务取出来进行执行（生产者-消费者模式）：

```java
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.
         */
        int c = ctl.get();
        //线程池中的线程数<corePoolSize
        if (workerCountOf(c) < corePoolSize) {
            //开启新线程执行，如果失败往下执行
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        //检测线程池状态，如果为Running向workQueue等待队列里面添加任务，
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            //再次检查如果状态不是Running，队列删除任务
            //reject执行拒绝策略
            if (! isRunning(recheck) && remove(command))
                reject(command);
            //否则如果当前线程池为空，添加一个线程
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        //队列已经满，则新增线程，失败执行拒绝策略
        else if (!addWorker(command, false))
            reject(command);
    }

	// if true use corePoolSize as bound, else maximumPoolSize.
	private boolean addWorker(Runnable firstTask, boolean core) {
    ......
    }
```



##### addWorker

下面仔细看一下`addWorker`的方法：

```java
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (int c = ctl.get();;) {
            // Check if queue empty only if necessary.
            //满足下面三种情况之一
            //runState为STOP、TIDYING、TERMINATED
            //runState=SHUTDOWN且已经有了第一个任务
            //runState=SHUTDOWN且任务队列为空
            if (runStateAtLeast(c, SHUTDOWN)
                && (runStateAtLeast(c, STOP)
                    || firstTask != null
                    || workQueue.isEmpty()))
                return false;

            //循环cas增加线程个数
            for (;;) {
                //比较数据，core=true关联corePoolSize，core=false关联maximumPoolSize
                //超出数量返回false
                if (workerCountOf(c)
                    >= ((core ? corePoolSize : maximumPoolSize) & COUNT_MASK))
                    return false;
                //cas递增线程池中线程数，如果成功，退出retry的代码标记
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                //如果状态改变了runState>=SHUTDOWN，进入retry循环，否则，继续尝试cas
                if (runStateAtLeast(c, SHUTDOWN))
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        //cas成功之后可以到达这里，线程个数已经修改，但是任务还没有开始执行
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            //创建worker
            w = new Worker(firstTask);
            //获取实际执行的任务（被newThread包装的worker线程）
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
				//获取锁
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int c = ctl.get();

                    //检测状态
                    if (isRunning(c) ||
                        (runStateLessThan(c, STOP) && firstTask == null)) {
                        if (t.getState() != Thread.State.NEW)
                            throw new IllegalThreadStateException();
                        //添加任务
                        workers.add(w);
                        workerAdded = true;
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                    }
                } finally {
                    mainLock.unlock();
                }
				//添加成功，启动任务,worker的run方式就是runWorker(this);
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            //启动任务失败，添加到失败的队列
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
```



##### runWorker

```java
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        //允许中断
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            //getTask()从workQueue里面拿出来的任务
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    try {
                        task.run();
                        afterExecute(task, null);
                    } catch (Throwable ex) {
                        afterExecute(task, ex);
                        throw ex;
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
```



##### shutdown

执行`shutdown`之后，线程池不在接收新的任务，但是工作队列里面的任务还是需要执行。

```java
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //权限检查
            checkShutdownAccess();
            //设置状态为SHUTDOWN
            advanceRunState(SHUTDOWN);
            //设置中断标志位
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        //尝试将状态更变为TERMINATED
        tryTerminate();
    }

	//cas修改runstate
    private void advanceRunState(int targetState) {
        // assert targetState == SHUTDOWN || targetState == STOP;
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }
```

与此对比的`shutdownNow`：线程池不会再接收新的任务，并且会丢弃工作队列里面的任务，正在执行的任务也会被中断。



##### awaitTermination

调用`awaitTermination`之后，当前线程阻塞直到线程池的状态修改为`TERMINATED`：

```java
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            //循环cas
            while (runStateLessThan(ctl.get(), TERMINATED)) {
                if (nanos <= 0L)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
            return true;
        } finally {
            mainLock.unlock();
        }
    }
```



### ScheduledThreadPoolExecutor

`ScheduledThreadPoolExecutor`是`ThreadPoolExecutor`的一个子类。

特点在于可以在指定一定时间后或者定时进行任务调度执行的线程池。



### 线程同步器原理



#### CountDownLatch

##### 应用

有的时候我们需要等待子线程执行完毕再操作的情况，一般使用`join`来等待线程执行。

`CountDownLatch`可以代替`join`灵活处理多种情况（比如说线程池）。

```java
public class CountDownLatchDemo {
    private static volatile CountDownLatch countDownLatch = new CountDownLatch(2);

    public static void main(String[] args) throws Exception {
        System.out.println(System.currentTimeMillis());
        //countDown将计数器减一
        new Thread(() -> countDownLatch.countDown()).start();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        //计数器为0才能返回
        countDownLatch.await();
        System.out.println("all over end=" + System.currentTimeMillis());
    }
}
```



