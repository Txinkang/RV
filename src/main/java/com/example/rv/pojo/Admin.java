package com.example.rv.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    private int adminId;
    private String adminAccount;
    private String adminPassword;
    private LocalDateTime adminCreatedAt;
    private LocalDateTime adminUpdatedAt;
}
