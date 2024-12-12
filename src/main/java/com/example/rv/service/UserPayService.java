package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Payments;

import java.math.BigDecimal;
import java.util.Map;

public interface UserPayService {
    /**
     * 用户充值
     * @param amount
     * @return Result
     */
    Result recharge(BigDecimal amount);

    /**
     * 付款
     * @param payment
     * @return Result
     */
    Result payment(Payments payment);

    /**
     * 退款
     * @param paramMap
     * @return Result
     */
    Result refund(Map<String,Integer> paramMap);
}
