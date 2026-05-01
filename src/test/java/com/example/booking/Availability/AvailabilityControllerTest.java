package com.example.booking.Availability;



import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.AvailabilityMS.AvailabilityController;
import com.example.booking.AvailabilityMS.AvailabilityType;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AvailabilityControllerTest {

    private TestableController controller;
    private AvailabilityServiceTestable service;

    // ── Fake Repository ──────────────────────────────
    static class FakeRepo implements TestAvailabilityRepository {

        private final Map<Long, Availability> store = new HashMap<>();
        private long idCounter = 1L;

        @Override
        public Availability save(Availability a) {
            if (a.getId() == null) a.setId(idCounter++);
            store.put(a.getId(), a);
            return a;
        }

        @Override
        public Optional<Availability> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Availability> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void deleteById(Long id) {
            store.remove(id);
        }

        @Override
        public void deleteByTutorId(Long tutorId) {
            store.values().removeIf(a -> tutorId.equals(a.getTutorId()));
        }

        @Override
        public List<Availability> findByTutorId(Long tutorId) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()))
                    .toList();
        }

        @Override
        public List<Availability> findByTutorIdAndAvailableTrue(Long tutorId) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()) && a.isAvailable())
                    .toList();
        }

        @Override
        public List<Availability> findByTutorIdAndAvailableIsTrue(Long tutorId) {
            return findByTutorIdAndAvailableTrue(tutorId);
        }

        @Override
        public List<Availability> findByTutorIdAndDayOfWeek(Long tutorId, DayOfWeek day) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()) && day.equals(a.getDayOfWeek()))
                    .toList();
        }



        @Override
        public List<Availability> findByTutorIdAndAvailabilityType(Long tutorId, AvailabilityType type) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()) && type.equals(a.getAvailabilityType()))
                    .toList();
        }

        @Override
        public List<Availability> findByTutorIdAndSpecificDate(Long tutorId, LocalDate date) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()) && date.equals(a.getSpecificDate()))
                    .toList();
        }

        @Override
        public List<Availability> findAvailableSlots(Long tutorId, DayOfWeek day,
                                                     LocalTime start, LocalTime end) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId())
                            && day.equals(a.getDayOfWeek())
                            && a.isAvailable()
                            && !a.getStartTime().isAfter(start)
                            && !a.getEndTime().isBefore(end))
                    .toList();
        }
    }

    // ── Controller testable ──────────────────────────
    static class TestableController extends AvailabilityController {

        private final AvailabilityServiceTestable service;

        public TestableController(AvailabilityServiceTestable service) {
            super(null);
            this.service = service;
        }

        @Override
        public ResponseEntity<Availability> addAvailability(Availability a) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.addAvailability(a));
        }

        @Override
        public ResponseEntity<Availability> getById(Long id) {
            return ResponseEntity.ok(service.getById(id));
        }

        @Override
        public ResponseEntity<List<Availability>> getAll() {
            return ResponseEntity.ok(service.getAll());
        }

        @Override
        public ResponseEntity<List<Availability>> getByTutor(Long tutorId) {
            return ResponseEntity.ok(service.getByTutor(tutorId));
        }

        @Override
        public ResponseEntity<List<Availability>> getAvailableByTutor(Long tutorId) {
            return ResponseEntity.ok(service.getAvailableSlotsByTutor(tutorId));
        }

        @Override
        public ResponseEntity<List<Availability>> getByTutorAndDay(Long tutorId, DayOfWeek day) {
            return ResponseEntity.ok(service.getByTutorAndDay(tutorId, day));
        }

        @Override
        public ResponseEntity<List<Availability>> getByTutorAndDate(Long tutorId, LocalDate date) {
            return ResponseEntity.ok(service.getByTutorAndDate(tutorId, date));
        }

        @Override
        public ResponseEntity<List<Availability>> getAvailableSlots(Long tutorId, DayOfWeek day,
                                                                    LocalTime start, LocalTime end) {
            return ResponseEntity.ok(service.getAvailableSlots(tutorId, day, start, end));
        }

        @Override
        public ResponseEntity<Availability> updateAvailability(Long id, Availability a) {
            return ResponseEntity.ok(service.updateAvailability(id, a));
        }

        @Override
        public ResponseEntity<Availability> toggleAvailability(Long id) {
            return ResponseEntity.ok(service.toggleAvailability(id));
        }

        @Override
        public ResponseEntity<Void> deleteAvailability(Long id) {
            service.deleteAvailability(id);
            return ResponseEntity.noContent().build();
        }

        @Override
        public ResponseEntity<Void> deleteAllByTutor(Long tutorId) {
            service.deleteAllByTutor(tutorId);
            return ResponseEntity.noContent().build();
        }
    }

    // ── Setup ────────────────────────────────────────

    @BeforeEach
    void setUp() {
        service    = new AvailabilityServiceTestable(new FakeRepo());
        controller = new TestableController(service);
    }

    private Availability make(Long tutorId, DayOfWeek day,
                              LocalTime start, LocalTime end,
                              boolean available, LocalDate date) {
        Availability a = new Availability();
        a.setTutorId(tutorId);
        a.setDayOfWeek(day);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setAvailable(available);
        a.setSpecificDate(date);
        return a;
    }

    // ── POST ─────────────────────────────────────────

    @Test
    @DisplayName("POST → 201 avec l'objet créé")
    void shouldReturn201WhenAdding() {
        ResponseEntity<Availability> r = controller.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertNotNull(r.getBody().getId());
        assertEquals(DayOfWeek.MONDAY, r.getBody().getDayOfWeek());
    }

    // ── GET /{id} ────────────────────────────────────

    @Test
    @DisplayName("GET /{id} → 200 si trouvé")
    void shouldReturn200ById() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        ResponseEntity<Availability> r = controller.getById(saved.getId());

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(saved.getId(), r.getBody().getId());
    }

    @Test
    @DisplayName("GET /{id} → exception si non trouvé")
    void shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> controller.getById(999L));
    }

    // ── GET / ────────────────────────────────────────

    @Test
    @DisplayName("GET / → 200 avec toute la liste")
    void shouldReturnAll() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), false, null));

        ResponseEntity<List<Availability>> r = controller.getAll();

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(2, r.getBody().size());
    }

    // ── GET /tutor/{id} ──────────────────────────────

    @Test
    @DisplayName("GET /tutor/{id} → filtre par tuteur")
    void shouldReturnByTutor() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.FRIDAY, LocalTime.of(10,0), LocalTime.of(12,0), true, null));

        ResponseEntity<List<Availability>> r = controller.getByTutor(1L);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(1, r.getBody().size());
        assertEquals(1L, r.getBody().get(0).getTutorId());
    }

    // ── GET /tutor/{id}/available ────────────────────

    @Test
    @DisplayName("GET /tutor/{id}/available → slots disponibles uniquement")
    void shouldReturnOnlyAvailable() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.TUESDAY, LocalTime.of(14,0), LocalTime.of(16,0), false, null));

        ResponseEntity<List<Availability>> r = controller.getAvailableByTutor(1L);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(1, r.getBody().size());
        assertTrue(r.getBody().get(0).isAvailable());
    }

    // ── GET /tutor/{id}/day ──────────────────────────

    @Test
    @DisplayName("GET /tutor/{id}/day → filtre par jour")
    void shouldReturnByDay() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), true, null));

        ResponseEntity<List<Availability>> r = controller.getByTutorAndDay(1L, DayOfWeek.MONDAY);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(1, r.getBody().size());
        assertEquals(DayOfWeek.MONDAY, r.getBody().get(0).getDayOfWeek());
    }

    // ── GET /tutor/{id}/date ─────────────────────────

    @Test
    @DisplayName("GET /tutor/{id}/date → filtre par date")
    void shouldReturnByDate() {
        LocalDate date = LocalDate.of(2025, 6, 2);
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, date));
        service.addAvailability(make(1L, DayOfWeek.TUESDAY, LocalTime.of(14,0), LocalTime.of(16,0), true,
                LocalDate.of(2025, 7, 1)));

        ResponseEntity<List<Availability>> r = controller.getByTutorAndDate(1L, date);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(1, r.getBody().size());
        assertEquals(date, r.getBody().get(0).getSpecificDate());
    }

    // ── GET /tutor/{id}/slots ────────────────────────

    @Test
    @DisplayName("GET /tutor/{id}/slots → filtre par créneau horaire")
    void shouldReturnSlots() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(13,0), LocalTime.of(15,0), true, null));

        ResponseEntity<List<Availability>> r = controller.getAvailableSlots(
                1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0));

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(1, r.getBody().size());
    }

    // ── PUT /{id} ────────────────────────────────────

    @Test
    @DisplayName("PUT /{id} → met à jour et retourne 200")
    void shouldUpdate() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        Availability updated = make(1L, DayOfWeek.FRIDAY,
                LocalTime.of(14,0), LocalTime.of(16,0), false, null);

        ResponseEntity<Availability> r = controller.updateAvailability(saved.getId(), updated);

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(DayOfWeek.FRIDAY, r.getBody().getDayOfWeek());
        assertEquals(LocalTime.of(14,0), r.getBody().getStartTime());
        assertFalse(r.getBody().isAvailable());
    }

    // ── PATCH /{id}/toggle ───────────────────────────

    @Test
    @DisplayName("PATCH /{id}/toggle → true devient false")
    void shouldToggleTrueToFalse() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        ResponseEntity<Availability> r = controller.toggleAvailability(saved.getId());

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertFalse(r.getBody().isAvailable());
    }

    @Test
    @DisplayName("PATCH /{id}/toggle → false devient true")
    void shouldToggleFalseToTrue() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), false, null));

        ResponseEntity<Availability> r = controller.toggleAvailability(saved.getId());

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(r.getBody().isAvailable());
    }

    // ── DELETE /{id} ─────────────────────────────────

    @Test
    @DisplayName("DELETE /{id} → 204 et supprimé")
    void shouldDeleteById() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        ResponseEntity<Void> r = controller.deleteAvailability(saved.getId());

        assertEquals(HttpStatus.NO_CONTENT, r.getStatusCode());
        assertThrows(RuntimeException.class, () -> controller.getById(saved.getId()));
    }

    // ── DELETE /tutor/{id} ───────────────────────────

    @Test
    @DisplayName("DELETE /tutor/{id} → 204 et tous les slots supprimés")
    void shouldDeleteAllByTutor() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.TUESDAY, LocalTime.of(10,0), LocalTime.of(12,0), true, null));

        ResponseEntity<Void> r = controller.deleteAllByTutor(1L);

        assertEquals(HttpStatus.NO_CONTENT, r.getStatusCode());
        assertEquals(0, controller.getByTutor(1L).getBody().size());
        assertEquals(1, controller.getByTutor(2L).getBody().size());
    }
}
