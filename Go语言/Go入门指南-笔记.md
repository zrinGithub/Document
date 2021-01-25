# Go入门指南 笔记

本文为[Go入门指南](https://www.ituring.com.cn/book/1205)的笔记



## 1.相关

### 资料

[Go官网](https://golang.org/)

[Go语言编码规范](https://golang.org/ref/spec)

[The Way to Go源码](https://sites.google.com/site/thewaytogo2012/Downhome/Topic3)

[FAQ](https://golang.org/doc/faq)

[Getting Started](https://golang.org/doc/install)

### 应用场景

作为静态语言仍然能够快速编译。

用于实现复杂事件处理，即海量的并行支持，高度抽象化和高性能。

因为垃圾回收和内存分配的原因，不适用于实时性高的软件。



[一些使用Go的实际项目](http://go-lang.cat-v.org/organizations-using-go)



### 特性

下面是书中总结的特性：

> 这里列举一些 Go 语言的必杀技：
>
> - 简化问题，易于学习
> - 内存管理，简洁语法，易于使用
> - 快速编译，高效开发
> - 高效执行
> - 并发支持，轻松驾驭
> - 静态类型
> - 标准类库，规范统一
> - 易于部署
> - 文档全面
> - 免费开源



## 安装

这里只写Windows下的安装：



### 安装包

[下载安装包](https://golang.org/dl/)

按照默认路径安装，不用修改环境变量（Path都给你配好了）。



### 环境变量

查看所有环境变量：`go env`

查看指定环境变量：`go env GOPATH`

我安装的版本（1.14.4）环境变量没啥可以修改的，可以看这篇文章[Golang环境变量设置详解](https://juejin.im/post/5cac9b73e51d456e8c1d3bfc)来进行配置。



下面是文章介绍的变量：

```shell
#go语言安装路径
GOROOT=c:\go

#开发工作区，可以设置多个export GOPATH=/opt/go;$home/go
#用于存储源码、测试文件、库静态文件、可执行文件的工作
GOPATH=C:\Users\userName\go


```







### 测试

测试：`go run hello_world.go`

```go
package main

func main() {
	println("Hello", "world")
}
```



### 安装目录

> 你的 Go 安装目录（`$GOROOT`）的文件夹结构应该如下所示：
>
> README, AUTHORS, CONTRIBUTORS, LICENSE
>
> - `\bin` 包含可执行文件，如：编译器，Go 工具
> - `\doc` 包含示例程序，代码工具，本地文档等
> - `\include` 包含 C/C++ 头文件
> - `\lib` 包含文档模版
> - `\misc` 包含与支持 Go 编辑器有关的配置文件以及 cgo 的示例
> - `\pkg\os_arch` 包含标准库的包的对象文件（`.a`）
> - `\src` 包含源代码构建脚本
> - `\src\cmd` 包含 Go 和 C 的编译器和命令行脚本
> - `\src\lib9` `\src\libbio` `\src\libmach` 包含 C 文件
> - `\src\pkg` 包含 Go 标准库的包的完整源代码（Go 是一门开源语言）



## 基本结构和数据类型

### 文件名、关键字和标识符

文件名：小写、.go为后缀、不含空格和特殊符号、下划线分割，如`hello_world.go`



代码区分大小写



变量名值得注意的是，`_`是特殊的空白标志符，可以赋值但是discard



关键字或者保留字

| -------- | ----------- | ------ | --------- | ------ |
| -------- | ----------- | ------ | --------- | ------ |
| break    | default     | func   | interface | select |
| case     | defer       | go     | map       | struct |
| chan     | else        | goto   | package   | switch |
| const    | fallthrough | if     | range     | type   |
| continue | for         | import | return    | var    |



36 个预定义标识符：

| ------ | ------- | ------- | ------- | ------ | ------- | --------- | ---------- | ------- |
| ------ | ------- | ------- | ------- | ------ | ------- | --------- | ---------- | ------- |
| append | bool    | byte    | cap     | close  | complex | complex64 | complex128 | uint16  |
| copy   | false   | float32 | float64 | imag   | int     | int8      | int16      | uint32  |
| int32  | int64   | iota    | len     | make   | new     | nil       | panic      | uint64  |
| print  | println | real    | recover | string | true    | uint      | uint8      | uintptr |



### 基本结构

导包和java一样，函数和c一样：

```go
package main

import "fmt"

func main() {
    fmt.Println("hello, world")
}
```

