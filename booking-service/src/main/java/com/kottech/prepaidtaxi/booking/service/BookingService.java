package com.kottech.prepaidtaxi.booking.service;

import com.kottech.prepaidtaxi.booking.repo.BookingRepo;
import com.kottech.prepaidtaxi.model.Booking;
import com.kottech.prepaidtaxi.model.BookingStatus;
import com.kottech.prepaidtaxi.model.PaymentStatus;
import com.kottech.prepaidtaxi.model.Route;
import com.kottech.prepaidtaxi.model.TripStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final String ORDERS_TOPIC = "booking-orders";

    private final BookingRepo bookingRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Booking createNewOrder(Route route) {

        //Create new booking
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setRoute(route);
        booking.setPrice(calculatePrice(route));
        booking.setBookingStatus(BookingStatus.CREATED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setTripStatus(TripStatus.PENDING);
        bookingRepo.create(booking);


        kafkaTemplate.send(ORDERS_TOPIC, booking.getBookingId(), booking)
                .addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.info("createOrder: Failed to push bookingid: {} to kafka: ", booking.getBookingId());
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                        log.info("CreateOrder: Succesfully pushed bookingid: {} to kafka: ", booking.getBookingId());
                    }
                });

        return booking;
    }

    private double calculatePrice(Route route) {
        double x = Math.pow(route.getStartX() - route.getEndX(), 2);
        double y = Math.pow(route.getEndX() - route.getEndY(), 2);
        double distance = Math.sqrt(x + y);
        return distance * 10;
    }

    public Booking cancelOrder(String bookingId) {
        Booking booking = bookingRepo.findOne(bookingId);

        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }

        if (booking.getTripStatus() == TripStatus.STARTED || booking.getTripStatus() == TripStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel. Trip is in progress or completed");
        }

        booking.setBookingStatus(BookingStatus.USER_CANCELED);
        bookingRepo.update(booking);

        kafkaTemplate.send(ORDERS_TOPIC, booking.getBookingId(), booking)
                .addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.info("CancelOrder: Failed to push bookingid: {} to kafka ", booking.getBookingId());
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                        log.info("CancelOrder: Succesfully pushed bookingid: {} to kafka", booking.getBookingId());
                    }
                });

        return booking;
    }

    public Booking getBooking(String bookingId) {
        Booking booking = bookingRepo.findOne(bookingId);

        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }
        return booking;
    }
}
