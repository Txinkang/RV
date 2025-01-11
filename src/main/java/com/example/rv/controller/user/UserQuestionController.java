package com.example.rv.controller.user;

import com.example.rv.Response.Result;
import com.example.rv.service.UserQuestionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserQuestionController {
    
    @Autowired
    private UserQuestionService userQuestionService;

    @GetMapping("/questionsInfo")
    public Result questionsInfo() {
        return userQuestionService.questionsInfo();
    }
   
    @PostMapping("/askQuestions")
    public Result askQuestions(@RequestBody Map<String, String> questionMap) {
        return userQuestionService.askQuestions(questionMap);
    }

    @PostMapping("/feedback")
    public Result feedback(@RequestBody Map<String, Object> feedbackMap) {
        return userQuestionService.feedback(feedbackMap);
    }

    @PostMapping("/satisfaction")
    public Result satisfaction(@RequestBody Map<String, Object> satisfactionMap) {
        return userQuestionService.satisfaction(satisfactionMap);
    }
} 