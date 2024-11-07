package com.example.rv;

import com.example.rv.service.common.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailTests {
    @Autowired
    private EmailService emailService;
    @Test
    void sendEmail(){
        String emailCode = emailService.generateVerificationCode();
        emailService.sendVerificationCode("2912528586@qq.com",emailCode);
    }
}
