package com.example.rv.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Payments {

  private int paymentId;
  private int paymentUserId;
  private int paymentVehicleReservationId;
  private int paymentCampgroundReservationId;
  @NotNull
  private int paymentTransactionType;
  private int paymentStatus;
  private java.sql.Timestamp paymentDate;
  private java.sql.Timestamp paymentCreatedAt;
  private java.sql.Timestamp paymentUpdatedAt;

}
