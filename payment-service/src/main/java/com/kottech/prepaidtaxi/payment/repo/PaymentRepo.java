package com.kottech.prepaidtaxi.payment.repo;


import com.kottech.prepaidtaxi.payment.model.Payment;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentRepo {
    private Map<String, Payment> payments = new ConcurrentHashMap<>();

    public Collection<Payment> findAll() {
        return payments.values();
    }

    public Payment findOne(String paymentId) {
        return payments.get(paymentId);
    }

    public Payment create(Payment payment) {
        if (payment != null) {
            payments.put(payment.getPaymentId(), payment);
        }
        return payment;
    }

    public Payment update(Payment payment) {

        if (payment == null || !payments.containsKey(payment.getPaymentId())) {
            throw new RuntimeException("Invalid payment");
        }

        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    public Payment findByBookingId(String bookingId) {
        return payments.values()
                .stream()
                .filter(payment -> payment.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst()
                .orElseGet(null);
    }
}
