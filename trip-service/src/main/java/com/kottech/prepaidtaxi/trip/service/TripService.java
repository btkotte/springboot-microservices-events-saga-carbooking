package com.kottech.prepaidtaxi.trip.service;

import com.kottech.prepaidtaxi.model.Booking;
import com.kottech.prepaidtaxi.model.PaymentUpdate;
import com.kottech.prepaidtaxi.model.Route;
import com.kottech.prepaidtaxi.model.TripStatus;
import com.kottech.prepaidtaxi.model.TripUpdate;
import com.kottech.prepaidtaxi.trip.model.Driver;
import com.kottech.prepaidtaxi.trip.model.Trip;
import com.kottech.prepaidtaxi.trip.repo.DriverRepo;
import com.kottech.prepaidtaxi.trip.repo.TripRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Comparator;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TripService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DriverRepo driverRepo;
    private final TripRepo tripRepo;

    @KafkaListener(topics = "booking-orders", groupId = "trip-service-booking-orders")
    public void bookingOrderListener(Booking booking) {
        log.info("Received booking order: {} ", booking.toString());
        switch (booking.getBookingStatus()) {
            case CREATED:
                processTrip(booking);
                break;

            case USER_CANCELED:
                cancelTrip(booking.getBookingId());
                log.info("Trip canceled for boooking: {}", booking.getBookingId());
                break;

        }
    }

    @KafkaListener(topics = "payment-updates", groupId = "trip-service-payment-updates")
    public void tripUpdateListener(PaymentUpdate paymentUpdate) {
        log.info("Received payment update: {} ", paymentUpdate.toString());
        switch (paymentUpdate.getPaymentStatus()) {
            case COMPLETED:
                //Start trip
                Trip trip = tripRepo.findByBookingId(paymentUpdate.getBookingId());
                trip.setTripStatus(TripStatus.STARTED);
                log.info("Trip started for boooking: {}", paymentUpdate.getBookingId());
                tripRepo.update(trip);
                break;

            case REJECTED:
                cancelTrip(paymentUpdate.getBookingId());
                log.info("Trip canceled for boooking: {}", paymentUpdate.getBookingId());
                break;
        }
    }

    private void processTrip(Booking booking) {
        Driver driver = findNearestDriver(booking.getRoute());
        if (driver == null) {
            sendUpdate(booking.getBookingId(), TripStatus.UNAVAILABLE);
            return;
        }

        driver.setAvailable(false);
        driverRepo.update(driver);

        Trip trip = new Trip();
        trip.setTripId(UUID.randomUUID().toString());
        trip.setBookingId(booking.getBookingId());
        trip.setDriverId(driver.getDriverId());
        trip.setTripStatus(TripStatus.PENDING);
        tripRepo.create(trip);
    }

    private Driver findNearestDriver(Route route) {
        return driverRepo.findAll()
                .stream()
                .filter(Driver::isAvailable)
                .min(Comparator.comparing(driver ->
                        Math.pow(driver.getLocationX() - route.getStartX(), 2) +
                                Math.pow(driver.getLocationY() - route.getStartY(), 2)))
                .orElse(null);
    }

    private void cancelTrip(String bookingId) {
        Trip trip = tripRepo.findByBookingId(bookingId);
        Driver driver = driverRepo.findOne(trip.getDriverId());
        driver.setAvailable(true);
        driverRepo.update(driver);

        trip.setTripStatus(TripStatus.CANCELED);
        tripRepo.update(trip);

        sendUpdate(bookingId, TripStatus.CANCELED);
    }

    private void sendUpdate(String bookingId, TripStatus tripStatus) {
        TripUpdate tripUpdate = new TripUpdate();
        tripUpdate.setBookingId(bookingId);
        tripUpdate.setTripStatus(tripStatus);
        kafkaTemplate.send("trip-updates", bookingId, tripUpdate) .addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.info("Failed to push tripUpdate for booking: {} to kafka ", bookingId);
            }

            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                log.info("Succesfully pushed tripUpdate for booking: {} to kafka", bookingId);
            }
        });
    }
}
