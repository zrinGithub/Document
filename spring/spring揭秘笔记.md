# spring揭秘笔记

该文档为《spring揭秘》笔记



## 1. IoC

IoC：Inversion of Control控制反转

DI：依赖注入



IoC是一种抽象的概念，我们把对象的控制（创建、依赖）转让给IoC Service Provider实现。

DI指将容器动态将依赖关系注入到逐渐里面。



**实现的方式**

- 构造器
- setter
- 接口
- 注解



IoC Service Provider需要**管理对象之间的依赖关系**，实现的方式有：

- 直接编码把对象注册到容器中
- 使用xml文件配置
- 元数据方式（Google的`@Inject`注解）



Spring的IoC容器不仅仅提供了对象创建于依赖注入的功能，还包含AOP框架、线程管理、对象生命周期等服务。



Spring提供了两种容器：

- BeanFactory：默认延迟初始化（lazy-load）
- ApplicationContext



**BeanFactory**

使用了BeanFactory之后，我们使用对象直接由BeanFactory提供。

作为实现类的DefaultListableBeanFactory还实现了BeanDefinitionRegistry接口，这个接口负责Bean注册管理。

每一个受管对象，都对应容器中的一个BeanDefination负责保存对象所有必要信息（class类型、构造方法参数、其他属性）。

