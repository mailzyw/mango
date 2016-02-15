package com.zhangyiwen.mango.sample.server;

import com.zhangyiwen.mango.sample.client.HelloService;
import com.zhangyiwen.mango.sample.client.Person;
import com.zhangyiwen.mango.server.RpcService;

/**
 * Created by zhangyiwen on 15/12/25.
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
