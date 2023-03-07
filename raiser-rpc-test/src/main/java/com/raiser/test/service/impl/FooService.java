package com.raiser.test.service.impl;

import com.raiser.rpc.annotation.RpcAutowired;
import com.raiser.test.service.Foo;
import com.raiser.test.service.HelloService;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 4:42 PM
 */
public class FooService implements Foo {
    @RpcAutowired(version = "1.0")
    private HelloService helloService1;

    @RpcAutowired(version = "2.0")
    private HelloService helloService2;

    @Override
    public String say(String s) {
        return helloService1.hello(s);
    }
}
