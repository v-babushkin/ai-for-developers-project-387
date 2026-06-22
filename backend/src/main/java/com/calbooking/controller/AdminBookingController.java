package com.calbooking.controller;

import com.calbooking.dto.BookingResponse;
import com.calbooking.service.BookingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> list() {
        return bookingService.listAll().stream()
                .map(BookingResponse::from)
                .toList();
    }
}
