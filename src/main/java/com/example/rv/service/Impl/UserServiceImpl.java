package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.UserMapper;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import com.example.rv.utils.Md5Util;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result register(Users user) {
        //验证参数
        if (user==null || Strings.isEmpty(user.getUserName()) || Strings.isEmpty(user.getUserPassword())){
            return new Result(ResultCode.R_ParamError);
        }
        //查询用户是否存在
        if (userMapper.checkUserByUserName(user.getUserName())!=null){
            return new Result(ResultCode.R_UserNameIsExist);
        }
        if (!Strings.isEmpty(user.getUserPhoneNumber()) && userMapper.checkUserByUserPhoneNumber(user.getUserPhoneNumber())!=null){
            return new Result(ResultCode.R_UserPhoneNumberIsExist);
        }
        if (!Strings.isEmpty(user.getUserEmail()) && userMapper.checkUserByUserEmail(user.getUserEmail())!=null){
            return new Result(ResultCode.R_UserEmailIsExist);
        }
        //处理一下数据准备写入
        user.setUserPassword(Md5Util.getMD5String(user.getUserPassword()));
        user.setUserPhoneNumber(Strings.isEmpty(user.getUserPhoneNumber())?null:user.getUserPhoneNumber());
        user.setUserEmail(Strings.isEmpty(user.getUserEmail())?null:user.getUserEmail());
        //写入数据库
        int rowAffected=userMapper.addUser(user);
        return new Result(rowAffected==1?ResultCode.R_Ok:ResultCode.R_Fail);
    }
}
