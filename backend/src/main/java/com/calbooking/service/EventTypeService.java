package com.calbooking.service;

import com.calbooking.dto.EventTypeCreateRequest;
import com.calbooking.exception.NotFoundException;
import com.calbooking.model.EventType;
import com.calbooking.repository.EventTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventTypeService {

    private final EventTypeRepository repository;

    public EventTypeService(EventTypeRepository repository) {
        this.repository = repository;
    }

    public List<EventType> listActive() {
        return repository.findAllActive();
    }

    public List<EventType> listAll() {
        return repository.findAll();
    }

    public EventType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event type not found: " + id));
    }

    public EventType create(EventTypeCreateRequest req) {
        EventType et = new EventType();
        et.setTitle(req.getTitle());
        et.setDescription(req.getDescription() != null ? req.getDescription() : "");
        et.setDurationMinutes(req.getDurationMinutes());
        et.setActive(req.getActive() != null ? req.getActive() : true);
        return repository.save(et);
    }
}
