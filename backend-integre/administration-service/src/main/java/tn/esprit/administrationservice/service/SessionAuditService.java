package tn.esprit.administrationservice.service;

import tn.esprit.administrationservice.entity.AuditActionType;
import tn.esprit.administrationservice.entity.SessionAudit;
import tn.esprit.administrationservice.entity.SessionStatus;

import java.util.List;

public interface SessionAuditService {

  void logAction(Long sessionId,
                 AuditActionType actionType,
                 String modifiedBy,
                 SessionStatus oldStatus,
                 SessionStatus newStatus,
                 String details);

  List<SessionAudit> getAuditBySessionId(Long sessionId);

  List<SessionAudit> getAllAudits();
}
