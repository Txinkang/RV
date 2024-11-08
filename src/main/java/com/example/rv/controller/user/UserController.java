package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import com.example.rv.utils.ThreadLocalUtil;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody @Validated Users user){
        return userService.register(user);
    }

    @PostMapping("/loginAccount")
    public Result loginAccount(@RequestBody Users user){
        return userService.loginAccount(user);
    }

    @GetMapping("/logout")
    //因为拦截器不拦截这个路径，所以需要从请求头取token
    public Result logout(@RequestHeader("Authorization") String token){
        return userService.logout(token);
    }

    @GetMapping("/userInfo")
    public Result userInfo(){
        return userService.userInfo();
    }

    @PatchMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody @Validated Users user){
        return userService.updateUserInfo(user);
    }

    @PatchMapping("/updatePassword")
    public Result updatePassword(@RequestBody Map<String,String> pwd){
        return userService.updatePassword(pwd);
    }

    @PatchMapping("/upgradeRole")
    public Result upgradeRole(){
        return userService.upgradeRole();
    }

    @PostMapping ("/getCode")
    public Result getCode(@RequestBody @Validated Users user){
        return userService.getCode(user);
    }

    @PostMapping("/loginPhoneNumber")
    public Result loginPhoneNumber(@RequestBody @NotEmpty Map<String,String> param){
        return userService.loginPhoneNumber(param);
    }
}
