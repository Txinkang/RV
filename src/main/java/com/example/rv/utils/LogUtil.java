package com.example.rv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil<T> {
    private final Logger logger;
    public LogUtil(Class<T> tClass){
        this.logger = LoggerFactory.getLogger(tClass);
    }

    public void info(String msg,Object... args){
        logger.error(msg,args);
    }
    public void warn(String msg,Object... args){
        logger.error(msg,args);
    }
    public void error(String msg,Object... args){
        logger.error(msg,args);
    }
    public void error(String msg,Throwable throwable){
        logger.error(msg,throwable);
    }
}