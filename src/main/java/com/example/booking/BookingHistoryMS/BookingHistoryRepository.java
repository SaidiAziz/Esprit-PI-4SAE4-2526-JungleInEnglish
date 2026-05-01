package com.example.booking.BookingHistoryMS;
import com.example.booking.BookingMS.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    List<BookingHistory> findByBookingId(Long bookingId);

    List<BookingHistory> findByBookingIdOrderByChangedAtDesc(Long bookingId);

    List<BookingHistory> findByChangedBy(String changedBy);

    List<BookingHistory> findByNewStatus(BookingStatus status); // ✅

    List<BookingHistory> findByBookingIdAndNewStatus(Long bookingId, BookingStatus status); // ✅
}
