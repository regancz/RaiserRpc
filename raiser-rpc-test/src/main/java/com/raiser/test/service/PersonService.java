package com.raiser.test.service;

import com.raiser.test.entity.Person;

import java.util.List;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 4:43 PM
 */
public interface PersonService {
    List<Person> callPerson(String name, Integer num);
}
