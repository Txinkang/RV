package com.example.rv.controller.business;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.BusinessVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

}
