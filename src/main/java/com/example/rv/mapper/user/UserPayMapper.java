package com.example.rv.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserPayMapper {
    @Update("update users set user_balance = user_balance + CAST(#{amount} AS DECIMAL(18, 2)) where user_id = #{checkUser}")
    Integer rechargeByUserId(BigDecimal amount, Integer checkUser);
}
