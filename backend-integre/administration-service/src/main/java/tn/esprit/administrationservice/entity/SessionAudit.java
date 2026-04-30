package tn.esprit.administrationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SessionAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sessionId;

  @Enumerated(EnumType.STRING)
  private AuditActionType actionType;

  private String modifiedBy;

  private LocalDateTime modifiedAt;

  @Enumerated(EnumType.STRING)
  private SessionStatus oldStatus;

  @Enumerated(EnumType.STRING)
  private SessionStatus newStatus;

  private String details;

  public SessionAudit() {
  }

  public SessionAudit(Long sessionId, AuditActionType actionType, String modifiedBy,
                      LocalDateTime modifiedAt, SessionStatus oldStatus,
                      SessionStatus newStatus, String details) {
    this.sessionId = sessionId;
    this.actionType = actionType;
    this.modifiedBy = modifiedBy;
    this.modifiedAt = modifiedAt;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.details = details;
  }

  public Long getId() {
    return id;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }

  public AuditActionType getActionType() {
    return actionType;
  }

  public void setActionType(AuditActionType actionType) {
    this.actionType = actionType;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(LocalDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public SessionStatus getOldStatus() {
    return oldStatus;
  }

  public void setOldStatus(SessionStatus oldStatus) {
    this.oldStatus = oldStatus;
  }

  public SessionStatus getNewStatus() {
    return newStatus;
  }

  public void setNewStatus(SessionStatus newStatus) {
    this.newStatus = newStatus;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
