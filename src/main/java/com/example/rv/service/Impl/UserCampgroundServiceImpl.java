package com.example.rv.service.Impl;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.pojo.Campground;
import com.example.rv.service.UserCampgroundService;
import com.github.pagehelper.PageHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Service
public class UserCampgroundServiceImpl implements UserCampgroundService {
    @Override
    public Result checkCampgroundList(Campground campground) {
        short status = campground.getCampgroundStatus();
        String campgroundName = campground.getCampgroundName();
        String campgroundLocation = campground.getCampgroundLocation();
        float campgroundPrice = campground.getCampgroundPrice();

        if (status < 0 || Strings.isEmpty(campgroundName) || Strings.isEmpty(campgroundLocation) || campgroundPrice < 0){
            return new Result(ResultCode.R_ParamError);
        }
        return null;
    }
}
