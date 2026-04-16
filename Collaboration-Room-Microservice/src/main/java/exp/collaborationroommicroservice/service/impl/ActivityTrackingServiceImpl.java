package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.entity.UserDailyActivity;
import exp.collaborationroommicroservice.repository.UserDailyActivityRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityTrackingServiceImpl implements ActivityTrackingService {

    private final UserDailyActivityRepository activityRepository;

    @Override
    public void track(Long userId) {
        LocalDate today = LocalDate.now();
        UserDailyActivity activity = activityRepository.findByUserIdAndActivityDate(userId, today)
                .orElseGet(() -> UserDailyActivity.builder()
                        .userId(userId)
                        .activityDate(today)
                        .actionsCount(0)
                        .build());
        activity.setActionsCount(activity.getActionsCount() + 1);
        activityRepository.save(activity);
    }

    @Override
    public boolean hasSevenDayStreak(Long userId) {
        List<UserDailyActivity> activities = activityRepository.findByUserIdOrderByActivityDateDesc(userId);
        if (activities.size() < 7) {
            return false;
        }
        LocalDate cursor = LocalDate.now();
        int streak = 0;
        for (UserDailyActivity activity : activities) {
            if (activity.getActivityDate().isEqual(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
                if (streak >= 7) {
                    return true;
                }
            } else if (activity.getActivityDate().isBefore(cursor)) {
                break;
            }
        }
        return false;
    }
}
