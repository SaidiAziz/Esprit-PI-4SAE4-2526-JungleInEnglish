package tn.esprit.administrationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.administrationservice.entity.AuditActionType;
import tn.esprit.administrationservice.entity.MeetingProvider;
import tn.esprit.administrationservice.entity.Session;
import tn.esprit.administrationservice.entity.SessionMode;
import tn.esprit.administrationservice.entity.SessionStatus;
import tn.esprit.administrationservice.exception.BadRequestException;
import tn.esprit.administrationservice.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

  private final SessionRepository sessionRepository;
  private final SessionAuditService sessionAuditService;
  private final JitsiMeetingService jitsiMeetingService;

  private final UserValidationService userValidationService;

  @Override
  public Session create(Session session) {
    validateTimes(session);
    userValidationService.validateTutor(session.getTutorId());

    boolean conflict = sessionRepository.existsConflictForTutor(
      session.getTutorId(),
      session.getSessionDate(),
      session.getStartTime(),
      session.getEndTime(),
      SessionStatus.CANCELED
    );

    if (conflict) {
      throw new BadRequestException(
        "Conflit: le tuteur " + session.getTutorId() +
          " a déjà une session qui chevauche " +
          session.getSessionDate() + " [" + session.getStartTime() + " - " + session.getEndTime() + "]"
      );
    }

    session.setStatus(SessionStatus.PLANNED);
    session.setCreatedAt(LocalDateTime.now());
    session.setUpdatedAt(LocalDateTime.now());

    Session savedSession = sessionRepository.save(session);

    attachMeetingData(savedSession);

    savedSession = sessionRepository.save(savedSession);

    sessionAuditService.logAction(
      savedSession.getId(),
      AuditActionType.CREATED,
      "ADMIN",
      null,
      savedSession.getStatus(),
      "Session created"
    );

    return savedSession;
  }

  @Override
  public Session update(Long id, Session session) {
    Session existing = getById(id);
    SessionStatus oldStatus = existing.getStatus();

    session.setId(existing.getId());
    session.setCreatedAt(existing.getCreatedAt());
    session.setUpdatedAt(LocalDateTime.now());

    if (session.getStatus() == null) {
      session.setStatus(existing.getStatus());
    }

    validateTimes(session);
    userValidationService.validateTutor(session.getTutorId());

    boolean conflict = sessionRepository.existsConflictForTutorExcludingSession(
      id,
      session.getTutorId(),
      session.getSessionDate(),
      session.getStartTime(),
      session.getEndTime(),
      SessionStatus.CANCELED
    );

    if (conflict) {
      throw new BadRequestException(
        "Conflit: le tuteur " + session.getTutorId() +
          " a déjà une session qui chevauche " +
          session.getSessionDate() + " [" + session.getStartTime() + " - " + session.getEndTime() + "]"
      );
    }

    preserveOrRegenerateMeetingData(existing, session);

    Session updatedSession = sessionRepository.save(session);

    sessionAuditService.logAction(
      updatedSession.getId(),
      AuditActionType.UPDATED,
      "ADMIN",
      oldStatus,
      updatedSession.getStatus(),
      "Session updated"
    );

    return updatedSession;
  }

  @Override
  public void delete(Long id) {
    Session existing = getById(id);

    sessionAuditService.logAction(
      existing.getId(),
      AuditActionType.DELETED,
      "ADMIN",
      existing.getStatus(),
      null,
      "Session deleted"
    );

    sessionRepository.deleteById(id);
  }

  @Override
  public Session getById(Long id) {
    return sessionRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Session not found"));
  }

  @Override
  public List<Session> getAll() {
    return sessionRepository.findAll();
  }

  @Override
  public List<Session> getByTutor(Long tutorId) {
    return sessionRepository.findByTutorId(tutorId);
  }

  @Override
  public Session cancel(Long id) {
    Session existing = getById(id);
    SessionStatus oldStatus = existing.getStatus();

    if (existing.getStatus() == SessionStatus.COMPLETED) {
      throw new BadRequestException("Impossible d'annuler une session déjà COMPLETED");
    }
    if (existing.getStatus() == SessionStatus.CANCELED) {
      return existing;
    }

    existing.setStatus(SessionStatus.CANCELED);
    existing.setUpdatedAt(LocalDateTime.now());

    Session savedSession = sessionRepository.save(existing);

    sessionAuditService.logAction(
      savedSession.getId(),
      AuditActionType.CANCELED,
      "ADMIN",
      oldStatus,
      SessionStatus.CANCELED,
      "Session canceled by admin"
    );

    return savedSession;
  }

  @Override
  public Session complete(Long id) {
    Session existing = getById(id);
    SessionStatus oldStatus = existing.getStatus();

    if (existing.getStatus() == SessionStatus.CANCELED) {
      throw new BadRequestException("Impossible de terminer une session CANCELED");
    }
    if (existing.getStatus() == SessionStatus.COMPLETED) {
      return existing;
    }

    existing.setStatus(SessionStatus.COMPLETED);
    existing.setUpdatedAt(LocalDateTime.now());

    Session savedSession = sessionRepository.save(existing);

    sessionAuditService.logAction(
      savedSession.getId(),
      AuditActionType.COMPLETED,
      "ADMIN",
      oldStatus,
      SessionStatus.COMPLETED,
      "Session marked as completed"
    );

    return savedSession;
  }

  private void validateTimes(Session session) {
    if (session.getTutorId() == null) {
      throw new BadRequestException("tutorId est obligatoire");
    }
    if (session.getSessionDate() == null) {
      throw new BadRequestException("sessionDate est obligatoire");
    }
    if (session.getStartTime() == null || session.getEndTime() == null) {
      throw new BadRequestException("startTime et endTime sont obligatoires");
    }
    if (!session.getEndTime().isAfter(session.getStartTime())) {
      throw new BadRequestException("endTime doit être strictement après startTime");
    }
    if (session.getMode() == null) {
      throw new BadRequestException("mode est obligatoire");
    }
  }

  private void attachMeetingData(Session session) {
    if (session.getMode() == SessionMode.ONLINE) {
      String roomName = jitsiMeetingService.generateRoomName(
        session.getId(),
        "Course" + session.getCourseId(),
        session.getTutorId()
      );

      session.setRoomName(roomName);
      session.setMeetingLink(jitsiMeetingService.buildMeetingLink(roomName));
      session.setMeetingProvider(MeetingProvider.JITSI);
    } else {
      session.setRoomName(null);
      session.setMeetingLink(null);
      session.setMeetingProvider(MeetingProvider.NONE);
    }
  }

  private void preserveOrRegenerateMeetingData(Session existing, Session updated) {
    if (updated.getMode() == SessionMode.ONLINE) {
      if (existing.getMode() == SessionMode.ONLINE
        && existing.getRoomName() != null
        && existing.getMeetingLink() != null) {

        updated.setRoomName(existing.getRoomName());
        updated.setMeetingLink(existing.getMeetingLink());
        updated.setMeetingProvider(existing.getMeetingProvider() != null
          ? existing.getMeetingProvider()
          : MeetingProvider.JITSI);
      } else {
        String roomName = jitsiMeetingService.generateRoomName(
          existing.getId(),
          "Course" + updated.getCourseId(),
          updated.getTutorId()
        );

        updated.setRoomName(roomName);
        updated.setMeetingLink(jitsiMeetingService.buildMeetingLink(roomName));
        updated.setMeetingProvider(MeetingProvider.JITSI);
      }
    } else {
      updated.setRoomName(null);
      updated.setMeetingLink(null);
      updated.setMeetingProvider(MeetingProvider.NONE);
    }
  }
}
