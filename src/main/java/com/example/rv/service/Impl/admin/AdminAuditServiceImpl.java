package com.example.rv.service.Impl.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.admin.AdminAuditMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.AdminAuditService;

@Service
public class AdminAuditServiceImpl implements AdminAuditService {

    @Autowired
    private AdminAuditMapper adminAuditMapper;

    @Override
    public Result getAuditCampgroundList() {
        Integer campgroundStatus = 3;
        List<Campground> campgroundList = adminAuditMapper.findCampgroundByStatus(campgroundStatus);
        return new Result(ResultCode.R_Ok, campgroundList);
    }

    @Override
    public Result auditCampground(Map<String, Object> requestBody) {
        // 获取参数
        // status1:不通过 2:通过
        Integer campgroundId = (Integer) requestBody.get("campgroundId");
        Integer status = (Integer) requestBody.get("status");
        String auditFailedMsg = (String) requestBody.get("auditFailedMsg");

        // 参数校验
        if (campgroundId == null || status == null || (status != 1 && status != 2)) {
            return new Result(ResultCode.R_ParamError);
        }

        // 查询营地是否存在
        Campground campground = adminAuditMapper.findCampgroundById(campgroundId);
        if (campground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }

        // 根据审核状态更新营地状态
        Integer campgroundStatus = status == 1 ? 4 : 0;
        
        // 更新营地状态和审核失败消息
        if(status == 2){
            auditFailedMsg = "";
        }
        Integer result = adminAuditMapper.updateCampgroundStatus(campgroundId, campgroundStatus, auditFailedMsg);
        return new Result(result == 1 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result getAuditVehicleList() {
        Integer vehicleStatus = 3;
        List<Vehicles> vehicleList = adminAuditMapper.findVehicleByStatus(vehicleStatus);
        return new Result(ResultCode.R_Ok, vehicleList);
    }

    @Override
    public Result auditVehicle(Map<String, Object> requestBody) {
        // 获取参数
        // status1:不通过 2:通过
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        Integer status = (Integer) requestBody.get("status");
        String auditFailedMsg = (String) requestBody.get("auditFailedMsg");

        // 参数校验
        if (vehicleId == null || status == null || (status != 1 && status != 2)) {
            return new Result(ResultCode.R_ParamError);
        }

        // 查询车辆是否存在
        Vehicles vehicle = adminAuditMapper.findVehicleById(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_CampNotFound);
        }

        // 根据审核状态更新车辆状态
        Integer vehicleStatus = status == 1 ? 4 : 0;
        
        // 更新车辆状态和审核失败消息
        if(status == 2){
            auditFailedMsg = "";
        }
        Integer result = adminAuditMapper.updateVehicleStatus(vehicleId, vehicleStatus, auditFailedMsg);
        return new Result(result == 1 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }
}


