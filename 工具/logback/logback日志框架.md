# LogBack日志框架介绍

## 基础知识

- 常见java日志组件：slf4j、log4j、logback、common-logging
- 介绍：基于log4j的基础上大量改良，不能单独使用。推荐配合日志框架slf4j来使用。当前分为三个模块：logback-core，logback-classic，logback-access；
- 核心对象
  - Logger：日志记录器
  - Appender：输出位置（如：控制台、文件）
  - Layout：布局（输出格式的配置）
- 日志级别：DEBUG < INFO < WARN < ERROR



- log4j的配置示例：

```properties
### 设置###
log4j.rootLogger = debug,stdout,D,E
	
### 输出信息到控制抬 ###
log4j.appender.stdout = org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target = System.out
log4j.appender.stdout.layout = org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern = [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} method:%l%n%m%n

### 输出DEBUG 级别以上的日志到=D://logs/error.log ###
log4j.appender.D = org.apache.log4j.DailyRollingFileAppender
log4j.appender.D.File = D://logs/log.log
log4j.appender.D.Append = true
log4j.appender.D.Threshold = DEBUG 
log4j.appender.D.layout = org.apache.log4j.PatternLayout
log4j.appender.D.layout.ConversionPattern = %-d{yyyy-MM-dd HH:mm:ss}  [ %t:%r ] - [ %p ]  %m%n

### 输出ERROR 级别以上的日志到=D://logs/error.log ###
log4j.appender.E = org.apache.log4j.DailyRollingFileAppender
log4j.appender.E.File =E://logs/error.log 
log4j.appender.E.Append = true
log4j.appender.E.Threshold = ERROR 
log4j.appender.E.layout = org.apache.log4j.PatternLayout
log4j.appender.E.layout.ConversionPattern = %-d{yyyy-MM-dd HH:mm:ss}  [ %t:%r ] - [ %p ]  %m%n 
```



Log4j日志转换为logback在线工具（支持log4j.properties转换为logback.xml,不支持 log4j.xml转换为logback.xml） 

https://logback.qos.ch/translator/



## SpringBoot2.x整合LogBack



### 官网说明：

#### 1. 文档地址：

官网介绍：https://docs.spring.io/spring-boot/docs/2.1.0.BUILD-SNAPSHOT/reference/htmlsingle/#boot-features-logging



> By default, if you use the “Starters”, Logback is used for logging. Appropriate Logback routing is also included to ensure that dependent libraries that use Java Util Logging, Commons Logging, Log4J, or SLF4J all work correctly.



#### 2. 日志格式：

官网包含默认输出的日志格式和代表含义：

如：

`2014-03-05 10:57:51.253  INFO 45469 --- [ost-startStop-1] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1358 ms`



The following items are output:

- Date and Time: Millisecond precision and easily sortable.
- Log Level: `ERROR`, `WARN`, `INFO`, `DEBUG`, or `TRACE`.
- Process ID.
- A `---` separator to distinguish the start of actual log messages.
- Thread name: Enclosed in square brackets (may be truncated for console output).
- Logger name: This is usually the source class name (often abbreviated).
- The log message.



如果需要以debug级别输出：`java -jar app.jar --debug`或者在application.properties里面配置debug=true



#### 3. 基本配置：

application.properties/yml里面可以添加的配置包括（用到哪里写哪些，后面补充）：

- 日志文件输出：

  ```properties
  logging.file=
  #or
  logging.path=
  ```

  

- 日志登记：

  ```properties
  logging.level.root=WARN
  logging.level.org.springframework.web=DEBUG
  logging.level.org.hibernate=ERROR
  ```

- 使用自定义xml文件：

  ```properties
  #推荐的文件名称
  logging.config=classpath:project-name-logback-spring.xml
  ```



### 实际操作

比较完整的样例：



示例代码：https://github.com/zrinGithub/kafka-demo.git



对于具体的配置，参考：

 logback手册：http://logback.qos.ch/manual/index.html



#### 1. 应用

```java
@Slf4j
@RestController
public class LogController {
    @GetMapping("test/log")
    public void testLogLevel(){
        log.error("----------LOGGER error----------");
        log.warn("----------LOGGER warn----------");
        log.info("----------LOGGER info----------");
        log.debug("----------LOGGER debug----------");
        log.trace("----------LOGGER trace----------");
    }
}
```



#### 2. logback-spring.xml

kafka-demo-logback-spring.xml配置：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>

    <appender name="consoleApp" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>
                %date{yyyy-MM-dd HH:mm:ss.SSS} %-5level[%thread]%logger{56}.%method:%L -%msg%n
            </pattern>
        </layout>
    </appender>

    <appender name="fileInfoApp" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>DENY</onMatch>
            <onMismatch>ACCEPT</onMismatch>
        </filter>
        <encoder>
            <pattern>
                %date{yyyy-MM-dd HH:mm:ss.SSS} %-5level[%thread]%logger{56}.%method:%L -%msg%n
            </pattern>
        </encoder>
        <!-- 滚动策略 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 路径 -->
            <fileNamePattern>app_log/log/app.info.%d.log</fileNamePattern>
        </rollingPolicy>
    </appender>

    <appender name="fileErrorApp" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder>
            <pattern>
                AAA %date{yyyy-MM-dd HH:mm:ss.SSS} %-5level[%thread]%logger{56}.%method:%L -%msg%n
            </pattern>
        </encoder>

        <!-- 设置滚动策略 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 路径 -->
            <fileNamePattern>app_log/log/app.err.%d.log</fileNamePattern>

            <!-- 控制保留的归档文件的最大数量，超出数量就删除旧文件，假设设置每个月滚动，
            且<maxHistory> 是1，则只保存最近1个月的文件，删除之前的旧文件 -->
            <MaxHistory>1</MaxHistory>

        </rollingPolicy>
    </appender>
    <root level="INFO">
        <appender-ref ref="consoleApp"/>
        <appender-ref ref="fileInfoApp"/>
        <appender-ref ref="fileErrorApp"/>
    </root>
</configuration>
```



#### 4. application.yml

application.yml指定日志配置位置：

```yml
logging:
  config: classpath:kafka-demo-logback-spring.xml
```



