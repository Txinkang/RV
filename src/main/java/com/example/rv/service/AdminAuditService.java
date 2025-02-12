package com.example.rv.service;

import java.util.Map;

import com.example.rv.Response.Result;

public interface AdminAuditService {

    Result getAuditCampgroundList();

    Result auditCampground(Map<String, Object> requestBody);

    Result getAuditVehicleList();

    Result auditVehicle(Map<String, Object> requestBody);
}
