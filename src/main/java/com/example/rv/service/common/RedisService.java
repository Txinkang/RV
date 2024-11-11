package com.example.rv.service.common;

import com.example.rv.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final LogUtil<RedisService> logUtil = new LogUtil<>(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    RedisService(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    public boolean set(String key, Object value, Integer timeout, TimeUnit timeUnit){
        try {
            redisTemplate.opsForValue().set(key, value,timeout,timeUnit);
            return true;
        }catch (Exception e){
            logUtil.error("redis set value error: "+e);
            return false;
        }
    }
}
