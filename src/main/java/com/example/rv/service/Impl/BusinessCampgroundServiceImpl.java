package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.constData.UploadFileConstData;
import com.example.rv.mapper.BusinessCampMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.service.BusinessCampgroundService;
import com.example.rv.utils.FileUtil;
import com.example.rv.utils.LogUtil;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class BusinessCampgroundServiceImpl implements BusinessCampgroundService {
    @Autowired
    private BusinessCampMapper businessCampMapper;
    @Autowired
    private static final LogUtil logUtil = LogUtil.getLogger(BusinessCampgroundServiceImpl.class);
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
                Strings.isEmpty(campground.getCampgroundFacilityDetails()) || campground.getCampgroundPrice() < 0
        ) {
            return new Result(ResultCode.R_ParamError);
        }
        String campgroundName = businessCampMapper.checkCampBycampgroundName(campground.getCampgroundName());
        String campgroundLocation = businessCampMapper.checkCampBycampgroundLocation(campground.getCampgroundLocation());
        if (!Strings.isEmpty(campgroundName) || !Strings.isEmpty(campgroundLocation)){
            return new Result(ResultCode.R_CampIsExist);
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
        return null;
    }
}
