# ClassLoader



系统三个类加载器的顺序：

- BootStrap ClassLoader 核心类库，主要是jre lib目录下rt.jar、resources.jar、charset.jar和class等
- Extention ClassLoader 加载扩展类，主要是jre lib/ext下的包
- AppClassLoader 加载当前应用的classpath的所有类



## Launcher





首先我们查看虚拟机入口应用：sun.misc.Launcher

```java
    public Launcher() {
        Launcher.ExtClassLoader var1;
        try {
            var1 = Launcher.ExtClassLoader.getExtClassLoader();
        } catch (IOException var10) {
            throw new InternalError("Could not create extension class loader", var10);
        }

        try {
            this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);
        } catch (IOException var9) {
            throw new InternalError("Could not create application class loader", var9);
        }
        
		
        Thread.currentThread().setContextClassLoader(this.loader);		
     	......   
    }
```



在构造器里面完成了ExtClassLoader、AppClassLoader的初始化



其中ExtClassLoader通过`getExtClassLoader()`获取，源码会在需要初始化实例时调用`getExtDirs()`这个方法把指定路径的文件路径取出来，我们可以测试一下，可以通过`-Djava.ext.dirs=xx`修改：

```java
    private static File[] getExtDirs() {
        String var0 = System.getProperty("java.ext.dirs");
        File[] var1;
        if (var0 != null) {
            StringTokenizer var2 = new StringTokenizer(var0, File.pathSeparator);
            int var3 = var2.countTokens();
            var1 = new File[var3];

            for (int var4 = 0; var4 < var3; ++var4) {
                var1[var4] = new File(var2.nextToken());
            }
        } else {
            var1 = new File[0];
        }

        return var1;
    }
```

这里也就是从系统参数取到路径并分割出来。



同样的，`getAppClassLoader`也是在获取`"java.class.path"`的参数值。



## 父加载器

通过下面的代码：

```java
System.out.println("Test:\t" + Test.class.getClassLoader());
//sun.misc.Launcher$AppClassLoader@18b4aac2
System.out.println("Test P1:\t" + Test.class.getClassLoader().getParent());
//sun.misc.Launcher$ExtClassLoader@6ed3ef1
System.out.println("Test P2:\t" + Test.class.getClassLoader().getParent().getParent());
//null
```

父加载器和父类无关（实际上AppClassLoader和ExtClassLoader的父类都是URLClassLoader）



这里的AppClassLoader父加载器是ExtClassLoader，也是源于之前Launcher的构造器：

```java
var1 = Launcher.ExtClassLoader.getExtClassLoader();
this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);
```

这个参数最后在`ClassLoader`里面作为父加载器在构造器传入。

同样，`ExtClassLoader`因为没有传参，父加载器为null



## BootStrap ClassLoader与委托模式

因为bootstrap classloader是由C写的，所以无法在java代码里面获取引用。



通过classloader寻找class与resources的时候，是使用委托的模式来进行的：

当类加载器查找一个类的时候，先不断递归找到父加载器，查看是否找到，最后一级一级返回来查找。

```java
System.out.println("Test:\t" + Test.class.getClassLoader());
//sun.misc.Launcher$AppClassLoader@18b4aac2
System.out.println("Test P1:\t" + Test.class.getClassLoader().getParent());
//sun.misc.Launcher$ExtClassLoader@6ed3ef1
System.out.println("Test P2:\t" + Test.class.getClassLoader().getParent().getParent());
//null
```



看下之前的代码，Test.class会

先通过AppClassLoader查看魂村中是否存在，

没有则委托父加载器ExtClassLoader，

ExtClassLoader继续委托BootStrapClassLoader开始查找，没有找到之后在回到下一级继续查找。



# Class

## 判断类型

```java
    public static boolean isWrapClass(Class<?> clz) {
        try {
            return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
    
	public static Class<?>[] getClazzByArgs(Object[] args) {
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ArrayList) {
                parameterTypes[i] = List.class;
                continue;
            }
            if (args[i] instanceof LinkedList) {
                parameterTypes[i] = List.class;
                continue;
            }
            if (args[i] instanceof HashMap) {
                parameterTypes[i] = Map.class;
                continue;
            }
            if (isWrapClass(args[i])){
                parameterTypes[i] = clz.getField("TYPE").get(null);
                continue;
            }
            if (args[i] instanceof TimeUnit){
                parameterTypes[i] = TimeUnit.class;
                continue;
            }
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }
```



为什么这里不直接调用`isPrimitive()`方法？

考虑会出现原类型未知的情况，必须通过field.get()取得原类型，这里的TYPE是静态变量，所以可以直接传null。

如：

```java
        Object o = 1;
        System.out.println(isWrapClass(o.getClass()));
        System.out.println(o.getClass().isPrimitive());
```



说明：

```java
    /**
     * Determines if the specified {@code Class} object represents a
     * primitive type.
     *
     * <p> There are nine predefined {@code Class} objects to represent
     * the eight primitive types and void.  These are created by the Java
     * Virtual Machine, and have the same names as the primitive types that
     * they represent, namely {@code boolean}, {@code byte},
     * {@code char}, {@code short}, {@code int},
     * {@code long}, {@code float}, and {@code double}.
     *
     * <p> These objects may only be accessed via the following public static
     * final variables, and are the only {@code Class} objects for which
     * this method returns {@code true}.
     *
     * @return true if and only if this class represents a primitive type
     *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
     * @since 1.1
     */
    @HotSpotIntrinsicCandidate
    public native boolean isPrimitive();
```





# 代理

## 静态代理

就是继承与组合中的**组合方式**



## JDK动态代理

