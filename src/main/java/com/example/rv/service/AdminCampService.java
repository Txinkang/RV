package com.example.rv.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;

public interface AdminCampService {

    Result checkCampgroundList(Map<String,Object> requestMap);

    Result updateCampground(Campground campground, List<MultipartFile> campgroundPictures);

    Result deleteCampground(Campground campground);

    Result maintenanceCampground(Map<String, Object> requestBody);

    Result campMaintenanceCancel(Map<String, Object> requestBody);

    Result campMaintenanceComplete(Map<String, Object> requestBody);
}
