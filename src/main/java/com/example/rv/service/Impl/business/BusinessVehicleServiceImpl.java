package com.example.rv.service.Impl.business;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.business.BusinessVehicleMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.pojo.Users;
import com.example.rv.pojo.VehicleMaintenance;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.BusinessVehicleService;
import com.example.rv.utils.FileUtil;
import com.example.rv.utils.LogUtil;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BusinessVehicleServiceImpl implements BusinessVehicleService {
    public static final LogUtil logUtil = LogUtil.getLogger(BusinessVehicleServiceImpl.class);

    @Value("${uploadFilePath.vehiclePicturesPath}")
    private String vehiclePicturesPath;
    
    @Autowired
    private BusinessVehicleMapper businessVehicleMapper;

    @Autowired
    private UserMapper userMapper;

    @SneakyThrows
    @Override
    public Result uploadVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures) {
        //验证参数
        if (vehicle == null || vehiclePictures.isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }
        boolean vehicleType = "A".equals(vehicle.getVehicleType()) || "B".equals(vehicle.getVehicleType());
        if (!vehicleType || Strings.isEmpty(vehicle.getVehicleType()) || Strings.isEmpty(vehicle.getVehicleLocation()) ||
                Strings.isEmpty(vehicle.getVehicleDescription()) || vehicle.getVehiclePrice() <= 0
        ) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null || userId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        Users user = userMapper.findByUserId(userId);
        if (user == null) {
            return new Result(ResultCode.R_Error);
        }
        if (user.getUserRole() != 1) {
            return new Result(ResultCode.R_NoAuthority);
        }
        //操作文件
        List<String> fileNames = new ArrayList<>();
        int runStep = 0;
        do {
            //拿取所有照片文件
            for (MultipartFile vehiclePicture : vehiclePictures) {
                String uniqueFileName = FileUtil.saveFile(vehiclePicture, vehiclePicturesPath);
                if (uniqueFileName == null) {
                    return new Result(ResultCode.R_SaveFileError);
                }
                fileNames.add(uniqueFileName);
            }
            //获取需要的数据
            Map<String, Object> userMap = ThreadLocalUtil.get();
            if (userMap == null) {
                runStep = 1;
                break;
            }
            Integer ownerId = (Integer) userMap.get("id");
            if (ownerId <= 0) {
                runStep = 2;
                break;
            }
            //将文件名列表转换为 JSON 字符串
            String pictureNamesJson = new ObjectMapper().writeValueAsString(fileNames);
            if (Strings.isEmpty(pictureNamesJson)) {
                runStep = 3;
                break;
            }
            vehicle.setVehicleOwnerId(ownerId);
            Integer rowAffected = businessVehicleMapper.addVehicle(vehicle, pictureNamesJson);
            return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
        } while (false);
        logUtil.error("uploadVehicle error in step : ", runStep);
        return new Result(ResultCode.R_Error);
    }


    @Override
    public Result checkVehicleList() {
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null) {
            return new Result(ResultCode.R_Error);
        }
        List<Vehicles> vehicleList = businessVehicleMapper.checkVehicleList(ownerId);
        if (vehicleList == null) {
            return new Result(ResultCode.R_Error);
        }
        // Convert JSON picture strings to arrays for each vehicle
        for (Vehicles vehicle : vehicleList) {
            try {
                String pictureJson = vehicle.getVehiclePicture();
                if (!Strings.isEmpty(pictureJson)) {
                    String[] pictureArray = new ObjectMapper().readValue(pictureJson, String[].class);
                    vehicle.setVehiclePicture(Arrays.toString(pictureArray));
                }
            } catch (JsonProcessingException e) {
                logUtil.error("Error parsing vehicle pictures JSON: ", e);
                return new Result(ResultCode.R_Error);
            }
        }
        return new Result(ResultCode.R_Ok, vehicleList);
    }

    @SneakyThrows
    @Override
    public Result updateVehicle(Vehicles vehicle, List<MultipartFile> vehiclePictures) {
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null) {
            return new Result(ResultCode.R_Error);
        }
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
        if (existingVehicle.getVehicleOwnerId() != ownerId) {
            return new Result(ResultCode.R_VehicleNotOwner);
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
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (existingVehicle.getVehicleOwnerId() != ownerId) {
            return new Result(ResultCode.R_VehicleNotOwner);
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
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (vehicle.getVehicleOwnerId() != ownerId) {
            return new Result(ResultCode.R_VehicleNotOwner);
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
        // 验证用户
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (vehicle.getVehicleOwnerId() != ownerId) {
            return new Result(ResultCode.R_VehicleNotOwner);
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
        // 验证用户
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (vehicle.getVehicleOwnerId() != ownerId) {
            return new Result(ResultCode.R_VehicleNotOwner);
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


