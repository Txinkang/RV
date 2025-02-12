package com.example.rv.controller.business;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.BusinessVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/business")
public class BusinessVehicleController {
    @Autowired
    private BusinessVehicleService businessVehicleService;

    @PostMapping("/uploadVehicle")
    public Result uploadVehicle(
            @RequestPart("vehicle") Vehicles vehicle,
            @RequestParam("vehiclePictures") List<MultipartFile> vehiclePictures){
        return businessVehicleService.uploadVehicle(vehicle,vehiclePictures);
    }

    @GetMapping("/checkVehicleList")
    public Result checkVehicleList(){
        return businessVehicleService.checkVehicleList();
    }

    @PatchMapping("/updateVehicle")
    public Result updateVehicle(
            @RequestPart("vehicle") Vehicles vehicle,
            @RequestParam("vehiclePictures") List<MultipartFile> vehiclePictures) {
        return businessVehicleService.updateVehicle(vehicle, vehiclePictures);
    }

    @DeleteMapping("/deleteVehicle")
    public Result deleteVehicle(@RequestBody Vehicles vehicle) {
        return businessVehicleService.deleteVehicle(vehicle);
    }

    @PostMapping("/maintenanceVehicle")
    public Result maintenanceVehicle(@RequestBody Map<String, Object> requestBody) {
        return businessVehicleService.maintenanceVehicle(requestBody);
    }

    @PostMapping("/checkVehicleLocation")
    public Result checkVehicleLocation(@RequestBody Map<String, Object> requestBody) {
        return businessVehicleService.checkVehicleLocation(requestBody);
    }

    @PostMapping("/returnVehicle")
    public Result returnVehicle(@RequestBody Map<String, Object> requestBody) {
        return businessVehicleService.returnVehicle(requestBody);
    }

    @PostMapping("/vehicleMaintenanceCancel")
    public Result vehicleMaintenanceCancel(@RequestBody Map<String, Object> requestBody) {
        return businessVehicleService.vehicleMaintenanceCancel(requestBody);
    }

    @PostMapping("/vehicleMaintenanceComplete")
    public Result vehicleMaintenanceComplete(@RequestBody Map<String, Object> requestBody) {
        return businessVehicleService.vehicleMaintenanceComplete(requestBody);
    }
}
