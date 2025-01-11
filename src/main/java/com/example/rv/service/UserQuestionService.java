package com.example.rv.service;

import java.util.Map;

import com.example.rv.Response.Result;


public interface UserQuestionService {

    /**
     * 获取用户问题信息
     * @return Result
     */
    Result questionsInfo();

    /**
     * 用户提问
     * @param questionMap
     * @return Result
     */
    Result askQuestions(Map<String, String> questionMap);

    /**
     * 用户反馈
     * @param feedbackMap
     * @return Result
     */
    Result feedback(Map<String, Object> feedbackMap);

    /**
     * 用户满意度评分
     * @param satisfactionMap
     * @return Result
     */
    Result satisfaction(Map<String, Object> satisfactionMap);

} 