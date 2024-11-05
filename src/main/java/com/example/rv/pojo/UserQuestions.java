package com.example.rv.pojo;

import lombok.Data;

@Data
public class UserQuestions {

  private int userQuestionId;
  private int userQuestionUserId;
  private String userQuestionDetails;
  private int userQuestionStatus;
  private java.sql.Timestamp userQuestionCreatedAt;
  private java.sql.Timestamp userQuestionUpdatedAt;

}
