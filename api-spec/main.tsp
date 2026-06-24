import "@typespec/http";
import "@typespec/openapi";

using TypeSpec.Http;
using TypeSpec.OpenAPI;

/**
 * Booking service API (cal.com-like MVP).
 *
 * Two implicit roles, no authentication:
 *  - Owner: a single pre-seeded profile used by default in the admin area.
 *  - Guest: books slots without an account or login.
 *
 * Availability comes from a fixed weekly schedule (seeded, e.g. Mon-Fri 09:00-17:00).
 * Bookable slots are generated for the next 14 days starting from "now".
 * Slot step equals the event type duration.
 * Busy rule: at most one booking per time range globally for the owner,
 * even across different event types.
 */
@service(#{ title: "CalBooking API" })
@info(#{ version: "0.1.0" })
@server("http://localhost:8080", "Local development server")
namespace CalBooking;

// ---------------------------------------------------------------------------
// Common models
// ---------------------------------------------------------------------------

/** Standard error payload returned for non-2xx responses. */
model Error {
  /** Machine-readable error code, e.g. "SLOT_TAKEN", "NOT_FOUND", "VALIDATION_ERROR". */
  code: string;

  /** Human-readable description of the error. */
  message: string;
}

/** Booking lifecycle status. */
union BookingStatus {
  confirmed: "CONFIRMED",
  cancelled: "CANCELLED",
}

// ---------------------------------------------------------------------------
// Event type models
// ---------------------------------------------------------------------------

/** A type of meeting the owner offers (e.g. "30 min intro call"). */
model EventType {
  /** Unique identifier of the event type. */
  @visibility(Lifecycle.Read)
  id: int64;

  /** Display name shown to guests. */
  @minLength(1)
  @maxLength(120)
  title: string;

  /** Description shown to guests. */
  @maxLength(2000)
  description: string;

  /** Meeting length in minutes. Also used as the slot step. */
  @minValue(1)
  @maxValue(1440)
  durationMinutes: int32;

  /** Whether the event type is visible/bookable by guests. */
  active: boolean;
}

/** Payload to create a new event type (owner/admin). */
model EventTypeCreate {
  @minLength(1)
  @maxLength(120)
  title: string;

  @maxLength(2000)
  description: string;

  @minValue(1)
  @maxValue(1440)
  durationMinutes: int32;

  /** Defaults to true when omitted. */
  active?: boolean = true;
}

// ---------------------------------------------------------------------------
// Slot models
// ---------------------------------------------------------------------------

/** A free, bookable time range for a given event type. */
model Slot {
  /** Inclusive start of the slot (ISO-8601, UTC). */
  start: utcDateTime;

  /** Exclusive end of the slot (ISO-8601, UTC). */
  end: utcDateTime;
}

// ---------------------------------------------------------------------------
// Booking models
// ---------------------------------------------------------------------------

/** A confirmed reservation made by a guest. */
model Booking {
  @visibility(Lifecycle.Read)
  id: int64;

  /** The event type this booking belongs to. */
  eventTypeId: int64;

  /** Guest display name. */
  @minLength(1)
  @maxLength(120)
  guestName: string;

  /** Guest email address. */
  @format("email")
  @maxLength(254)
  guestEmail: string;

  /** Inclusive start of the booking (ISO-8601, UTC). */
  start: utcDateTime;

  /** Exclusive end of the booking (ISO-8601, UTC). */
  @visibility(Lifecycle.Read)
  end: utcDateTime;

  @visibility(Lifecycle.Read)
  status: BookingStatus;

  @visibility(Lifecycle.Read)
  createdAt: utcDateTime;
}

/** Payload to create a booking on a chosen slot (guest). */
model BookingCreate {
  /** The event type to book. */
  eventTypeId: int64;

  @minLength(1)
  @maxLength(120)
  guestName: string;

  @format("email")
  @maxLength(254)
  guestEmail: string;

  /** Start of the chosen slot (must match a free slot in the 14-day window). */
  start: utcDateTime;
}

// ---------------------------------------------------------------------------
// Guest-facing operations
// ---------------------------------------------------------------------------

@route("/api/event-types")
@tag("Guest")
interface EventTypes {
  /** List all active event types available to guests. */
  @get
  list(): EventType[];

  /** Get a single event type by id. */
  @get
  @route("{id}")
  get(@path id: int64): EventType | {
    @statusCode statusCode: 404;
    @body error: Error;
  };

  /**
   * List free, bookable slots for an event type over the next 14 days.
   * Slots are generated from the owner's weekly availability, stepped by the
   * event type duration, excluding any time that overlaps an existing booking.
   */
  @get
  @route("{id}/slots")
  slots(@path id: int64): Slot[] | {
    @statusCode statusCode: 404;
    @body error: Error;
  };
}

@route("/api/bookings")
@tag("Guest")
interface Bookings {
  /**
   * Create a booking on a chosen slot.
   * Fails with 409 if the time range overlaps any existing booking
   * (busy rule is global across all event types for the owner).
   */
  @post
  create(@body body: BookingCreate):
    | {
        @statusCode statusCode: 201;
        @body booking: Booking;
      }
    | {
        @statusCode statusCode: 400;
        @body error: Error;
      }
    | {
        @statusCode statusCode: 404;
        @body error: Error;
      }
    | {
        @statusCode statusCode: 409;
        @body error: Error;
      };

  /** Get a booking by id. */
  @get
  @route("{id}")
  get(@path id: int64): Booking | {
    @statusCode statusCode: 404;
    @body error: Error;
  };

  /**
   * Cancel a booking by id.
   * Fails with 409 if the booking is already cancelled.
   */
  @post
  @route("{id}/cancel")
  cancel(@path id: int64):
    | Booking
    | {
        @statusCode statusCode: 404;
        @body error: Error;
      }
    | {
        @statusCode statusCode: 409;
        @body error: Error;
      };
}

// ---------------------------------------------------------------------------
// Owner / admin operations (no auth; default owner profile)
// ---------------------------------------------------------------------------

@route("/api/admin/event-types")
@tag("Admin")
interface AdminEventTypes {
  /** List all event types, including inactive ones. */
  @get
  list(): EventType[];

  /** Create a new event type. */
  @post
  create(@body body: EventTypeCreate): {
    @statusCode statusCode: 201;
    @body eventType: EventType;
  } | {
    @statusCode statusCode: 400;
    @body error: Error;
  };
}

@route("/api/admin/bookings")
@tag("Admin")
interface AdminBookings {
  /**
   * List upcoming bookings across all event types in a single list,
   * ordered by start time.
   */
  @get
  list(): Booking[];
}
