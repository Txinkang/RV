package com.example.rv.service.Impl.user;

import com.example.rv.Response.PageResponse;
import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserCampgroundMapper;
import com.example.rv.mapper.user.UserMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.pojo.CampgroundReservations;
import com.example.rv.service.UserCampgroundService;
import com.example.rv.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class UserCampgroundServiceImpl implements UserCampgroundService {
    @Autowired
    private UserCampgroundMapper userCampgroundMapper;
    @Autowired
    private UserMapper userMapper;

    //把json转换成campground类对象
    private Campground convertToCampground(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(data, Campground.class);
    }

    @Override
    public Result checkCampgroundList(Map<String, Object> requestMap) {
        Object requestCampground = requestMap.get("campground");
        Campground campground = convertToCampground(requestCampground);
        Integer pageNum = (Integer) requestMap.get("pageNum");
        Integer pageSize = (Integer) requestMap.get("pageSize");
        if (pageNum <= 0 || pageSize <= 0) {
            return new Result(ResultCode.R_ParamError);
        }
        if (campground.getCampgroundPrice() < 0) {
            return new Result(ResultCode.R_ParamError);
        }
        //设置分页查询
        PageResponse<Campground> pageResponse = new PageResponse<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Campground> campgroundList;
        //判断传没传status
        Map<String, Object> checkCampground = (Map<String, Object>) requestCampground;
        if (checkCampground == null) {
            return new Result(ResultCode.R_Fail);
        }
        if (checkCampground.containsKey("campgroundStatus")) {
            //只允许查询状态为0～2的
            if (campground.getCampgroundStatus() < 0 || campground.getCampgroundStatus() > 2) {
                return new Result(ResultCode.R_ParamError);
            }
            campgroundList = userCampgroundMapper.findCampHasStatus(campground);
        } else {
            campgroundList = userCampgroundMapper.findCampNoStatus(campground);
        }
        Page<Campground> campgroundPage = (Page<Campground>) campgroundList;
        pageResponse.setTotal(campgroundPage.getTotal());
        pageResponse.setItems(campgroundPage.getResult());
        return new Result(ResultCode.R_Ok, pageResponse);
    }

    @Override
    public Result campgroundReservation(CampgroundReservations campgroundReservations) {
        int CampgroundId = campgroundReservations.getCampgroundReservationCampgroundId();
        Timestamp CampgroundStartDate = campgroundReservations.getCampgroundReservationStartDate();
        Timestamp CampgroundEndDate = campgroundReservations.getCampgroundReservationEndDate();
        double CampgroundTotalPrice = campgroundReservations.getCampgroundReservationTotalPrice();
        //验证营地信息
        Campground queryCamp = userCampgroundMapper.findCampByCampId(CampgroundId);
        if (CampgroundId <= 0 || queryCamp == null) {
            return new Result(ResultCode.R_CampNotFound);
        }
        int campgroundStatus = queryCamp.getCampgroundStatus();
        if (campgroundStatus != 0) {
            return new Result(ResultCode.R_CampAlreadyReserved);
        }
        double campPrice = queryCamp.getCampgroundPrice();
        if (CampgroundTotalPrice < campPrice) {
            return new Result(ResultCode.R_PriceIsLow);
        }
        //获取当前时间进行对比
        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTime);
        if (currentTimestamp.after(CampgroundStartDate) || CampgroundStartDate.after(CampgroundEndDate)) {
            return new Result(ResultCode.R_DateError);
        }
        //验证预定人是否合格
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer renterId = (Integer) userMap.get("id");
        if (renterId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(renterId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        CampgroundReservations isReserved = userCampgroundMapper.checkReservationByRenterId(renterId);
        if (isReserved != null) {
            return new Result(ResultCode.R_IsReserved);
        }
        //开始预定
        Integer reserveRowAffected = userCampgroundMapper.reserveCamp(campgroundReservations, renterId);
        if (reserveRowAffected > 0) {
            int campStatus = 1;
            Integer statusRowAffected = userCampgroundMapper.updateCampStatusByCampId(CampgroundId, campStatus);
            return new Result(statusRowAffected > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
        }
        return new Result(ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result checkBookedCampground() {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer renterId = (Integer) userMap.get("id");
        if (renterId == null) {
            return new Result(ResultCode.R_Error);
        }
        Integer checkUser = userMapper.checkUserByUserId(renterId);
        if (checkUser == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        CampgroundReservations queryReservation = userCampgroundMapper.checkReservationByRenterId(renterId);
        if (queryReservation == null) {
            return new Result(ResultCode.R_UserNotReserved);
        }
        return new Result(ResultCode.R_Ok, queryReservation);
    }
}
