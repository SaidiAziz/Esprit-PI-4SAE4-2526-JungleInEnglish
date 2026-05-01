package com.example.booking.booking;

import com.example.booking.BookingMS.Booking;
import com.example.booking.BookingMS.BookingStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class FakeBookingRepository implements TestBookingRepository {

    private final Map<Long, Booking> store = new HashMap<>();
    private long idCounter = 1L;

    @Override
    public Booking save(Booking b) {
        if (b.getId() == null) b.setId(idCounter++);
        store.put(b.getId(), b);
        return b;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void delete(Booking b) {
        store.remove(b.getId());
    }

    @Override
    public List<Booking> findByStudentId(Long studentId) {
        return store.values().stream()
                .filter(b -> studentId.equals(b.getStudentId()))
                .toList();
    }

    @Override
    public List<Booking> findByTutorId(Long tutorId) {
        return store.values().stream()
                .filter(b -> tutorId.equals(b.getTutorId()))
                .toList();
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        return store.values().stream()
                .filter(b -> status.equals(b.getStatus()))
                .toList();
    }

    @Override
    public List<Booking> findByStudentIdAndStatus(Long studentId, BookingStatus status) {
        return store.values().stream()
                .filter(b -> studentId.equals(b.getStudentId()) && status.equals(b.getStatus()))
                .toList();
    }

    @Override
    public List<Booking> findByTutorIdAndStatus(Long tutorId, BookingStatus status) {
        return store.values().stream()
                .filter(b -> tutorId.equals(b.getTutorId()) && status.equals(b.getStatus()))
                .toList();
    }

    @Override
    public List<Booking> findBySessionDate(LocalDate date) {
        return store.values().stream()
                .filter(b -> date.equals(b.getSessionDate()))
                .toList();
    }

    @Override
    public List<Booking> findConflictingBookings(Long tutorId, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime) {
        return store.values().stream()
                .filter(b -> tutorId.equals(b.getTutorId())
                        && date.equals(b.getSessionDate())
                        && b.getStatus() != BookingStatus.CANCELLED
                        && b.getStatus() != BookingStatus.REJECTED
                        && b.getStartTime().isBefore(endTime)
                        && b.getEndTime().isAfter(startTime))
                .toList();
    }
}