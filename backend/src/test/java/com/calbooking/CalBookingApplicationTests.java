package com.calbooking;

import com.calbooking.model.BookingStatus;
import com.calbooking.repository.BookingRepository;
import com.calbooking.repository.EventTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CalBookingApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        eventTypeRepository.clear();
        bookingRepository.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listEventTypesInitiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAndListEventTypes() throws Exception {
        String body = """
                {"title":"30 min call","description":"Quick intro","durationMinutes":30}
                """;

        mockMvc.perform(post("/api/admin/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("30 min call"))
                .andExpect(jsonPath("$.description").value("Quick intro"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("30 min call"));
    }

    @Test
    void getEventTypeById() throws Exception {
        createEventType("Intro", "Short intro", 30);
        long id = 1;

        mockMvc.perform(get("/api/event-types/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Intro"));
    }

    @Test
    void getEventTypeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/event-types/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void guestOnlySeesActiveEventTypes() throws Exception {
        createEventType("Active", "Active one", 30);
        createEventType("Inactive", "Hidden", 45, false);
        createEventType("Active2", "Another", 60);

        mockMvc.perform(get("/api/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", hasItems("Active", "Active2")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Inactive"))));
    }

    @Test
    void slotsForEventType() throws Exception {
        createEventType("Consultation", "One-on-one", 60);

        mockMvc.perform(get("/api/event-types/{id}/slots", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].start").isNotEmpty())
                .andExpect(jsonPath("$[0].end").isNotEmpty());
    }

    @Test
    void slotsForNonExistentEventType() throws Exception {
        mockMvc.perform(get("/api/event-types/{id}/slots", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void createBooking() throws Exception {
        createEventType("30 min", "", 30);
        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0).toString();
        String body = """
                {"eventTypeId":1,"guestName":"John","guestEmail":"john@example.com","start":"%s"}
                """.formatted(start);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.eventTypeId").value(1))
                .andExpect(jsonPath("$.guestName").value("John"))
                .andExpect(jsonPath("$.guestEmail").value("john@example.com"))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.name()))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createBookingOnOverlappingSlotReturns409() throws Exception {
        createEventType("30 min", "", 30);
        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0).toString();

        String body = """
                {"eventTypeId":1,"guestName":"John","guestEmail":"john@example.com","start":"%s"}
                """.formatted(start);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_TAKEN"));
    }

    @Test
    void createBookingNonExistentEventTypeReturns404() throws Exception {
        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                .withHour(10).withMinute(0).toString();
        String body = """
                {"eventTypeId":999,"guestName":"John","guestEmail":"john@example.com","start":"%s"}
                """.formatted(start);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void createBookingWithInvalidBodyReturns400() throws Exception {
        String body = """
                {"eventTypeId":null,"guestName":"","guestEmail":"bad","start":null}
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getBookingById() throws Exception {
        createEventType("30 min", "", 30);
        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0).toString();
        String body = """
                {"eventTypeId":1,"guestName":"John","guestEmail":"john@example.com","start":"%s"}
                """.formatted(start);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.guestName").value("John"));
    }

    @Test
    void getBookingByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/bookings/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void adminListAllEventTypes() throws Exception {
        createEventType("Active", "", 30);
        createEventType("Inactive", "", 45, false);

        mockMvc.perform(get("/api/admin/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void adminCreateEventTypeWithDefaultActive() throws Exception {
        String body = """
                {"title":"Default active","description":"","durationMinutes":30}
                """;

        mockMvc.perform(post("/api/admin/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void adminCreateEventTypeValidationError() throws Exception {
        String body = """
                {"title":"","description":"","durationMinutes":0}
                """;

        mockMvc.perform(post("/api/admin/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void adminListBookings() throws Exception {
        createEventType("30 min", "", 30);
        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0).toString();
        String body = """
                {"eventTypeId":1,"guestName":"Alice","guestEmail":"alice@example.com","start":"%s"}
                """.formatted(start);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].guestName").value("Alice"));
    }

    @Test
    void adminListBookingsEmpty() throws Exception {
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void fullWorkflow() throws Exception {
        String typeBody = """
                {"title":"Consultation","description":"45 min session","durationMinutes":45}
                """;

        mockMvc.perform(post("/api/admin/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(typeBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/event-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(45));

        mockMvc.perform(get("/api/event-types/1/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].start").isNotEmpty());

        String start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2)
                .withHour(14).withMinute(0).withSecond(0).withNano(0).toString();
        String bookingBody = """
                {"eventTypeId":1,"guestName":"Bob","guestEmail":"bob@example.com","start":"%s"}
                """.formatted(start);

        String createdBooking = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_TAKEN"));

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private void createEventType(String title, String description, int durationMinutes) throws Exception {
        createEventType(title, description, durationMinutes, true);
    }

    private void createEventType(String title, String description, int durationMinutes, boolean active) throws Exception {
        String body = """
                {"title":"%s","description":"%s","durationMinutes":%d,"active":%b}
                """.formatted(title, description, durationMinutes, active);
        mockMvc.perform(post("/api/admin/event-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
