package com.kottech.prepaidtaxi.booking.api;

import com.kottech.prepaidtaxi.booking.service.BookingService;
import com.kottech.prepaidtaxi.model.Booking;
import com.kottech.prepaidtaxi.model.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Mono<Booking> createBooking(@RequestBody Route route) {
        log.info("Received new booking request for route: {}", route);
        return Mono.fromCallable(() -> bookingService.createNewOrder(route));
    }

    @DeleteMapping("/{bookingId}")
    public Mono<Booking> cancelBooking(@PathVariable String bookingId) {
        return Mono.fromCallable(() -> bookingService.cancelOrder(bookingId));
    }


    @GetMapping("/{bookingId}")
    public Mono<Booking> getStatus(@PathVariable String bookingId) {
        return Mono.fromCallable(() -> bookingService.getBooking(bookingId));
    }
}