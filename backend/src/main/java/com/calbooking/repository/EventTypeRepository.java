package com.calbooking.repository;

import com.calbooking.model.EventType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class EventTypeRepository {
    private final ConcurrentHashMap<Long, EventType> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public EventType save(EventType et) {
        if (et.getId() == null) {
            et.setId(idSeq.getAndIncrement());
        }
        store.put(et.getId(), et);
        return et;
    }

    public Optional<EventType> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<EventType> findAll() {
        return List.copyOf(store.values());
    }

    public List<EventType> findAllActive() {
        return store.values().stream()
                .filter(EventType::isActive)
                .toList();
    }

    public void clear() {
        store.clear();
        idSeq.set(1);
    }
}
