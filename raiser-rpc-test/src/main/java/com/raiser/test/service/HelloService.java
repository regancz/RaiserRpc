package com.raiser.test.service;

import com.raiser.test.entity.Person;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:07 PM
 */
public interface HelloService {
    String hello(String name);

    String hello(Person person);

    String hello(String name, Integer age);
}
