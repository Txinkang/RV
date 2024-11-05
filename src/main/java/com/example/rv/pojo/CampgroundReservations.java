package com.example.rv.pojo;

import lombok.Data;

@Data
public class CampgroundReservations {

  private int campgroundReservationId;
  private int campgroundReservationCampgroundId;
  private int campgroundReservationRenterId;
  private java.sql.Timestamp campgroundReservationStartDate;
  private java.sql.Timestamp campgroundReservationEndDate;
  private int campgroundReservationContractDetails;
  private java.sql.Timestamp campgroundReservationSignedAt;
  private int campgroundReservationStatus;
  private double campgroundReservationTotalPrice;
  private java.sql.Timestamp campgroundReservationCreatedAt;
  private java.sql.Timestamp campgroundReservationUpdatedAt;

}
