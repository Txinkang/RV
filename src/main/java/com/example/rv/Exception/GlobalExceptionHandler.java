package com.example.rv.Exception;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result Exception(Exception e){
        Result result=new Result(ResultCode.R_Error);
        result.setMessage("异常："+e.getMessage());
        return result;

        /*
        // 精简错误信息
        // 正则表达式用于匹配 "default message [内容]"
        Pattern pattern = Pattern.compile("default message \\[(.*?)]");
        Matcher matcher = pattern.matcher(e.getMessage());
        String errorMessage="";
        // 查找并提取所有匹配的内容
        while (matcher.find()) {
            errorMessage+=matcher.group(1)+",";
        }
        */
    }
}
