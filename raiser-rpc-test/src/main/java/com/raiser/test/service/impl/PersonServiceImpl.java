package com.raiser.test.service.impl;

import com.raiser.rpc.annotation.RpcService;
import com.raiser.test.entity.Person;
import com.raiser.test.service.PersonService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 4:43 PM
 */
@RpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    @Override
    public List<Person> callPerson(String name, Integer num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
