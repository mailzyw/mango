mango-server部分，主要包含：
1、@RpcService
    注解，标注在Rpc服务接口的实现类上

2、RpcServer
    Rpc服务器，用于发布Rpc服务，通过Spring容器加载启动
    1）RpcServer实现ApplicationContextAware接口，当加载Spring配置文件时，调用其setApplicationContext()方法
        a)RpcServer内部维护一个Map<String,Object> handlerMap 的field，用于存储<服务接口名称,实际的serviceBean>的映射关系
        b)setApplicationContext方法内获取spring配置文件加载时扫描到的所有带有@RpcService注解的serviceBean,并将映射关系存入handlerMap
    2）RpcServer实现InitializingBean接口，当RpcServer的所有属性注入完成后，触发其afterPropertiesSet()方法
        a)基于netty,启动一个基于NIO的socketServer(从配置文件获取host,port),该server即为Rpc服务器
        b)handler流中加入RpcDecoder(RpcRequest.class)，用于解码客户端请求为RpcRequest对象
        c)handler流中加入RpcEncoder(RpcResponse.class)，用于编码服务器返回的RpcResponse对象
        d)handler流中加入了RpcHandler(handlerMap)，用于实际处理客户端的Rpc请求
        e)socketServer启动成功后，调用ServiceRegistry将socketServer对于的服务地址进行服务注册（实现分布式服务）

3、RpcHandler
    Rpc处理器，用于实际处理(接收RpcRequest,返回RpcResponse)Rpc请求
    1)RpcHandler继承Netty的SimpleChannelInboundHandler<RpcRequest>，用于接收Rpc请求
        a)解析RpcReqeust,获取Rpc请求信息：请求的服务接口clazz、方法名、方法签名、实际方法参数值
        b)从RpcServer传入的handlerMap中，根据服务接口clazz名称获取实际serviceBean
        c)使用java反射或cgLib反射的方式，执行实际的方法，并得到执行结果Object result
        d)将执行结果Object result封装入RpcResponse,将RpcResponse回传给客户端
