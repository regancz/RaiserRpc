package com.raiser.benchmark.client;

import com.raiser.benchmark.bean.Page;
import com.raiser.benchmark.bean.User;
import com.raiser.benchmark.service.UserService;
import com.raiser.benchmark.service.impl.UserServiceServerImpl;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhengyangxin
 * @date: 9/6/2022 1:54 PM
 */
public abstract class AbstractClient {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final UserService _serviceUserService = new UserServiceServerImpl();

    protected abstract UserService getUserService();

    public boolean existUser() throws Exception {
        String email = String.valueOf(counter.getAndIncrement());
        return getUserService().existUser(email);
    }

    public boolean createUser() throws Exception {
        int id = counter.getAndIncrement();
        User user = _serviceUserService.getUser(id);
        return getUserService().createUser(user);
    }

    public User getUser() throws Exception {
        int id = counter.getAndIncrement();
        return getUserService().getUser(id);
    }

    public Page<User> listUser() throws Exception {
        int pageNo = counter.getAndIncrement();
        return getUserService().listUser(pageNo);
    }

}
