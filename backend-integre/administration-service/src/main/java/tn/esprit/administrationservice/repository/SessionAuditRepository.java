package tn.esprit.administrationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.administrationservice.entity.SessionAudit;

import java.util.List;

public interface SessionAuditRepository extends JpaRepository<SessionAudit, Long> {

  List<SessionAudit> findBySessionIdOrderByModifiedAtDesc(Long sessionId);

  List<SessionAudit> findAllByOrderByModifiedAtDesc();
}
