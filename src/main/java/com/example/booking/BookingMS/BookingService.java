package com.example.booking.BookingMS;

import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.AvailabilityMS.AvailabilityRepository;
import com.example.booking.BookingHistoryMS.BookingHistory;
import com.example.booking.BookingHistoryMS.BookingHistoryRepository;
import com.example.booking.SessionFeedbackMS.SessionFeedback;
import com.example.booking.SessionFeedbackMS.SessionFeedbackRepository;
import com.example.booking.SessionMS.Session;
import com.example.booking.SessionMS.SessionRepository;
import com.example.booking.SessionMS.SessionStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionFeedbackRepository feedbackRepository;

    @Autowired
    private BookingHistoryRepository historyRepository;

    @Autowired
    private PusherBeamsService pusherBeamsService;

    // ─────────────────────────────────────────────
    // CRUD BOOKING
    // ─────────────────────────────────────────────

    @Transactional
    public Booking createBooking(Booking booking) {

        // Étape 1 — vérifier la disponibilité SANS la consommer
        verifyTutorAvailability(
                booking.getTutorId(),
                booking.getSessionDate(),
                booking.getStartTime(),
                booking.getEndTime());

        // Étape 2 — créer la réservation
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);

        // Étape 3 — historique
        saveHistory(savedBooking, null, BookingStatus.PENDING, "STUDENT", "Booking created");

        // Étape 4 — créer la session avec lien Jitsi
        Session session = new Session();
        session.setBooking(savedBooking);
        session.setMeetingLink(generateJitsiLink(savedBooking.getId()));
        session.setStatus(SessionStatus.SCHEDULED);
        sessionRepository.save(session);

        // Étape 5 — notifier le tuteur
        pusherBeamsService.notifyTutor(
                booking.getTutorId(),
                "📅 Nouvelle demande de réservation",
                "Un étudiant a demandé une session le " +
                        booking.getSessionDate() + " de " +
                        booking.getStartTime() + " à " +
                        booking.getEndTime());

        return savedBooking;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByStudent(Long studentId) {
        return bookingRepository.findByStudentId(studentId);
    }

    public List<Booking> getBookingsByTutor(Long tutorId) {
        return bookingRepository.findByTutorId(tutorId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking existing = getBookingById(id);

        if (existing.getStatus() == BookingStatus.COMPLETED ||
                existing.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot update a " + existing.getStatus() + " booking");
        }

        BookingStatus oldStatus = existing.getStatus();
        existing.setSessionDate(updatedBooking.getSessionDate());
        existing.setStartTime(updatedBooking.getStartTime());
        existing.setEndTime(updatedBooking.getEndTime());
        existing.setNotes(updatedBooking.getNotes());
        existing.setType(updatedBooking.getType());
        existing.setUpdatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(existing);
        saveHistory(saved, oldStatus, saved.getStatus(), "STUDENT", "Booking updated");
        return saved;
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.delete(getBookingById(id));
    }

    // ─────────────────────────────────────────────
    // GESTION DES STATUTS
    // ─────────────────────────────────────────────

    @Transactional
    public Booking confirmBooking(Long id) {
        Booking booking = getBookingById(id);

        // ✅ Consommer (supprimer) le créneau uniquement à la confirmation
        consumeTutorAvailability(
                booking.getTutorId(),
                booking.getSessionDate(),
                booking.getStartTime(),
                booking.getEndTime());

        return changeStatus(id, BookingStatus.CONFIRMED, "TUTOR", "Booking confirmed by tutor");
    }

    @Transactional
    public Booking rejectBooking(Long id, String reason) {
        // Le créneau n'a pas été consommé → il reste disponible automatiquement
        return changeStatus(id, BookingStatus.REJECTED, "TUTOR", reason);
    }

    @Transactional
    public Booking cancelBooking(Long id, String cancelledBy, String reason) {
        return changeStatus(id, BookingStatus.CANCELLED, cancelledBy, reason);
    }

    @Transactional
    public Booking completeBooking(Long id) {
        return changeStatus(id, BookingStatus.COMPLETED, "SYSTEM", "Session completed");
    }

    // ─────────────────────────────────────────────
    // AVAILABILITY CRUD
    // ─────────────────────────────────────────────

    public Availability addAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }

    public List<Availability> getAvailabilityByTutor(Long tutorId) {
        return availabilityRepository.findByTutorIdAndAvailableTrue(tutorId);
    }

    public Availability updateAvailability(Long id, Availability updated) {
        Availability existing = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found with id: " + id));
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setAvailable(updated.isAvailable());
        existing.setDayOfWeek(updated.getDayOfWeek());
        return availabilityRepository.save(existing);
    }

    public void deleteAvailability(Long id) {
        availabilityRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────
    // SESSION FEEDBACK CRUD
    // ─────────────────────────────────────────────

    @Transactional
    public SessionFeedback addFeedback(Long sessionId, SessionFeedback feedback) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        if (session.getStatus() != SessionStatus.DONE) {
            throw new RuntimeException("Cannot rate a session that is not completed yet");
        }

        feedback.setSession(session);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    public List<SessionFeedback> getFeedbackByTutor(Long tutorId) {
        return feedbackRepository.findByTutorId(tutorId);
    }

    // ─────────────────────────────────────────────
    // MÉTHODES PRIVÉES
    // ─────────────────────────────────────────────

    private Booking changeStatus(Long id, BookingStatus newStatus,
                                 String changedBy, String reason) {
        Booking booking = getBookingById(id);
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        saveHistory(saved, oldStatus, newStatus, changedBy, reason);
        return saved;
    }

    /**
     * Vérifie qu'un créneau correspondant existe pour le tuteur,
     * SANS le supprimer. Utilisé lors de la création du booking (PENDING).
     */
    private void verifyTutorAvailability(Long tutorId, LocalDate date,
                                         LocalTime start, LocalTime end) {
        List<Availability> slots = availabilityRepository
                .findByTutorIdAndAvailableTrue(tutorId);

        Availability matchedSlot = findMatchingSlot(slots, date, start, end);

        if (matchedSlot == null) {
            throw new RuntimeException(buildDebugMessage(tutorId, date, start, end, slots));
        }
        // ✅ Ne pas supprimer ici — on attend la confirmation
    }

    /**
     * Vérifie et SUPPRIME le créneau correspondant.
     * Utilisé uniquement lors de la confirmation du booking.
     */
    private void consumeTutorAvailability(Long tutorId, LocalDate date,
                                          LocalTime start, LocalTime end) {
        List<Availability> slots = availabilityRepository
                .findByTutorIdAndAvailableTrue(tutorId);

        Availability matchedSlot = findMatchingSlot(slots, date, start, end);

        if (matchedSlot == null) {
            throw new RuntimeException(
                    "Availability no longer exists for tutor " + tutorId +
                            " on " + date + " from " + start + " to " + end);
        }

        // ✅ Suppression du créneau uniquement à la confirmation
        availabilityRepository.delete(matchedSlot);
    }

    /**
     * Logique commune de matching d'un créneau.
     */
    private Availability findMatchingSlot(List<Availability> slots, LocalDate date,
                                          LocalTime start, LocalTime end) {
        return slots.stream().filter(slot -> {
            boolean dateMatches = false;

            if (slot.getAvailabilityType() != null &&
                    slot.getAvailabilityType().name().equals("ONE_TIME")) {
                if (slot.getSpecificDate() != null) {
                    dateMatches = slot.getSpecificDate().equals(date);
                }
            } else if (slot.getDayOfWeek() != null) {
                dateMatches = slot.getDayOfWeek().equals(date.getDayOfWeek());
            } else if (slot.getSpecificDate() != null) {
                dateMatches = slot.getSpecificDate().equals(date);
            }

            boolean timeMatches = false;
            if (slot.getStartTime() != null && slot.getEndTime() != null) {
                timeMatches = !start.isBefore(slot.getStartTime()) &&
                        !end.isAfter(slot.getEndTime());
            }

            return dateMatches && timeMatches;
        }).findFirst().orElse(null);
    }

    private String buildDebugMessage(Long tutorId, LocalDate date,
                                     LocalTime start, LocalTime end, List<Availability> slots) {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("Tutor is not available on ").append(date)
                .append(" from ").append(start).append(" to ").append(end)
                .append(" | Found slots: ");
        if (slots.isEmpty()) {
            debugInfo.append("NO 'available=true' slots found for tutor ")
                    .append(tutorId).append(" in database.");
        } else {
            for (Availability s : slots) {
                debugInfo.append("[Type:").append(s.getAvailabilityType())
                        .append(", Day:").append(s.getDayOfWeek())
                        .append(", Date:").append(s.getSpecificDate())
                        .append(", Time:").append(s.getStartTime())
                        .append("-").append(s.getEndTime()).append("] ");
            }
        }
        return debugInfo.toString();
    }

    private void saveHistory(Booking booking, BookingStatus oldStatus,
                             BookingStatus newStatus, String changedBy, String reason) {
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private String generateJitsiLink(Long bookingId) {
        return "https://meet.jit.si/JungleInEnglish-Session-" + bookingId;
    }
}