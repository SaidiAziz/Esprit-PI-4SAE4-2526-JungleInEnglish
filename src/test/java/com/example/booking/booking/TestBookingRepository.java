package com.example.booking.booking;


import com.example.booking.BookingMS.Booking;
import com.example.booking.BookingMS.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TestBookingRepository {
    Booking save(Booking b);
    Optional<Booking> findById(Long id);
    List<Booking> findAll();
    void delete(Booking b);
    List<Booking> findByStudentId(Long studentId);
    List<Booking> findByTutorId(Long tutorId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStudentIdAndStatus(Long studentId, BookingStatus status);
    List<Booking> findByTutorIdAndStatus(Long tutorId, BookingStatus status);
    List<Booking> findBySessionDate(LocalDate date);
    List<Booking> findConflictingBookings(Long tutorId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime);
}