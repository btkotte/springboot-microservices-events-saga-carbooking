package com.kottech.prepaidtaxi.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class PaymentUpdate implements Serializable {
    private String bookingId;
    private String paymentId;
    private PaymentStatus paymentStatus;
}
