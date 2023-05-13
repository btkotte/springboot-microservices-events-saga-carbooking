package com.kottech.prepaidtaxi.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Booking implements Serializable {
    private String bookingId;
    private String paymentId;
    private String tripId;

    private Route route;
    private double price;

    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private TripStatus tripStatus;
}