jdk动态代理编译完成后没有生成实际的class文件，而是运行时动态生成类字节码，并且加载到JVM中。

```java
public interface BaseApi {
    int insert();

    void test();
}
```



```java
public class DataApi implements BaseApi {
    @Override
    public int insert() {
        return 0;
    }

    @Override
    public void test() {
        System.out.println(getClass().getSimpleName() + "test");
    }
}
```



```java
public class ApiProxy {
    private Object object;

    public ApiProxy(Object object) {
        this.object = object;
    }

    public Object getProxyInstance() {
        return Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("代理执行开始");

                        Object invoke = method.invoke(object, args);

                        System.out.println("代理执行结束");

                        return invoke;
                    }
                }
        );
    }

    /**
     * output:
     * class com.zr.network.basic.classdemo.proxy.api.DataApi
     * 代理执行开始
     * 代理执行结束
     * 0
     * 代理执行开始
     * DataApitest
     * 代理执行结束
     */
    public static void main(String[] args) {
        BaseApi dataApi = new DataApi();
        System.out.println(dataApi.getClass());

        BaseApi proxyInstance = (BaseApi) new ApiProxy(dataApi).getProxyInstance();
        System.out.println(proxyInstance.insert());
        proxyInstance.test();
    }
}
```



这里代理的类是

`proxyInstance.getClass()`=`class com.sun.proxy.$Proxy0`

## cglib代理

运行时在内存中动态生成一个子类对象：

- 使用动态代理的对象必须实现一个或多个接口，同样优点也是可以代理没有实现的接口

-  底层使用字节码处理框架ASM来转换字节码生成新的类

- spring-core里面包含了cglib的包，单独使用需要引入：

  ```xml
  <!-- https://mvnrepository.com/artifact/cglib/cglib -->
  <dependency>
      <groupId>cglib</groupId>
      <artifactId>cglib</artifactId>
      <version>3.2.5</version>
  </dependency>
  ```





代码示例：

```java
public class CglibProxy implements MethodInterceptor {
    private Object object;

    public CglibProxy(Object object) {
        this.object = object;
    }

    public Object getProxyInstance() {
        Enhancer enhancer = new Enhancer();
        //这里直接使用子类来操作
        enhancer.setSuperclass(object.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("----- 进入代理拦截器");
        Object invoke = method.invoke(object, objects);
        System.out.println("----- 结束代理拦截器");
        return invoke;
    }

    public static void main(String[] args) {
        BaseApi dataApi = new DataApi();
        System.out.println(dataApi.getClass());

        BaseApi proxyInstance = (BaseApi) new CglibProxy(dataApi).getProxyInstance();
        System.out.println(proxyInstance.insert());
        proxyInstance.test();
    }
}
```

这里代理类：

`proxyInstance.getClass()`=`class com.zr.network.basic.classdemo.proxy.api.DataApi$$EnhancerByCGLIB$$c744a384`



### Cglib代理后注解为空

示例代码：https://github.com/zrinGithub/aop-demo.git

- 问题描述：

使用cglib代理后，拿不到注解。

- 解决方法：
  - 使用类来获取
  - 使用spring工具类AnnotationUtils.findAnnotation
  - 使用jdk动态代理
  - 如果是类上的注解拿不到，可以在注解上面加`@Inherited`，因为cglibs使用的是子类，不过这个只对类有帮助，方法还是会报错，可以查看对应的问题： https://stackoverflow.com/questions/1706751/retain-annotations-on-cglib-proxies 





# MethodHandles & VarHandle

博客：https://mingshan.fun/2018/10/05/use-variablehandles-to-replace-unsafe/



MehthodHandles作用和反射包里面的Method比较相似，但是性能上面，MethodHandles更快，因为创建的时候就会执行访问检查。

VarHandle出现再很多并发包的工具类里面，作为取代Unsafe类来使用（实际上我们如果要用Unsafe也是反射绕过访问控制来使用的）



创建VarHandle需要通过`MethodHandles`这个类调用其静态方法来实现，根据要访问类的不同类型的成员变量调用不同的静态方法：

- `MethodHandles.lookup` 访问类非私有属性
- `MethodHandles.privateLookupIn` 访问类的私有属性
- `MethodHandles.arrayElementVarHandle` 访问类中的数组



获取到Lookup，然后通过调用`findVarHandle`方法来获取`VarHandle`实例，在JDK9中，

- `findVarHandle`：用于创建对象中非静态字段的VarHandle。接收参数有三个，第一个为接收者的Class对象，第二个是字段名称，第三个是字段类型。
- `findStaticVarHandle`：用于创建对象中静态字段的VarHandle，接收参数与findVarHandle一致。
- `unreflectVarHandle`：通过反射字段创建VarHandle。

为了保证效率，VarHandle类的实例通常需要被声明为static final变量（其实就是常量），这样可以在编译期对它进行优化。代码如下：



```java
private static final VarHandle VH_TEST_FIELD_I;
private static final VarHandle VH_TEST_ARRAY;
private static final VarHandle VH_TEST_FIELD_J;

int i = 1;
int[] arr = new int[]{1, 2, 3};
private int j = 2;

static {
    try {
        VH_TEST_FIELD_I = MethodHandles.lookup()
                .in(Test.class)
                .findVarHandle(Test.class, "i", int.class);

        VH_TEST_ARRAY = MethodHandles.arrayElementVarHandle(int[].class);

        VH_TEST_FIELD_J = MethodHandles.privateLookupIn(Test.class, MethodHandles.lookup())
                .findVarHandle(Test.class, "j", int.class);
    } catch (ReflectiveOperationException e) {
        throw new Error(e);
    }
}
```



