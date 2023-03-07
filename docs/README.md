# 轻量级分布式 RPC 框架

```
TODO We may don't need to reconnect remote server if the server'IP and server'port are not changed
```

## 基于Tcp协议与基于Http协议的RPC

### 基于TCP协议的RPC

在Java中，可以利用Socket API实现基于TCP协议的RPC调用，由服务的调用方与服务的提供方建立Socket连接，并由服务的调用方通过Socket将需要调用的接口名称、方法名称和参数序列化后传递给服务的提供方，服务的提供方反序列化后再利用反射调用相关的方法，最后将结果返回给服务的调用方。整个基于TCP协议的PRC调用大致如此，但是在实例应用中则会进行一系列的封装，譬如RMI便是在TCP协议上传递可序列化的java对象。

### 基于HTTP协议的RPC

而基于HTTP协议的RPC调用则更像是我们访问网页一样，只是它的返回结果更加单一简单。其大致流程为：由服务的调用者向服务的提供者发送请求，这种请求的方式可能是GET、POST、PUT、DELETE等中的一种（服务的提供者可能会根据不同的请求方式做出不同的处理，或者某个方法只允许某种请求方式），而调用的具体方法则根据URL进行方法调用，而方法所需参数则可能是对服务调用方传输过去的XML数据或JSON数据解析后的结果，最后返回JOSN或XML的数据结果（这需要根据实际应用定义相关的协议）。由于目前有很多开源的WEB服务器，如Tomcat，JBoss等，所以其实现起来更加容易（就跟做Web项目一样）。



## 介绍

- 异步调用，支持Future机制，支持回调函数Callback
- 基于时间差实现心跳检测，不仅限于Tcp协议（？？？？）
- 支持多种序列化机制，包括Protostuff、Kryo、Hessian
- 支持多种负载均衡策略，包括一致性哈希、LFU、LRU、随机、轮询
- 解决Tcp导致的拆包和粘包
- 使用Cglib解决JDK反射导致的性能不佳
- Server端支持线程池异步处理
- 客户端和服务端均支持异步处理
- 客户端使用代理模式调用服务
- 支持指定版本、方法名、多参数和lambda方式调用服务
- 客户端支持通过proxy支持同步调用，通过future支持异步调用





使用Cglib解决JDK反射导致的性能不佳

这句话有问题，错的



## 序列化方式

众所周知，TCP 是传输层协议，HTTP 是应用层协议，而传输层较应用层更加底层，在数据传输方面，越底层越快，因此，在一般情况下，TCP 一定比 HTTP 快。就序列化而言，Java 提供了默认的序列化方式，但在高并发的情况下，这种方式将会带来一些性能上的瓶颈，于是市面上出现了一系列优秀的序列化框架，比如：Protobuf、Kryo、Hessian、Jackson 等，它们可以取代 Java 默认的序列化，从而提供更高效的性能。



本rpc支持Protobuf、Kryo、Hessian、Jackson



## IO模型

###  AIO（Asynchronous IO）

从效率上来看，AIO 无疑是最高的，然而，美中不足的是目前作为广大服务器使用的系统 linux 对 AIO 的支持还不完善，导致我们还不能愉快的使用 AIO 这项技术，Netty 实际也是使用过 AIO 技术，但是实际并没有带来很大的性能提升，目前还是基于 Java NIO 实现的。



采用NIO



## 服务注册

![系统架构](http://static.oschina.net/uploads/space/2014/1229/002234_ENsM_223750.png)

该框架基于 TCP 协议，提供了 NIO 特性，提供高效的序列化方式，同时也具备服务注册与发现的能力。



根据以上技术需求，我们可使用如下技术选型：

1. Spring：它是最强大的依赖注入框架，也是业界的权威标准。
2. Netty：它使 NIO 编程更加容易，屏蔽了 Java 底层的 NIO 细节。
3. Protostuff：它基于 Protobuf 序列化框架，面向 POJO，无需编写 .proto 文件。
4. ZooKeeper：提供服务注册与发现功能，开发分布式系统的必备选择，同时它也具备天生的集群能力。



## Java并发编程

### CountDownLatch

```Java
public` `void` `await() ``throws` `InterruptedException { };  ``//调用await()方法的线程会被挂起，它会等待直到count值为0才继续执行
public` `boolean` `await(``long` `timeout, TimeUnit unit) ``throws` `InterruptedException { }; ``//和await()类似，只不过等待一定的时间后count值还没变为0的话就会继续执行
public` `void` `countDown() { }; ``//将count值减1
```



### CyclicBarrier

```java
public` `int` `await() ``throws` `InterruptedException, BrokenBarrierException { };
public` `int` `await(``long` `timeout, TimeUnit unit)``throws` `InterruptedException,BrokenBarrierException,TimeoutException { };
```

　从执行结果可以看出，在初次的4个线程越过barrier状态后，又可以用来进行新一轮的使用。而CountDownLatch无法进行重复使用。



### Semaphore

　　Semaphore翻译成字面意思为 信号量，Semaphore可以控同时访问的线程个数，通过 acquire() 获取一个许可，如果没有就等待，而 release() 释放一个许可。



## 实例化对象

Cglib的FastClass和objenesis

为什么这里要这么处理，不够明白fastclass



## 注解

### 对象

java.lang.annotation.ElementType.TYPE：类、接口（包括注解类型）和枚举的声明



### 生命周期

首先要明确生命周期长度 SOURCE < CLASS < RUNTIME ，所以前者能作用的地方后者一定也能作用。一般如果需要在运行时去动态获取注解信息，那只能用 RUNTIME 注解；如果要在编译时进行一些预处理操作，比如生成一些辅助代码（如 ButterKnife），就用 CLASS注解；如果只是做一些检查性的操作，比如 @Override 和 @SuppressWarnings，则可选用 SOURCE 注解。



## Logger

日志级别由高到底是：fatal -> error -> warn -> info -> debug,低级别的会输出高级别的信息，高级别的不会输出低级别的

常用的info，error，debug



每个Logger都被了一个日志级别（log level），用来控制日志信息的输出。日志级别从高到低分为：
A：off 最高等级，用于关闭所有日志记录。
B：fatal 指出每个严重的错误事件将会导致应用程序的退出。
C：error 指出虽然发生错误事件，但仍然不影响系统的继续运行。
D：warm 表明会出现潜在的错误情形。
E：info 一般和在粗粒度级别上，强调应用程序的运行全程。
F：debug 一般用于细粒度级别上，对调试应用程序非常有帮助。
G：all 最低等级，用于打开所有日志记录。

上面这些级别是定义在org.apache.log4j.Level类中。Log4j只建议使用4个级别，优先级从高到低分别是error,warn,info和debug。通过使用日志级别，可以控制应用程序中相应级别日志信息的输出。例如，如果使用b了info级别，则应用程序中所有低于info级别的日志信息(如debug)将不会被打印出来。





## TCP

![img](https://img2018.cnblogs.com/blog/1169746/201810/1169746-20181001222742129-1182603054.png)



## EventLoop

    DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

即默认为cpu对应线程数*2。



## bossGroup WorkerGroup

**重点！！！！**

**bossGroup线程的机制是多路复用，虽然是一个线程但是可以监听多个新连接；**

代码：  bootstrap.group(bossGroup, workerGroup) ; 

1、这个代码是不是就是初始化两个线程，线程已经开启了？

解惑：是初始化两个线程，一个线程负责接受新的连接，一个负责处理读写；是开启了；

2、bossGroup线程初始化以后再干嘛？

解惑：bossGroup线程的机制是多路复用，虽然是一个线程但是可以监听多个新连接；

举个例子：

         假设有A,B,C三个连接，先连接A，如果A还没连接上，立刻返回，不会一直等待，就不会阻塞其他客户端连接服务器；
    
          然后去连接B，B连接上了；
    
          再连接C，C连接还没到的话，立刻返回，不去等待，避免其他客户端连接不上阻塞；
    
         现在再返回连接A，A连接上了，就ok了；

核心思想：轮询，没有连接立刻返回不要耽误其他的客户端连接，不会阻塞；一个线程不会浪费资源；

3、这个代码怎么与多路复用selector怎么挂钩？

解惑：两个线程，一个是bossGroup，一个是workerGroup，他们的机制都是多路复用selector，一个线程监听多个客户端，看上面问题的例子，就知道什么是多路复用selector

4、bossGroup负责与客户端建立连接的，既然有客户端等待连接，就不存在连不上，怎么会需要多路复用轮询呢？

解惑： 需要用多路复用，服务器与客户端一对多，轮询的核心代码是for循环；

           既然存在客户端连接，不存在连接不上，轮询次数比较少（除非客户端有问题）；




## 线程

1) start：用start方法来启动线程，真正实现了多线程运行，这时无需等待run方法体代码执行完毕而直接继续执行下面的代码。通过调用Thread类的start()方法来启动一个线程，这时此线程处于就绪（可运行）状态，并没有运行，一旦得到cpu时间片，就开始执行run()方法，这里方法 run()称为线程体，它包含了要执行的这个线程的内容，Run方法运行结束，此线程随即终止。

2) run： run()方法只是类的一个普通方法而已，如果直接调用Run方法，程序中依然只有主线程这一个线程，其程序执行路径还是只有一条，还是要顺序执行，还是要等待run方法体执行完毕后才可继续执行下面的代码，这样就没有达到写线程的目的。
3) 总结：调用start方法方可启动线程，而run方法只是thread的一个普通方法调用，还是在主线程里执行。这两个方法应该都比较熟悉，把需要并行处理的代码放在run()方法中，start()方法启动线程将自动调用 run()方法，这是由jvm的内存机制规定的。并且run()方法必须是public访问权限，返回值类型为void。





