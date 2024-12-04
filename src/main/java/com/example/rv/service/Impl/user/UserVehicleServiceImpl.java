package com.example.rv.service.Impl.user;

import com.example.rv.Response.PageResponse;
import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserVehicleMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.pojo.CampgroundReservations;
import com.example.rv.pojo.Vehicles;
import com.example.rv.pojo.VehiclesReservations;
import com.example.rv.service.UserVehicleService;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class UserVehicleServiceImpl implements UserVehicleService {
    @Autowired
    private UserVehicleMapper userVehicleMapper;
    @Autowired
    private UserMapper userMapper;
    //把json转换成vehicle类对象
    private Vehicles convertToVehicle(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, Vehicles.class);
    }
    @Override
    public Result checkVehicleList(Map<String, Object> requestMap) {
        Object requestVehicle = requestMap.get("vehicle");
        Vehicles vehicle = convertToVehicle(requestVehicle);
        Integer pageNum = (Integer) requestMap.get("pageNum");
        Integer pageSize = (Integer) requestMap.get("pageSize");
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_ParamError);
        }
        if (vehicle.getVehiclePrice() < 0) {
            return new Result(ResultCode.R_ParamError);
        }
        //设置分页查询
        PageResponse<Vehicles> pageResponse = new PageResponse<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Vehicles> vehicleList;
        //判断传没传status
        Map<String, Object> checkVehicle = (Map<String, Object>) requestVehicle;
        if (checkVehicle == null) {
            return new Result(ResultCode.R_Fail);
        }
        if (checkVehicle.containsKey("vehicleStatus")) {
            //只允许查询状态为0～2的
            if (vehicle.getVehicleStatus() < 0 || vehicle.getVehicleStatus() > 2) {
                return new Result(ResultCode.R_ParamError);
            }
            vehicleList = userVehicleMapper.findVehicleHasStatus(vehicle);
        } else {
            vehicleList = userVehicleMapper.findVehicleNoStatus(vehicle);
        }
        Page<Vehicles> vehiclePage = (Page<Vehicles>) vehicleList;
        pageResponse.setTotal(vehiclePage.getTotal());
        pageResponse.setItems(vehiclePage.getResult());
        return new Result(ResultCode.R_Ok, pageResponse);
    }

    @Override
    public Result vehicleReservation(VehiclesReservations vehiclesReservations) {
        int VehicleId = vehiclesReservations.getVehicleReservationVehicleId();
        Timestamp VehicleStartDate = vehiclesReservations.getVehicleReservationStartDate();
        Timestamp VehicleEndDate = vehiclesReservations.getVehicleReservationEndDate();
        double VehicleTotalPrice = vehiclesReservations.getVehicleReservationTotalPrice();
        //验证车辆信息
        Vehicles queryVehicle = userVehicleMapper.findVehicleByVehicleId(VehicleId);
        if (VehicleId <= 0 || queryVehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        int vehicleStatus = queryVehicle.getVehicleStatus();
        if (vehicleStatus != 0) {
            return new Result(ResultCode.R_VehicleAlreadyReserved);
        }
        double vehiclePrice = queryVehicle.getVehiclePrice();
        if (VehicleTotalPrice < vehiclePrice) {
            return new Result(ResultCode.R_PriceIsLow);
        }
        //获取当前时间进行对比
        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTime);
        if (currentTimestamp.after(VehicleStartDate) || VehicleStartDate.after(VehicleEndDate)) {
            return new Result(ResultCode.R_DateError);
        }
        //验证预定人是否合格
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer renterId = (Integer) userMap.get("id");
        if (renterId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(renterId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        VehiclesReservations isReserved = userVehicleMapper.checkReservationByRenterId(renterId);
        if (isReserved != null) {
            return new Result(ResultCode.R_IsReserved);
        }
        //开始预定
        Integer reserveRowAffected = userVehicleMapper.reserveVehicle(vehiclesReservations, renterId);
        if (reserveRowAffected > 0) {
            int Status = 1;
            Integer statusRowAffected = userVehicleMapper.updateVehicleStatusByVehicleId(VehicleId, Status);
            return new Result(statusRowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result checkBookedVehicle() {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer renterId = (Integer) userMap.get("id");
        if (renterId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(renterId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        VehiclesReservations queryReservation = userVehicleMapper.checkReservationByRenterId(renterId);
        if (queryReservation == null) {
            return new Result(ResultCode.R_UserNotReserved);
        }
        return new Result(ResultCode.R_Ok, queryReservation);
    }

    @Override
    public Result vehicleCancelReservation(VehiclesReservations vehiclesReservations) {
        //校验参数
        if(vehiclesReservations == null){
            return new Result(ResultCode.R_ParamError);
        }
        int vehicleReservationId = vehiclesReservations.getVehicleReservationId();
        if (vehicleReservationId <= 0){
            return new Result(ResultCode.R_ParamError);
        }
        //验证用户
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer userId = (Integer) userMap.get("id");
        if (userId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer queryUser = userMapper.checkUserByUserId(userId);
        if (queryUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        //取消预约
        VehiclesReservations queryVehicleReservation = userVehicleMapper.findReservationById(vehicleReservationId);
        if (queryVehicleReservation == null) {
            return new Result(ResultCode.R_ReservationNotFound);
        }
        int vehicleId = queryVehicleReservation.getVehicleReservationVehicleId();
        int vehicleReservationStatus = 1;
        int vehicleStatus = 0;
        Integer cancel = userVehicleMapper.cancelReservationById(vehicleReservationId,vehicleId,vehicleReservationStatus,vehicleStatus);
        return new Result(cancel > 0 ?ResultCode.R_Ok:ResultCode.R_UpdateDbFailed);
    }
}
