package com.example.rv.Exception;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result Exception(MethodArgumentNotValidException e){
        Result result=new Result(ResultCode.R_Error);
        Map<String, String> errorMessages = new HashMap<>();

        // 遍历校验错误的字段，获取 default message 并添加到返回信息中
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errorMessages.put(error.getField(), error.getDefaultMessage());
        }
        result.setMessage("异常："+errorMessages);
        // 精简错误信息
        /*// 正则表达式用于匹配 "default message [内容]"
        Pattern pattern = Pattern.compile("default message \\[(.*?)]");
        Matcher matcher = pattern.matcher(e.getMessage());
        String errorMessage="";
        // 查找并提取所有匹配的内容
        while (matcher.find()) {
            errorMessage+=matcher.group(1)+",";
        }
        result.setMessage("异常："+e.getMessage());*/
        return result;
    }
}
