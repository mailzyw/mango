package com.zhangyiwen.mango.server;

import com.zhangyiwen.mango.common.RpcRequest;
import com.zhangyiwen.mango.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC处理器，用于处理RPC请求
 * Created by zhangyiwen on 15/12/25.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String,Object> handlerMap;

    public RpcHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            Object result = handle(rpcRequest);
            response.setResult(result);
        } catch (Throwable throwable) {
            response.setError(throwable);
        }
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);        // 具体的serviceBean

        Class<?> serviceClass = serviceBean.getClass();        // serviceBean对应的Class
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // java反射的方式执行方法
        /*
        Method method = serviceClass.getMethod(methodName,parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean,parameters);
        */

        // Cglib提供的反射方式执行方法
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName,parameterTypes);
        return serviceFastMethod.invoke(serviceBean,parameters);
    }


}
