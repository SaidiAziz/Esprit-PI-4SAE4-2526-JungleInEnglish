package com.example.booking.Availability;


import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.AvailabilityMS.AvailabilityType;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AvailabilityServiceTest {

    private AvailabilityServiceTestable service;

    // ── Fake Repository en mémoire ───────────────────
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

    @BeforeEach
    void setUp() {
        service = new AvailabilityServiceTestable(new FakeRepo());
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

    // ── CREATE ──────────────────────────────────────

    @Test
    @DisplayName("addAvailability → doit sauvegarder et retourner l'availability")
    void shouldAddAvailability() {
        Availability result = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        assertNotNull(result.getId());
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
    }

    // ── READ ─────────────────────────────────────────

    @Test
    @DisplayName("getById → doit retourner l'availability si elle existe")
    void shouldGetById() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        Availability result = service.getById(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    @DisplayName("getById → doit lever une exception si l'id n'existe pas")
    void shouldThrowWhenNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getById(999L));
        assertEquals("Availability not found with id: 999", ex.getMessage());
    }

    @Test
    @DisplayName("getAll → doit retourner toutes les availabilities")
    void shouldGetAll() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), false, null));

        assertEquals(2, service.getAll().size());
    }

    @Test
    @DisplayName("getByTutor → doit retourner les slots du tuteur")
    void shouldGetByTutor() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.FRIDAY, LocalTime.of(10,0), LocalTime.of(12,0), true, null));

        List<Availability> result = service.getByTutor(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTutorId());
    }

    @Test
    @DisplayName("getAvailableSlotsByTutor → doit retourner uniquement les slots disponibles")
    void shouldGetAvailableSlots() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.TUESDAY, LocalTime.of(14,0), LocalTime.of(16,0), false, null));

        List<Availability> result = service.getAvailableSlotsByTutor(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    @DisplayName("getByTutorAndDay → doit filtrer par jour")
    void shouldGetByTutorAndDay() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), true, null));

        List<Availability> result = service.getByTutorAndDay(1L, DayOfWeek.MONDAY);

        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
    }

    @Test
    @DisplayName("getByTutorAndDate → doit filtrer par date")
    void shouldGetByTutorAndDate() {
        LocalDate date = LocalDate.of(2025, 6, 2);
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, date));
        service.addAvailability(make(1L, DayOfWeek.TUESDAY, LocalTime.of(14,0), LocalTime.of(16,0), true, LocalDate.of(2025, 7, 1)));

        List<Availability> result = service.getByTutorAndDate(1L, date);

        assertEquals(1, result.size());
        assertEquals(date, result.get(0).getSpecificDate());
    }

    // ── UPDATE ───────────────────────────────────────

    @Test
    @DisplayName("updateAvailability → doit mettre à jour tous les champs")
    void shouldUpdate() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        Availability updated = make(1L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), false, null);
        Availability result = service.updateAvailability(saved.getId(), updated);

        assertEquals(DayOfWeek.FRIDAY, result.getDayOfWeek());
        assertEquals(LocalTime.of(14,0), result.getStartTime());
        assertFalse(result.isAvailable());
    }

    @Test
    @DisplayName("toggleAvailability → true devient false")
    void shouldToggleTrueToFalse() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        assertFalse(service.toggleAvailability(saved.getId()).isAvailable());
    }

    @Test
    @DisplayName("toggleAvailability → false devient true")
    void shouldToggleFalseToTrue() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), false, null));

        assertTrue(service.toggleAvailability(saved.getId()).isAvailable());
    }

    // ── DELETE ───────────────────────────────────────

    @Test
    @DisplayName("deleteAvailability → doit supprimer par id")
    void shouldDeleteById() {
        Availability saved = service.addAvailability(
                make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));

        service.deleteAvailability(saved.getId());

        assertThrows(RuntimeException.class, () -> service.getById(saved.getId()));
    }

    @Test
    @DisplayName("deleteAllByTutor → doit supprimer tous les slots du tuteur")
    void shouldDeleteAllByTutor() {
        service.addAvailability(make(1L, DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(11,0), true, null));
        service.addAvailability(make(1L, DayOfWeek.FRIDAY, LocalTime.of(14,0), LocalTime.of(16,0), true, null));
        service.addAvailability(make(2L, DayOfWeek.TUESDAY, LocalTime.of(10,0), LocalTime.of(12,0), true, null));

        service.deleteAllByTutor(1L);

        assertEquals(0, service.getByTutor(1L).size());
        assertEquals(1, service.getByTutor(2L).size());
    }
}