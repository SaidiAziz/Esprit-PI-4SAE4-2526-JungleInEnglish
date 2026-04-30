package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumBadWord;

import java.util.Optional;

@Repository
public interface ForumBadWordRepository extends JpaRepository<ForumBadWord, Long> {
    Optional<ForumBadWord> findByWord(String word);
}

