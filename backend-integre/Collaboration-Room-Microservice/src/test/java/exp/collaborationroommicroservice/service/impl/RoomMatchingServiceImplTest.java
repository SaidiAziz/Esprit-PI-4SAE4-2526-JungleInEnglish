package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.response.RoomMatchResponse;
import exp.collaborationroommicroservice.entity.CollaborationRoom;
import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.entity.RoomStatus;
import exp.collaborationroommicroservice.entity.RoomType;
import exp.collaborationroommicroservice.integration.AiSummaryClient;
import exp.collaborationroommicroservice.integration.dto.AiSummaryClientResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomMatchingServiceImplTest {

    @Mock
    private RoomService roomService;

    @Mock
    private AiSummaryClient aiSummaryClient;

    @Spy
    private CollaborationMapper collaborationMapper;

    @InjectMocks
    private RoomMatchingServiceImpl roomMatchingService;

    @Test
    void recommendForCurrentUserPrioritizesMatchingVoiceRoomsForSpeakingWeakness() {
        AiSummaryClientResponse summary = new AiSummaryClientResponse();
        summary.setCurrentLevel("INTERMEDIATE");
        summary.setWeakestSkill("SPEAKING");

        CollaborationRoom bestRoom = room(1L, RoomLevel.INTERMEDIATE, RoomType.VOICE, true, RoomStatus.ACTIVE);
        CollaborationRoom secondRoom = room(2L, RoomLevel.INTERMEDIATE, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE);
        CollaborationRoom thirdRoom = room(3L, RoomLevel.BEGINNER, RoomType.VOICE, true, RoomStatus.ACTIVE);

        when(aiSummaryClient.getCurrentSummary()).thenReturn(summary);
        when(roomService.getPublicRooms()).thenReturn(List.of(secondRoom, thirdRoom, bestRoom));

        List<RoomMatchResponse> result = roomMatchingService.recommendForCurrentUser();

        assertEquals(3, result.size());
        assertEquals(bestRoom.getId(), result.get(0).getRoom().getId());
        assertTrue(result.get(0).getRecommendationReason().contains("voice rooms"));
        assertTrue(result.get(0).getPriorityScore() > result.get(1).getPriorityScore());
    }

    @Test
    void recommendForCurrentUserLimitsResultsToFive() {
        AiSummaryClientResponse summary = new AiSummaryClientResponse();
        summary.setCurrentLevel("BEGINNER");
        summary.setWeakestSkill("GRAMMAR");

        when(aiSummaryClient.getCurrentSummary()).thenReturn(summary);
        when(roomService.getPublicRooms()).thenReturn(List.of(
                room(1L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE),
                room(2L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE),
                room(3L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE),
                room(4L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE),
                room(5L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE),
                room(6L, RoomLevel.BEGINNER, RoomType.TEXT_CHAT, true, RoomStatus.ACTIVE)
        ));

        List<RoomMatchResponse> result = roomMatchingService.recommendForCurrentUser();

        assertEquals(5, result.size());
    }

    private CollaborationRoom room(Long id, RoomLevel level, RoomType type, boolean isPublic, RoomStatus status) {
        return CollaborationRoom.builder()
                .id(id)
                .title("Room " + id)
                .nativeLanguage("French")
                .targetLanguage("English")
                .level(level)
                .type(type)
                .maxParticipants(8)
                .isPublic(isPublic)
                .topic("Topic " + id)
                .status(status)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
