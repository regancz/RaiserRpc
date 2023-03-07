package com.raiser.benchmark.service;

import com.raiser.benchmark.bean.Page;
import com.raiser.benchmark.bean.User;

public interface UserService {
	public boolean existUser(String email);

	public boolean createUser(User user);

	public User getUser(long id);

	public Page<User> listUser(int pageNo);

}
