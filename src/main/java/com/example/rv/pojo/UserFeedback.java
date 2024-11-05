package com.example.rv.pojo;

import lombok.Data;

@Data
public class UserFeedback {

  private int userFeedbackId;
  private int userFeedbackUserId;
  private String userFeedbackContent;
  private int userFeedbackSatisfaction;
  private java.sql.Timestamp userFeedbackDate;

}
