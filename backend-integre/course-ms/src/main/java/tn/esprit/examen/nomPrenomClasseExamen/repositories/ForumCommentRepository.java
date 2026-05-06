package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumComment;

import java.time.LocalDateTime;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    long countByAuthorNameAndCreatedAtAfter(String authorName, LocalDateTime after);
}

