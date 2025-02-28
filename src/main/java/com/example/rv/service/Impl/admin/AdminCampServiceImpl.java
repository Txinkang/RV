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
import com.example.rv.mapper.admin.AdminCampMapper;
import com.example.rv.mapper.business.BusinessCampMapper;
import com.example.rv.mapper.user.UserCampgroundMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.pojo.CampgroundMaintenance;
import com.example.rv.service.AdminCampService;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.SneakyThrows;

@Service
public class AdminCampServiceImpl implements AdminCampService {

    //把json转换成campground类对象
    private Campground convertToCampground(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, Campground.class);
    }

    @Autowired
    private AdminCampMapper adminCampMapper;

    @Autowired
    private UserCampgroundMapper userCampgroundMapper;

    @Autowired
    private BusinessCampMapper businessCampMapper;

    @Value("${uploadFilePath.campgroundPicturesPath}")
    private String campgroundPicturesPath;
    @Override
    public Result checkCampgroundList(Map<String, Object> requestMap) {
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
        Object requestCampground = requestMap.get("campground");
        if (requestCampground == null) {
            return new Result(ResultCode.R_ParamError); 
        }
        Campground campground = convertToCampground(requestCampground);
        if (campground.getCampgroundPrice() < 0) {
            return new Result(ResultCode.R_ParamError);
        }
        //设置分页查询
        PageResponse<Campground> pageResponse = new PageResponse<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Campground> campgroundList;
        //判断传没传status
        if (!(requestCampground instanceof Map)) {
            return new Result(ResultCode.R_Fail);
        }
        Map<String, Object> checkCampground = (Map<String, Object>) requestCampground;
        if (checkCampground.containsKey("campgroundStatus")) {
            campgroundList = userCampgroundMapper.findCampHasStatus(campground);
        } else {
            campgroundList = userCampgroundMapper.findCampNoStatus(campground);
        }
        Page<Campground> campgroundPage = (Page<Campground>) campgroundList;
        try {
            pageResponse.setTotal(campgroundPage.getTotal());
            pageResponse.setItems(campgroundPage.getResult());
            return new Result(ResultCode.R_Ok, pageResponse);
        } finally {
            campgroundPage.close();
        }
    }

    @SneakyThrows
    @Override
    public Result updateCampground(Campground campground, List<MultipartFile> campgroundPictures){
        //验证参数
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
