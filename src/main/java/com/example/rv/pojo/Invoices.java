package com.example.rv.pojo;

import lombok.Data;

@Data
public class Invoices {

  private int invoiceId;
  private int invoicePaymentId;
  private String invoiceNumber;
  private java.sql.Timestamp invoiceIssueDate;
  private double invoiceTotalAmount;
  private java.sql.Timestamp invoiceCreatedAt;

}
