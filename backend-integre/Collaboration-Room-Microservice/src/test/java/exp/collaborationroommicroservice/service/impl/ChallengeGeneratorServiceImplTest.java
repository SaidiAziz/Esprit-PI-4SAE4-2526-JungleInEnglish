package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.CreateChallengeRequest;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.service.ChallengeService;
import exp.collaborationroommicroservice.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeGeneratorServiceImplTest {

    @Mock
    private RoomService roomService;

    @Mock
    private ChallengeService challengeService;

    @InjectMocks
    private ChallengeGeneratorServiceImpl challengeGeneratorService;

    @Test
    void generateForBeginnerRoomBuildsEasyFillBlankChallenge() {
        CollaborationRoom room = CollaborationRoom.builder()
                .id(7L)
                .level(RoomLevel.BEGINNER)
                .build();

        LinguisticChallenge expected = LinguisticChallenge.builder().id(1L).build();
        ArgumentCaptor<CreateChallengeRequest> captor = ArgumentCaptor.forClass(CreateChallengeRequest.class);

        when(roomService.getById(7L)).thenReturn(room);
        when(challengeService.create(eq(7L), eq(9L), captor.capture())).thenReturn(expected);

        LinguisticChallenge result = challengeGeneratorService.generateForRoom(7L, 9L);

        CreateChallengeRequest request = captor.getValue();
        assertEquals(expected, result);
        assertEquals(ChallengeType.FILL_BLANK, request.getType());
        assertEquals(ChallengeDifficulty.EASY, request.getDifficulty());
        assertEquals(10, request.getPointsReward());
        assertNotNull(request.getCorrectAnswer());
        assertTrue(request.getPrompt().contains("Fill in the blank"));
        assertTrue(request.getDeadline().isAfter(LocalDateTime.now().plusMinutes(100)));
    }

    @Test
    void generateForAdvancedRoomBuildsStoryBuilderChallenge() {
        CollaborationRoom room = CollaborationRoom.builder()
                .id(8L)
                .level(RoomLevel.ADVANCED)
                .build();

        ArgumentCaptor<CreateChallengeRequest> captor = ArgumentCaptor.forClass(CreateChallengeRequest.class);
        when(roomService.getById(8L)).thenReturn(room);
        when(challengeService.create(eq(8L), eq(15L), captor.capture()))
                .thenReturn(LinguisticChallenge.builder().id(2L).build());

        challengeGeneratorService.generateForRoom(8L, 15L);

        CreateChallengeRequest request = captor.getValue();
        assertEquals(ChallengeType.STORY_BUILDER, request.getType());
        assertEquals(ChallengeDifficulty.HARD, request.getDifficulty());
        assertEquals("", request.getCorrectAnswer());
        assertTrue(request.getPrompt().contains("Story Builder"));
    }
}
