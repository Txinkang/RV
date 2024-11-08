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

    /**
     * 升级用户权限，由普通用户升级为商家
     * @return
     */
    Result upgradeRole();

    /**
     * 获取验证码
     * @param user
     * @return Result
     */
    Result getCode(Users user);

    /**
     * 手机号登录
     * @param param
     * @return Result
     */
    Result loginPhoneNumber(Map<String, String> param);

    /**
     * 邮箱登录
     * @param param
     * @return Result
     */
    Result loginEmail(Map<String, String> param);
}
