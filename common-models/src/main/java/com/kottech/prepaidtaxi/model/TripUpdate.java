package com.kottech.prepaidtaxi.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class TripUpdate implements Serializable {
    private String bookingId;
    private String tripId;
    private TripStatus tripStatus;
}
