package com.example.rv.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;

public interface AdminVehicleService {

    Result checkVehicleList(Map<String,Object> requestMap);

    Result updateVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures);

    Result deleteVehicle(Vehicles vehicle);

    Result maintenanceVehicle(Map<String,Object> requestBody);

    Result checkVehicleLocation(Map<String,Object> requestBody);

    Result returnVehicle(Map<String,Object> requestBody);

    Result vehicleMaintenanceCancel(Map<String,Object> requestBody);

    Result vehicleMaintenanceComplete(Map<String,Object> requestBody);
}
