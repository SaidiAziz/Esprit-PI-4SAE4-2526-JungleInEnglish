package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.response.RoomAnalyticsResponse;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.repository.LanguageMessageRepository;
import exp.collaborationroommicroservice.repository.LinguisticChallengeRepository;
import exp.collaborationroommicroservice.repository.PeerCorrectionRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomAnalyticsServiceImplTest {

    @Mock
    private LanguageMessageRepository messageRepository;

    @Mock
    private PeerCorrectionRepository correctionRepository;

    @Mock
    private LinguisticChallengeRepository challengeRepository;

    @Mock
    private RoomParticipantRepository participantRepository;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomAnalyticsServiceImpl roomAnalyticsService;

    @Test
    void getAnalyticsAggregatesMessageCorrectionAndChallengeData() {
        Long roomId = 5L;
        LocalDateTime base = LocalDateTime.of(2026, 4, 29, 10, 0);

        LanguageMessage first = message(1L, roomId, "EN", base);
        LanguageMessage second = message(2L, roomId, "EN", base.plusMinutes(10));
        LanguageMessage third = message(3L, roomId, "FR", base.plusMinutes(30));

        PeerCorrection accepted = correction(10L, 1L, true);
        PeerCorrection rejected = correction(11L, 2L, false);

        RoomParticipant firstParticipant = participant(100L, 3, 1);
        RoomParticipant secondParticipant = participant(200L, 1, 4);

        LinguisticChallenge open = challenge(21L, ChallengeStatus.OPEN);
        LinguisticChallenge closed = challenge(22L, ChallengeStatus.CLOSED);

        when(roomService.getById(roomId)).thenReturn(CollaborationRoom.builder().id(roomId).build());
        when(messageRepository.findByRoomIdOrderBySentAtAsc(roomId)).thenReturn(List.of(first, second, third));
        when(correctionRepository.findByMessageIdOrderByCorrectedAtDesc(1L)).thenReturn(List.of(accepted));
        when(correctionRepository.findByMessageIdOrderByCorrectedAtDesc(2L)).thenReturn(List.of(rejected));
        when(correctionRepository.findByMessageIdOrderByCorrectedAtDesc(3L)).thenReturn(List.of());
        when(participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId)).thenReturn(List.of(firstParticipant, secondParticipant));
        when(challengeRepository.findByRoomIdOrderByDeadlineDesc(roomId)).thenReturn(List.of(open, closed));

        RoomAnalyticsResponse response = roomAnalyticsService.getAnalytics(roomId);

        assertEquals(roomId, response.getRoomId());
        assertEquals(3, response.getTotalMessages());
        assertEquals(15.0, response.getAvgResponseTimeMinutes());
        assertEquals(200L, response.getTopContributorUserId());
        assertEquals("EN", response.getMostActiveLanguage());
        assertEquals(50.0, response.getCorrectionsAcceptanceRate());
        assertEquals(50.0, response.getChallengeCompletionRate());
        assertEquals(10, response.getPeakHour());
    }

    private LanguageMessage message(Long id, Long roomId, String language, LocalDateTime sentAt) {
        return LanguageMessage.builder()
                .id(id)
                .roomId(roomId)
                .senderId(1L)
                .content("message " + id)
                .language(language)
                .sentAt(sentAt)
                .build();
    }

    private PeerCorrection correction(Long id, Long messageId, boolean accepted) {
        return PeerCorrection.builder()
                .id(id)
                .messageId(messageId)
                .correctorId(2L)
                .accepted(accepted)
                .correctedAt(LocalDateTime.now())
                .build();
    }

    private RoomParticipant participant(Long userId, int messagesCount, int correctionsGiven) {
        return RoomParticipant.builder()
                .userId(userId)
                .messagesCount(messagesCount)
                .correctionsGiven(correctionsGiven)
                .build();
    }

    private LinguisticChallenge challenge(Long id, ChallengeStatus status) {
        return LinguisticChallenge.builder()
                .id(id)
                .status(status)
                .deadline(LocalDateTime.now())
                .build();
    }
}
