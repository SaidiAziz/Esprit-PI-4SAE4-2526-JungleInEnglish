package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.response.RoomAnalyticsResponse;

public interface RoomAnalyticsService {

    RoomAnalyticsResponse getAnalytics(Long roomId);
}
