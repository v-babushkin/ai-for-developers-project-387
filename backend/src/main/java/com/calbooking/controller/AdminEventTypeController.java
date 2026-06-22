package com.calbooking.controller;

import com.calbooking.dto.EventTypeCreateRequest;
import com.calbooking.dto.EventTypeResponse;
import com.calbooking.service.EventTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/event-types")
public class AdminEventTypeController {

    private final EventTypeService eventTypeService;

    public AdminEventTypeController(EventTypeService eventTypeService) {
        this.eventTypeService = eventTypeService;
    }

    @GetMapping
    public List<EventTypeResponse> list() {
        return eventTypeService.listAll().stream()
                .map(EventTypeResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventTypeResponse create(@Valid @RequestBody EventTypeCreateRequest req) {
        return EventTypeResponse.from(eventTypeService.create(req));
    }
}
