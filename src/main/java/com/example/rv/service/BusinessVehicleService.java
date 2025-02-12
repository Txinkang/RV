package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BusinessVehicleService {
    /**
     * 上传车辆信息
     * @param vehicle
     * @param vehiclePictures
     * @return Result
     */
    Result uploadVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures);

    Result checkVehicleList();

    Result updateVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures);

    Result deleteVehicle(Vehicles vehicle);

    Result maintenanceVehicle(Map<String, Object> requestBody);

    Result checkVehicleLocation(Map<String, Object> requestBody);

    Result returnVehicle(Map<String, Object> requestBody);

    Result vehicleMaintenanceCancel(Map<String, Object> requestBody);

    Result vehicleMaintenanceComplete(Map<String, Object> requestBody);
}


