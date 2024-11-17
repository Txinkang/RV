package com.example.rv.service;

import com.example.rv.Response.Result;
import com.example.rv.pojo.Campground;

public interface UserCampgroundService {
    Result checkCampgroundList(Campground campground);
}
