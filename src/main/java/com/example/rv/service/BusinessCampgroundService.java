package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BusinessCampgroundService {
    /**
     * 上传营地信息进行审核
     *
     * @param campground
     * @param campgroundPicture
     * @return Result
     */
    Result uploadCampground(Campground campground, List<MultipartFile> campgroundPicture);

    /**
     * 查看营地信息
     * @return Result
     */
    Result checkCampgroundList();

    Result updateCampground(Campground campground, List<MultipartFile> campgroundPicture);

    Result deleteCampground(Campground campground);

    Result maintenanceCampground(Map<String, Object> requestBody);

    Result campMaintenanceCancel(Map<String, Object> requestBody);

    Result campMaintenanceComplete(Map<String, Object> requestBody);

    Result returnCamp(Map<String, Object> requestBody);
}
