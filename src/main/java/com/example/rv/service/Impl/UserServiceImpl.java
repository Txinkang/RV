package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.UserMapper;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import com.example.rv.utils.JwtUtil;
import com.example.rv.utils.Md5Util;
import com.example.rv.utils.ThreadLocalUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

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

    @Override
    public Result loginAccount(Users user) {
        //验证参数
        if (user==null || Strings.isEmpty(user.getUserName()) || Strings.isEmpty(user.getUserPassword())){
            return new Result(ResultCode.R_ParamError);
        }
        //查询用户是否存在
        Users queryUser = userMapper.findByUserName(user.getUserName());
        if (queryUser==null){
            return new Result(ResultCode.R_UserNotFound);
        }
        //查询密码是否正确
        String verifyPassword=Md5Util.getMD5String(user.getUserPassword());
        if (!verifyPassword.equals(queryUser.getUserPassword())){
            return new Result(ResultCode.R_PasswordError);
        }
        //存入信息生成token，进行登录
        Map<String,Object> userMap=new HashMap<>();
        userMap.put("id", queryUser.getUserId());
        String token= JwtUtil.genToken(userMap);
        redisTemplate.opsForValue().set(token,token,30, TimeUnit.SECONDS);
        return new Result(ResultCode.R_Ok,token);
    }

    @Override
    public Result logout(String token) {
        redisTemplate.opsForValue().getAndDelete(token);
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result userInfo() {
        Map<String,Object> userMap=ThreadLocalUtil.get();
        Integer userId= (Integer) userMap.get("id");
        Users queryUser=userMapper.findByUserId(userId);
        queryUser.setUserPassword(null);
        return new Result(ResultCode.R_Ok,queryUser);
    }

    @Override
    public Result updateUserInfo(Users user) {
        Map<String,Object> userMap=ThreadLocalUtil.get();
        Integer userId= (Integer) userMap.get("id");
        return new Result(ResultCode.R_Ok);
    }
}










