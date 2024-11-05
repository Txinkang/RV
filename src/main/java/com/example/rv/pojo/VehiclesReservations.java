package com.example.rv.pojo;

import lombok.Data;

@Data
public class VehiclesReservations {

  private int vehicleReservationId;
  private int vehicleReservationVehicleId;
  private int vehicleReservationRenterId;
  private java.sql.Timestamp vehicleReservationStartDate;
  private java.sql.Timestamp vehicleReservationEndDate;
  private double vehicleReservationTotalPrice;
  private int vehicleReservationContractDetails;
  private java.sql.Timestamp vehicleReservationSignedAt;
  private int vehicleReservationStatus;
  private java.sql.Timestamp vehicleReservationCreatedAt;
  private java.sql.Timestamp vehicleReservationUpdatedAt;


}
