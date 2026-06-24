package com.calbooking.controller;

import com.calbooking.dto.BookingCreateRequest;
import com.calbooking.dto.BookingResponse;
import com.calbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest req) {
        return BookingResponse.from(bookingService.create(req));
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(bookingService.getById(id));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}
