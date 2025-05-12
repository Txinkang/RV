package com.example.rv.mapper.admin;

import com.example.rv.pojo.Admin;
import com.example.rv.pojo.UserFeedback;
import com.example.rv.pojo.UserQuestions;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminMapper {
    @Select("select * from admin where admin_account = #{adminAccount}")
    Admin checkAdminByAccount(String adminAccount);

    // 获取车辆总数
    @Select("SELECT COUNT(*) FROM vehicles")
    Integer getVehicleCount();

    // 获取营地总数
    @Select("SELECT COUNT(*) FROM campground")
    Integer getCampCount();

    // 获取订单总数
    @Select("SELECT COUNT(*) FROM payments")
    Integer getPaymentCount();

    // 获取已完成订单数量(status=0)
    @Select("SELECT COUNT(*) FROM payments WHERE payment_status = #{status}")
    Integer getPaymentCountByStatus(Integer status);

    // 根据id获取问题
    @Select("SELECT * FROM user_questions WHERE user_question_id = #{userQuestionId}")
    UserQuestions getQuestionById(int userQuestionId);

    // 更新问题
    @Update("UPDATE user_questions SET user_question_answer = #{userQuestionAnswer}, user_question_status = #{userQuestionStatus} WHERE user_question_id = #{userQuestionId}")
    int updateQuestion(UserQuestions question);

    // 获取所有问题
    @Select("SELECT * FROM user_questions")
    List<UserQuestions> getQuestions();

    // 获取所有反馈
    @Select("SELECT * FROM user_feedback")
    List<UserFeedback> getFeedbacks();

}
