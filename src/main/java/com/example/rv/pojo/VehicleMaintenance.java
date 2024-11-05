package com.example.rv.pojo;

import lombok.Data;

@Data
public class VehicleMaintenance {

  private int vehicleMaintenanceId;
  private int vehicleMaintenanceVehicleId;
  private String vehicleMaintenanceMaintenanceDetails;
  private java.sql.Timestamp vehicleMaintenanceCompletedAt;
  private int vehicleMaintenanceStatus;
  private java.sql.Timestamp vehicleMaintenanceCreatedAt;
  private java.sql.Timestamp vehicleMaintenanceUpdatedAt;


}
