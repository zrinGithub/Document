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
        try (RandomAccessFile accessFile = new RandomAccessFile("test1.txt", "rw")) {
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

   

7. **标记与复位**

   通过mark()设定当前位置（`mark = position;`）可以使用reset()把`position=为mark`。
   
   
   
8. **比较**

   `equals()`比较buffer剩余元素是否相同

   `compareTo()`

   

## 四. Scatter/Gather

scatter,gather分别对应读写两个概念，scatter read从通道读取数据到多个buffer，gather write标识多个buffer写数据到channel。





### scatter read

   ![scatter](.\image\scatter.png)



```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

ByteBuffer[] bufferArray = { header, body };

channel.read(bufferArray);
```

scatter会首先填充第一个buffer，再移动到下一个buffer。所以有定长的header的时候，可以使用scatter来操作。



### gather write

![gather](.\image\gather.png)

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

//write data into buffers

ByteBuffer[] bufferArray = { header, body };

channel.write(bufferArray);
```

写入的时候如果header只有1byte数据，那么只有1byte的数据会真正写入，所以gather适用于不固定长度数据的写入。



## 五. Channel 到 Channel 传输

NIO里面你可以直接把数据从Channel转移到另一个Channel里面。



`transferFrom()`：FileChannel--->目标FileChannel

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();

toChannel.transferFrom(fromChannel, position, count);
```

SocketChannel只会存储准备就绪的数据。



`transferTo()`只是调用方发生改变：

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel	fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();

fromChannel.transferTo(position, count, toChannel);
```



## 六. Selector

Selector用于检验多个通道是否就绪。

![overview-selectors](.\image\overview-selectors.png)



### 使用场景

selector用于使用单线程管理多个Channel。单线程减少了线程切换的消耗。



然而在现在的操作系统，CPU一般是多核并且支持多任务操作，在这种情况下，不利用多线程反而会浪费CPU的性能。



### 操作

#### 1.创建Selector:

`Selector selector = Selector.open();`



#### 2. 注册Channel到Selector:

Channel是非阻塞的，这里是使用的SelectableChannel的方法，Socket channel可以切换为非阻塞模式。

```java
channel.configureBlocking(false);
//注册到
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
```

SelectionKey一共有四种状态常量，在这里标识你关注的事件：

- OP_CONNECT：channel与server连接成功->连接就绪。

- OP_ACCEPT：channel接收请求连接

- OP_READ：有数据可读

- OP_WRITE：能够进行数据写入

如果需要多个事件，使用

`int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;`

  

注册的返回值为SelectionKey，这个对象包含了一些属性：

- The interest set
- The ready set
- The Channel
- The Selector
- An attached object (optional)



下面分别介绍一下：

##### The interest set

我们关注的事件集合，可以通过与运算获取。

```java
int interestSet = selectionKey.interestOps();

boolean isInterestedInAccept  = interestSet & SelectionKey.OP_ACCEPT;
boolean isInterestedInConnect = interestSet & SelectionKey.OP_CONNECT;
boolean isInterestedInRead    = interestSet & SelectionKey.OP_READ;
boolean isInterestedInWrite   = interestSet & SelectionKey.OP_WRITE;  
```



##### The ready set

在选择channel之后可以查看channel处于就绪的状态集合。

```java
int readySet = selectionKey.readyOps();
//可以像interestSet那样通过与操作，也可以直接调用方法获取指定状态
selectionKey.isAcceptable();
selectionKey.isConnectable();
selectionKey.isReadable();
selectionKey.isWritable();
```



##### Channel Selector

获取方式：

```java
Channel  channel  = selectionKey.channel();

Selector selector = selectionKey.selector();    
```



##### Attaching Objects

给SelectionKey附加一个Object，这样可以方便我们识别一个Channel，或者为Channel添加更多的信息。

附加Object：

```java
selectionKey.attach(theObject);

Object attachedObj = selectionKey.attachment();
```



或者直接在调用注册方法的时候附带：

```java
SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);
```



#### 3. 通过Selector选择Channel

当注册了Channel之后到Selector之后，注册的时候设定了关注的事件（interest event:connect、accept、read、write）。

这样当你通过select()方法选择的时候，选择的就是对应事件就绪(event ready)的Channel。



几种选取方式：

- `int select()`一直阻塞直到获取一个channel就绪关注事件。
- `int select(long timeout)`同上，但是限制了阻塞时间。
- `int selectNow()`不阻塞直接返回。

返回值表示就绪的Channel数量



#### 4. selectedKeys()

使用`select()`获取了就绪的Channel之后，可以使用Selector的`selectedKeys()`通过一个选中的key值set来操作就绪的Channel。

```java
//获取就绪的SelectionKey列表
Set<SelectionKey> selectedKeys = selector.selectedKeys();    
Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
//可以通过遍历来获取指定事件就绪的Channel
while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    //处理完Channel之后，必须要做移除。在下一次该Channel就绪之后，Selector会自动把它加进来
    keyIterator.remove();
}
```



拿到指定的SelectionKey之后，可以通过`SelectionKey.channel()`并转为指定的Channel类型来进行操作。



#### 5. 唤醒等待获取Channel的线程

当线程调用`select()`方法开始阻塞之后，其他线程可以使用`Selector.wakeup()`方法来唤醒该线程，被唤醒的线程的`select()`直接返回。



#### 6. 关闭Selector

调用`close()`方法来关闭Selector，调用后Selector关闭，Selector上面注册的所有SelectionKey实例将会失效。

而Channel本身不会被关闭。



### 完整Selector操作示例

```java
//创建Selector
Selector selector = Selector.open();
//注册Channel到Selector
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

