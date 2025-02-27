package com.example.rv.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;
import com.example.rv.service.AdminCampService;

@RestController
@RequestMapping("/admin")
public class AdminCampController {

    @Autowired
    private AdminCampService adminCampService;

    @PostMapping("/checkCampgroundList")
    public Result checkCampgroundList(@RequestBody Map<String,Object> requestMap){
        return adminCampService.checkCampgroundList(requestMap);
    }

    @PostMapping("/updateCampground")
    public Result updateCampground(
        @RequestPart("campground") Campground campground,
        @RequestParam(value = "campgroundPictures", required = false) List<MultipartFile> campgroundPictures){
        return adminCampService.updateCampground(campground, campgroundPictures);
    }

    @DeleteMapping("/deleteCampground")
    public Result deleteCampground(
        @RequestBody Campground campground){
        return adminCampService.deleteCampground(campground);
    }

    @PostMapping("/maintenanceCampground")
    public Result maintenanceCampground(
        @RequestBody Map<String, Object> requestBody){
        return adminCampService.maintenanceCampground(requestBody);
    }

    @PostMapping("/campMaintenanceCancel")
    public Result campMaintenanceCancel(
        @RequestBody Map<String, Object> requestBody){
        return adminCampService.campMaintenanceCancel(requestBody);
    }

    @PostMapping("/campMaintenanceComplete")
    public Result campMaintenanceComplete(
        @RequestBody Map<String, Object> requestBody){
        return adminCampService.campMaintenanceComplete(requestBody);
    }
}
