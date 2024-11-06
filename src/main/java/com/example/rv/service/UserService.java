package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Users;

import java.util.Map;

public interface UserService {
    /**
     * 注册
     * @param user
     * @return Result
     */
    Result register(Users user);

    /**
     * 账号密码登录
     * @param user
     * @return Result
     */
    Result loginAccount(Users user);

    /**
     * 退出登录
     *
     * @param token
     * @return Result
     */
    Result logout(String token);

    /**
     * 查看用户信息
     * @return Result
     */
    Result userInfo();

    /**
     * 修改用户信息
     * @param user
     * @return Result
     */
    Result updateUserInfo(Users user);

    /**
     * 更新密码
     *
     * @param pwd
     * @return Result
     */
    Result updatePassword(Map<String, String> pwd);
}
