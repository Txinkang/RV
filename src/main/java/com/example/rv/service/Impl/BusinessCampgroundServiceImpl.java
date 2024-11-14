package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.constData.UploadFileConstData;
import com.example.rv.pojo.Campground;
import com.example.rv.service.BusinessCampgroundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BusinessCampgroundServiceImpl implements BusinessCampgroundService {
    @SneakyThrows
    @Override
    public Result uploadCampground(Campground campground, List<MultipartFile> campgroundPictures) {
        List<String> fileNames = new ArrayList<>();
        int runStep = 0;
        //验证参数
        if (campground == null || campgroundPictures.isEmpty()){
            runStep = 1;
            return new Result(ResultCode.R_ParamError);
        }
        if (Strings.isEmpty(campground.getCampgroundName()) || Strings.isEmpty(campground.getCampgroundLocation()) ||
            Strings.isEmpty(campground.getCampgroundFacilityDetails()) || campground.getCampgroundPrice()< 0
        ){
            runStep = 2;
            return new Result(ResultCode.R_ParamError);
        }
        //拿取所有照片文件
        for (MultipartFile campgroundPicture : campgroundPictures){
            if (campgroundPicture.isEmpty()){
                runStep = 3;
                return new Result(ResultCode.R_Error);
            }
            //生成文件名
            String originalFilename = campgroundPicture.getOriginalFilename();
            if (Strings.isEmpty(originalFilename)){
                runStep = 4;
                return new Result(ResultCode.R_Error);
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (Strings.isEmpty(fileExtension)){
                runStep = 5;
                return new Result(ResultCode.R_Error);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            if (Strings.isEmpty(uniqueFileName)){
                runStep = 6;
                return new Result(ResultCode.R_Error);
            }
            //生成文件保存路径
            File directory = new File(UploadFileConstData.CAMP_PICTURES_SAVED_PATH);
            if (!directory.exists()){
                if (!directory.mkdirs()){
                    return new Result(ResultCode.R_Error);
                }
            }
            File fileToSave = new File(directory , uniqueFileName);
            campgroundPicture.transferTo(fileToSave);
            fileNames.add(uniqueFileName);
            //生成文件url
            //String fileUrl = "/images/" + uniqueFileName;
        }
        // 将文件名列表转换为 JSON 字符串
        String fileNamesJson = new ObjectMapper().writeValueAsString(fileNames);
        //写入数据库
        return new Result(ResultCode.R_Ok);
    }
}
