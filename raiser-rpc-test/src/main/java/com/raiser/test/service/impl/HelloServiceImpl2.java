package com.raiser.test.service.impl;

import com.raiser.test.entity.Person;
import com.raiser.test.service.HelloService;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:50 PM
 */
public class HelloServiceImpl2 implements HelloService {
    public HelloServiceImpl2() {

    }

    @Override
    public String hello(String name) {
        return "Hi " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hi " + person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public String hello(String name, Integer age) {
        return name + " is " + age + " years old";
    }
}
