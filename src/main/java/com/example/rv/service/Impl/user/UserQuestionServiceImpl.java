package com.example.rv.service.Impl.user;

import com.example.rv.Response.Result;
import com.example.rv.Response.ResultCode;
import com.example.rv.mapper.user.UserQuestionMapper;
import com.example.rv.pojo.UserFeedback;
import com.example.rv.pojo.UserQuestions;
import com.example.rv.service.UserQuestionService;
import com.example.rv.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserQuestionServiceImpl implements UserQuestionService {

    @Autowired
    private UserQuestionMapper userQuestionMapper;

    @Override
    public Result questionsInfo() {
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        
        List<UserQuestions> questions = userQuestionMapper.findQuestionsByUserId(userId);
        return new Result(ResultCode.R_Ok, questions);
    }

    @Override
    public Result askQuestions(Map<String, String> questionMap) {
        // TODO Auto-generated method stub
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        if (questionMap == null || !questionMap.containsKey("questionsDetails")) {
            return new Result(ResultCode.R_ParamError);
        }
        String questionDetails = questionMap.get("questionsDetails");
        if (questionDetails == null || questionDetails.trim().isEmpty()) {
            return new Result(ResultCode.R_ParamError);
        }

        UserQuestions question = new UserQuestions();
        question.setUserQuestionUserId(userId);
        question.setUserQuestionDetails(questionDetails);
        question.setUserQuestionStatus(1);
        userQuestionMapper.insertQuestion(question);
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result feedback(Map<String, Object> feedbackMap) {
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        if (feedbackMap == null || !feedbackMap.containsKey("feedbackContent") 
            || !feedbackMap.containsKey("feedbackSatisfaction")) {
            return new Result(ResultCode.R_ParamError);
        }

        String feedbackContent = (String) feedbackMap.get("feedbackContent");
        Integer feedbackSatisfaction = (Integer) feedbackMap.get("feedbackSatisfaction");

        if (feedbackContent == null || feedbackContent.trim().isEmpty() 
            || feedbackSatisfaction == null || feedbackSatisfaction < 0 || feedbackSatisfaction > 5) {
            return new Result(ResultCode.R_ParamError);
        }

        UserFeedback feedback = new UserFeedback();
        feedback.setUserFeedbackUserId(userId);
        feedback.setUserFeedbackContent(feedbackContent);
        feedback.setUserFeedbackSatisfaction(feedbackSatisfaction);        
        userQuestionMapper.insertFeedback(feedback);
        return new Result(ResultCode.R_Ok);
    }

    @Override
    public Result satisfaction(Map<String, Object> satisfactionMap) {
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_UserNotFound);
        }

        if (satisfactionMap == null || !satisfactionMap.containsKey("satisfaction")) {
            return new Result(ResultCode.R_ParamError);
        }

        Integer satisfaction = (Integer) satisfactionMap.get("satisfaction");
        if (satisfaction == null || satisfaction < 0 || satisfaction > 5) {
            return new Result(ResultCode.R_ParamError);
        }

        UserFeedback feedback = new UserFeedback();
        feedback.setUserFeedbackUserId(userId);
        feedback.setUserFeedbackSatisfaction(satisfaction);
        
        userQuestionMapper.insertSatisfaction(feedback);
        return new Result(ResultCode.R_Ok);
    }
} 