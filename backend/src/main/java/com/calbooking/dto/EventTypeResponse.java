package com.calbooking.dto;

import com.calbooking.model.EventType;

public class EventTypeResponse {
    private Long id;
    private String title;
    private String description;
    private int durationMinutes;
    private boolean active;

    public static EventTypeResponse from(EventType et) {
        EventTypeResponse r = new EventTypeResponse();
        r.id = et.getId();
        r.title = et.getTitle();
        r.description = et.getDescription();
        r.durationMinutes = et.getDurationMinutes();
        r.active = et.isActive();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isActive() { return active; }
}