## 异步绑定端口

```
//future用于异步操作的通知回调
ChannelFuture future = bootstrap.bind(port).sync();
//等待服务器监听端口关闭
future.channel().closeFuture().sync();
```

一定要sync，异步



## 服务注册

之前的版本注册完了没有注销，注册默认采用的是永久，所以需要注销



## Curator

### create node

节点存在时不可再次建立，否则会报异常

ZK的节点不允许递归建立，在上述代码中显示创建了持久节点 /5AnJam 再建立的节点 /5AnJam/test，但不可以省略第一步，直接创建 /5AnJam/Test

临时节点不可创建子节点，子节点只能创建在持久节点下

### 

工厂模式构造framework

```
CuratorTempFramework client = CuratorFrameworkFactory.builder()
    .connectString("127.0.0.1:2181")// 连接串
    .retryPolicy(new RetryNTimes(10, 5000))// 重试策略
    .connectionTimeoutMs(100) // 连接超时
    .sessionTimeoutMs(100) // 会话超时
    .buildTemp(100, TimeUnit.MINUTES); // 临时客户端并设置连接时间
```

节点名称用hashcode标示



#### 节点类型

```
public enum CreateMode {
    
    /**
     * The znode will not be automatically deleted upon client's disconnect.
     */
    PERSISTENT (0, false, false),
    /**
    * The znode will not be automatically deleted upon client's disconnect,
    * and its name will be appended with a monotonically increasing number.
    */
    PERSISTENT_SEQUENTIAL (2, false, true),
    /**
     * The znode will be deleted upon the client's disconnect.
     */
    EPHEMERAL (1, true, false),
    /**
     * The znode will be deleted upon the client's disconnect, and its name
     * will be appended with a monotonically increasing number.
     */
    EPHEMERAL_SEQUENTIAL (3, true, true);
```



### watch

```java
client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);
        build().getData().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("监听器watchedEvent：" + watchedEvent);
            }
        }).forPath(path);
```

watch只能监听一次，所以curator提供了cache机制



#### PathChildrenCacheListener

```
BUILD_INITIAL_CACHE：启动时，同步初始化cache，以及创建cache后，就从服务器拉取对应的数据。

POST_INITIALIZED_EVENT：启动时，异步初始化cache，初始化完成触发PathChildrenCacheEvent.Type#INITIALIZED事件，cache中Listener会收到该事件的通知。

最后是第一个枚举常量，NORMAL：启动时，异步初始化cache，完成后不会发出通知。
```

这里我们采用同步

```
PathChildrenCacheListener监听器接口中，也只定义了一个简单的方法 childEvent，当子节点有变化时，这个方法就会被回调到。

在创建完PathChildrenCacheListener的实例之后，需要将这个实例注册到PathChildrenCache缓存实例，使用缓存实例的addListener方法。 然后使用缓存实例nodeCache的start方法，启动节点的事件监听。
```



## ZK会话超时

所以这里也简单总结下，一旦发生会话超时，那么存储在ZK上的所有临时数据与注册的订阅者都会被移除，此时需要重新创建一个ZooKeeper客户端实例，需要自己编码做一些额外的处理。



## ZK重试策略

![image-20220830215202309](C:\Users\Charles\AppData\Roaming\Typora\typora-user-images\image-20220830215202309.png)



配置 次数，间隔时间，时间 最大最小

```
其参数除了连接字符串之外，还有如下是三个参数：
1：连接超时时间：如果配置了curator-default-connection-timeout参数，则取该参数值。 默认值是15秒
2：会话超时时间：如果配置了curator-default-session-timeout参数，则取该参数值。 默认值是60秒
3：重试策略。curator提供了如下重试策略：
3.1：RetryForever。一直重试，参数是重试间隔时间，单位毫秒
3.2：RetryOneTime。重试一次，参数是重试之间的间隔时间，单位毫秒。
3.3：RetryNTimes。重试N次, 参数是重试次数和重试之间的间隔时间，单位毫秒。
3.4：ExponentialBackoffRetry。参数为最大重试次数（默认值29），最大休眠时间，基本休眠时间。其休眠时间不确定。
3.5：BoundedExponentialBackoffRetry。该类继承了ExponentialBackoffRetry。
```



## ZK配置

超时时间的配置etc



## Json序列化数据

### jackson or fastjson？

fastjson速度更快，但难免有些bug，可能会在以后的拓展，定制化遇到问题。

因此使用jackson，同时，也是SpringMVC默认指定json解析工具



### jackson配置

