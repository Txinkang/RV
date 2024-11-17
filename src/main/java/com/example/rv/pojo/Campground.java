package com.example.rv.pojo;

import lombok.Data;

@Data
public class Campground {

  private int campgroundId;
  private int campgroundOwnerId;
  private String campgroundName;
  private String campgroundLocation;
  private String campgroundFacilityDetails;
  private float campgroundPrice;
  private String campgroundPicture;
  private String campgroundAuditFailedMsg;
  private short campgroundStatus;
  private java.sql.Timestamp campgroundCreatedAt;
  private java.sql.Timestamp campgroundUpdatedAt;

}
