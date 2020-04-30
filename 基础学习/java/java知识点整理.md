### 一. 知识点分析

#### 1. 介绍

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

  

   

### 二. 编程基础

#### 1. 运算符



##### 运算符 &和&&、|和||的区别吗？



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



##### 用最有效率的方法计算2乘以8



原理：**将一个数左移n位，相当于乘以2的n次方，位运算是CPU直接支持的，所以效率高**
答案：2<<3



例子：

常见的JDK源码里面HashMap的默认容量16
int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

直接是二进制操作了，表示1左移4位，变成10000，转为10进制也就是16, 直接以二进制形式去运行，效率更高



##### 位移

<< 左移，x<<1相当于x乘以2

\>>右移，x<<1相当于x除以以2

\>>>无符号右移，忽略符号位，空位补0



##### 传递两个非0的int数值进去，实现变量交换的方式

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




  


#### 2. 数据类型

##### 数据类型分类



### 3. try-catch-finally

##### try-catch-finally里面都有return，会返回什么？

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



