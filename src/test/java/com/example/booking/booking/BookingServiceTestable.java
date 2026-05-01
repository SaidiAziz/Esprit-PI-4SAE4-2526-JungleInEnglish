package com.example.booking.booking;

// src/test/java/com/example/booking/BookingMS/BookingServiceTestable.java


import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.AvailabilityMS.AvailabilityType;
import com.example.booking.BookingMS.Booking;
import com.example.booking.BookingMS.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

public class BookingServiceTestable {

    private final TestBookingRepository bookingRepo;
    private final FakeAvailabilityStore availabilityStore;

    public BookingServiceTestable(TestBookingRepository bookingRepo,
                                  FakeAvailabilityStore availabilityStore) {
        this.bookingRepo       = bookingRepo;
        this.availabilityStore = availabilityStore;
    }

    // ── Availability store en mémoire ────────────────
    static class FakeAvailabilityStore {
        private final Map<Long, Availability> store = new HashMap<>();
        private long idCounter = 1L;

        public Availability save(Availability a) {
            if (a.getId() == null) a.setId(idCounter++);
            store.put(a.getId(), a);
            return a;
        }

        public Optional<Availability> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        public List<Availability> findByTutorIdAndAvailableTrue(Long tutorId) {
            return store.values().stream()
                    .filter(a -> tutorId.equals(a.getTutorId()) && a.isAvailable())
                    .toList();
        }

        public void delete(Availability a) {
            store.remove(a.getId());
        }

        public void deleteById(Long id) {
            store.remove(id);
        }
    }

    // ── CRUD Booking ─────────────────────────────────

    public Booking createBooking(Booking booking) {
        verifyTutorAvailability(
                booking.getTutorId(),
                booking.getSessionDate(),
                booking.getStartTime(),
                booking.getEndTime());

        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepo.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public List<Booking> getBookingsByStudent(Long studentId) {
        return bookingRepo.findByStudentId(studentId);
    }

    public List<Booking> getBookingsByTutor(Long tutorId) {
        return bookingRepo.findByTutorId(tutorId);
    }

    public Booking updateBooking(Long id, Booking updated) {
        Booking existing = getBookingById(id);

        if (existing.getStatus() == BookingStatus.COMPLETED ||
                existing.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot update a " + existing.getStatus() + " booking");
        }

        existing.setSessionDate(updated.getSessionDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setNotes(updated.getNotes());
        existing.setUpdatedAt(LocalDateTime.now());
        return bookingRepo.save(existing);
    }

    public void deleteBooking(Long id) {
        bookingRepo.delete(getBookingById(id));
    }

    // ── Statuts ──────────────────────────────────────

    public Booking confirmBooking(Long id) {
        Booking booking = getBookingById(id);
        consumeTutorAvailability(
                booking.getTutorId(),
                booking.getSessionDate(),
                booking.getStartTime(),
                booking.getEndTime());
        return changeStatus(id, BookingStatus.CONFIRMED);
    }

    public Booking rejectBooking(Long id, String reason) {
        return changeStatus(id, BookingStatus.REJECTED);
    }

    public Booking cancelBooking(Long id, String cancelledBy, String reason) {
        return changeStatus(id, BookingStatus.CANCELLED);
    }

    public Booking completeBooking(Long id) {
        return changeStatus(id, BookingStatus.COMPLETED);
    }

    // ── Availability ──────────────────────────────────

    public Availability addAvailability(Availability a) {
        return availabilityStore.save(a);
    }

    public List<Availability> getAvailabilityByTutor(Long tutorId) {
        return availabilityStore.findByTutorIdAndAvailableTrue(tutorId);
    }

    public Availability updateAvailability(Long id, Availability updated) {
        Availability existing = availabilityStore.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found with id: " + id));
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setAvailable(updated.isAvailable());
        existing.setDayOfWeek(updated.getDayOfWeek());
        return availabilityStore.save(existing);
    }

    public void deleteAvailability(Long id) {
        availabilityStore.deleteById(id);
    }

    // ── Méthodes privées ─────────────────────────────

    private Booking changeStatus(Long id, BookingStatus newStatus) {
        Booking booking = getBookingById(id);
        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepo.save(booking);
    }

    private void verifyTutorAvailability(Long tutorId, LocalDate date,
                                         LocalTime start, LocalTime end) {
        List<Availability> slots = availabilityStore.findByTutorIdAndAvailableTrue(tutorId);
        if (findMatchingSlot(slots, date, start, end) == null) {
            throw new RuntimeException("Tutor is not available on " + date +
                    " from " + start + " to " + end);
        }
    }

    private void consumeTutorAvailability(Long tutorId, LocalDate date,
                                          LocalTime start, LocalTime end) {
        List<Availability> slots = availabilityStore.findByTutorIdAndAvailableTrue(tutorId);
        Availability matched = findMatchingSlot(slots, date, start, end);
        if (matched == null) {
            throw new RuntimeException("Availability no longer exists for tutor " + tutorId);
        }
        availabilityStore.delete(matched);
    }

    private Availability findMatchingSlot(List<Availability> slots, LocalDate date,
                                          LocalTime start, LocalTime end) {
        return slots.stream().filter(slot -> {
            boolean dateMatches = false;
            if (slot.getAvailabilityType() != null &&
                    slot.getAvailabilityType().name().equals("ONE_TIME")) {
                dateMatches = slot.getSpecificDate() != null &&
                        slot.getSpecificDate().equals(date);
            } else if (slot.getDayOfWeek() != null) {
                dateMatches = slot.getDayOfWeek().equals(date.getDayOfWeek());
            } else if (slot.getSpecificDate() != null) {
                dateMatches = slot.getSpecificDate().equals(date);
            }
            boolean timeMatches = slot.getStartTime() != null &&
                    slot.getEndTime() != null &&
                    !start.isBefore(slot.getStartTime()) &&
                    !end.isAfter(slot.getEndTime());
            return dateMatches && timeMatches;
        }).findFirst().orElse(null);
    }
}