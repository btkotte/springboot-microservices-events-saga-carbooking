package com.kottech.prepaidtaxi.booking.repo;

import com.kottech.prepaidtaxi.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookingRepo {
    private Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public Collection<Booking> findAll() {
        return bookings.values();
    }

    public Booking findOne(String bookingId) {
        return bookings.get(bookingId);
    }

    public Booking create(Booking booking) {
        if (booking != null) {
            bookings.put(booking.getBookingId(), booking);
        }
        return booking;
    }

    public Booking update(Booking booking) {
        if (booking == null || !bookings.containsKey(booking.getPaymentId())) {
            throw new RuntimeException("Invalid payment");
        }

        bookings.put(booking.getPaymentId(), booking);
        return booking;
    }
}
