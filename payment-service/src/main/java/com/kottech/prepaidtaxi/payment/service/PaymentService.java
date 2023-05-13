package com.kottech.prepaidtaxi.payment.service;

import com.kottech.prepaidtaxi.model.Booking;
import com.kottech.prepaidtaxi.model.PaymentStatus;
import com.kottech.prepaidtaxi.model.PaymentUpdate;
import com.kottech.prepaidtaxi.model.TripStatus;
import com.kottech.prepaidtaxi.model.TripUpdate;
import com.kottech.prepaidtaxi.payment.model.Payment;
import com.kottech.prepaidtaxi.payment.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "booking-orders", groupId = "payment-service-booking-orders")
    public void bookingOrderListener(Booking booking) {
        log.info("Received booking order: {} ", booking.toString());
        switch (booking.getBookingStatus()) {
            case CREATED:
                processPayment(booking);
                break;

            case USER_CANCELED:
                processRefund(booking.getBookingId());
                break;

        }
    }

    @KafkaListener(topics = "trip-updates", groupId = "payment-service-trip-updates")
    public void tripUpdateListener(TripUpdate tripUpdate) {
        log.info("Received trip update: {} ", tripUpdate.toString());
        if (tripUpdate.getTripStatus().equals(TripStatus.UNAVAILABLE)) {
            processRefund(tripUpdate.getBookingId());
        }
    }

    private void processPayment(Booking booking) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setBookingId(booking.getBookingId());

        double amount = booking.getPrice();
        payment.setAmount(amount);

        if (amount % 5 == 0) { //Mocking failed transactions
            payment.setPaymentStatus(PaymentStatus.REJECTED);
            log.warn("Payment rejected for bookingId: {}", booking.getBookingId());
        } else {
            payment.setMode("CARD");
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            log.info("Payment success for bookingId: {} with paymentId: {}", booking.getBookingId(), payment.getPaymentId());
        }

        paymentRepo.create(payment);

        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setBookingId(booking.getBookingId());
        paymentUpdate.setPaymentId(payment.getPaymentId());
        paymentUpdate.setPaymentStatus(payment.getPaymentStatus());

        kafkaTemplate.send("payment-updates", booking.getBookingId(), paymentUpdate)
                .addCallback(new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.info("processPayment: Failed to push paymentUpdate: {} to kafka ", paymentUpdate.getPaymentId());
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                        log.info("processPayment: Succesfully pushed paymentUpdate: {} to kafka", paymentUpdate.getPaymentId());
                    }
                });
    }

    private void processRefund(String bookingId) {
        Payment payment = paymentRepo.findByBookingId(bookingId);

        if (payment == null) {
            throw new RuntimeException("No payment found");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepo.update(payment);

        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setBookingId(bookingId);
        paymentUpdate.setPaymentId(payment.getPaymentId());
        paymentUpdate.setPaymentStatus(payment.getPaymentStatus());

        kafkaTemplate.send("payment-updates", bookingId, paymentUpdate)
                .addCallback(new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        log.info("processRefund: Failed to push paymentUpdate: {} to kafka ", paymentUpdate.getPaymentId());
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                        log.info("processRefund: Succesfully pushed paymentUpdate: {} to kafka", paymentUpdate.getPaymentId());
                    }
                });

    }
}
