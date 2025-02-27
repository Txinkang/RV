package com.example.rv.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.AdminVehicleService;
@RestController
@RequestMapping("/admin")
public class AdminVehicleController {

    @Autowired
    private AdminVehicleService adminVehicleService;

    @PostMapping("/checkVehicleList")
    public Result checkVehicleList(@RequestBody Map<String,Object> requestMap){
        return adminVehicleService.checkVehicleList(requestMap);
    }
    @PostMapping("/updateVehicle")
    public Result updateVehicle(
            @RequestPart("vehicle") Vehicles vehicle,
            @RequestParam(value = "vehiclePictures", required = false) List<MultipartFile> vehiclePictures) {
        return adminVehicleService.updateVehicle(vehicle, vehiclePictures);
    }

    @DeleteMapping("/deleteVehicle")
    public Result deleteVehicle(@RequestBody Vehicles vehicle) {
        return adminVehicleService.deleteVehicle(vehicle);
    }

    @PostMapping("/maintenanceVehicle")
    public Result maintenanceVehicle(@RequestBody Map<String, Object> requestBody) {
        return adminVehicleService.maintenanceVehicle(requestBody);
    }

    @PostMapping("/checkVehicleLocation")
    public Result checkVehicleLocation(@RequestBody Map<String, Object> requestBody) {
        return adminVehicleService.checkVehicleLocation(requestBody);
    }

    @PostMapping("/returnVehicle")
    public Result returnVehicle(@RequestBody Map<String, Object> requestBody) {
        return adminVehicleService.returnVehicle(requestBody);
    }

    @PostMapping("/vehicleMaintenanceCancel")
    public Result vehicleMaintenanceCancel(@RequestBody Map<String, Object> requestBody) {
        return adminVehicleService.vehicleMaintenanceCancel(requestBody);
    }

    @PostMapping("/vehicleMaintenanceComplete")
    public Result vehicleMaintenanceComplete(@RequestBody Map<String, Object> requestBody) {
        return adminVehicleService.vehicleMaintenanceComplete(requestBody);
    }
}
