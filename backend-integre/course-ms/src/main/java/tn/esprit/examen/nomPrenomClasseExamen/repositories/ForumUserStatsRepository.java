package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumUserStats;

import java.util.Optional;

@Repository
public interface ForumUserStatsRepository extends JpaRepository<ForumUserStats, Long> {
    Optional<ForumUserStats> findByAuthorName(String authorName);
}

