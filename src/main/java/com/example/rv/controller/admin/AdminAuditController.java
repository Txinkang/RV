package com.example.rv.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rv.Response.Result;
import com.example.rv.service.AdminAuditService;

@RestController
@RequestMapping("/admin")
public class AdminAuditController {

    @Autowired
    private AdminAuditService adminAuditService;

    @GetMapping("/getAuditCampgroundList")
    public Result getAuditCampgroundList() {
        return adminAuditService.getAuditCampgroundList();
    }

    @PostMapping("/auditCampground")
    public Result auditCampground(@RequestBody Map<String, Object> requestBody) {
        return adminAuditService.auditCampground(requestBody);
    }

    @GetMapping("/getAuditVehicleList")
    public Result getAuditVehicleList() {
        return adminAuditService.getAuditVehicleList();
    }

    @PostMapping("/auditVehicle")
    public Result auditVehicle(@RequestBody Map<String, Object> requestBody) {
        return adminAuditService.auditVehicle(requestBody);
    }
}
