package com.example.rv.controller.business;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;
import com.example.rv.service.BusinessCampgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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


    @PostMapping("/updateCampground")
    public Result updateCampground(
        @RequestPart("campground") Campground campground,
        @RequestParam(value = "campgroundPictures", required = false) List<MultipartFile> campgroundPicture){
        return businessCampgroundService.updateCampground(campground, campgroundPicture);
    }

    @DeleteMapping("/deleteCampground")
    public Result deleteCampground(
        @RequestBody Campground campground){
        return businessCampgroundService.deleteCampground(campground);
    }

    @PostMapping("/maintenanceCampground")
    public Result maintenanceCampground(
        @RequestBody Map<String, Object> requestBody){
        return businessCampgroundService.maintenanceCampground(requestBody);
    }

    @PostMapping("/campMaintenanceCancel")
    public Result campMaintenanceCancel(
        @RequestBody Map<String, Object> requestBody){
        return businessCampgroundService.campMaintenanceCancel(requestBody);
    }

    @PostMapping("/campMaintenanceComplete")
    public Result campMaintenanceComplete(
        @RequestBody Map<String, Object> requestBody){
        return businessCampgroundService.campMaintenanceComplete(requestBody);
    }

}
