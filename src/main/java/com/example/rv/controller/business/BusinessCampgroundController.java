package com.example.rv.controller.business;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;
import com.example.rv.service.BusinessCampgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/business")
public class BusinessCampgroundController {

    @Autowired
    private BusinessCampgroundService businessCampgroundService;
    @PostMapping("/uploadCampground")
    public Result uploadCampground(
            @RequestPart("campground") Campground campground,
            @RequestParam("campgroundPictures") List<MultipartFile> campgroundPicture){
        return businessCampgroundService.uploadCampground(campground, campgroundPicture);
    }

    @GetMapping("/checkCampgroundList")
    public Result checkCampgroundList(){
        return businessCampgroundService.checkCampgroundList();
    }


}
