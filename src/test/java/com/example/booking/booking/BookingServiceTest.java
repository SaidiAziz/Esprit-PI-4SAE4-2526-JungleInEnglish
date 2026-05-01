package com.example.booking.booking;



import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.BookingMS.Booking;
import com.example.booking.BookingMS.BookingStatus;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingServiceTestable service;

    @BeforeEach
    void setUp() {
        FakeBookingRepository bookingRepo = new FakeBookingRepository();
        BookingServiceTestable.FakeAvailabilityStore availStore =
                new BookingServiceTestable.FakeAvailabilityStore();
        service = new BookingServiceTestable(bookingRepo, availStore);
    }

    // ── Helpers ──────────────────────────────────────

    private Availability makeAvailability(Long tutorId, DayOfWeek day,
                                          LocalTime start, LocalTime end) {
        Availability a = new Availability();
        a.setTutorId(tutorId);
        a.setDayOfWeek(day);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setAvailable(true);
        return a;
    }

    private Booking makeBooking(Long studentId, Long tutorId,
                                LocalDate date, LocalTime start, LocalTime end) {
        Booking b = new Booking();
        b.setStudentId(studentId);
        b.setTutorId(tutorId);
        b.setSessionDate(date);
        b.setStartTime(start);
        b.setEndTime(end);
        return b;
    }

    // LUNDI 2 juin 2025
    private final LocalDate MONDAY = LocalDate.of(2025, 6, 2);

    // ── CREATE ──────────────────────────────────────

    @Test
    @DisplayName("createBooking → doit créer avec statut PENDING si disponibilité existe")
    void shouldCreateBookingWhenAvailable() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));

        Booking result = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        assertNotNull(result.getId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("createBooking → doit lever une exception si tuteur non disponible")
    void shouldThrowWhenTutorNotAvailable() {
        assertThrows(RuntimeException.class, () ->
                service.createBooking(
                        makeBooking(10L, 1L, MONDAY,
                                LocalTime.of(9, 0), LocalTime.of(11, 0))));
    }

    @Test
    @DisplayName("createBooking → doit lever exception si créneau hors plage horaire")
    void shouldThrowWhenOutsideTimeRange() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0)));

        assertThrows(RuntimeException.class, () ->
                service.createBooking(
                        makeBooking(10L, 1L, MONDAY,
                                LocalTime.of(9, 0), LocalTime.of(11, 0)))); // end dépasse
    }

    // ── READ ─────────────────────────────────────────

    @Test
    @DisplayName("getBookingById → doit retourner le booking si trouvé")
    void shouldGetBookingById() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        Booking saved = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        Booking result = service.getBookingById(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    @DisplayName("getBookingById → doit lever une exception si non trouvé")
    void shouldThrowWhenBookingNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getBookingById(999L));
        assertEquals("Booking not found with id: 999", ex.getMessage());
    }

    @Test
    @DisplayName("getAllBookings → doit retourner tous les bookings")
    void shouldGetAllBookings() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
        service.createBooking(makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(10,0)));
        service.createBooking(makeBooking(11L, 1L, MONDAY, LocalTime.of(10,0), LocalTime.of(11,0)));

        assertEquals(2, service.getAllBookings().size());
    }

    @Test
    @DisplayName("getBookingsByStudent → doit filtrer par étudiant")
    void shouldGetByStudent() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
        service.createBooking(makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(10,0)));
        service.createBooking(makeBooking(20L, 1L, MONDAY, LocalTime.of(10,0), LocalTime.of(11,0)));

        List<Booking> result = service.getBookingsByStudent(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getStudentId());
    }

    @Test
    @DisplayName("getBookingsByTutor → doit filtrer par tuteur")
    void shouldGetByTutor() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        service.addAvailability(makeAvailability(2L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        service.createBooking(makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));
        service.createBooking(makeBooking(10L, 2L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));

        List<Booking> result = service.getBookingsByTutor(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTutorId());
    }

    // ── UPDATE ───────────────────────────────────────

    @Test
    @DisplayName("updateBooking → doit mettre à jour les champs")
    void shouldUpdateBooking() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0)));
        Booking saved = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));

        Booking updated = makeBooking(10L, 1L, MONDAY, LocalTime.of(10,0), LocalTime.of(12,0));
        updated.setNotes("Note mise à jour");

        Booking result = service.updateBooking(saved.getId(), updated);

        assertEquals(LocalTime.of(10, 0), result.getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getEndTime());
        assertEquals("Note mise à jour", result.getNotes());
    }

    @Test
    @DisplayName("updateBooking → doit lever exception si COMPLETED")
    void shouldThrowWhenUpdatingCompleted() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        Booking saved = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));
        service.completeBooking(saved.getId());

        assertThrows(RuntimeException.class, () ->
                service.updateBooking(saved.getId(),
                        makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0))));
    }

    @Test
    @DisplayName("updateBooking → doit lever exception si CANCELLED")
    void shouldThrowWhenUpdatingCancelled() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        Booking saved = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));
        service.cancelBooking(saved.getId(), "STUDENT", "Annulé");

        assertThrows(RuntimeException.class, () ->
                service.updateBooking(saved.getId(),
                        makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0))));
    }

    // ── DELETE ───────────────────────────────────────

    @Test
    @DisplayName("deleteBooking → doit supprimer le booking")
    void shouldDeleteBooking() {
        service.addAvailability(makeAvailability(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0)));
        Booking saved = service.createBooking(
                makeBooking(10L, 1L, MONDAY, LocalTime.of(9,0), LocalTime.of(11,0)));

        service.deleteBooking(saved.getId());

        assertThrows(RuntimeException.class, () -> service.getBookingById(saved.getId()));
    }

    // ── STATUTS ──────────────────────────────────────

    }