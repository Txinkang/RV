package com.example.rv.interceptors;

import com.example.rv.utils.JwtUtil;
import com.example.rv.utils.Md5Util;
import com.example.rv.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class CheckTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从token获取userId，拿到redis的key
        String token=request.getHeader("Authorization");
        Map<String,Object> userMap = JwtUtil.parseToken(token);
        String redisKey = Md5Util.getMD5String((String) userMap.get("id"));
        String redisToken = redisTemplate.opsForValue().get(redisKey);
        if (redisToken == null){
            response.setStatus(401);
            return false;
        }
        //没过期就把数据存进线程
        userMap.put("token",redisToken);
        ThreadLocalUtil.set(userMap);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
