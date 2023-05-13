package com.kottech.prepaidtaxi.trip.model;

import com.kottech.prepaidtaxi.model.TripStatus;
import lombok.Data;

@Data
public class Trip {
    String tripId;
    String bookingId;
    String driverId;
    TripStatus tripStatus;
}
