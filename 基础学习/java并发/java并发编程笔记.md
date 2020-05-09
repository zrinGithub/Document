# Java并发编程笔记

该文档为《Java并发编程之美》的读书笔记



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

z