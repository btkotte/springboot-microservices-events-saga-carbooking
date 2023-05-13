package com.kottech.prepaidtaxi.booking.service;

import com.kottech.prepaidtaxi.booking.repo.BookingRepo;
import com.kottech.prepaidtaxi.model.Booking;
import com.kottech.prepaidtaxi.model.PaymentUpdate;
import com.kottech.prepaidtaxi.model.TripUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusListener {

    private final BookingRepo bookingRepo;

    @KafkaListener(topics = "trip-updates", groupId = "booking-service-trip-updates")
    public void tripUpdateListener(TripUpdate tripUpdate) {
        log.info("received trip update: {}", tripUpdate.toString());
        Booking booking = bookingRepo.findOne(tripUpdate.getBookingId());
        booking.setTripStatus(tripUpdate.getTripStatus());
        bookingRepo.update(booking);
    }

    @KafkaListener(topics = "payment-updates", groupId = "booking-service-payment-updates")
    public void paymentUpdateListener(PaymentUpdate paymentUpdate) {
        log.info("received payment update: {}", paymentUpdate.toString());
        Booking booking = bookingRepo.findOne(paymentUpdate.getBookingId());
        booking.setPaymentStatus(paymentUpdate.getPaymentStatus());
        bookingRepo.update(booking);
    }
}
