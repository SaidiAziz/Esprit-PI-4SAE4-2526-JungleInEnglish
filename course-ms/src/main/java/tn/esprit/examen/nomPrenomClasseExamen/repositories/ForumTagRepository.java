package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumTag;

import java.util.Optional;

@Repository
public interface ForumTagRepository extends JpaRepository<ForumTag, Long> {
    Optional<ForumTag> findBySlug(String slug);
}

