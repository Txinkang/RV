package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Users;

public interface UserService {
    /**
     * 注册
     * @param user
     * @return
     */
    Result register(Users user);
}
