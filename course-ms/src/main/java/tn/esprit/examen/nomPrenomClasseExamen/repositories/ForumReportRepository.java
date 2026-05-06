package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReport;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReportStatus;

@Repository
public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {
    Page<ForumReport> findByStatus(ForumReportStatus status, Pageable pageable);
}

