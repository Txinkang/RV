package com.example.rv.Exception;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result Exception(MethodArgumentNotValidException e){
        Result result=new Result(ResultCode.R_Error);
        // 精简错误信息
        // 遍历校验错误的字段，获取 default message 并添加到返回信息中
        Map<String, String> errorMessages = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errorMessages.put(error.getField(), error.getDefaultMessage());
        }
        result.setMessage("异常："+errorMessages);
        return result;
    }
}
