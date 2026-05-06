package tn.esprit.examen.nomPrenomClasseExamen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPost;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumPostReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ForumReactionType;

import java.util.Optional;

@Repository
public interface ForumPostReactionRepository extends JpaRepository<ForumPostReaction, Long> {

    Optional<ForumPostReaction> findByPostIdAndVoterName(Long postId, String voterName);

    @Query("select count(r) from ForumPostReaction r where r.post = :post and r.type = :type")
    long countByPostAndType(ForumPost post, ForumReactionType type);
}

