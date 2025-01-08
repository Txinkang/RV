package com.example.rv.utils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.rv.mapper.user.UserMapper;

import jakarta.annotation.PostConstruct;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
@Component
public class ThreadLocalUtil {

    private static UserMapper userMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        userMapper = applicationContext.getBean(UserMapper.class);
    }
    
    private static final LogUtil logUtil = LogUtil.getLogger(ThreadLocalUtil.class);
    //提供thread对象
    private static final ThreadLocal THREAD_LOCAL=new ThreadLocal<>();

    //提供get方法，拿取线程存储的值
    public static <T> T get(){
        try {
            return (T) THREAD_LOCAL.get();
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil get value error : " + e);
            return null;
        }
    }
    public static Integer getUserId(){
        try {
            Map<String, Object> userMap = get();
            if (userMap == null) {
                return null;
            }
            Integer userId = (Integer) userMap.get("id");
            if (userId == null) {
                return null;
            }
            Integer queryUser = userMapper.checkUserByUserId(userId);
            if (queryUser == null) {
                return null;
            }
            return userId;
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil getUserId error : " + e);
            return null;
        }
    }

    //提供set方法，向线程内写入值
    public static void set(Object value){
        try {
            THREAD_LOCAL.set(value);
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil set value error : " + e);
        }
    }

    //使用完毕，移除线程，防止内存泄漏
    public static void remove(){
        try {
            THREAD_LOCAL.remove();
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil remove value error : " + e);
        }
    }


    /*//提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }
	
    //存储键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }

    //清除ThreadLocal 防止内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }*/
}
