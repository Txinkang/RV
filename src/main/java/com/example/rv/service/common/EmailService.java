package com.example.rv.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // 生成6位数字验证码
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 发送验证码邮件
    public void sendVerificationCode(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("我们是房车租赁平台");
        message.setText("您的登录验证码是: " + verificationCode);
        message.setFrom("2912528586@qq.com");
        mailSender.send(message);
    }
}
