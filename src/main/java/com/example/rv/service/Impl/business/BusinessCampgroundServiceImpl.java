package com.example.rv.service.Impl.business;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.business.BusinessCampMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.pojo.CampgroundMaintenance;
import com.example.rv.pojo.Users;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.BusinessCampgroundService;
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
public class BusinessCampgroundServiceImpl implements BusinessCampgroundService {
    @Autowired
    private BusinessCampMapper businessCampMapper;

    @Autowired
    private static final LogUtil logUtil = LogUtil.getLogger(BusinessCampgroundServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${uploadFilePath.campgroundPicturesPath}")
    private String campgroundPicturesPath;
   
    @SneakyThrows
    @Override
    public Result uploadCampground(Campground campground, List<MultipartFile> campgroundPictures) {
        //验证参数
        if (campground == null || campgroundPictures.isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }
        if (Strings.isEmpty(campground.getCampgroundName()) || Strings.isEmpty(campground.getCampgroundLocation()) ||
                Strings.isEmpty(campground.getCampgroundFacilityDetails()) || campground.getCampgroundPrice() <= 0
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
            for (MultipartFile campgroundPicture : campgroundPictures) {
                String uniqueFileName = FileUtil.saveFile(campgroundPicture, campgroundPicturesPath);
                if (Strings.isEmpty(uniqueFileName)) {
                    runStep = 1;
                    break;
                }
                fileNames.add(uniqueFileName);
            }
            //获取需要的数据
            Map<String, Object> userMap = ThreadLocalUtil.get();
            if (userMap == null) {
                runStep = 2;
                break;
            }
            Integer ownerId = (Integer) userMap.get("id");
            if (ownerId <= 0) {
                runStep = 3;
                break;
            }
            //将文件名列表转换为 JSON 字符串
            String pictureNamesJson = new ObjectMapper().writeValueAsString(fileNames);
            if (Strings.isEmpty(pictureNamesJson)) {
                runStep = 4;
                break;
            }
            campground.setCampgroundOwnerId(ownerId);
            Integer rowAffected = businessCampMapper.addCampground(campground, pictureNamesJson);
            return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
        } while (false);
        logUtil.error("uploadCampground error in step : ", runStep);
        return new Result(ResultCode.R_Error);
    }

    @Override
    public Result checkCampgroundList() {
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null) {
            return new Result(ResultCode.R_Error);
        }
        List<Campground> campgroundList = businessCampMapper.checkCampgroundList(ownerId);
        if (campgroundList == null) {
            return new Result(ResultCode.R_Error);
        }
        // Convert JSON picture strings to arrays for each campground
        for (Campground campground : campgroundList) {
            try {
                String pictureJson = campground.getCampgroundPicture();
                if (!Strings.isEmpty(pictureJson)) {
                    String[] pictureArray = new ObjectMapper().readValue(pictureJson, String[].class);
                    campground.setCampgroundPicture(Arrays.toString(pictureArray));
                }
            } catch (JsonProcessingException e) {
                logUtil.error("Error parsing campground pictures JSON: ", e);
                return new Result(ResultCode.R_Error); 
            }
        }
        return new Result(ResultCode.R_Ok, campgroundList);
    }

