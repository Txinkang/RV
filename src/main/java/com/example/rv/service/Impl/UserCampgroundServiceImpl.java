package com.example.rv.service.Impl;

import com.example.rv.Response.PageResponse;
import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.UserCampgroundMapper;
import com.example.rv.pojo.Campground;
import com.example.rv.service.UserCampgroundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserCampgroundServiceImpl implements UserCampgroundService {
    @Autowired
    private UserCampgroundMapper userCampgroundMapper;

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
}
