package com.example.rv.service.Impl.business;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.business.BusinessVehicleMapper;
import com.example.rv.pojo.Vehicles;
import com.example.rv.service.BusinessVehicleService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BusinessVehicleServiceImpl implements BusinessVehicleService {
    public static final LogUtil logUtil = LogUtil.getLogger(BusinessVehicleServiceImpl.class);

    @Value("${uploadFilePath.vehiclePicturesPath}")
    private String vehiclePicturesPath;
    @Autowired
    private BusinessVehicleMapper businessVehicleMapper;

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
}
