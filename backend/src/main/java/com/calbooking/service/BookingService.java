package com.calbooking.service;

import com.calbooking.dto.BookingCreateRequest;
import com.calbooking.exception.NotFoundException;
import com.calbooking.exception.SlotTakenException;
import com.calbooking.model.Booking;
import com.calbooking.model.BookingStatus;
import com.calbooking.model.EventType;
import com.calbooking.repository.BookingRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventTypeService eventTypeService;

    public BookingService(BookingRepository bookingRepository, EventTypeService eventTypeService) {
        this.bookingRepository = bookingRepository;
        this.eventTypeService = eventTypeService;
    }

    public synchronized Booking create(BookingCreateRequest req) {
        EventType et = eventTypeService.getById(req.getEventTypeId());

        OffsetDateTime start = req.getStart();
        OffsetDateTime end = start.plusMinutes(et.getDurationMinutes());

        if (bookingRepository.hasOverlap(start, end)) {
            throw new SlotTakenException("The requested slot overlaps an existing booking");
        }

        Booking b = new Booking();
        b.setEventTypeId(req.getEventTypeId());
        b.setGuestName(req.getGuestName());
        b.setGuestEmail(req.getGuestEmail());
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return bookingRepository.save(b);
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + id));
    }

    public java.util.List<Booking> listAll() {
        return bookingRepository.findAllOrderByStart();
    }
}
