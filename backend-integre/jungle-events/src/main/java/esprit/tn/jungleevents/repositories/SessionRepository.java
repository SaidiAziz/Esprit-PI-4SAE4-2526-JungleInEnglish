package esprit.tn.jungleevents.repositories;

import esprit.tn.jungleevents.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByEvent_Id(Long eventId);
}
