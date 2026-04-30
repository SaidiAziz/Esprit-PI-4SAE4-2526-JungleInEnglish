package exp.usermicroservice.Repositories;

import exp.usermicroservice.Entities.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    java.util.Optional<StudentProfile> findByUser_Id(Long userId);
}
