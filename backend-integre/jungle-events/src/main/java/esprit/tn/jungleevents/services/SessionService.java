package esprit.tn.jungleevents.services;
import esprit.tn.jungleevents.DTO.SessionRequestDTO;
import esprit.tn.jungleevents.entities.Session;

import java.util.List;

public interface SessionService {


    List<Session> getAllSessions();

    List<Session> getSessionsByEvent(Long eventId);

    Session updateSession(Long id, SessionRequestDTO session);

    void deleteSession(Long id);

    Session createSession(SessionRequestDTO dto);
}
