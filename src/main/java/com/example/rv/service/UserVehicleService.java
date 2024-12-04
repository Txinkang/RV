package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.VehiclesReservations;

import java.util.Map;

public interface UserVehicleService {
    /**
     * 搜索查看车辆信息
     * @param requestMap
     * @return Result
     */
    Result checkVehicleList(Map<String, Object> requestMap);

    /**
     * 预定车辆
     * @param vehiclesReservations
     * @return Result
     */
    Result vehicleReservation(VehiclesReservations vehiclesReservations);

    /**
     * 查看已预定车辆
     * @return Result
     */
    Result checkBookedVehicle();

    /**
     * 取消预订车辆
     * @param vehiclesReservations
     * @return Result
     */
    Result vehicleCancelReservation(VehiclesReservations vehiclesReservations);
}
