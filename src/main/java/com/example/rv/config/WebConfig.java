package com.example.rv.config;

import com.example.rv.interceptors.CheckTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePathList=new ArrayList<>();
        excludePathList.add("/user/register");
        excludePathList.add("/user/loginAccount");
        excludePathList.add("/user/loginPhoneNumber");
        excludePathList.add("/user/logout");
        excludePathList.add("/user/getCode");
        registry.addInterceptor(checkTokenInterceptor).excludePathPatterns(excludePathList);
    }
}
