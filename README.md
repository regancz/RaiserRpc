# RaiserRpc

RaiserRpc is a Java-based open-source RPC framework. 

## Features

客户端支持同步、异步Future机制，Callback，指定方法名、版本号和Lambda的方式调用服务

支持心跳检测，多种序列化机制，包括Protostuff、Kryo、Hessian

支持多种负载均衡策略，包括一致性哈希、LFU、LRU、随机、轮询

Protostuff，32Thread单机搭建，测试结果TPS：30-40ops/ms，P90：1ms/op，RT：0.7-1.0ms/op

客户端通过代理发送请求，服务端根据监听者动态更新服务节点