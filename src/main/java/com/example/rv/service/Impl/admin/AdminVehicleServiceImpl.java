package com.example.rv.service.Impl.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.rv.utils.FileUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.rv.Response.PageResponse;
import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.admin.AdminVehicleMapper;
import com.example.rv.mapper.business.BusinessVehicleMapper;
import com.example.rv.mapper.user.UserVehicleMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.pojo.VehicleMaintenance;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.AdminVehicleService;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.SneakyThrows;

@Service
public class AdminVehicleServiceImpl implements AdminVehicleService {
    //把json转换成vehicle类对象
    private Vehicles convertToVehicle(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, Vehicles.class);
    }

    @Autowired
    private AdminVehicleMapper adminVehicleMapper;

    @Autowired
    private UserVehicleMapper userVehicleMapper;

    @Autowired
    private BusinessVehicleMapper businessVehicleMapper;

    @Value("${uploadFilePath.vehiclePicturesPath}")
    private String vehiclePicturesPath;
    @Override
    public Result checkVehicleList(Map<String, Object> requestMap) {
        //验证参数
        if (requestMap == null) {
            return new Result(ResultCode.R_ParamError);
        }
        
        //获取分页参数
        Integer pageNum = (Integer) requestMap.get("pageNum");
        Integer pageSize = (Integer) requestMap.get("pageSize");
        if (pageNum == null || pageSize == null || pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_ParamError);
        }

        //获取营地查询条件
        Object requestVehicle = requestMap.get("vehicle");
        if (requestVehicle == null) {
            return new Result(ResultCode.R_ParamError); 
        }
        Vehicles vehicle = convertToVehicle(requestVehicle);
        if (vehicle.getVehiclePrice() < 0) {
            return new Result(ResultCode.R_ParamError);
        }

        //设置分页查询
        PageResponse<Vehicles> pageResponse = new PageResponse<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Vehicles> vehicleList;
        //判断传没传status
        if (!(requestVehicle instanceof Map)) {
            return new Result(ResultCode.R_Fail);
        }
        Map<String, Object> checkVehicle = (Map<String, Object>) requestVehicle;
        if (checkVehicle.containsKey("vehicleStatus")) {
            vehicleList = userVehicleMapper.findVehicleHasStatus(vehicle);
        } else {
            vehicleList = userVehicleMapper.findVehicleNoStatus(vehicle);
        }
        Page<Vehicles> vehiclePage = (Page<Vehicles>) vehicleList;
        try {
            pageResponse.setTotal(vehiclePage.getTotal());
            pageResponse.setItems(vehiclePage.getResult());
            return new Result(ResultCode.R_Ok, pageResponse);
        } finally {
            vehiclePage.close();
        }
    }

    @SneakyThrows
    @Override
    public Result updateVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures) {
        if (vehicle == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (vehicle.getVehicleId() < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        // 检查车辆
        Vehicles existingVehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicle.getVehicleId());
        if (existingVehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (existingVehicle.getVehicleStatus() == 1) {
            return new Result(ResultCode.R_VehicleAlreadyReserved);
        }
        
        // 传入参数为空时，使用原有数据
        if (vehicle.getVehiclePrice() <= 0) {
            vehicle.setVehiclePrice(existingVehicle.getVehiclePrice());
        }
        if (vehicle.getVehicleLocation() == null || Strings.isEmpty(vehicle.getVehicleLocation())) {
            vehicle.setVehicleLocation(existingVehicle.getVehicleLocation());
        }
        if (vehicle.getVehicleDescription() == null || Strings.isEmpty(vehicle.getVehicleDescription())) {
            vehicle.setVehicleDescription(existingVehicle.getVehicleDescription());
        }
        if (vehicle.getVehicleType() == null || 
            (!("A".equals(vehicle.getVehicleType()) || "B".equals(vehicle.getVehicleType())))) {
            vehicle.setVehicleType(existingVehicle.getVehicleType());
        }
        
        // 操作图片
        List<String> pictureNames = new ArrayList<>();
        if (vehiclePictures != null && !vehiclePictures.isEmpty() && !Strings.isEmpty(vehiclePictures.get(0).getOriginalFilename())) {
            for (MultipartFile picture : vehiclePictures) {
                if (picture.isEmpty()) {
                    continue;
                }
                String uniqueFileName = FileUtil.saveFile(picture, vehiclePicturesPath);
                if (Strings.isEmpty(uniqueFileName)) {
                    break;
                }
                pictureNames.add(uniqueFileName);
//                String originalFilename = picture.getOriginalFilename();
//                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//                String newFileName = UUID.randomUUID().toString() + extension;
//                pictureNames.add(newFileName);
            }
            //将文件名列表转换为 JSON 字符串
            String pictureNamesJson = new ObjectMapper().writeValueAsString(pictureNames);
            if (Strings.isEmpty(pictureNamesJson)) {
                    return new Result(ResultCode.R_Error);
            }
            vehicle.setVehiclePicture(pictureNamesJson);
        }else {
            vehicle.setVehiclePicture(existingVehicle.getVehiclePicture());
        }
        // 更新车辆
        Integer rowAffected = businessVehicleMapper.updateVehicle(vehicle);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result deleteVehicle(Vehicles vehicle) {
        if (vehicle == null || vehicle.getVehicleId() < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        Vehicles existingVehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicle.getVehicleId());
        if (existingVehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (existingVehicle.getVehicleStatus() == 1) {
            return new Result(ResultCode.R_VehicleAlreadyReserved);
        }
        Integer vehicleStatus = 99;
        Integer rowAffected = businessVehicleMapper.updateVehicleStatus(vehicle.getVehicleId(), vehicleStatus);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result maintenanceVehicle(Map<String, Object> requestBody) {
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("vehicleId") || !requestBody.containsKey("maintenanceDetails")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        String maintenanceDetails = (String) requestBody.get("maintenanceDetails");
        if (vehicleId < 1 || Strings.isEmpty(maintenanceDetails)) {
            return new Result(ResultCode.R_ParamError);
        }
        Vehicles vehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (vehicle.getVehicleStatus() != 0) {
            return new Result(ResultCode.R_VehicleNotMaintenance);
        }
        Integer maintenanceRowAffected = businessVehicleMapper.addVehicleMaintenance(vehicleId, maintenanceDetails);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        Integer vehicleStatus = 2;
        Integer rowAffected = businessVehicleMapper.updateVehicleStatus(vehicleId, vehicleStatus);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }


    @Override
    public Result checkVehicleLocation(Map<String, Object> requestBody) {
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("vehicleId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        if (vehicleId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        Vehicles vehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        return new Result(ResultCode.R_Ok, vehicle.getVehicleLocation());
    }


    @Override
    public Result returnVehicle(Map<String, Object> requestBody) {
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("vehicleId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        if (vehicleId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        Vehicles vehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (vehicle.getVehicleStatus() != 0 && vehicle.getVehicleStatus() != 1) {
            return new Result(ResultCode.R_Fail);
        }
        Integer rowAffected = businessVehicleMapper.updateVehicleStatus(vehicleId, 0);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }


    @Override
    public Result vehicleMaintenanceCancel(Map<String, Object> requestBody) {
        // 验证参数
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("vehicleId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        if (vehicleId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证车辆
        Vehicles vehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (vehicle.getVehicleStatus() != 2) {
            return new Result(ResultCode.R_VehicleNotMaintenance);
        }
        // 更新车辆状态
        Integer rowAffected = businessVehicleMapper.updateVehicleStatus(vehicleId, 0);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        // 查找维护信息
        Integer maintenanceStatus = 0;
        VehicleMaintenance maintenance = businessVehicleMapper.checkVehicleMaintenance(vehicleId, maintenanceStatus);
        if (maintenance == null) {
            return new Result(ResultCode.R_VehicleMaintenanceNotFound);
        }
        // 更新维护信息
        Integer vehicleMaintenceStatus = 2;
        Integer maintenanceRowAffected = businessVehicleMapper.updateVehicleMaintenanceStatus(maintenance.getVehicleMaintenanceId(), vehicleMaintenceStatus);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }


    @Override
    public Result vehicleMaintenanceComplete(Map<String, Object> requestBody) {
        //验证参数
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("vehicleId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer vehicleId = (Integer) requestBody.get("vehicleId");
        if (vehicleId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证车辆
        Vehicles vehicle = businessVehicleMapper.checkVehicleByvehicleId(vehicleId);
        if (vehicle == null) {
            return new Result(ResultCode.R_VehicleNotFound);
        }
        if (vehicle.getVehicleStatus() != 2) {
            return new Result(ResultCode.R_VehicleNotMaintenance);
        }
        // 更新车辆状态
        Integer rowAffected = businessVehicleMapper.updateVehicleStatus(vehicleId, 0);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        // 查找维护信息
        Integer maintenanceStatus = 0;
        VehicleMaintenance maintenance = businessVehicleMapper.checkVehicleMaintenance(vehicleId, maintenanceStatus);
        if (maintenance == null) {
            return new Result(ResultCode.R_VehicleMaintenanceNotFound);
        }
        // 更新维护信息
        Integer vehicleMaintenceStatus = 1;
        Integer maintenanceRowAffected = businessVehicleMapper.updateVehicleMaintenanceStatus(maintenance.getVehicleMaintenanceId(), vehicleMaintenceStatus);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }
}
