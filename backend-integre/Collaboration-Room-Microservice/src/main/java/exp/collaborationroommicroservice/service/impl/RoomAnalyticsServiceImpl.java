package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.response.RoomAnalyticsResponse;
import exp.collaborationroommicroservice.entity.LanguageMessage;
import exp.collaborationroommicroservice.entity.PeerCorrection;
import exp.collaborationroommicroservice.entity.RoomParticipant;
import exp.collaborationroommicroservice.repository.LanguageMessageRepository;
import exp.collaborationroommicroservice.repository.LinguisticChallengeRepository;
import exp.collaborationroommicroservice.repository.PeerCorrectionRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.RoomAnalyticsService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomAnalyticsServiceImpl implements RoomAnalyticsService {

    private final LanguageMessageRepository messageRepository;
    private final PeerCorrectionRepository correctionRepository;
    private final LinguisticChallengeRepository challengeRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomService roomService;

    @Override
    public RoomAnalyticsResponse getAnalytics(Long roomId) {
        roomService.getById(roomId);
        List<LanguageMessage> messages = messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        List<PeerCorrection> corrections = messages.stream()
                .flatMap(message -> correctionRepository.findByMessageIdOrderByCorrectedAtDesc(message.getId()).stream())
                .toList();
        List<RoomParticipant> participants = participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId);
        long totalChallenges = challengeRepository.findByRoomIdOrderByDeadlineDesc(roomId).size();
        long closedChallenges = challengeRepository.findByRoomIdOrderByDeadlineDesc(roomId).stream()
                .filter(challenge -> challenge.getStatus().name().equals("CLOSED"))
                .count();

        double avgResponseTime = 0;
        for (int i = 1; i < messages.size(); i++) {
            avgResponseTime += Duration.between(messages.get(i - 1).getSentAt(), messages.get(i).getSentAt()).toMinutes();
        }
        if (messages.size() > 1) {
            avgResponseTime = avgResponseTime / (messages.size() - 1);
        }

        Map<String, Long> langCounts = messages.stream()
                .collect(Collectors.groupingBy(LanguageMessage::getLanguage, Collectors.counting()));
        String mostActiveLanguage = langCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Integer peakHour = messages.stream()
                .collect(Collectors.groupingBy(m -> m.getSentAt().getHour(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Long topContributor = participants.stream()
                .max(Comparator.comparingInt(p -> p.getMessagesCount() + p.getCorrectionsGiven()))
                .map(RoomParticipant::getUserId)
                .orElse(null);

        double correctionAcceptanceRate = corrections.isEmpty() ? 0 :
                (corrections.stream().filter(PeerCorrection::isAccepted).count() * 100.0) / corrections.size();
        double challengeCompletionRate = totalChallenges == 0 ? 0 : (closedChallenges * 100.0) / totalChallenges;

        return RoomAnalyticsResponse.builder()
                .roomId(roomId)
                .totalMessages(messages.size())
                .avgResponseTimeMinutes(Math.round(avgResponseTime * 100.0) / 100.0)
                .topContributorUserId(topContributor)
                .mostActiveLanguage(mostActiveLanguage)
                .correctionsAcceptanceRate(Math.round(correctionAcceptanceRate * 100.0) / 100.0)
                .challengeCompletionRate(Math.round(challengeCompletionRate * 100.0) / 100.0)
                .peakHour(peakHour)
                .build();
    }
}
