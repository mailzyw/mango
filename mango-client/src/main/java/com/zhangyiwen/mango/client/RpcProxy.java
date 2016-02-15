package com.zhangyiwen.mango.client;

import com.zhangyiwen.mango.common.RpcRequest;
import com.zhangyiwen.mango.common.RpcResponse;
import com.zhangyiwen.mango.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC代理，用于创建RPC服务代理
 * Created by zhangyiwen on 15/12/25.
 */
public class RpcProxy{

    private String serverAddress;
    private ServiceDiscovery serverDiscovery;

    public RpcProxy(String serverAddress){
        this.serverAddress = serverAddress;
    }

    public RpcProxy(String serverAddress, ServiceDiscovery serverDiscovery) {
        this.serverAddress = serverAddress;
        this.serverDiscovery = serverDiscovery;
    }

    public <T> T create(Class<T> interfaceClass){
        return (T)Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);

                if(serverDiscovery != null){
                    serverAddress = serverDiscovery.discover();
                }

                String[] array = serverAddress.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);

                RpcClient client = new RpcClient(host,port);
                RpcResponse response = client.send(request);
                if(response.isError()){
                    throw response.getError();
                }else {
                    return response.getResult();
                }
            }
        });
    }


}
