package esprit.tn.jungleevents.services;

import esprit.tn.jungleevents.DTO.SessionRequestDTO;
import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.entities.Session;
import esprit.tn.jungleevents.entities.SessionType;
import esprit.tn.jungleevents.repositories.EventRepository;
import esprit.tn.jungleevents.repositories.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    private final EventRepository eventRepository;

    public SessionServiceImpl(SessionRepository sessionRepository, EventRepository eventRepository) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Session createSession(SessionRequestDTO dto) {

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        Session session = new Session();
        session.setDate(dto.getDate());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setType(SessionType.valueOf(dto.getType()));
        session.setRoom(dto.getRoom());
        session.setMeetingLink(dto.getMeetingLink());

        // places automatiques
        session.setAvailableSeats(event.getMaxParticipants());

        session.setEvent(event);

        return sessionRepository.save(session);
    }


    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public List<Session> getSessionsByEvent(Long eventId) {
        return sessionRepository.findByEvent_Id(eventId);
    }

    @Override
    public Session updateSession(Long id, SessionRequestDTO session) {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        existing.setDate(session.getDate());
        existing.setStartTime(session.getStartTime());
        existing.setEndTime(session.getEndTime());
        existing.setRoom(session.getRoom());
        existing.setMeetingLink(session.getMeetingLink());
        existing.setAvailableSeats(session.getAvailableSeats());

        return sessionRepository.save(existing);
    }

    @Override
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }
}