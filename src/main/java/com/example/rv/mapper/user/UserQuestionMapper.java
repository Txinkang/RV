package com.example.rv.mapper.user;

import com.example.rv.pojo.UserFeedback;
import com.example.rv.pojo.UserQuestions;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserQuestionMapper {
    @Select("SELECT * " +
           "FROM user_questions WHERE user_question_user_id = #{userId}")
    List<UserQuestions> findQuestionsByUserId(Integer userId);

    @Insert("INSERT INTO user_questions (user_question_user_id, user_question_details, user_question_status) " +
            "VALUES (#{userQuestionUserId}, #{userQuestionDetails}, #{userQuestionStatus})")
    void insertQuestion(UserQuestions question);

    @Insert("INSERT INTO user_feedback (user_feedback_user_id, user_feedback_content, user_feedback_satisfaction) " +
            "VALUES (#{userFeedbackUserId}, #{userFeedbackContent}, #{userFeedbackSatisfaction})")
    void insertFeedback(UserFeedback feedback);

    @Insert("INSERT INTO user_feedback (user_feedback_user_id, user_feedback_satisfaction) " +
            "VALUES (#{userFeedbackUserId}, #{userFeedbackSatisfaction})")
    void insertSatisfaction(UserFeedback feedback);
} 