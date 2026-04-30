package tn.esprit.administrationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.administrationservice.entity.AuditActionType;
import tn.esprit.administrationservice.entity.SessionAudit;
import tn.esprit.administrationservice.entity.SessionStatus;
import tn.esprit.administrationservice.repository.SessionAuditRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionAuditServiceImpl implements SessionAuditService {

  private final SessionAuditRepository sessionAuditRepository;

  @Override
  public void logAction(Long sessionId,
                        AuditActionType actionType,
                        String modifiedBy,
                        SessionStatus oldStatus,
                        SessionStatus newStatus,
                        String details) {

    SessionAudit audit = new SessionAudit();
    audit.setSessionId(sessionId);
    audit.setActionType(actionType);
    audit.setModifiedBy(modifiedBy);
    audit.setModifiedAt(LocalDateTime.now());
    audit.setOldStatus(oldStatus);
    audit.setNewStatus(newStatus);
    audit.setDetails(details);

    sessionAuditRepository.save(audit);
  }

  @Override
  public List<SessionAudit> getAuditBySessionId(Long sessionId) {
    return sessionAuditRepository.findBySessionIdOrderByModifiedAtDesc(Long.valueOf(sessionId));
  }

  @Override
  public List<SessionAudit> getAllAudits() {
    return sessionAuditRepository.findAllByOrderByModifiedAtDesc();
  }
}
