package com.zhangyiwen.mango.sample.app;

import com.zhangyiwen.mango.client.RpcProxy;
import com.zhangyiwen.mango.sample.client.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangyiwen on 15/12/28.
 */
@Component
public class MyHelloService {

    @Autowired
    private RpcProxy rpcProxy;

    public void test1(){
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println("result is " + result);
    }


}
