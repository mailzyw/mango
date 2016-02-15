package com.zhangyiwen.mango.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by zhangyiwen on 15/12/25.
 */
public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }

}
