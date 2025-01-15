# Gitlet Design Document

**Name**: Grey Qian

## Classes and Data Structures

### Commit
> 用于实现commit这个类

#### 字段

1. timestamp：时间戳
2. message：提交的日志信息
3. directParentID：直接的父亲
4. otherParentID：用于合并的第二个分支的父亲
5. contentMap：这个提交包括了哪些文件以及对应的文件名映射关系


### Repository
> 用于实现所有功能的部分，每个功能都为一个单独的函数

### MyUtils
> 用于提供一些对文件内容进行操作的一些辅助函数


## Persistence
使用下面的文件目录结构：
- .gitlet: 用于存储git的信息
  - blobs:
    - 文件目录用于存储保存的二进制文件，这些文件的文件名为依据其文件内容产生的hashcode
  - commits:
    - 文件目录用于存储commit对象，文件名为依据commit的部分内容产生的hashcode
  - branches:
    - 是一个序列化的hashmap对象用于存放分支名以及其对应的最近的commit的id
  - stageAdd:
    - 是一个序列化的hashmap对象用于存放需要添加的暂存区的文件名以及其对应的id，文件存放于blobs中
  - stegeRemoval:
    - 是一个序列化的hashmap对象用于存放需要删除的暂存区的文件名以及其对应的id，文件存放于blobs中
  - HEAD:
    - 用于存放当前指向的分支以及对应的commitID
- working space: 工作区


## testing
下面是编写测试的一些规则：

```text
# ...  A comment, producing no effect.注释内容
I FILE 类似于C语言中的#include, 将 FILE 的内容插入到当前位置, FILE 的路径是相对于当前 .in 文件所在目录。
C DIR 创建一个名为 DIR 的子目录（如果不存在）,并且切换当前目录到DIR目录，如果参数省略， 切换到默认的主目录
T N 设置剩余测试步骤中 gitlet 命令的超时时间为 N 秒。用于限制长时间运行的命令，避免测试卡住。
+ NAME F 从 src 目录中复制文件 F 的内容到当前目录下的新文件 NAME 中。
- NAME 删除当前目录中名为 NAME 的文件。
= NAME F 检查当前目录中 NAME 文件的内容是否与 src/F 文件一致。
* NAME 检查文件 NAME 是否不存在。用于确保程序正确删除了指定文件。
E NAME 检查文件或目录 NAME 是否存在。
> COMMAND OPERANDS
LINE1
LINE2
...
<<<
执行命令 gitlet.Main，参数为 COMMAND OPERANDS。运行结束后，将输出与 LINE1、LINE2 等预期输出逐行比较。如果 <<< 后跟 *，表示 LINE1、LINE2 是正则表达式，用于匹配输出。
D VAR "VALUE" 定义变量 VAR，赋值为 VALUE。
```

> 实际上测试才是这里最难的部分之一，学会编写好的测试，测试驱动开发，能极大的减轻自己的负担。
> 当你编写了一大堆自己没有信心的代码的时候，这个时候，你应该放弃写代码，开始编写测试，并且让自己对自己的代码有信心。

## 总结
这个项目大概花了四五天的时间吧，差不过60小时的工作量，应该是目前做过最大的一个项目了。
项目的难点在于以下几点：
1. 需要自己独立设计项目的结构，设计一个好的持久化储存文件的方式能很好的简化自己的工作量
2. 需要自己编写测试，每当我信心满满的认为自己的代码没有问题的时候，跑官方测试总是会出现各种小bug，其最本质的原因还是在于对于命令的一些情况的理解不透彻，**因此， 本项目对我来说最大的提升在于，让我学会了一点，先将问题弄清楚之后，在下手写测试，之后才是写代码验证自己的想法，测试驱动开发。**
3. 项目比较大，建议分模块编写，比如我就将一些小的存储文件的工具放置在了一个专门的类里，并且设置Commit类，来分担一部分的内容，**模块化和Helper Function能够帮我们将问题分解，并且再后续的Debug阶段会轻松很多。**

最后这个项目教会了我很多东西，Just Do It, Keep Going!