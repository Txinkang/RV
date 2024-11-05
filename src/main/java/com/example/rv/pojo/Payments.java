package com.example.rv.pojo;

import lombok.Data;

@Data
public class Payments {

  private int paymentId;
  private int paymentUserId;
  private int paymentVehicleReservationId;
  private int paymentCampgroundReservationId;
  private int paymentTransactionType;
  private int paymentStatus;
  private java.sql.Timestamp paymentDate;
  private java.sql.Timestamp paymentCreatedAt;
  private java.sql.Timestamp paymentUpdatedAt;

}
