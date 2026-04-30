package esprit.tn.jungleevents.repositories;

import esprit.tn.jungleevents.entities.Participation;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    List<Participation> findBySessionId(Long sessionId);

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);
    // ✅ Nouveau : trouver par userId + eventId via la session
    @Query("SELECT p FROM Participation p WHERE p.userId = :userId AND p.session.event.id = :eventId")
    Optional<Participation> findByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );
    // ✅ nouveau : chercher par email directement
    @Query("SELECT p FROM Participation p WHERE p.userEmail = :email AND p.session.event.id = :eventId")
    Optional<Participation> findByUserEmailAndSessionEventId(
            @Param("email") String email,
            @Param("eventId") Long eventId
    );

}
