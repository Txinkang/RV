package com.example.rv.pojo;

import lombok.Data;

@Data
public class CampgroundMaintenance {

  private int campgroundMaintenanceId;
  private int campgroundMaintenanceCampgroundId;
  private String campgroundMaintenanceDetails;
  private java.sql.Timestamp campgroundMaintenanceCompletedAt;
  private char campgroundMaintenanceStatus;
  private java.sql.Timestamp campgroundMaintenanceCreatedAt;
  private java.sql.Timestamp campgroundMaintenanceUpdatedAt;
}
