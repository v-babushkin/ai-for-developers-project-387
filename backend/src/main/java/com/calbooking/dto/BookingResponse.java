package com.calbooking.dto;

import com.calbooking.model.Booking;
import com.calbooking.model.BookingStatus;
import java.time.OffsetDateTime;

public class BookingResponse {
    private Long id;
    private Long eventTypeId;
    private String guestName;
    private String guestEmail;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private BookingStatus status;
    private OffsetDateTime createdAt;

    public static BookingResponse from(Booking b) {
        BookingResponse r = new BookingResponse();
        r.id = b.getId();
        r.eventTypeId = b.getEventTypeId();
        r.guestName = b.getGuestName();
        r.guestEmail = b.getGuestEmail();
        r.start = b.getStart();
        r.end = b.getEnd();
        r.status = b.getStatus();
        r.createdAt = b.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getEventTypeId() { return eventTypeId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public OffsetDateTime getStart() { return start; }
    public OffsetDateTime getEnd() { return end; }
    public BookingStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
