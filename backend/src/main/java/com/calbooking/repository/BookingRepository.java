package com.calbooking.repository;

import com.calbooking.model.Booking;
import com.calbooking.model.BookingStatus;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {
    private final ConcurrentHashMap<Long, Booking> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public Booking save(Booking b) {
        if (b.getId() == null) {
            b.setId(idSeq.getAndIncrement());
        }
        store.put(b.getId(), b);
        return b;
    }

    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Booking> findAll() {
        return List.copyOf(store.values());
    }

    public List<Booking> findAllOrderByStart() {
        return store.values().stream()
                .sorted(Comparator.comparing(Booking::getStart))
                .toList();
    }

    public boolean hasOverlap(OffsetDateTime start, OffsetDateTime end) {
        return store.values().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .anyMatch(b -> b.getStart().isBefore(end) && b.getEnd().isAfter(start));
    }

    public void clear() {
        store.clear();
        idSeq.set(1);
    }
}
