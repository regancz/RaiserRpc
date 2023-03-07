package com.raiser.test.service.impl;

import com.raiser.test.entity.Person;
import com.raiser.test.service.HelloService;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:17 PM
 */
public class HelloServiceImpl implements HelloService {
    public HelloServiceImpl() {

    }

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello " + person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public String hello(String name, Integer age) {
        return name + " is " + age;
    }
}
