package com.calbooking.dto;

import com.calbooking.model.Slot;
import java.time.OffsetDateTime;

public class SlotResponse {
    private OffsetDateTime start;
    private OffsetDateTime end;

    public static SlotResponse from(Slot s) {
        SlotResponse r = new SlotResponse();
        r.start = s.getStart();
        r.end = s.getEnd();
        return r;
    }

    public OffsetDateTime getStart() { return start; }
    public OffsetDateTime getEnd() { return end; }
}
