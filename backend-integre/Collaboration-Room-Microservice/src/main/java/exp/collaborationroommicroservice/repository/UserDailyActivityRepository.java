package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, Long> {

    Optional<UserDailyActivity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    List<UserDailyActivity> findByUserIdOrderByActivityDateDesc(Long userId);
}
