package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;
import com.example.rv.service.UserCampgroundService;
import com.example.rv.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
public class UserCampgroundController {

    @Autowired
    private UserCampgroundService userCampgroundService;

    @PostMapping("/checkCampgroundList")
    public Result checkCampgroundList(@RequestBody Campground campground){
        return userCampgroundService.checkCampgroundList(campground);
    }
}
