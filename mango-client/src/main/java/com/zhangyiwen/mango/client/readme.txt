mango-client部分，主要包含：

1、RpcClient:Rpc客户端，用于发送Rpc请求及接收Rpc响应
    1)发送Rpc请求
        a)send(RpcRequest request)方法实现向RpcServer发送Rpc请求
        b)使用Netty新建一个NioSocketChannel的引导
        c)handler流中加入RpcEncoder(RpcRequest.class)、RpcDecoder(RpcResponse.class)、RpcClient本身
        d)发送RpcRequest request给RpcServer,实现请求发送
        e)使用object.wait()实现同步等待请求，object.notifyAll()会在收到响应时被调用
    2)接收Rpc响应
        a)RpcClient继承SimpleChannelInboundHandler<RpcResponse>,用于处理Rpc响应
        b)收到RpcResponse后，调用object.notifyAll()
        c)RpcClient.send()方法会继续执行返回响应结果

2、RpcProxy:用于创建RPC服务的代理
    1)RpcProxy.create(Class<T> interfaceClass)创建指定服务接口的RPC代理服务类
        a)使用Java动态代理Proxy.newProxyInstance进行RPC代理服务类创建
        b)构建RpcRequest
        c)使用ServiceDiscovery发现Rpc服务器地址
        d)使用Rpc服务器地址、RpcRequest实例化RpcClient
        e)利用RpcClient实现Rpc请求的发送及Rpc结果的接收