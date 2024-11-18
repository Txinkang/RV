package com.example.rv.service;

import com.example.rv.Response.Result;

import java.util.Map;

public interface UserCampgroundService {
    Result checkCampgroundList(Map<String, Object> requestMap);
}
