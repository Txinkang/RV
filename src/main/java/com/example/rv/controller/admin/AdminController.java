package com.example.rv.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Admin;
import com.example.rv.service.AdminService;

@Validated
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }

    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return adminService.logout(token);
    }

    @GetMapping("/dataAnalyze")
    public Result dataAnalyze() {
        return adminService.dataAnalyze();
    }
}
