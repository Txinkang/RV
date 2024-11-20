package com.example.rv.service.Impl.user;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.mapper.user.UserPayMapper;
import com.example.rv.service.UserPayService;
import com.example.rv.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class UserPayServiceImpl implements UserPayService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserPayMapper userPayMapper;

    @Override
    public Result recharge(BigDecimal amount) {
        BigDecimal decimalAmount = new BigDecimal(String.valueOf(amount));
        if (decimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new Result(ResultCode.R_ParamError);
        }
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer userId = (Integer) userMap.get("id");
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(userId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        Integer rowAffected = userPayMapper.rechargeByUserId(amount, checkUser);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok:ResultCode.R_UpdateDbFailed);
    }
}
