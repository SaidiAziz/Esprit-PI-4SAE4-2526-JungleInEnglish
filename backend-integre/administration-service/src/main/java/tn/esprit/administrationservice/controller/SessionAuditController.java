package tn.esprit.administrationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.administrationservice.entity.SessionAudit;
import tn.esprit.administrationservice.service.SessionAuditService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sessions/audit")
@RequiredArgsConstructor
public class SessionAuditController {

  private final SessionAuditService sessionAuditService;

  @GetMapping
  public List<SessionAudit> getAllAudits() {
    return sessionAuditService.getAllAudits();
  }

  @GetMapping("/{sessionId}")
  public List<SessionAudit> getAuditBySessionId(@PathVariable Long sessionId) {
    return sessionAuditService.getAuditBySessionId(sessionId);
  }
}
