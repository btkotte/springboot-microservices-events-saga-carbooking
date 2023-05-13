package com.kottech.prepaidtaxi.payment.model;

import com.kottech.prepaidtaxi.model.PaymentStatus;
import lombok.Data;

@Data
public class Payment {
    private String paymentId;
    private String bookingId;
    private String mode;
    private double amount;
    private PaymentStatus paymentStatus;
}