```yml
spring:
  jackson:
    # 设置属性命名策略,对应jackson下PropertyNamingStrategy中的常量值，SNAKE_CASE-返回的json驼峰式转下划线，json body下划线传到后端自动转驼峰式
    property-naming-strategy: SNAKE_CASE
    # 全局设置@JsonFormat的格式pattern
    date-format: yyyy-MM-dd HH:mm:ss
    # 当地时区
    locale: zh_CN
    # 设置全局时区
    time-zone: GMT+8
    # 常用，全局设置pojo或被@JsonInclude注解的属性的序列化方式
    default-property-inclusion: NON_NULL #不为空的属性才会序列化,具体属性可看JsonInclude.Include
    # 常规默认,枚举类SerializationFeature中的枚举属性为key，值为boolean设置jackson序列化特性,具体key请看SerializationFeature源码
    visibility:
      #属性序列化的可见范围
      getter: non_private
      #属性反序列化的可见范围
      setter: protected_and_public
      #静态工厂方法的反序列化
      CREATOR: public_only
      #字段
      FIELD: public_only
      #布尔的序列化
      IS_GETTER: public_only
      #所有类型(即getter setter FIELD）不受影响,无意义
      NONE: public_only
      #所有类型(即getter setter FIELD）都受其影响（慎用）
      ALL: public_only
    serialization:
      #反序列化是否有根节点
      WRAP_ROOT_VALUE: false
      #是否使用缩进，格式化输出
      INDENT_OUTPUT: false
      FAIL_ON_EMPTY_BEANS: true # 对象不含任何字段时是否报错，默认true
      FAIL_ON_SELF_REFERENCES: true #循环引用报错
      WRAP_EXCEPTIONS: true #是否包装异常
      FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS: true #JsonUnwrapped标记的类有类型信息是否报错
      WRITE_SELF_REFERENCES_AS_NULL: false #循环引用返回null
      CLOSE_CLOSEABLE: true #若对象实现了CLOSEABLE接口，在序列化后是否调用Close方法
      FLUSH_AFTER_WRITE_VALUE: false #流对象序列化之后是否强制刷新
      WRITE_DATES_AS_TIMESTAMPS: true # 返回的java.util.date转换成时间戳
      WRITE_DATES_WITH_ZONE_ID: true #2011-12-03T10:15:30+01:00[Europe/Paris]带时区id
      WRITE_DURATIONS_AS_TIMESTAMPS: true #将DURATIONS转换成时间戳
      WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS: false #是否字符数组输出json数组 (false则输出字符串)
      WRITE_ENUMS_USING_TO_STRING: false # 将枚举输出toString
      WRITE_ENUMS_USING_INDEX: false #枚举下标
      WRITE_ENUM_KEYS_USING_INDEX: false #枚举key类似
      WRITE_NULL_MAP_VALUES: false #是否输出map中的空entry(此特性已过期，请使用JsonInclude注解)
      WRITE_EMPTY_JSON_ARRAYS: true # 对象属性值是空集合是否输出空json数组
      WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED: false #是否将单个元素的集合展开，（即：去除数组符号"[]"）
      WRITE_BIGDECIMAL_AS_PLAIN: false #是否调用BigDecimal#toPlainString()输出
      WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS: #将timestamp输出为纳秒
      ORDER_MAP_ENTRIES_BY_KEYS: false #map序列化后，是否用key对其排序
      EAGER_SERIALIZER_FETCH: true #是否马上获取序列化器
      USE_EQUALITY_FOR_OBJECT_ID: false #是否使用objectId比较是否相等（在ORM框架Hibernate中有应用）

    # 枚举类DeserializationFeature中的枚举属性为key，值为boolean设置jackson反序列化特性,具体key请看DeserializationFeature源码
    deserialization:
      USE_BIG_DECIMAL_FOR_FLOATS: false #将浮点数反序列化为BIG_DECIMAL
      USE_BIG_INTEGER_FOR_INTS: false #将整数反序列化为BIG_INTEGER
      USE_LONG_FOR_INTS: false #将整型反序列化为长整
      USE_JAVA_ARRAY_FOR_JSON_ARRAY: false #无明确类型时，是否将json数组反序列化为java数组（若是true，就对应Object[] ,反之就是List<?>）
      FAIL_ON_UNKNOWN_PROPERTIES: false # 常用,json中含pojo不存在属性时是否失败报错,默认true
      FAIL_ON_NULL_FOR_PRIMITIVES: false #将null反序列化为基本数据类型是否报错
      FAIL_ON_NUMBERS_FOR_ENUMS: false #用整数反序列化为枚举是否报错
      FAIL_ON_INVALID_SUBTYPE: false #找不至合适的子类否报错 （如注解JsonTypeInfo指定的子类型）
      FAIL_ON_READING_DUP_TREE_KEY: false #出现重复的json字段是否报错
      FAIL_ON_IGNORED_PROPERTIES: false #如果json中出现了java实体字段中已显式标记应当忽略的字段，是否报错
      FAIL_ON_UNRESOLVED_OBJECT_IDS: true #如果反序列化发生了不可解析的ObjectId是否报错
      FAIL_ON_MISSING_CREATOR_PROPERTIES: false #如果缺少静态工厂方法的参数是否报错（false,则使用null代替需要的参数）
      FAIL_ON_NULL_CREATOR_PROPERTIES: false #将空值绑定到构造方法或静态工厂方法的参数是否报错
      FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY: false #注解JsonTypeInfo.As#EXTERNAL_PROPERTY标记的属性缺失，是否报错
      FAIL_ON_TRAILING_TOKENS: false #出现尾随令牌是否报错（如果是true,则调用JsonParser#nextToken，检查json的完整性）
      WRAP_EXCEPTIONS: true #是否包装反序列化出现的异常
      ACCEPT_SINGLE_VALUE_AS_ARRAY: true #反序列化时是否将一个对象封装成单元素数组
      UNWRAP_SINGLE_VALUE_ARRAYS: false #反序列化时是否将单元素数组展开为一个对象
      UNWRAP_ROOT_VALUE: false #是否将取消根节点的包装
      ACCEPT_EMPTY_STRING_AS_NULL_OBJECT: false #是否将空字符("")串当作null对象
      ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT: false #是否接受将空数组("[]")作为null
      ACCEPT_FLOAT_AS_INT: true #是否接受将浮点数作为整数
      READ_ENUMS_USING_TO_STRING: false #按照枚举toString()方法读取，（false则按枚举的name()方法读取）
      READ_UNKNOWN_ENUM_VALUES_AS_NULL: false #读取到未知的枚举当作null
      READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE: false #读取到未知的枚举,将其当作被JsonEnumDefaultValue注解标记的枚举
      READ_DATE_TIMESTAMPS_AS_NANOSECONDS: true #将时间戳视为纳秒(false,则视为毫秒)
      ADJUST_DATES_TO_CONTEXT_TIME_ZONE: true #反序列化是否会适应DeserializationContext#getTimeZone()提供的时区 （此特性仅对java8的时间/日期有效）
      EAGER_DESERIALIZER_FETCH: true  #是否马上获取反序列化器
    # 枚举类MapperFeature中的枚举属性为key，值为boolean设置jackson ObjectMapper特性
    # ObjectMapper在jackson中负责json的读写、json与pojo的互转、json tree的互转,具体特性请看MapperFeature,常规默认即可
    mapper:
      USE_ANNOTATIONS: true #是否使用注解自省（检查JsonProperties这些）
      # 使用getter取代setter探测属性，这是针对集合类型，可以直接修改集合的属性
      USE_GETTERS_AS_SETTERS: true #默认false
      PROPAGATE_TRANSIENT_MARKER: false #如何处理transient字段，如果true(不能访问此属性) ，若是false则不能通过字段访问（还是可以使用getter和setter访问）
      AUTO_DETECT_CREATORS: true #是否自动检测构造方法或单参且名为valueOf的静态工厂方法
      AUTO_DETECT_FIELDS: true #是否自动检测字段 （若true,则将所有public实例字段视为为属性）
      AUTO_DETECT_GETTERS: true #确定是否根据标准 Bean 命名约定自动检测常规“getter”方法的（不包括is getter）
      AUTO_DETECT_IS_GETTERS: true #确定是否根据标准 Bean 命名约定自动检测“is getter”方法
      AUTO_DETECT_SETTERS: false # 确定是否根据标准 Bean 命名约定自动检测“setter”方法
      REQUIRE_SETTERS_FOR_GETTERS: false #getter方法必需要有对应的setter或字段或构造方法参数，才能视为一个属性
      ALLOW_FINAL_FIELDS_AS_MUTATORS: true #是否可以修改final成员字段
      INFER_PROPERTY_MUTATORS: true #是否能推断属性，(即使用字段和setter是不可见的，但getter可见即可推断属性)
      INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES: true #是否自动推断ConstructorProperties注解
      CAN_OVERRIDE_ACCESS_MODIFIERS: true #调用AccessibleObject#setAccessible设为true .将原来不可见的属性，变为可见
      OVERRIDE_PUBLIC_ACCESS_MODIFIERS: true #对所有的属性调用AccessibleObject#setAccessible设为true .（即使用是公共的）
      USE_STATIC_TYPING: false #序列化使用声明的静态类型还是动态类型  JsonSerialize#typing注解可覆盖它
      USE_BASE_TYPE_AS_DEFAULT_IMPL: false # 反序列化是否使用基本类作为默实现 @JsonTypeInfo.defaultImpl
      DEFAULT_VIEW_INCLUSION: true #没有JsonView注解标记的属性是否会被包含在json序列化视图中
      SORT_PROPERTIES_ALPHABETICALLY: false #按字母表顺序序列化字段（若false，按字段声明的顺序）
      ACCEPT_CASE_INSENSITIVE_PROPERTIES: false #反序列化属性时不区分大小写 （true时，会影响性能）
      ACCEPT_CASE_INSENSITIVE_ENUMS: false #枚举反序列化不区别大小写
      ACCEPT_CASE_INSENSITIVE_VALUES: false #允许解析一些枚举的基于文本的值类型但忽略反序列化值的大小写 如日期/时间类型反序列化器
      USE_WRAPPER_NAME_AS_PROPERTY_NAME: false # 使用包装器名称覆盖属性名称 AnnotationIntrospector#findWrapperName指定的
      USE_STD_BEAN_NAMING: false # 是否以强制与 Bean 名称自省严格兼容的功能，若开启后（getURL()）变成URL （jackson默认false, url）
      ALLOW_EXPLICIT_PROPERTY_RENAMING: false #是否允许JsonProperty注解覆盖PropertyNamingStrategy
      ALLOW_COERCION_OF_SCALARS: true # 是否允许强制使用文本标题 ，即将字符串的"true"当作布尔的true ,字符串的"1.0"当作"double"
      IGNORE_DUPLICATE_MODULE_REGISTRATIONS: true #如果模块相同（Module#getTypeId()返回值相同），只有第一次能会真正调用注册方法
      IGNORE_MERGE_FOR_UNMERGEABLE: true #在合并不能合并的属性时是否忽略错误
      BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES: false #阻止不安全的基类（如Object Closeable Cloneable AutoCloseable Serializable）
    parser:
      AUTO_CLOSE_SOURCE: true #是否自动关闭不属于解析器的底层输入流
      ALLOW_COMMENTS: false #是否允许json注解（Json规范是不能加注释的，但这里可以配置）
      ALLOW_YAML_COMMENTS: false #是否允许出现yaml注释
      ALLOW_UNQUOTED_FIELD_NAMES: false #是否允许出现字段名不带引号
      ALLOW_SINGLE_QUOTES: false # 是否允许出现单引号,默认false
      ALLOW_UNQUOTED_CONTROL_CHARS: false #是否允许出现未加转义的控制字符
      ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER: false #是否允许对所有字符都可加反斜杠转义
      ALLOW_NUMERIC_LEADING_ZEROS: false #是否允许前导的零 000001
      ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS: false #是否允许前导的小点数 如 ".04314"会被解析成"0.04314"
      ALLOW_NON_NUMERIC_NUMBERS: false #是否允许NaN型的浮点数 （"INF"当作正无穷  "-INF"当作负无穷 "NaN"非数字，类型于除数为0）
      ALLOW_MISSING_VALUES: false # 是否允许json数组中出现缺失值 （如["value1",,"value3",]将被反序列化为["value1", null, "value3", null]）
      ALLOW_TRAILING_COMMA: false # 是否允许json尾部有逗号 （如{"a": true,}）
      STRICT_DUPLICATE_DETECTION: false #是否启用严格的字段名重复检查（开启后会增加20-30%左右的性能开销）
      IGNORE_UNDEFINED: false #属性定义未找到是否报错（这不是针对json,是针对Avro, protobuf等需要Schema的格式）
      INCLUDE_SOURCE_IN_LOCATION: false #是否包含其源信息（如总字节数，总字符数 行号 列号 ）
    generator:
      AUTO_CLOSE_TARGET: true #是否自动关闭不属于生成器的底层输出流
      AUTO_CLOSE_JSON_CONTENT: true #是否自动补全json(当有不匹配的JsonToken#START_ARRAY JsonToken#START_OBJECT时)
      FLUSH_PASSED_TO_STREAM: true #是否刷新generator
      QUOTE_FIELD_NAMES: true #是否为字段名添加引号
      QUOTE_NON_NUMERIC_NUMBERS: true #对于NaN浮点数是否加引号
      ESCAPE_NON_ASCII: false #非ASCII码是否需要转义
      WRITE_NUMBERS_AS_STRINGS: false #将数字当作字符串输出 （防止Javascript长度限制被截断）
      WRITE_BIGDECIMAL_AS_PLAIN: false #按BigDecimal的toPlainString()输出
      STRICT_DUPLICATE_DETECTION: false #是否启用严格的字段名重复检查
      IGNORE_UNKNOWN: false #属性定义未找到是否报错（这不是针对json,是针对Avro, protobuf等需要Schema的格式）


```



