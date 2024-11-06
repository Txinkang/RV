package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import com.example.rv.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Result loginAccount(@RequestBody Users user,@RequestHeader("Authorization") String token){
        return userService.loginAccount(user);
    }

    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        return userService.logout(token);
    }

    @GetMapping("/userInfo")
    public Result userInfo(){
        return userService.userInfo();
    }

    @PatchMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody Users user){
        return userService.updateUserInfo(user);
    }
}
