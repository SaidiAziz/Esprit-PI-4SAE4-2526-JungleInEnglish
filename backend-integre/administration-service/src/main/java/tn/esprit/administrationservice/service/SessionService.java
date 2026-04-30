package tn.esprit.administrationservice.service;

import tn.esprit.administrationservice.entity.Session;

import java.util.List;

public interface SessionService {
    Session create(Session session);
    Session update(Long id, Session session);
    void delete(Long id);
    Session getById(Long id);
    List<Session> getAll();
    List<Session> getByTutor(Long tutorId);
    Session cancel(Long id);
    Session complete(Long id);
}
