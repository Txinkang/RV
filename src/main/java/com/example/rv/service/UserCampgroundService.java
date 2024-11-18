package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.CampgroundReservations;

import java.util.Map;

public interface UserCampgroundService {
    /**
     * 搜索查看营地信息
     * @param requestMap
     * @return Result
     */
    Result checkCampgroundList(Map<String, Object> requestMap);

    /**
     * 预定营地
     * @param campgroundReservations
     * @return Result
     */
    Result campgroundReservation(CampgroundReservations campgroundReservations);
}
