package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Payments;
import com.example.rv.service.UserPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/user")
public class UserPayController {

    @Autowired
    private UserPayService userPayService;

    @PostMapping("/recharge")
    public Result recharge(@RequestParam BigDecimal amount){
        return userPayService.recharge(amount);
    }

    @PostMapping("/payment")
    public Result payment(@RequestBody @Validated Payments payment){
        return userPayService.payment(payment);
    }
}
