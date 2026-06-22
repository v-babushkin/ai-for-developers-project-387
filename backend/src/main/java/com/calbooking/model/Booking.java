package com.calbooking.model;

import java.time.OffsetDateTime;

public class Booking {
    private Long id;
    private Long eventTypeId;
    private String guestName;
    private String guestEmail;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private BookingStatus status;
    private OffsetDateTime createdAt;

    public Booking() {}

    public Booking(Long id, Long eventTypeId, String guestName, String guestEmail,
                   OffsetDateTime start, OffsetDateTime end, BookingStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.eventTypeId = eventTypeId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.start = start;
        this.end = end;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventTypeId() { return eventTypeId; }
    public void setEventTypeId(Long eventTypeId) { this.eventTypeId = eventTypeId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
