mango RPC 框架简介

## 技术选型
1. 依赖注入：String 3.2
2. 网络数据传输：Netty 4.0
3. 序列化：Protostuff 1.0.8
4. 服务注册：ZooKeeper 3.4.6

## 使用方法
1. 启动 ZooKeeper 服务器（测试时可在本地启动）。
2. 运行`mango-sample-server`模块中的`RpcBootstrap`类，启动 RPC 服务器。
3. 运行`mango-sample-app`模块中的`ClientBootstrap`类，启动客户端测试。

## 基本实现思路
1. Rpc服务端
    基于Netty的Server端，接收Client端的RpcRequest
    内部维护<服务接口,实际服务对象serviceBean>的映射关系
    解析RpcRequest,使用Java/CGLIB反射执行服务接口对应的serviceBean的相应方法
    通过网络回传客户端执行结果

2. Rpc客户端
    基于Java动态代理，为服务接口创建代理服务对象serviceProxy
    serviceProxy根据要执行的Method构建RpcRequest
    RpcClient基于Netty创建Client端，发送RpcRequest给Rpc服务端，并接收Rpc服务端响应RpcResponse
    响应结果RpcResponse即为Rpc方法执行的返回结果

3. 服务注册与发现
    Rpc服务端启动成功后，向zk注册路径下写入临时递增子目录，目录内容记录server端host:port
    ServiceDiscovery观察zk注册路径下子目录变化，将最新子目录维护在服务地址集合中List<String> dataList
    Rpc客户端向服务端发送Rpc请求前，从List<String> dataList中随机取一个服务地址,向其对应的host:port发送请求