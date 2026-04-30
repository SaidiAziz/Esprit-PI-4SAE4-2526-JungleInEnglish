package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.response.RoomMatchResponse;

import java.util.List;

public interface RoomMatchingService {

    List<RoomMatchResponse> recommendForCurrentUser();
}