## Thread

### **线程池ThreadPoolExecutor的工作流程**

![threadpoolExecutor.png](https://img-blog.csdnimg.cn/img_convert/17f4e4d6e798bee282f6f6ad30d56909.png)



### 用户线程和核心线程

#### 用户线程

通常在os之上的某个中间系统实现，由进程创建，用户可管理。

允许每个进程定制自己的调度算法，线程管理比较灵活。这就是必须自己写管理程序，与内核线程的区别。

创建和销毁线程、线程切换代价等线程管理的代价比内核线程少得多。

#### 核心线程

由os管控，os创建



### CoreSize

```

System.out.println(Runtime.getRuntime().availableProcessors()); //输出本机CPU的数量,是一个数字
```



计算密集型任务：特点是要进行大量的计算，消耗CPU资源。比如计算圆周率，对视频进行高清解码等，全靠CPU的运算能力。这种计算密集型任务虽然也可以用多任务完成，但是任务越多，花在任务切换的时间就越多，CPU执行任务的效率就越低，所以，要最高效的利用CPU，计算密集型任务同时进行的数量应当等于CPU的核心数。

计算密集型任务由于主要消耗CPU资源，因此，代码运行效率非常重要。例如脚本语言运行效率很低，完全不适合计算密集型任务。

**IO密集型任务： 涉及到网络，磁盘IO的任务都是IO密集型任务。这类任务的特点是CPU消耗很少，任务的大部分时间都在等待IO操作完成（因为 IO 的速度远远低于CPU和内存的速度）。对于IO密集型任务，任务越多，CPU效率越高，但也有一个限度。常见的大部分任务都是IO密集型任务，比如Web应用。**





### handler Policy

```
AbortPolicy: 默认饱和策略，丢弃任务并抛出RejectedExecutionException异常。调用者可以捕获这个异常并自行处理。

CallerRunsPolicy：线程池中不再处理该任务，由调用线程处理该任务。

DiscardPolicy：当任务无法添加到队列中等待执行时，DiscardPolicy策略会丢弃任务，并且不抛异常。

DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试提交新的任务。
```



### ConcurrentHashMap 

**在理想状态下，ConcurrentHashMap 可以支持 16 个线程执行并发写操作（如果并发级别设为16），及任意数量线程的读操作。**



## 序列化机制

|                        | **优点**                                 | **缺点**                                                     |
| ---------------------- | ---------------------------------------- | ------------------------------------------------------------ |
| **Kryo**               | 速度快，序列化后体积小                   | 跨语言支持较复杂                                             |
| **Hessian**            | 默认支持跨语言                           | 较慢                                                         |
| **Protostuff**         | 速度快，基于protobuf                     | 需静态编译                                                   |
| **Protostuff-Runtime** | 无需静态编译，但序列化前需预先传入schema | 不支持无默认构造函数的类，反序列化时需用户自己初始化序列化后的对象，其只负责将该对象进行赋值 |
| **Java**               | 使用方便，可序列化所有类                 | 速度慢，占空间                                               |

### Protostuff

每次获得schema都要重新生成，所以在内存中保存，提高性能

他做了，computeIfAbsent这个方法



### Kryo

在多线程环境中，在每个线程中创建 Kryo 实例的成本太高。

我将向您展示如何池化 Kryo 实例以及如何使用 Kryo 池对 Java 对象进行序列化和反序列化。

#### 线程不安全

Kryo 不是线程安全的。每个线程都应该有自己的 Kryo 对象、输入和输出实例。

因此在多线程环境中，可以考虑使用 ThreadLocal 或者对象池来保证线程安全性。



### Thrift

不支持复杂的java类，所以没有添加



## Suppress

@SuppressWarnings("unchecked")

告诉编译器忽略 unchecked 警告信息，如使用List，ArrayList等未进行参数化产生的警告信息。



## Objenesis

Java已经支持通过Class.newInstance()动态实例化Java类，但是这需要Java类有个适当的构造器。很多时候一个Java类无法通过这种途径创建，例如：

构造器需要参数
构造器有副作用
构造器会抛出异常
Objenesis可以绕过上述限制。它一般用于：

序列化、远程处理和持久化：无需调用代码即可将Java类实例化并存储特定状态。
代理、AOP库和Mock对象：可以创建特定Java类的子类而无需考虑super()构造器。
容器框架：可以用非标准方式动态实例化Java类。例如Spring引入Objenesis后，Bean不再必须提供无参构造器了。



## Netty解决粘包

本篇文章主要是介绍使用LengthFieldBasedFrameDecoder解码器自定义协议。通常，协议的格式如下:

![img](https://img2018.cnblogs.com/blog/1168971/201909/1168971-20190902155412644-364015440.png)

LengthFieldBasedFrameDecoder是netty解决拆包粘包问题的一个重要的类，主要结构就是header+body结构。我们只需要传入正确的参数就可以发送和接收正确的数据，那么重点就在于这几个参数的意义。下面我们就具体了解一下这几个参数的意义。先来看一下LengthFieldBasedFrameDecoder主要的构造方法：

```
public LengthFieldBasedFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip)
```

那么这几个重要的参数如下：

- maxFrameLength：最大帧长度。也就是可以接收的数据的最大长度。如果超过，此次数据会被丢弃。
- lengthFieldOffset：长度域偏移。就是说数据开始的几个字节可能不是表示数据长度，需要后移几个字节才是长度域。
- lengthFieldLength：长度域字节数。用几个字节来表示数据长度。
- lengthAdjustment：数据长度修正。因为长度域指定的长度可以使header+body的整个长度，也可以只是body的长度。如果表示header+body的整个长度，那么我们需要修正数据长度。
- initialBytesToStrip：跳过的字节数。如果你需要接收header+body的所有数据，此值就是0，如果你只想接收body数据，那么需要跳过header所占用的字节数。



## HeartBeat

### keepalive 

> 使用 TCP 协议层面的 keepalive 机制。在 Netty 中使用该策略：

```xml
.childOption(ChannelOption.SO_KEEPALIVE, true); 
```

在应用层上实现自定义的心跳机制.，虽然在 TCP 协议层面上, 提供了 keepalive 保活机制, 但是使用它有几个缺点:

它不是 TCP 的标准协议, 并且是默认关闭的.
TCP keepalive 机制依赖于操作系统的实现, 默认的 keepalive 心跳时间是 两个小时, 并且对 keepalive 的修改需要系统调用(或者修改系统配置), 灵活性不够.
TCP keepalive 与 TCP 协议绑定, 因此如果需要更换为 UDP 协议时, keepalive 机制就失效了.



### IdleStateHandler

心跳：一分半的间隔

- IdleStateHandler心跳检测主要是通过向线程任务队列中添加定时任务，判断channelRead()方法或write()方法是否调用空闲超时，如果超时则触发超时事件执行自定义userEventTrigger()方法；



## writeAndFlush

1.pipeline中的编码器原理是创建一个ByteBuf,将java对象转换为ByteBuf，然后再把ByteBuf继续向前传递
 2.调用write方法并没有将数据写到Socket缓冲区中，而是写到了一个单向链表的数据结构中，flush才是真正的写出
 3.writeAndFlush等价于先将数据写到netty的缓冲区，再将netty缓冲区中的数据写到Socket缓冲区中，写的过程与并发编程类似，用**自旋锁**保证写成功
 4.netty中的缓冲区中的ByteBuf为DirectByteBuf



## 反射

### cglib or jdk



## 设计模式

### 代理模式

### 工厂模式

### 适配器模式

   ChannelInboundHandler(入站): 处理输入数据和Channel状态类型改变。

​                   适配器: ChannelInboundHandlerAdapter（适配器设计模式）

​                   常用的: SimpleChannelInboundHandler

  ChannelOutboundHandler(出站): 处理输出数据

​                   适配器: ChannelOutboundHandlerAdapter



### 单例模式

```java
private static class SingletonHolder {
    private static final ConnectionManager instance = new ConnectionManager();
}

public static ConnectionManager getInstance() {
    return SingletonHolder.instance;
}
```

#### double check

类锁

```java
public static KryoPool getKryoInstance() {
    if (poolFactory == null) {
        synchronized (KryoPoolFactory.class) {
            if (poolFactory == null) {
                poolFactory = new KryoPoolFactory();
            }
        }
    }
    return poolFactory.getPool();
}
```



```
看似没有任何问题了，其实不然，主要问题在于instance = new SingleInstance()这行代码并不是原子性的，也就是说，这行代码需要处理器分为多步才能完成，其中主要包含两个操作，分配内存空间，引用变量指向内存，由于编译器可能会产生指令重排序的优化操作，所以两个步骤不能确定实际的先后顺序，假如线程A已经指向了内存，但是并没有分配空间，线程A阻塞，那么当线程B执行时，会发现Instance已经非空了，那么这时返回的Instance变量实际上还没有分配内存，显然是错误的。

4 单例模式中的volatile
volatile是Java提供的关键字，它具有可见性和有序性，被volatile修饰的写变量不能和之前的读写代码调整，读变量不能和之后的读写代码调整，所以，只要把instance加上volatile关键字就可以避免3中的问题了。代码如下所示：

public class SingleInstance{
    private static volatile SingleInstance instance = null;
    private SingleInstance(){}
    public static SingleInstance getInstance(){
        if(instance == null){
            synchronized(SingleInstance.class){
                if(instance == null){
                    instance = new SingleInstance();
                }        
            }
        }
        return instance;    
    }
}
```



## ChannelPipeline

ChannelPipeline是一个双向链表

ChannelPipeline中的ChannelHandler有两种ChannelInboundHandler和ChannelOutboundHandler

顺序是先执行ChannelInboundHandler然后再执行ChannelOutboundHandler

在使用Handler的过程中，需要注意：
1、ChannelInboundHandler之间的传递，通过调用 ctx.fireChannelRead(msg) 实现；调用ctx.write(msg) 将传递到ChannelOutboundHandler。
2、ctx.write()方法执行后，需要调用flush()方法才能令它立即执行。
3、ChannelOutboundHandler 在注册的时候需要放在最后一个ChannelInboundHandler之前，否则将无法传递到ChannelOutboundHandler。

关于上面第3点的原因是因为调用write方法时是通过AbstractChannelHandlerContext.findContextOutbound来寻找下一个ChannelOutboundHandler的，而findContextOutbound方法中是向pipeline链表的前方进行遍历来寻找ChannelOutboundHandler



## Client Proxy

### cglib or jdk

JDK动态代理要求target对象是一个接口的实现对象，假如target对象只是一个单独的对象，并没有实现任何接口，这时候就会用到Cglib代理(Code Generation Library)，即通过构建一个子类对象，从而实现对target对象的代理，因此目标对象不能是final类(报错)，且目标对象的方法不能是final或static（不执行代理功能）。



支持非实现接口的对象代理



## EventLoop、EventLoopGroup

由下图所示，NioEventLop是EventLoop的一个具体实现，EventLoop是EventLoopGroup的一个属性，NioEventLoopGroup是EventLoopGroup的具体实现，都是基于ExecutorService进行的线程池管理，因此EventLoop、EventLoopGroup组件的核心作用就是进行Selector的维护以及线程池的维护。



![img](https://img2020.cnblogs.com/i-beta/1010726/202003/1010726-20200318045454621-632746501.png)

![img](https://img2020.cnblogs.com/i-beta/1010726/202003/1010726-20200318045517798-166353930.png)

　关于EventLoop以及EventLoopGroup的映射关系为：

- 一个EventLoopGroup 包含一个或者多个EventLoop；
- 一个EventLoop 在它的生命周期内只和一个Thread 绑定；
- 所有由EventLoop 处理的I/O 事件都将在它专有的Thread 上被处理；
- 一个Channel 在它的生命周期内只注册于一个EventLoop；
- 一个EventLoop 可能会被分配给一个或多个Channel。

　　Channel 为Netty 网络操作抽象类，EventLoop 主要是为Channel 处理 I/O 操作，两者配合参与 I/O 操作。当一个连接到达时，Netty 就会注册一个 Channel，然后从 EventLoopGroup 中分配一个 EventLoop 绑定到这个Channel上，在该Channel的整个生命周期中都是有这个绑定的 EventLoop 来服务的。



![img](https://img-blog.csdnimg.cn/20210702141206227.png#pic_center)



## Collection

所有均采用线程安全

### HashSet和CopyOnWriteArraySet



## JUC Condition

给可重入锁通信的

在Synchronized加锁状态时，是使用wait/[notify](https://so.csdn.net/so/search?q=notify&spm=1001.2101.3001.7020)/notifyAll进行线程间的通信。那么在使用ReentrantLock加锁时，是如何实现线程间通信问题的呢？在JUC中既然提供了Lock，也提供了用作其线程间通信的方式，再次引入了Condition。来



## 不清楚的地方

```
ConnectionManager的updateConnectedServer
```



## TODO

jboss，tomcat，servelet



## volatile

Java的volatile关键字用于标记一个变量“应当存储在主存”。更确切地说，每次读取volatile变量，都应该从主存读取，而不是从CPU缓存读取。每次写入一个volatile变量，应该写到主存中，而不是仅仅写到CPU缓存。



## synchronized

**1、对于静态方法，由于此时对象还未生成，所以只能采用类锁；**

**2、只要采用类锁，就会拦截所有线程，只能让一个线程访问。**

**3、对于对象锁（this），如果是同一个实例，就会按顺序访问，但是如果是不同实例，就可以同时访问。**

**4、如果对象锁跟访问的对象没有关系，那么就会都同时访问。**



## Load Balance

### roundRobin

类静态变量AtomicInteger来实现

```
AtomicInteger
```



### ConsistentHash

一致性hash算法解决了分布式环境下机器增加或者减少时，简单的取模运算无法获取较高命中率的问题。通过虚拟节点的使用，一致性hash算法可以均匀分担机器的负载，使得这一算法更具现实的意义。正因如此，一致性hash算法被广泛应用于分布式系统中。

```
int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
```



### LRU

```
LinkedHashMap
```

```
accessOrder的设置

accessOrder – the ordering mode - true for access-order, false for insertion-order
```

LinkedHashMap使用get、put方法的时候，会将对应的节点放在队尾，队首对应的就是lru的节点

```
removeEldestEntry
```

方法会在超出initialCapacity的时候进行调用



### LFU

hashmap排序来实现，没访问一次那么它的frequency就加一

感觉排序比较耗时间



保证线程安全使用ConcurrentMap来实现



## Future

基于juc实现的，而不是netty



```
isCancelled
```

关于cancel 的方法都没有进行处理，选择直接抛出异常

```
throw new UnsupportedOperationException();
```



## Exception

**throws** : 如果一个方法可能会出现异常，但没有能力处理这种异常，可以在方法声明处用[throws](https://so.csdn.net/so/search?q=throws&spm=1001.2101.3001.7020)子句来声明抛出异常。用它修饰的方法向调用者表明该方法可能会抛出异常（可以是一种类型，也可以是多种类型，用逗号隔开）（位置: **写在方法名 或方法名列表之后 ，在方法体之前**。）



## ArrayList

###  数组越界异常 

`ArrayIndexOutOfBoundsException`

add需要检查capacity，如果不够就扩容，多线程，同时访问，存在有越界的情况



### 元素值覆盖和为空问题



项目采用锁机制保证线程安全



## ArrayList线程安全处理

### Collections.synchronizedList

最常用的方法是**通过 Collections 的 synchronizedList 方法**将 ArrayList 转换成线程安全的容器后再使用。

```
List<Object> list =Collections.synchronizedList(new ArrayList<Object>);
```

###  

### 为list.add()方法加锁

```
synchronized(list.get()) {
list.get().add(model);
}
```



### CopyOnWriteArrayList

使用线程安全的 CopyOnWriteArrayList 代替线程不安全的 ArrayList。

```
List<Object> list1 = new CopyOnWriteArrayList<Object>();
```



### 使用ThreadLocal

使用ThreadLocal变量确保线程封闭性(封闭线程往往是比较安全的， 但由于使用ThreadLocal封装变量，相当于把变量丢进执行线程中去，每new一个新的线程，变量也会new一次，一定程度上会造成性能[内存]损耗，但其执行完毕就销毁的机制使得ThreadLocal变成比较优化的并发解决方案)。

```
ThreadLocal<List<Object>> threadList = new ThreadLocal<List<Object>>() {
        @Override
         protected List<Object> initialValue() {
              return new ArrayList<Object>();
         }
 };
```



## 多线程结果获取

![img](https://img-blog.csdnimg.cn/20190514165358288.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NTYyMDkz,size_16,color_FFFFFF,t_70)

本项目采用future机制



## SerializedLambda

支持lambda方式调用服务，使用SerializedLambda返回结果

```
SerializedLambda serializedLambda = (SerializedLambda) method.invoke(this);
```

```
return serializedLambda.getImplMethodName();
```

拿到SerializedLambda



lambda在序列化前也会调用writeReplace()

那么我们也可以通过反射的方式来手动调用`writeReplace()`方法，返回 `SerializedLambda`对象，然后再调用`serializedLambda.getImplMethodName()`得到表达式中的方法名



## Zookeeper

结点采用递增形式命名



value存储了host port 

{ "host" : "127.0.0.1", "port" : 18899, "serviceInfoList" : [ { "interfaceName" : "com.raiser.test.service.HelloService", "version" : "2.0" }, { "interfaceName" : "com.raiser.test.service.HelloService", "version" : "1.0" } ] }



读取服务信息，通过注解的方法，可以通过反射获取注解的value和version



## Promise, Future 和 Callback

在并发编程中，我们通常会用到一组非阻塞的模型：Promise，Future 和 Callback。其中的 Future 表示一个可能还没有实际完成的异步任务的结果，针对这个结果可以添加 Callback 以便在任务执行成功或失败后做出对应的操作，而 Promise 交由任务执行者，任务执行者通过 Promise 可以标记任务完成或者失败。 可以说这一套模型是很多异步非阻塞架构的基础。



## watch机制

监听者机制有三种缓存实现方式

Curator中的Cache共有三种：NodeCache、PathChildrenCache、TreeCache。

NodeCache结点，子节点，

tree自己和儿子



## JVM

堆(Heap)和非堆(Non-heap)内存按照官方的说法：“Java 虚拟机具有一个堆，堆是运行时数据区域，所有类实例和数组的内存均从此处分配。堆是在 Java 虚拟机启动时创建的。”“在JVM中堆之外的内存称为非堆内存(Non-heap memory)”。可以看出JVM主要管理两种类型的内存：堆和非堆。简单来说堆就是Java代码可及的内存，是留给开发人员使用的；非堆就是JVM留给自己用的，所以方法区、JVM内部处理或优化所需的内存(如JIT编译后的代码缓存)、每个类结构(如运行时常数池、字段和方法数据)以及方法和构造方法的代码都在非堆内存中。 堆内存分配JVM初始分配的内存由-Xms指定，默认是物理内存的1/64；JVM最大分配的内存由-Xmx指定，默认是物理内存的1/4。默认空余堆内存小于40%时，JVM就会增大堆直到-Xmx的最大限制；空余堆内存大于70% 时，JVM会减少堆直到-Xms的最小限制。因此服务器一般设置-Xms、-Xmx相等以避免在每次GC 后调整堆的大小。 非堆内存分配JVM使用-XX:PermSize设置非堆内存初始值，默认是物理内存的1/64；由XX:MaxPermSize设置最大非堆内存的大小，默认是物理内存的1/4。 JVM内存限制(最大值)首先JVM内存限制于实际的最大物理内存(废话！呵呵)，假设物理内存无限大的话，JVM内存的最大值跟操作系统有很大的关系。简单的说就32位处理器虽然可控内存空间有4GB,但是具体的操作系统会给一个限制，这个限制一般是2GB-3GB（一般来说Windows系统下为1.5G-2G，Linux系统下为2G-3G），而64bit以上的处理器就不会有限制了。
所以说设置VM参数导致程序无法启动主要有以下几种原因：
\1) 参数中-Xms的值大于-Xmx，或者-XX:PermSize的值大于-XX:MaxPermSize；2) -Xmx的值和-XX:MaxPermSize的总和超过了JVM内存的最大限制，比如当前操作系统最大内存限制，或者实际的物理内存等等。



**一般只需要加个xms和xmx就够了**



## 测试

## 运行结果

> 生成时间: 
>
> 硬件环境: 12th Gen Ite(R) Core(M) i5-12490F，16G
>
> 软件环境: 
>
> 启动参数:
>
>  java -server -Xmx1g -Xms1g -XX:MaxDirectMemorySize=1g -XX:+UseG1GC.（参考）



预热的目的是**让 JVM 对被测代码进行足够多的优化**，比如，在预热后，被测代码应该得到了充分的 JIT 编译和优化。



没有配置jvm，用预热三轮



开了四个服务，轮询



**@BenchmarkMode**
 基准测试类型。这里选择的是Throughput也就是吞吐量。根据源码点进去，每种类型后面都有对应的解释，比较好理解，吞吐量会得到单位时间内可以进行的操作数。

Throughput: 整体吞吐量，例如“1秒内可以执行多少次调用”。

AverageTime: 调用的平均时间，例如“每次调用平均耗时xxx毫秒”。

SampleTime: 随机取样，最后输出取样结果的分布，例如“99%的调用在xxx毫秒以内，99.99%的调用在xxx毫秒以内”

SingleShotTime: 以上模式都是默认一次 iteration 是 1s，唯有 SingleShotTime 是只运行一次。往往同时把 warmup 次数设为0，用于测试冷启动时的性能。
 All(“all”, “All benchmark modes”);



  Histogram, ms/op:
    [ 0.000,  2.500) = 1230895 
    [ 2.500,  5.000) = 3379 
    [ 5.000,  7.500) = 445 
    [ 7.500, 10.000) = 165 
    [10.000, 12.500) = 309 
    [12.500, 15.000) = 450 
    [15.000, 17.500) = 146 
    [17.500, 20.000) = 0 
    [20.000, 22.500) = 0 
    [22.500, 25.000) = 0 
    [25.000, 27.500) = 47 

  Percentiles, ms/op:
      p(0.0000) =      0.103 ms/op
     p(50.0000) =      0.710 ms/op
     p(90.0000) =      1.133 ms/op
     p(95.0000) =      1.356 ms/op
     p(99.0000) =      2.064 ms/op
     p(99.9000) =      6.874 ms/op
     p(99.9900) =     16.269 ms/op
     p(99.9990) =     28.845 ms/op
     p(99.9999) =     29.911 ms/op
    p(100.0000) =     29.950 ms/op







Benchmark                                        Mode      Cnt   Score    Error   Units
ClientBenchmark.createUser                      thrpt        3  33.646 ±  4.822  ops/ms
ClientBenchmark.existUser                       thrpt        3  34.104 ±  3.758  ops/ms
ClientBenchmark.getUser                         thrpt        3  28.574 ± 23.601  ops/ms
ClientBenchmark.listUser                        thrpt        3  17.338 ±  7.148  ops/ms
ClientBenchmark.createUser                       avgt        3   0.988 ±  0.381   ms/op
ClientBenchmark.existUser                        avgt        3   0.943 ±  0.217   ms/op
ClientBenchmark.getUser                          avgt        3   1.031 ±  0.168   ms/op
ClientBenchmark.listUser                         avgt        3   1.532 ±  0.258   ms/op
ClientBenchmark.createUser                     sample  1028655   0.933 ±  0.002   ms/op
ClientBenchmark.createUser:createUser·p0.00    sample            0.112            ms/op
ClientBenchmark.createUser:createUser·p0.50    sample            0.865            ms/op
ClientBenchmark.createUser:createUser·p0.90    sample            1.458            ms/op
ClientBenchmark.createUser:createUser·p0.95    sample            1.649            ms/op
ClientBenchmark.createUser:createUser·p0.99    sample            2.290            ms/op
ClientBenchmark.createUser:createUser·p0.999   sample            4.637            ms/op
ClientBenchmark.createUser:createUser·p0.9999  sample           17.007            ms/op
ClientBenchmark.createUser:createUser·p1.00    sample           30.114            ms/op
ClientBenchmark.existUser                      sample  1032205   0.930 ±  0.002   ms/op
ClientBenchmark.existUser:existUser·p0.00      sample            0.106            ms/op
ClientBenchmark.existUser:existUser·p0.50      sample            0.860            ms/op
ClientBenchmark.existUser:existUser·p0.90      sample            1.475            ms/op
ClientBenchmark.existUser:existUser·p0.95      sample            1.679            ms/op
ClientBenchmark.existUser:existUser·p0.99      sample            2.363            ms/op
ClientBenchmark.existUser:existUser·p0.999     sample            6.142            ms/op
ClientBenchmark.existUser:existUser·p0.9999    sample           18.379            ms/op
ClientBenchmark.existUser:existUser·p1.00      sample           33.096            ms/op
ClientBenchmark.getUser                        sample   941077   1.020 ±  0.003   ms/op
ClientBenchmark.getUser:getUser·p0.00          sample            0.116            ms/op
ClientBenchmark.getUser:getUser·p0.50          sample            0.911            ms/op
ClientBenchmark.getUser:getUser·p0.90          sample            1.548            ms/op
ClientBenchmark.getUser:getUser·p0.95          sample            1.790            ms/op
ClientBenchmark.getUser:getUser·p0.99          sample            2.449            ms/op
ClientBenchmark.getUser:getUser·p0.999         sample           11.631            ms/op
ClientBenchmark.getUser:getUser·p0.9999        sample           30.114            ms/op
ClientBenchmark.getUser:getUser·p1.00          sample           32.604            ms/op
ClientBenchmark.listUser                       sample   630734   1.523 ±  0.006   ms/op
ClientBenchmark.listUser:listUser·p0.00        sample            0.177            ms/op
ClientBenchmark.listUser:listUser·p0.50        sample            1.237            ms/op
ClientBenchmark.listUser:listUser·p0.90        sample            2.818            ms/op
ClientBenchmark.listUser:listUser·p0.95        sample            3.490            ms/op
ClientBenchmark.listUser:listUser·p0.99        sample            5.136            ms/op
ClientBenchmark.listUser:listUser·p0.999       sample           13.505            ms/op
ClientBenchmark.listUser:listUser·p0.9999      sample           34.921            ms/op
ClientBenchmark.listUser:listUser·p1.00        sample           51.905            ms/op









| Benchmark                                                    | Mode   | Threads | Samples | Score     | Score Error (99.9%) | Unit   |
| ------------------------------------------------------------ | ------ | ------- | ------- | --------- | ------------------- | ------ |
| com.raiser.benchmark.client.ClientBenchmark.createUser       | thrpt  | 32      | 3       | 41.510991 | 2.507246            | ops/ms |
| com.raiser.benchmark.client.ClientBenchmark.existUser        | thrpt  | 32      | 3       | 42.664593 | 3.307837            | ops/ms |
| com.raiser.benchmark.client.ClientBenchmark.getUser          | thrpt  | 32      | 3       | 41.402032 | 4.207067            | ops/ms |
| com.raiser.benchmark.client.ClientBenchmark.listUser         | thrpt  | 32      | 3       | 34.383818 | 1.014214            | ops/ms |
| com.raiser.benchmark.client.ClientBenchmark.createUser       | avgt   | 32      | 3       | 0.769638  | 0.145933            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser        | avgt   | 32      | 3       | 0.743938  | 0.053889            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser          | avgt   | 32      | 3       | 0.76802   | 0.006136            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser         | avgt   | 32      | 3       | 0.929863  | 0.026542            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser       | sample | 32      | 1271834 | 0.754614  | 0.001603            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.00 | sample | 32      | 1       | 0.098944  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.50 | sample | 32      | 1       | 0.687104  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.90 | sample | 32      | 1       | 1.081344  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.95 | sample | 32      | 1       | 1.306624  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.99 | sample | 32      | 1       | 2.004992  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.999 | sample | 32      | 1       | 5.513216  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p0.9999 | sample | 32      | 1       | 28.34432  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.createUser:createUser·p1.00 | sample | 32      | 1       | 30.998528 | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser        | sample | 32      | 1286919 | 0.745475  | 0.001584            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.00 | sample | 32      | 1       | 0.096384  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.50 | sample | 32      | 1       | 0.685056  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.90 | sample | 32      | 1       | 1.067008  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.95 | sample | 32      | 1       | 1.265664  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.99 | sample | 32      | 1       | 1.964032  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.999 | sample | 32      | 1       | 4.612751  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p0.9999 | sample | 32      | 1       | 28.518253 | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.existUser:existUser·p1.00 | sample | 32      | 1       | 33.193984 | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser          | sample | 32      | 1228222 | 0.781359  | 0.001908            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.00 | sample | 32      | 1       | 0.098816  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.50 | sample | 32      | 1       | 0.705536  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.90 | sample | 32      | 1       | 1.142784  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.95 | sample | 32      | 1       | 1.386496  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.99 | sample | 32      | 1       | 2.125824  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.999 | sample | 32      | 1       | 9.977856  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p0.9999 | sample | 32      | 1       | 28.377088 | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.getUser:getUser·p1.00 | sample | 32      | 1       | 44.2368   | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser         | sample | 32      | 1035200 | 0.927245  | 0.002076            | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.00 | sample | 32      | 1       | 0.132352  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.50 | sample | 32      | 1       | 0.806912  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.90 | sample | 32      | 1       | 1.550336  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.95 | sample | 32      | 1       | 1.88416   | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.99 | sample | 32      | 1       | 2.531328  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.999 | sample | 32      | 1       | 4.659601  | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p0.9999 | sample | 32      | 1       | 27.885568 | NaN                 | ms/op  |
| com.raiser.benchmark.client.ClientBenchmark.listUser:listUser·p1.00 | sample | 32      | 1       | 32.571392 | NaN                 | ms/op  |







Benchmark                                        Mode      Cnt   Score   Error   Units
ClientBenchmark.createUser                      thrpt        3  41.568 ± 6.767  ops/ms
ClientBenchmark.existUser                       thrpt        3  43.183 ± 1.663  ops/ms
ClientBenchmark.getUser                         thrpt        3  41.392 ± 0.647  ops/ms
ClientBenchmark.listUser                        thrpt        3  34.722 ± 2.192  ops/ms
ClientBenchmark.createUser                       avgt        3   0.764 ± 0.133   ms/op
ClientBenchmark.existUser                        avgt        3   0.752 ± 0.124   ms/op
ClientBenchmark.getUser                          avgt        3   0.772 ± 0.067   ms/op
ClientBenchmark.listUser                         avgt        3   0.930 ± 0.136   ms/op
ClientBenchmark.createUser                     sample  1266775   0.758 ± 0.001   ms/op
ClientBenchmark.createUser:createUser·p0.00    sample            0.098           ms/op
ClientBenchmark.createUser:createUser·p0.50    sample            0.700           ms/op
ClientBenchmark.createUser:createUser·p0.90    sample            1.069           ms/op
ClientBenchmark.createUser:createUser·p0.95    sample            1.253           ms/op
ClientBenchmark.createUser:createUser·p0.99    sample            1.960           ms/op
ClientBenchmark.createUser:createUser·p0.999   sample            6.234           ms/op
ClientBenchmark.createUser:createUser·p0.9999  sample           16.313           ms/op
ClientBenchmark.createUser:createUser·p1.00    sample           29.393           ms/op
ClientBenchmark.existUser                      sample  1285866   0.746 ± 0.001   ms/op
ClientBenchmark.existUser:existUser·p0.00      sample            0.095           ms/op
ClientBenchmark.existUser:existUser·p0.50      sample            0.697           ms/op
ClientBenchmark.existUser:existUser·p0.90      sample            1.055           ms/op
ClientBenchmark.existUser:existUser·p0.95      sample            1.208           ms/op
ClientBenchmark.existUser:existUser·p0.99      sample            1.919           ms/op
ClientBenchmark.existUser:existUser·p0.999     sample            6.111           ms/op
ClientBenchmark.existUser:existUser·p0.9999    sample           15.892           ms/op
ClientBenchmark.existUser:existUser·p1.00      sample           29.622           ms/op
ClientBenchmark.getUser                        sample  1235885   0.776 ± 0.002   ms/op
ClientBenchmark.getUser:getUser·p0.00          sample            0.103           ms/op
ClientBenchmark.getUser:getUser·p0.50          sample            0.710           ms/op
ClientBenchmark.getUser:getUser·p0.90          sample            1.133           ms/op
ClientBenchmark.getUser:getUser·p0.95          sample            1.356           ms/op
ClientBenchmark.getUser:getUser·p0.99          sample            2.064           ms/op
ClientBenchmark.getUser:getUser·p0.999         sample            6.874           ms/op
ClientBenchmark.getUser:getUser·p0.9999        sample           16.269           ms/op
ClientBenchmark.getUser:getUser·p1.00          sample           29.950           ms/op
ClientBenchmark.listUser                       sample  1037311   0.925 ± 0.002   ms/op
ClientBenchmark.listUser:listUser·p0.00        sample            0.129           ms/op
ClientBenchmark.listUser:listUser·p0.50        sample            0.801           ms/op
ClientBenchmark.listUser:listUser·p0.90        sample            1.552           ms/op
ClientBenchmark.listUser:listUser·p0.95        sample            1.872           ms/op
ClientBenchmark.listUser:listUser·p0.99        sample            2.535           ms/op
ClientBenchmark.listUser:listUser·p0.999       sample            7.351           ms/op
ClientBenchmark.listUser:listUser·p0.9999      sample           17.269           ms/op
ClientBenchmark.listUser:listUser·p1.00        sample           43.713           ms/op

Benchmark result is saved to jmh-result.json

Process finished with exit code 0



### 总结

三次Benchmark，获取list在34opt/ms，增改查在40左右

平均时间0.7-1

p90大概是1.5倍，95,99就很高了

长尾延迟很高



## ApplicationContextAware

当一个类实现了这个接口（ApplicationContextAware）之后，这个类就可以方便获得ApplicationContext中的所有bean。换句话说，就是这个类可以直接获取[spring](http://lib.csdn.net/base/javaee)配置文件中，所有有引用到的bean对象。



## LinkedBlockingQueue

LinkedBlockingQueue队列也是按 FIFO（先进先出）排序元素。队列的头部是在队列中时间最长的元素，队列的尾部 是在队列中时间最短的元素，新元素插入到队列的尾部，而队列执行获取操作会获得位于队列头部的元素。在正常情况下，链接队列的吞吐量要高于基于数组的队列（ArrayBlockingQueue），因为其内部实现添加和删除操作使用的两个ReenterLock来控制并发执行，而ArrayBlockingQueue内部只是使用一个ReenterLock控制并发，因此LinkedBlockingQueue的吞吐量要高于ArrayBlockingQueue。


## 服务端多线程

单个服务端是单线程，然后线程再创建一个线程池

通过启动server的时候创建线程池，然后将其传递到handler，handler进行多线程处理



不需要保证服务端的ArrayList线程安全，没有那么多服务上下线

实际没有使用list，list作为传递的参数，实际上使用的是线程安全的set来保证唯一性

同时使用set来实现节点的更新



## 反射

(1) 概念

　　反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意一个方法和属性；这种动态获取的信息以及动态调用对象的方法的功能称为java语言的反射机制。

(2) 功能

　　反射机制主要提供了以下功能：

　　在运行时判断任意一个对象所属的类；
　　在运行时构造任意一个类的对象；
　　在运行时判断任意一个类所具有的成员变量和方法；
　　在运行时调用任意一个对象的方法；
　　生成动态代理。
(3) 反射的缺点

1.性能第一:反射包括了一些动态类型，所以 JVM 无法对这些代码进行优化。因此，反射操作的 效率要比那些非反射操作低得多。我们应该避免在经常被 执行的代码或对性能要求很高的程 序中使用反射。 　　

2.安全限制:使用反射技术要求程序必须在一个没有安全限制的环境中运行。如果一个程序必须在有安全限制的环境中运行，如    Applet，那么这就是个问题了 　　

3.内部暴露:由于反射允许代码执行一些在正常情况下不被允许的操作（比如访问私有的属性和方法），所以使用反射可能会导致意料之外的副作用－－代码有功能上的错误，降低可移植性。 反射代码破坏了抽象性，因此当平台发生改变的时候，代码的行为就有可能　               也随着变化。    

(4) Bean中配置解析(反射) 　　



## 客户端多线程

通过创建proxy来进行代理，需要class和handler，handler中的invoke方法

在用代理进行调用方法的时候会调用invoke方法，

### 步骤

1首先，先执行服务发现，去zk拿服务信息，包括接口信息，地址。会初始化protocol，handler这个map



2要拿到protocol对应的handler，就需要依次与服务端进行连接，确保可用，并且获取channel中的handler，handler就是针对性处理

handler会writeandflush，交由outbound来处理



3然后，创建服务，通过代理模式，调用方法时，会调用重写的invoke

invoke通过sendrequest，然后交到channel队列去处理，然后channel中的handler会通过request id映射的方式拿到future，set future里的result



4然后回到invoke里的future，同步状态结束，future get返回结果



# callback

```
CountDownLatch实现同步获取结果
```

通过可重入锁来实现list线程安全



# Future

继承juc的future

两个锁，一个是继承aqs，保证future获取结果的

一个是可重入锁，保证callback

当future执行完的时候会唤醒，去执行callback



## 支持指定方法名称和lambda方式调用

lambda在序列化的时候会调用writereplace方法，通过反射获取方法名称（具体因为lambda也继承了Serializable，所以我们也继承Serializable写一个接口，再在接口写一个default方法就可以实现获取方法名称）



## InetSocketAddress

InetSocketAddress类主要作用是封装端口 他是在在InetAddress基础上加端口，但它是有构造器的。具体的一些方法可以去帮助文档查看。



# FunctionalInterface

用来实现lambda指定方法，通过继承SerializableFunction接口，只能有一个抽象方法



## 什么是IO多路复用

一句话解释：单线程或单进程同时监测若干个文件描述符是否可以执行IO操作的能力。
