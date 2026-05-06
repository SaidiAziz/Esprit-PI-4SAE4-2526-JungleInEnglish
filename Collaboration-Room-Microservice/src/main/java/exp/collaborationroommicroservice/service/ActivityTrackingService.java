package exp.collaborationroommicroservice.service;

public interface ActivityTrackingService {

    void track(Long userId);

    boolean hasSevenDayStreak(Long userId);
}
