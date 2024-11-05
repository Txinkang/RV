package com.example.rv.pojo;

import lombok.Data;

@Data
public class UserActivityLogs {

  private int userActivityLogId;
  private int userActivityLogUserId;
  private int userActivityLogActivityType;
  private java.sql.Timestamp userActivityLogActivityTimestamp;


}
