package com.raiser.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:49 PM
 */
public class RpcServerBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
