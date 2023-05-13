package com.kottech.prepaidtaxi.trip.model;

import lombok.Data;

@Data
public class Driver {
    String driverId;
    String carNo;
    double locationX;
    double locationY;
    boolean available;
}
