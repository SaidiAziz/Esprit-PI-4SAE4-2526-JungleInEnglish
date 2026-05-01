package com.example.booking.BookingHistory;

import com.example.booking.BookingHistoryMS.BookingHistory;
import com.example.booking.BookingHistoryMS.BookingHistoryRepository;
import com.example.booking.BookingHistoryMS.BookingHistoryService;
import com.example.booking.BookingMS.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingHistoryServiceTest {

    @Mock
    private BookingHistoryRepository historyRepository;

    @InjectMocks
    private BookingHistoryService bookingHistoryService;

    private BookingHistory history;

    // ── Helper ───────────────────────────────────────────────────────
    private BookingHistory makeHistory(Long id, BookingStatus newStatus, String changedBy) {
        BookingHistory h = new BookingHistory();
        h.setId(id);
        h.setChangedBy(changedBy);
        h.setNewStatus(newStatus);
        h.setChangedAt(LocalDateTime.now());
        return h;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        history = makeHistory(1L, BookingStatus.COMPLETED, "TUTOR"); // ✅
    }

    // ─────────────────────────────
    // SAVE ✅
    // ─────────────────────────────
    @Test
    void shouldSaveHistory() {
        when(historyRepository.save(any(BookingHistory.class))).thenReturn(history);

        BookingHistory result = bookingHistoryService.save(history);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(historyRepository, times(1)).save(history);
    }

    // ─────────────────────────────
    // GET BY ID ✅
    // ─────────────────────────────
    @Test
    void shouldGetHistoryById() {
        when(historyRepository.findById(1L)).thenReturn(Optional.of(history));

        BookingHistory result = bookingHistoryService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("TUTOR", result.getChangedBy());
    }

    // ─────────────────────────────
    // GET BY ID — NOT FOUND ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenHistoryNotFound() {
        when(historyRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingHistoryService.getById(99L)
        );

        assertTrue(ex.getMessage().contains("History not found with id: 99"));
    }

    // ─────────────────────────────
    // GET ALL ✅
    // ─────────────────────────────
    @Test
    void shouldGetAllHistories() {
        List<BookingHistory> list = List.of(
                makeHistory(1L, BookingStatus.COMPLETED, "TUTOR"),  // ✅
                makeHistory(2L, BookingStatus.CANCELLED, "STUDENT") // ✅
        );
        when(historyRepository.findAll()).thenReturn(list);

        List<BookingHistory> result = bookingHistoryService.getAll();

        assertEquals(2, result.size());
    }

    // ─────────────────────────────
    // GET BY BOOKING ID ✅
    // ─────────────────────────────
    @Test
    void shouldGetHistoryByBookingId() {
        List<BookingHistory> list = List.of(history);
        when(historyRepository.findByBookingIdOrderByChangedAtDesc(1L)).thenReturn(list);

        List<BookingHistory> result = bookingHistoryService.getByBookingId(1L);

        assertEquals(1, result.size());
        verify(historyRepository).findByBookingIdOrderByChangedAtDesc(1L);
    }

    // ─────────────────────────────
    // GET BY CHANGED BY ✅
    // ─────────────────────────────
    @Test
    void shouldGetHistoryByChangedBy() {
        List<BookingHistory> list = List.of(history);
        when(historyRepository.findByChangedBy("TUTOR")).thenReturn(list);

        List<BookingHistory> result = bookingHistoryService.getByChangedBy("TUTOR");

        assertEquals(1, result.size());
        assertEquals("TUTOR", result.get(0).getChangedBy());
    }

    // ─────────────────────────────
    // GET BY NEW STATUS ✅
    // ─────────────────────────────
    @Test
    void shouldGetHistoryByNewStatus() {
        List<BookingHistory> list = List.of(history);
        when(historyRepository.findByNewStatus(BookingStatus.COMPLETED)).thenReturn(list); // ✅

        List<BookingHistory> result = bookingHistoryService.getByNewStatus(BookingStatus.COMPLETED); // ✅

        assertEquals(1, result.size());
        assertEquals(BookingStatus.COMPLETED, result.get(0).getNewStatus()); // ✅
    }

    // ─────────────────────────────
    // GET BY BOOKING AND STATUS ✅
    // ─────────────────────────────
    @Test
    void shouldGetHistoryByBookingIdAndStatus() {
        List<BookingHistory> list = List.of(history);
        when(historyRepository.findByBookingIdAndNewStatus(1L, BookingStatus.COMPLETED)) // ✅
                .thenReturn(list);

        List<BookingHistory> result = bookingHistoryService.getByBookingAndStatus(
                1L, BookingStatus.COMPLETED // ✅
        );

        assertEquals(1, result.size());
    }

    // ─────────────────────────────
    // DELETE BY ID ✅
    // ─────────────────────────────
    @Test
    void shouldDeleteHistoryById() {
        doNothing().when(historyRepository).deleteById(1L);

        bookingHistoryService.deleteHistory(1L);

        verify(historyRepository, times(1)).deleteById(1L);
    }

    // ─────────────────────────────
    // DELETE ALL BY BOOKING ✅
    // ─────────────────────────────
    @Test
    void shouldDeleteAllHistoryByBookingId() {
        List<BookingHistory> list = List.of(
                makeHistory(1L, BookingStatus.COMPLETED, "TUTOR"),  // ✅
                makeHistory(2L, BookingStatus.CANCELLED, "STUDENT") // ✅
        );
        when(historyRepository.findByBookingId(1L)).thenReturn(list);
        doNothing().when(historyRepository).deleteById(anyLong());

        bookingHistoryService.deleteAllByBooking(1L);

        verify(historyRepository, times(1)).deleteById(1L);
        verify(historyRepository, times(1)).deleteById(2L);
    }
}