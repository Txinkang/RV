package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Admin;

public interface AdminService {
    /**
     * 管理员登录
     * @param admin
     * @return Result
     */
    Result login(Admin admin);

    /**
     * 管理员登出
     * @param token
     * @return Result
     */
    Result logout(String token);

    Result dataAnalyze();
}
