package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.UserMapper;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import com.example.rv.service.common.EmailService;
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
    @Autowired
    private EmailService emailService;

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
        Integer rowAffected=userMapper.addUser(user);
        return new Result(rowAffected>0?ResultCode.R_Ok:ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result getCode(Users user) {
        String userPhoneNumber=user.getUserPhoneNumber();
        String userEmail=user.getUserEmail();
        Result result=new Result();
        //两个全为空或全不为空，都不行
        if ((Strings.isEmpty(userPhoneNumber) && Strings.isEmpty(userEmail)) || (!Strings.isEmpty(userPhoneNumber) && !Strings.isEmpty(userEmail))){
            return new Result(ResultCode.R_ParamError);
        }
        //0是手机号验证，1是邮箱验证
        int verifyType=Strings.isEmpty(userPhoneNumber)?1:0;
        switch (verifyType) {
            case 0 -> {
                if (userMapper.checkUserByUserPhoneNumber(userPhoneNumber) == null) {
                    result.setResultCode(ResultCode.R_UserNotFound);
                    result.setMessage("该手机号不存在");
                    break;
                }
                //反正都是6位数，就用emailService生成了
                String phoneCode = emailService.generateVerificationCode();
                //存进redis里，1分钟后过期
                redisTemplate.opsForValue().set(userPhoneNumber, phoneCode, 1, TimeUnit.MINUTES);
                result.setResultCode(ResultCode.R_Ok);
                result.setMessage("成功");
                result.setData(phoneCode);
            }
            case 1 -> {
                if (userMapper.checkUserByUserEmail(userEmail) == null) {
                    result.setResultCode(ResultCode.R_UserNotFound);
                    result.setMessage("该邮箱不存在");
                    break;
                }
                String emailCode = emailService.generateVerificationCode();
                emailService.sendVerificationCode(userEmail,emailCode);
                redisTemplate.opsForValue().set(userEmail, emailCode, 1, TimeUnit.MINUTES);
                result.setResultCode(ResultCode.R_Ok);
                result.setMessage("成功");
            }
        }
        return result;
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
        redisTemplate.opsForValue().set(token,token,1, TimeUnit.HOURS);
        return new Result(ResultCode.R_Ok,token);
    }

    @Override
    public Result loginPhoneNumber(Map<String, String> param) {
        String phoneNum = param.get("userPhoneNUmber");
        String phoneCode = param.get("code");
        Users queryUser = userMapper.findUserByUserPhoneNumber(phoneNum);
        //验证参数
        if (Strings.isEmpty(phoneNum) || Strings.isEmpty(phoneCode)){
            return new Result(ResultCode.R_ParamError);
        }
        //查询用户是否存在
        if (queryUser == null){
            return new Result(ResultCode.R_UserNotFound);
        }
        //验证code
        String redisCode=redisTemplate.opsForValue().get(phoneNum);
        if (!redisCode.equals(phoneCode)){
            return new Result(ResultCode.R_CodeError);
        }
        //生成token，准备登录
        Map<String,Object> userMap=new HashMap<>();
        userMap.put("id",queryUser.getUserId());
        String token=JwtUtil.genToken(userMap);
        redisTemplate.opsForValue().set(token,token,1,TimeUnit.HOURS);
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
        //获取userId
        Map<String,Object> userMap=ThreadLocalUtil.get();
        Integer userId= (Integer) userMap.get("id");
        //验证所要修改信息是否已存在
        if (!Strings.isEmpty(user.getUserName()) && userMapper.checkUserByUserName(user.getUserName())!=null){
            return new Result(ResultCode.R_UserNameIsExist);
        }
        if (!Strings.isEmpty(user.getUserEmail()) && userMapper.checkUserByUserEmail(user.getUserEmail())!=null){
            return new Result(ResultCode.R_UserEmailIsExist);
        }
        if (!Strings.isEmpty(user.getUserPhoneNumber()) && userMapper.checkUserByUserPhoneNumber(user.getUserPhoneNumber())!=null){
            return new Result(ResultCode.R_UserPhoneNumberIsExist);
        }
        //验证通过开始修改
        Integer rowAffected = userMapper.updateUserByUserId(user,userId);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result updatePassword(Map<String, String> pwd) {
        Map<String,Object> userMap=ThreadLocalUtil.get();
        Integer userId= (Integer) userMap.get("id");
        Users queryUser=userMapper.findByUserId(userId);
        //验证参数
        if (Strings.isEmpty(pwd.get("oldPassword")) || Strings.isEmpty(pwd.get("newPassword")) ||Strings.isEmpty(pwd.get("confirmNewPassword"))){
            return new Result(ResultCode.R_ParamError);
        }
        //验证旧密码是否正确
        if (!Md5Util.getMD5String(pwd.get("oldPassword")).equals(queryUser.getUserPassword())){
            return new Result(ResultCode.R_OldPasswordError);
        }
        //看两次新密码是否一致
        if (!pwd.get("newPassword").equals(pwd.get("confirmNewPassword"))){
            return new Result(ResultCode.R_NewPasswordNotSame);
        }
        //密码加密
        String newPassword = Md5Util.getMD5String(pwd.get("newPassword"));
        Integer rowAffected = userMapper.updatePasswordByUserId(newPassword, userId);
        //修改完毕，删除token，重新登录
        if (!(rowAffected > 0)){
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        redisTemplate.opsForValue().getAndDelete((String) userMap.get("token"));
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result upgradeRole() {
        //查询用户是否已升级
        Map<String,Object> userMap=ThreadLocalUtil.get();
        Integer userId= (Integer) userMap.get("id");
        Users queryUser = userMapper.findByUserId(userId);
        if (queryUser.getUserRole()!=0){
            return new Result(ResultCode.R_RoleAlreadyUpgrade);
        }
        //升级
        Integer rowAffected=userMapper.updateUserRoleByUserId(userId);
        return new Result(rowAffected>0?ResultCode.R_Ok:ResultCode.R_UpdateDbFailed);
    }





}










