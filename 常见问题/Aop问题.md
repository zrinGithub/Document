# Cglib代理后注解为空

示例代码：https://github.com/zrinGithub/aop-demo.git

- 问题描述：

使用cglib代理后，拿不到注解。

- 解决方法：
  - 使用类来获取
  - 使用spring工具类AnnotationUtils.findAnnotation
  - 使用jdk动态代理
  - 如果是类上的注解拿不到，可以在注解上面加`@Inherited`，因为cglibs使用的是子类，不过这个只对类有帮助，方法还是会报错，可以查看对应的问题： https://stackoverflow.com/questions/1706751/retain-annotations-on-cglib-proxies 

