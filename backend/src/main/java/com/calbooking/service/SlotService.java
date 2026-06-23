package com.calbooking.service;

import com.calbooking.model.EventType;
import com.calbooking.model.Slot;
import com.calbooking.repository.BookingRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SlotService {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);
    private static final int WINDOW_DAYS = 14;

    private final BookingRepository bookingRepository;

    public SlotService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Slot> generateSlots(EventType eventType) {
        int duration = eventType.getDurationMinutes();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        LocalDate today = now.toLocalDate();
        List<Slot> result = new ArrayList<>();

        for (int i = 0; i < WINDOW_DAYS; i++) {
            LocalDate date = today.plusDays(i);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            OffsetDateTime slotStart = OffsetDateTime.of(date, WORK_START, ZoneOffset.UTC);
            OffsetDateTime dayEnd = OffsetDateTime.of(date, WORK_END, ZoneOffset.UTC);
            OffsetDateTime windowStart = (i == 0) ? now : slotStart;

            while (slotStart.plusMinutes(duration).isBefore(dayEnd) || slotStart.plusMinutes(duration).equals(dayEnd)) {
                OffsetDateTime slotEnd = slotStart.plusMinutes(duration);
                if (!slotEnd.isAfter(windowStart)) {
                    slotStart = slotEnd;
                    continue;
                }
                if (slotStart.isBefore(windowStart)) {
                    long diffMinutes = Duration.between(slotStart, windowStart).toMinutes();
                    long intervals = (diffMinutes + duration - 1) / duration;
                    slotStart = slotStart.plusMinutes(intervals * duration);
                }
                slotEnd = slotStart.plusMinutes(duration);
                if (slotEnd.isAfter(dayEnd)) {
                    break;
                }
                if (!bookingRepository.hasOverlap(slotStart, slotEnd)) {
                    result.add(new Slot(slotStart, slotEnd));
                }
                slotStart = slotEnd;
            }
        }
        return result;
    }
}
