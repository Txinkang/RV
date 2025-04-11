package com.example.rv.service.Impl.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.constData.MagicMathConstData;
import com.example.rv.constData.RedisConstData;
import com.example.rv.mapper.admin.AdminMapper;
import com.example.rv.pojo.Admin;
import com.example.rv.pojo.UserFeedback;
import com.example.rv.pojo.UserQuestions;
import com.example.rv.service.AdminService;
import com.example.rv.service.common.RedisService;
import com.example.rv.utils.JwtUtil;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public Result login(Admin admin) {
        // 验证参数
        if (admin == null) {
            return new Result(ResultCode.R_ParamError);
        }
        if (admin.getAdminAccount() == null || admin.getAdminPassword() == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证管理员
        Admin adminQuery = adminMapper.checkAdminByAccount(admin.getAdminAccount());
        if (adminQuery == null) {
            return new Result(ResultCode.R_AdminNotFound);
        }
        // 验证密码
        if (!adminQuery.getAdminPassword().equals(admin.getAdminPassword())) {
            return new Result(ResultCode.R_AdminPasswordError);
        }
        //存入信息生成token
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", adminQuery.getAdminId());
        String token = JwtUtil.genToken(userMap);
        boolean redisSet = redisService.set(RedisConstData.ADMIN_LOGIN_TOKEN + adminQuery.getAdminId(), token, MagicMathConstData.REDIS_VERIFY_TOKEN_TIMEOUT, TimeUnit.HOURS);
        if (!redisSet) {
            return new Result(ResultCode.R_Fail);
        }
        return new Result(ResultCode.R_Ok, token);
    }

    @Override
    public Result logout(String token) {
        Map<String, Object> userMap = JwtUtil.parseToken(token);
        //为null大概就是过期了，直接回ok就行
        if (userMap == null){
            return new Result(ResultCode.R_Ok);
        }
        boolean redisDelete = redisService.delete(RedisConstData.ADMIN_LOGIN_TOKEN + userMap.get("id"));
        return new Result(redisDelete ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result dataAnalyze() {
        // 获取各种统计数据
        Map<String, Object> dataMap = new HashMap<>();
        
        // 获取车辆总数
        Integer vehicleCount = adminMapper.getVehicleCount();
        dataMap.put("vehicleCount", vehicleCount);

        // 获取营地总数
        Integer campCount = adminMapper.getCampCount(); 
        dataMap.put("campCount", campCount);

        // 获取订单总数
        Integer paymentCount = adminMapper.getPaymentCount();
        dataMap.put("paymentCount", paymentCount);

        // 获取已完成订单数量(status=0)
        Integer completedCount = adminMapper.getPaymentCountByStatus(0);
        dataMap.put("completedCount", completedCount);

        // 获取未支付订单数量(status=2)
        Integer unpaidCount = adminMapper.getPaymentCountByStatus(2);
        dataMap.put("unpaidCount", unpaidCount);

        return new Result(ResultCode.R_Ok, dataMap);
    }

    @Override
    public Result questionsInfo() {
        // 获取所有问题
        List<UserQuestions> questions = adminMapper.getQuestions();
        return new Result(ResultCode.R_Ok, questions);
    }

    @Override
    public Result answer(UserQuestions userQuestion) {
        // 验证参数
        if (userQuestion == null) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证问题是否存在
        UserQuestions question = adminMapper.getQuestionById(userQuestion.getUserQuestionId());
        if (question == null) {
            return new Result(ResultCode.R_Error);
        }
        // 回答问题
        question.setUserQuestionAnswer(userQuestion.getUserQuestionAnswer());
        question.setUserQuestionStatus(2);
        int update = adminMapper.updateQuestion(question);
        return new Result(update > 0 ? ResultCode.R_Ok : ResultCode.R_UpdateDbFailed);
    }

    @Override
    public Result getFeedback() {
        // 获取所有反馈
        List<UserFeedback> feedbacks = adminMapper.getFeedbacks();
        return new Result(ResultCode.R_Ok, feedbacks);
    }

}
