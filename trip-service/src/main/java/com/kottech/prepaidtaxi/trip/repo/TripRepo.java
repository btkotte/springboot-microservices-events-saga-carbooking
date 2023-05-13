package com.kottech.prepaidtaxi.trip.repo;


import com.kottech.prepaidtaxi.trip.model.Trip;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TripRepo {
    private Map<String, Trip> trips = new ConcurrentHashMap<>();

    public Collection<Trip> findAll() {
        return trips.values();
    }

    public Trip findOne(String tripId) {
        return trips.get(tripId);
    }

    public Trip findByBookingId(String bookingId) {
        return trips.values()
                .stream()
                .filter(trip -> trip.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst()
                .orElseGet(null);
    }

    public Trip create(Trip trip) {
        if (trip != null) {
            trips.put(trip.getTripId(), trip);
        }
        return trip;
    }

    public Trip update(Trip trip) {

        if (trip == null || !trips.containsKey(trip.getTripId())) {
            throw new RuntimeException("Invalid trip");
        }

        trips.put(trip.getTripId(), trip);
        return trip;
    }
}
