package com.example.rv.pojo;

import lombok.Data;

@Data
public class Vehicles {

  private int vehicleId;
  private int vehicleOwnerId;
  private int vehicleType;
  private String vehicleLocation;
  private String vehicleDescription;
  private double vehiclePrice;
  private String vehiclePicture;
  private String vehicleAuditFailedMsg;
  private int vehicleStatus;
  private java.sql.Timestamp vehicleCreatedAt;
  private java.sql.Timestamp vehicleUpdatedAt;


}
