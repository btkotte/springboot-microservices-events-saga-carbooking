package com.kottech.prepaidtaxi.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Route {
    private String userId;
    private double startX;
    private double startY;
    private double endX;
    private double endY;
}