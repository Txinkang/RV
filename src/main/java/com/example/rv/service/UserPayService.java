package com.example.rv.service;

import com.example.rv.Response.Result;

import java.math.BigDecimal;

public interface UserPayService {
    /**
     * 用户充值
     * @param amount
     * @return Result
     */
    Result recharge(BigDecimal amount);
}
