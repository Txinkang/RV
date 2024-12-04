package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.pojo.CampgroundReservations;
import com.example.rv.pojo.VehiclesReservations;
import com.example.rv.service.UserVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/user")
public class UserVehicleController {
    @Autowired
    private UserVehicleService userVehicleService;

    @PostMapping("/checkVehicleList")
    public Result checkVehicleList(@RequestBody Map<String,Object> requestMap){
        return userVehicleService.checkVehicleList(requestMap);
    }

    @PostMapping("/vehicleReservation")
    public Result vehicleReservation(@RequestBody VehiclesReservations vehiclesReservations){
        return userVehicleService.vehicleReservation(vehiclesReservations);
    }

    @GetMapping("/checkBookedVehicle")
    public Result checkBookedVehicle(){
        return userVehicleService.checkBookedVehicle();
    }

    @PostMapping("/vehicleCancelReservation")
    public Result vehicleCancelReservation(@RequestBody VehiclesReservations vehiclesReservations){
        return userVehicleService.vehicleCancelReservation(vehiclesReservations);
    }
}
