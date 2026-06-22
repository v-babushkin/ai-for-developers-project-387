package com.calbooking.controller;

import com.calbooking.dto.EventTypeResponse;
import com.calbooking.dto.SlotResponse;
import com.calbooking.model.EventType;
import com.calbooking.service.EventTypeService;
import com.calbooking.service.SlotService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-types")
public class EventTypeController {

    private final EventTypeService eventTypeService;
    private final SlotService slotService;

    public EventTypeController(EventTypeService eventTypeService, SlotService slotService) {
        this.eventTypeService = eventTypeService;
        this.slotService = slotService;
    }

    @GetMapping
    public List<EventTypeResponse> list() {
        return eventTypeService.listActive().stream()
                .map(EventTypeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EventTypeResponse get(@PathVariable Long id) {
        return EventTypeResponse.from(eventTypeService.getById(id));
    }

    @GetMapping("/{id}/slots")
    public List<SlotResponse> slots(@PathVariable Long id) {
        EventType et = eventTypeService.getById(id);
        return slotService.generateSlots(et).stream()
                .map(SlotResponse::from)
                .toList();
    }
}
