package com.example.rv.interceptors;

import com.example.rv.utils.JwtUtil;
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
        //验证token过没过期
        String token=request.getHeader("Authorization");
        String redisToken=redisTemplate.opsForValue().get(token);
        if (redisToken==null){
            response.setStatus(401);
            return false;
        }
        //没过期就从token取数据,存进线程
        Map<String,Object> map= JwtUtil.parseToken(redisToken);
        map.put("token",redisToken);
        ThreadLocalUtil.set(map);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
