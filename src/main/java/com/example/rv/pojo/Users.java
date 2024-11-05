package com.example.rv.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Users {
    private int userId;
    private int userRole;
    private String userName;
    private String userPassword;

    @NotNull
    @Email(message = "邮箱格式错误")
    private String userEmail;

    @NotNull
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式错误")
    private String userPhoneNumber;
    private float userBalance;
    private float userPoints;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
}
