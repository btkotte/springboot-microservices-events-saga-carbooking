package com.kottech.prepaidtaxi.trip.repo;


import com.kottech.prepaidtaxi.trip.model.Driver;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DriverRepo {
    private Map<String, Driver> drivers = new ConcurrentHashMap<>();

    public Collection<Driver> findAll() {
        return drivers.values();
    }

    public Driver findOne(String driverId) {
        return drivers.get(driverId);
    }

    public Driver create(Driver driver) {
        if (driver != null) {
            drivers.put(driver.getDriverId(), driver);
        }
        return driver;
    }

    public Driver update(Driver driver) {

        if (driver == null || !drivers.containsKey(driver.getDriverId())) {
            throw new RuntimeException("Invalid driver");
        }

        drivers.put(driver.getDriverId(), driver);
        return driver;
    }
}
