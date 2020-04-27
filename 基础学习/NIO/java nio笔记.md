# java NIO笔记

参考文档：

[Java NIO Tutorial](http://tutorials.jenkov.com/java-nio/index.html)

[Java NIO — Buffer 的基本觀念與使用方式](https://medium.com/@clu1022/java-nio-buffer-c98b52fd93ca)



代码地址：https://github.com/zrinGithub/work-util.git

## 一. 特性

NIO作为java IO与java Networking API的替代方案。



包含以下特性：

- Channels and Buffers：
  - 使用通道与缓存，而不是字符流字节流
  - 读：channel->buffer
  - 写：buffer->channel

![overview-channels-buffers](.\image\overview-channels-buffers.png)

- Non-blocking
  - 非阻塞，读取数据同时能够进行其他操作

- Selector
  - Selector监控多条Channel的事件（like: connection opened, data arrived etc.），因此单线程能够处理多条通道数据。

![overview-selectors](.\image\overview-selectors.png)



## 二. Channel

### 特性

Channel与Stream类似但具有以下的不同：

- Stream只能单向读写，Channel可以读也可以写
- Channel可以异步操作
- Channel的目的是Buffer



### 实现类

一些重要的实现类：

- FileChannel：文件读写
- DatagramChannel：通过UDP读写网络数据
- SocketChannel：通过TCP读写网络数据
- ServerSocketChannel：允许我们监听TCP连接请求，每一次连接请求创建一个SocketChannel





测试代码：

```java
public class ChannelDemo {

    public static void main(String[] args) {
        try (RandomAccessFile accessFile = new RandomAccessFile("D:\\code\\util\\work-util\\src\\main\\resources\\test1.txt", "rw")) {
            //获取输入通道
            FileChannel inChannel = accessFile.getChannel();
            //开辟空间，参数：The new buffer's capacity, in bytes
            //这里每次读取一个字节
            ByteBuffer buffer = ByteBuffer.allocate(1);

            int bytesRead = inChannel.read(buffer);
            while (bytesRead != -1) {
                System.out.println("read :" + bytesRead);
                //flip是为了后面读取数据
                buffer.flip();

                while (buffer.hasRemaining())
                    System.out.println((char) buffer.get());

                buffer.clear();
                bytesRead = inChannel.read(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



## 三. Buffer

### 实现类

- ByteBuffer
- MappedByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer





### Capacity,Position,Limit

buffer本质上是一块内存区域，你可以把数据写入并再次读取。NIO的Buffer对象包装了对这一块内存的操作。



为了更好的理解buffer的应用，你需要理解这些底层的机制：

- capacity
- position
- limit



capacity标识这当前内存的固定长(fixed size)，而position和limit 则在读写模式不同的情况下含义不同。



下面两图是写结束和读取开始的示意图：



![buffers-modes](.\image\buffers-modes.png)

- 写模式：

  - position:表示当前写的位置，可以看做指针

    开始前：position=0 

    读取xbyte：position=x（最大可以到capacity-1）

  - limit:表示你能够写入的最大容量，在写模式中等于capacity

- 读模式：

  - position:表示当前读取的位置
  - limit:表示当前数据你可以读取的定长

- buffer.flip做了什么：

  切换写模式到读模式，position重置为0，limit设置为写的最后位置（也就是写模式最后position的位置）。





### Buffer的基本使用

1. **开辟内存空间：**

   开辟1byte大小空间

   `ByteBuffer buffer = ByteBuffer.allocate(1);`

   开辟1个字符大小空间

   `CharBuffer buffer = CharBuffer.allocate(1);`

   

2. **写数据到Buffer：**

   1. 通过Channel写数据到Buffer

      `int bytesRead = inChannel.read(buf);`

   2. 直接使用buffer.put()写入数据

      `buffer.put()`

   

3. **调用buffer.flip()**

   因为我们从Buffer的`写模式`切换到`读模式`，具体细节在后面

   

4. **从Buffer中读取数据**

   1. 通过Channel读取数据

      `int bytesWritten = inChannel.write(buf);`

   2. 通过buffer读取数据

      `byte aByte = buf.get();`    

      

   

5. **重新读取**

   `buffer.rewind()`设置position=0，也就是可以再读取一次数据。

   

6. **清空数据（以便继续写数据，相当于读模式切换到了写模式）**

   1. `clear()`清空所有的数据，`position=0,limit=capacity`，数据还是存在的，只是相当于指针的位置变了。
   2. `compact()`仅清空你已经读取的数据。

   

7. 通过mark()设定当前位置（`mark = position;`）可以使用reset()把`position=为mark`。

   