while(true) {
	//立刻获取当前就绪的Channel
  	int readyChannels = selector.selectNow();
  	if(readyChannels == 0) continue;
	//获取Channel
  	Set<SelectionKey> selectedKeys = selector.selectedKeys();
  	Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
  	while(keyIterator.hasNext()) {
    	SelectionKey key = keyIterator.next();

    	if(key.isAcceptable()) {
        	// a connection was accepted by a ServerSocketChannel.
    	} else if (key.isConnectable()) {
       		// a connection was established with a remote server.
    	} else if (key.isReadable()) {
        	// a channel is ready for reading
    	} else if (key.isWritable()) {
        	// a channel is ready for writing
    	}
    	keyIterator.remove();
  	}
}
```



## 七. FileChannel

有了FileChannel，你不必再使用标准的java IO API来与文件进行交互。



### 操作

#### 1.打开FileChannel

通过InputStream，OutputStream或者RandomAccessFile获取Channel。

```java
//RandomAccessFile获取channel
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

//FileInputStream里面的getChannel()方法
public FileChannel getChannel() {
    ......
}
```



#### 2.通过FileChannel读取数据

```java
ByteBuffer buf = ByteBuffer.allocate(48);
//把数据通过Channel读取进buffer
int bytesRead = inChannel.read(buf);
```

如果返回值bytesRead是-1，表示已经到达文件尾。



#### 3.通过FileChannel写数据

```java
String newData = "New String to write to file..." + System.currentTimeMillis();
//buffer填充数据
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
//buffer转为写模式
buf.flip();
//无法确定每次写入多少byte，所以使用循环读取
while(buf.hasRemaining()) {
	//把buffer中的数据写入channel中
    channel.write(buf);
}
```



#### 4.关闭FileChannel

```java
channel.close();    
```



#### 5.FileChannel 位置

当你通过FileChannel在指定位置进行读写操作的时候，可以调用`position()`获取当前位置。

```java
public abstract long position() throws IOException;
public abstract FileChannel position(long newPosition) throws IOException;
```



应用：

```java
//如果到文件尾了，返回-1
long pos = channel.position();
//如果不进行边界监测的话，文件会继续扩充读取磁盘的下一位置的物理文件
channel.position(pos +123);
```



#### 6.FileChannel 大小

```java
long fileSize = channel.size();    
```



#### 7.FileChannel截取

如果截取的size大于文件大小，则没有改变，如果小于则边界的数据被丢弃。

```java
//参数为byte
channel.truncate(1024);
```



#### 8.FileChannel缓存强制写入

使用`force()`将缓存中的数据强制写入磁盘。

```java
//参数为true表示文件的数据与元数据都要强制刷入
channel.force(true);
```



## 八. SocketChannel

SocketChannel是连接到TCP网络的通道。

有两种创建SocketChannel的方式：

- 调用SocketChannel的`open`方法，然后连接到网络上的服务器
- 请求连接到SeverSocketChannel的时候，一个SocketChannel可以被创建



### 操作方式

#### 1.打开SocketChannel

```java
SocketChannel socketChannel = SocketChannel.open();
socketChannel.connect(new InetSocketAddress("http://ip", 80));
```



#### 2.关闭SocketChannel

```java
socketChannel.close();   
```



#### 3.通过SocketChannel读取数据

首先buffer开辟内存空间，之后SocketChannel接收到的数据传输到buffer中。

```java
ByteBuffer buf = ByteBuffer.allocate(48);
//读取数据，如果返回-1则说明到达了数据流的结尾，也就是连接已经关闭。
int bytesRead = socketChannel.read(buf);
```



#### 4.向SocketChannel写数据

```java
String newData = "New String to write to file..." + System.currentTimeMillis();
//向buffer填充数据
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
//buffer切换为写模式，也就是position=0，limit=之前position的位置
buf.flip();
//向channel写数据，循环判断buffer中是否有数据残留
while(buf.hasRemaining()) {
    channel.write(buf);
}
```





#### 5.非阻塞模式

使用异步的方式调用`connect()`，`read()`，`write`这些方法。



##### connect()

```java
//设置非阻塞方式
socketChannel.configureBlocking(false);
//创建连接，因为是异步执行的，所以可能会在创建连接之前方法就返回了
socketChannel.connect(new InetSocketAddress("http://ip", 80));
//在循环里面判断连接是否已经创建
while(!socketChannel.finishConnect() ){
    //wait, or do something else...    
}
```



##### write()

调用`write()`可能在真正写入数据之前方法就返回了，但是因为我们是放到循环里面的，所以操作没什么不同。



##### read()

调用`read()`可能在真正有数据读出之前方法就返回了，但是我们一般会放在循环里面，并把返回的`int`值作为判断的依据，所以也没有什么不同。



##### 非阻塞模式下的Selector

在非阻塞模式下，Selector可以与SocketChannel配合，通过向一个Selector注册多个SocketChannel达到了单线程操作多通道读写的能力。



## 九. ServerSocketChannel

ServerSocketChannel用于监听TCP连接请求。



### 操作方式

示例：

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));

while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();
    //do something with socketChannel...
}
```



##### 打开ServerSocketChannel

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
```









