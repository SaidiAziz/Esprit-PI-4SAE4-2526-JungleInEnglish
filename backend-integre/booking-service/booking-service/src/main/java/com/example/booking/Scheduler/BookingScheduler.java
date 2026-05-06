package com.example.booking.Scheduler;

import com.example.booking.BookingMS.Booking;
import com.example.booking.BookingMS.BookingRepository;
import com.example.booking.BookingMS.BookingStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    public BookingScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedRate = 60000) // chaque 1 minute
    public void cancelExpiredBookings() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.minusHours(24);

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, limit);
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.saveAll(expiredBookings);
    }
}