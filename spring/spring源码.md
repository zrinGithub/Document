# 一. beans包





<img src=".\images\DefaultListableBeanFactory.png" alt="DefaultListableBeanFactory" style="zoom:100%;" />

AliasRegistry: 管理alias

BeanFactory: 获取bean及其属性

SingletonBeanRegistry: 单例类的注册获取



## 1 示例源码跟踪

首先看一个bean的使用：

```java
BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("org/springframework/zr/BeanFactoryTest.xml"));
MyBean bean = beanFactory.getBean("aliasName", MyBean.class);
assertEquals("factoryBean", fb.getTestStr());
```

这里使用到了：Resource用于获取配置、BeanFactory用于注册类以及取出对应bean

因为是xml文件，里面用到了BeanDefinationReader用于解析文件



### Resource

spring自己实现InputStreamSource接口用于完成资源解析。

```java
public interface InputStreamSource {
	InputStream getInputStream() throws IOException;
}
```

Resource则定义了更多类型的资源处理方式，如针对文件的`getFile()`



`new ClassPathResource("org/springframework/zr/BeanFactoryTest.xml"));`

做了什么？

构建除了ClassPathResource的实例，注入path变量数据以及classloader指定为当前线程的classloader，不指定就是父线程main的，也就是AppClassLoader：

```java
	public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		Assert.notNull(path, "Path must not be null");
        //cleanPath就是转换windows下\\到/
		String pathToUse = StringUtils.cleanPath(path);
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		this.path = pathToUse;
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}
```



### XmlBeanFactory

把构建的类路径资源->ClassPathResource实例化传给XmlBeanFactory

```java
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}

	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}
```

这里可以看到引入了parentBeanFactory这个概念了，我们顺着父类构造器网上查看，实际上是在AbstractBeanFactory的`setParentBeanFactory`方法里面注入了parentBeanFactory。

我们再看之前的类图，实际上HierarchicalBeanFactory就开始提供`getParentBeanFactory`，而AbstractBeanFactory的角色是作为整合ConfigurableBeanFactory与FactoryBeanRegistrySupport而出现。

因为整个类图比较复杂，我们首先从上到下理清楚这么分层的作用：

------

AliasRegistry：alias的管理接口定义，定义了alias的注册、移除、判断、获取

BeanDefinitionRegistry：beandefinition相关的操作

SimpleAliasRegistry：使用aliasMap（ConcurrentHashMap）来保存别名（key）与类名（val）的映射关系。

SingletonBeanRegistry：定义了单例类的注册、获取接口。

DefaultSingletonBeanRegistry：整合SimpleAliasRegistry，实现了SingletonBeanRegistry中单例的注册获取

FactoryBeanRegistrySupport：继承DefaultSingletonBeanRegistry，增加了对FactoryBean的操作



BeanFactory：定义bean容器的接口

HierarchicalBeanFactory：引入了`getParentBeanFactory()`的概念

ConfigurableBeanFactory：继承HierarchicalBeanFactory, SingletonBeanRegistry，实现了各种配置的方法

AutowireCapableBeanFactory：定义创建bean、自动注入、初始化、以及后处理

ListableBeanFactory：定义了根据各种条件获取beandefinition的方法



回到之前的代码：

`this.reader.loadBeanDefinitions(resource);`

reader就是XmlBeanDefinitionReader，