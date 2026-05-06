package exp.usermicroservice.Repositories;

import exp.usermicroservice.Entities.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {

    java.util.Optional<TutorProfile> findByUser_Id(Long userId);
}