    @SneakyThrows
    @Override
    public Result updateCampground(Campground campground, List<MultipartFile> campgroundPictures){
        //验证参数
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (campground == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (campground.getCampgroundId() < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        // 检查营地
        Campground existingCampground = businessCampMapper.checkCampBycampgroundId(campground.getCampgroundId());
        if (existingCampground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (existingCampground.getCampgroundStatus() == 1) {
            return new Result(ResultCode.R_CampAlreadyReserved);
        }
        if (existingCampground.getCampgroundOwnerId() != ownerId) {
            return new Result(ResultCode.R_CampNotOwner);
        }
        // 传入参数为空时，使用原有数据
        if (campground.getCampgroundName() == null || Strings.isEmpty(campground.getCampgroundName())) {
            campground.setCampgroundName(existingCampground.getCampgroundName());
        }
        if (campground.getCampgroundLocation() == null || Strings.isEmpty(campground.getCampgroundLocation())) {
            campground.setCampgroundLocation(existingCampground.getCampgroundLocation());
        }
        if (campground.getCampgroundFacilityDetails() == null || Strings.isEmpty(campground.getCampgroundFacilityDetails())) {
            campground.setCampgroundFacilityDetails(existingCampground.getCampgroundFacilityDetails());
        }
        if (campground.getCampgroundPrice() <= 0) {
            campground.setCampgroundPrice(existingCampground.getCampgroundPrice());
        }
        // 操作图片
        List<String> pictureNames = new ArrayList<>();
        if (campgroundPictures != null && !campgroundPictures.isEmpty() && !Strings.isEmpty(campgroundPictures.get(0).getOriginalFilename())) {
            for (MultipartFile picture : campgroundPictures) {
                if (picture.isEmpty()) {
                    continue;
                }
                String uniqueFileName = FileUtil.saveFile(picture, campgroundPicturesPath);
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
            campground.setCampgroundPicture(pictureNamesJson);
        }else {
            campground.setCampgroundPicture(existingCampground.getCampgroundPicture());
        }
        // 更新数据库
        Integer rowAffected = businessCampMapper.updateCampground(campground);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result deleteCampground(Campground campground) {
        if (campground == null || campground.getCampgroundId() < 1) {
            return new Result(ResultCode.R_ParamError);
        }   
        Campground existingCampground = businessCampMapper.checkCampBycampgroundId(campground.getCampgroundId());
        if (existingCampground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (existingCampground.getCampgroundStatus() == 1) {
            return new Result(ResultCode.R_CampAlreadyReserved);
        }
        Integer ownerId = ThreadLocalUtil.getUserId();  
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (existingCampground.getCampgroundOwnerId() != ownerId) {
            return new Result(ResultCode.R_CampNotOwner);
        }
        Integer campgroundStatus = 99;
        Integer rowAffected = businessCampMapper.updateCampgroundStatus(campground.getCampgroundId(), campgroundStatus);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result maintenanceCampground(Map<String, Object> requestBody) {
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("campgroundId") || !requestBody.containsKey("maintenanceDetails")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer campgroundId = (Integer) requestBody.get("campgroundId");
        String maintenanceDetails = (String) requestBody.get("maintenanceDetails");
        if (campgroundId < 1 || Strings.isEmpty(maintenanceDetails)) {
            return new Result(ResultCode.R_ParamError);
        }
        Campground campground = businessCampMapper.checkCampBycampgroundId(campgroundId);
        if (campground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (campground.getCampgroundStatus() != 0) {
            return new Result(ResultCode.R_CampNotMaintenance);
        }
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (campground.getCampgroundOwnerId() != ownerId) {
            return new Result(ResultCode.R_CampNotOwner);
        }
        Integer maintenanceRowAffected = businessCampMapper.addCampgroundMaintenance(campgroundId, maintenanceDetails);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        Integer campgroundStatus = 2;
        Integer rowAffected = businessCampMapper.updateCampgroundStatus(campgroundId, campgroundStatus);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result campMaintenanceCancel(Map<String, Object> requestBody) {
        //验证参数
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("campgroundId")) {
            return new Result(ResultCode.R_ParamError);
        }   
        Integer campgroundId = (Integer) requestBody.get("campgroundId");
        if (campgroundId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        //验证营地
        Campground campground = businessCampMapper.checkCampBycampgroundId(campgroundId);
        if (campground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (campground.getCampgroundStatus() != 2) {
            return new Result(ResultCode.R_CampNotMaintenance);
        }
        //验证用户
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (campground.getCampgroundOwnerId() != ownerId) {
            return new Result(ResultCode.R_CampNotOwner);   
        }
        // 更新营地状态
        Integer rowAffected = businessCampMapper.updateCampgroundStatus(campgroundId, 0);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        // 查找维护信息
        Integer maintenanceStatus = 0;
        CampgroundMaintenance maintenance = businessCampMapper.checkCampgroundMaintenance(campgroundId, maintenanceStatus);
        if (maintenance == null) {
            return new Result(ResultCode.R_CampMaintenanceNotFound);
        }
        // 更新维护信息
        Integer campMaintenceStatus = 2;
        Integer maintenanceRowAffected = businessCampMapper.updateCampgroundMaintenanceStatus(maintenance.getCampgroundMaintenanceId(), campMaintenceStatus);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result campMaintenanceComplete(Map<String, Object> requestBody) {
        //验证参数
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("campgroundId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer campgroundId = (Integer) requestBody.get("campgroundId");
        if (campgroundId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        //验证营地
        Campground campground = businessCampMapper.checkCampBycampgroundId(campgroundId);
        if (campground == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (campground.getCampgroundStatus() != 2) {
            return new Result(ResultCode.R_CampNotMaintenance);
        }
        //验证用户
        Integer ownerId = ThreadLocalUtil.getUserId();
        if (ownerId == null || ownerId <= 0) {
            return new Result(ResultCode.R_Error);
        }
        if (campground.getCampgroundOwnerId() != ownerId) {
            return new Result(ResultCode.R_CampNotOwner);
        }
        //更新营地状态
        Integer campgroundStatus = 0;
        Integer rowAffected = businessCampMapper.updateCampgroundStatus(campgroundId, campgroundStatus);
        if (rowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        // 查找维护信息
        Integer maintenanceStatus = 0;
        CampgroundMaintenance maintenance = businessCampMapper.checkCampgroundMaintenance(campgroundId, maintenanceStatus);
        if (maintenance == null) {
            return new Result(ResultCode.R_CampMaintenanceNotFound);
        }
        // 更新维护信息
        Integer campMaintenceStatus = 1;
        Integer maintenanceRowAffected = businessCampMapper.updateCampgroundMaintenanceStatus(maintenance.getCampgroundMaintenanceId(), campMaintenceStatus);
        if (maintenanceRowAffected <= 0) {
            return new Result(ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result returnCamp(Map<String, Object> requestBody) {
        if (requestBody == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (!requestBody.containsKey("campgroundId")) {
            return new Result(ResultCode.R_ParamError);
        }
        Integer campgroundId = (Integer) requestBody.get("campgroundId");
        if (campgroundId < 1) {
            return new Result(ResultCode.R_ParamError);
        }
        Campground camp = businessCampMapper.checkCampBycampgroundId(campgroundId);
        if (camp == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        if (camp.getCampgroundStatus() != 0 && camp.getCampgroundStatus() != 1) {
            return new Result(ResultCode.R_Fail);
        }
        Integer rowAffected = businessCampMapper.updateCampgroundStatus(campgroundId, 0);
        return new Result(rowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }
}

