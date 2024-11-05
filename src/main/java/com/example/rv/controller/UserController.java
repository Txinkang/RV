package com.example.rv.controller;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Users;
import com.example.rv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("register")
    public Result register(@RequestBody @Validated Users user){
        return userService.register(user);
    }

    @PostMapping("/loginAccount")
    public Result loginAccount(@RequestBody Users user,@RequestHeader("Authorization") String token){
        return userService.loginAccount(user);
    }
}
