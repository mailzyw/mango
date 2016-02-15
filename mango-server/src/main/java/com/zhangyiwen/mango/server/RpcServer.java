package com.zhangyiwen.mango.server;

import com.zhangyiwen.mango.common.RpcDecoder;
import com.zhangyiwen.mango.common.RpcEncoder;
import com.zhangyiwen.mango.common.RpcRequest;
import com.zhangyiwen.mango.common.RpcResponse;
import com.zhangyiwen.mango.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC服务器，用于发布RPC服务，通过Spring容器加载启动
 * Created by zhangyiwen on 15/12/25.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;

    private Map<String,Object> handlerMap = new HashMap<>();    //Map<服务接口名称,实际的serviceBean>

    public RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        //遍历所有加入了@RpcService的实体Bean,构建handlerMap
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for(Object serviceBean:serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                System.out.println("new rpc service interface found: interface -> "+ interfaceName + " , serviceBean -> "+ serviceBean.getClass().getName());
                handlerMap.put(interfaceName,serviceBean);
            }
        }else {
            System.out.println("!!!");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host,port).sync();
            LOGGER.info("server started on port {}",port);
            System.out.println("server started on port "+port);

            if(serviceRegistry != null){
                serviceRegistry.register(serverAddress);
            }

            future.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}
