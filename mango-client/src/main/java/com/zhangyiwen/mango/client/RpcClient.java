package com.zhangyiwen.mango.client;

import com.zhangyiwen.mango.common.RpcDecoder;
import com.zhangyiwen.mango.common.RpcEncoder;
import com.zhangyiwen.mango.common.RpcRequest;
import com.zhangyiwen.mango.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC客户端，用于发送RPC请求及接收RPC响应
 * Created by zhangyiwen on 15/12/25.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private  String host;
    private  int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture future = bootstrap.connect(host,port).sync();
            future.channel().writeAndFlush(request).sync();

            //等待服务器返回RpcResponse
            synchronized (obj){
                obj.wait();
            }

            if(request != null){
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

}
