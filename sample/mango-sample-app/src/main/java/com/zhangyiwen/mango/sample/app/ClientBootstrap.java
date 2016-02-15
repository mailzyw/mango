package com.zhangyiwen.mango.sample.app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by zhangyiwen on 15/12/27.
 */
public class ClientBootstrap {

    public static void main(String[] args) {
        ApplicationContext ctx =  new ClassPathXmlApplicationContext("spring.xml");
        MyHelloService service = ctx.getBean(MyHelloService.class);
        service.test1();
    }

}
