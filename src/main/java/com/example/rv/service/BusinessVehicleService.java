package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Vehicles;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BusinessVehicleService {
    /**
     * 上传车辆信息
     * @param vehicle
     * @param vehiclePictures
     * @return Result
     */
    Result uploadVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures);
}
