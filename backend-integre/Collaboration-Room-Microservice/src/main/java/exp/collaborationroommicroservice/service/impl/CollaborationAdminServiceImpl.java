package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.response.*;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.repository.*;
import exp.collaborationroommicroservice.service.CollaborationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationAdminServiceImpl implements CollaborationAdminService {

    private final CollaborationRoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final LanguageMessageRepository messageRepository;
    private final PeerCorrectionRepository correctionRepository;
    private final LinguisticChallengeRepository challengeRepository;
    private final ChallengeSubmissionRepository challengeSubmissionRepository;
    private final CollaborationMapper collaborationMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        List<AdminRoomOverviewResponse> roomOverviews = getRoomOverviews();
        long totalRooms = roomRepository.count();
        long activeRooms = roomOverviews.stream().filter(room -> room.getStatus() == RoomStatus.ACTIVE).count();

        return AdminDashboardResponse.builder()
                .totalRooms(totalRooms)
                .activeRooms(activeRooms)
                .totalParticipants(participantRepository.count())
                .totalMessages(messageRepository.count())
                .totalCorrections(correctionRepository.count())
                .totalChallenges(challengeRepository.count())
                .totalChallengeSubmissions(challengeSubmissionRepository.count())
                .topRooms(roomOverviews.stream()
                        .sorted(Comparator.comparingLong((AdminRoomOverviewResponse room) ->
                                room.getMessageCount() + room.getParticipantCount() + room.getChallengeCount()).reversed())
                        .limit(5)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRoomOverviewResponse> getRoomOverviews() {
        List<CollaborationRoom> rooms = roomRepository.findAll().stream()
                .sorted(Comparator.comparing(CollaborationRoom::getCreatedAt).reversed())
                .toList();
        List<RoomParticipant> participants = participantRepository.findAll();
        List<LanguageMessage> messages = messageRepository.findAll();
        List<PeerCorrection> corrections = correctionRepository.findAll();
        List<LinguisticChallenge> challenges = challengeRepository.findAll();

        return buildRoomOverviews(rooms, participants, messages, corrections, challenges);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRoomDetailResponse getRoomDetail(Long roomId) {
        CollaborationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        List<RoomParticipant> participants = participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId);
        List<LanguageMessage> messages = messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        Map<Long, LanguageMessage> messagesById = messages.stream()
                .collect(Collectors.toMap(LanguageMessage::getId, Function.identity()));
        List<PeerCorrection> corrections = correctionRepository.findAll().stream()
                .filter(correction -> {
                    LanguageMessage message = messagesById.get(correction.getMessageId());
                    return message != null && Objects.equals(message.getRoomId(), roomId);
                })
                .sorted(Comparator.comparing(PeerCorrection::getCorrectedAt).reversed())
                .toList();
        List<LinguisticChallenge> challenges = challengeRepository.findByRoomIdOrderByDeadlineDesc(roomId);

        AdminRoomOverviewResponse overview = buildRoomOverviews(
                List.of(room), participants, messages, corrections, challenges
        ).get(0);

        return AdminRoomDetailResponse.builder()
                .overview(overview)
                .participants(participants.stream().map(collaborationMapper::toResponse).toList())
                .recentMessages(messages.stream().sorted(Comparator.comparing(LanguageMessage::getSentAt).reversed()).limit(8).map(collaborationMapper::toResponse).toList())
                .recentCorrections(corrections.stream().limit(8).map(collaborationMapper::toResponse).toList())
                .recentChallenges(challenges.stream().limit(5).map(collaborationMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getRoomParticipants(Long roomId) {
        return participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId).stream()
                .map(collaborationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminRoomOverviewResponse updateRoomStatus(Long roomId, RoomStatus status) {
        CollaborationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));
        room.setStatus(status);
        roomRepository.save(room);
        return getRoomDetail(roomId).getOverview();
    }

    @Override
    @Transactional
    public void removeParticipant(Long roomId, Long userId) {
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found for room " + roomId + " and user " + userId));
        participantRepository.delete(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActivityItemResponse> getRecentActivity() {
        List<CollaborationRoom> rooms = roomRepository.findAll();
        Map<Long, Long> messageToRoomMap = messageRepository.findAll().stream()
                .collect(Collectors.toMap(LanguageMessage::getId, LanguageMessage::getRoomId));

        List<AdminActivityItemResponse> activity = new ArrayList<>();
        messageRepository.findAll().forEach(message -> activity.add(AdminActivityItemResponse.builder()
                .type("MESSAGE")
                .roomId(message.getRoomId())
                .entityId(message.getId())
                .actorUserId(message.getSenderId())
                .description("User " + message.getSenderId() + " sent a message in room " + message.getRoomId())
                .createdAt(message.getSentAt())
                .build()));
        correctionRepository.findAll().forEach(correction -> activity.add(AdminActivityItemResponse.builder()
                .type("CORRECTION")
                .roomId(messageToRoomMap.get(correction.getMessageId()))
                .entityId(correction.getId())
                .actorUserId(correction.getCorrectorId())
                .description("User " + correction.getCorrectorId() + " submitted a correction")
                .createdAt(correction.getCorrectedAt())
                .build()));
        challengeRepository.findAll().forEach(challenge -> activity.add(AdminActivityItemResponse.builder()
                .type("CHALLENGE")
                .roomId(challenge.getRoomId())
                .entityId(challenge.getId())
                .actorUserId(challenge.getCreatorId())
                .description("Challenge created in room " + challenge.getRoomId())
                .createdAt(challenge.getDeadline())
                .build()));
        challengeSubmissionRepository.findAll().forEach(submission -> {
            Long roomId = rooms.stream()
                    .flatMap(room -> challengeRepository.findByRoomIdOrderByDeadlineDesc(room.getId()).stream())
                    .filter(challenge -> Objects.equals(challenge.getId(), submission.getChallengeId()))
                    .map(LinguisticChallenge::getRoomId)
                    .findFirst()
                    .orElse(null);
            activity.add(AdminActivityItemResponse.builder()
                    .type("SUBMISSION")
                    .roomId(roomId)
                    .entityId(submission.getId())
                    .actorUserId(submission.getUserId())
                    .description("User " + submission.getUserId() + " submitted a challenge answer")
                    .createdAt(submission.getSubmittedAt())
                    .build());
        });

        return activity.stream()
                .sorted(Comparator.comparing(AdminActivityItemResponse::getCreatedAt).reversed())
                .limit(25)
                .toList();
    }

    private List<AdminRoomOverviewResponse> buildRoomOverviews(List<CollaborationRoom> rooms,
                                                               List<RoomParticipant> participants,
                                                               List<LanguageMessage> messages,
                                                               List<PeerCorrection> corrections,
                                                               List<LinguisticChallenge> challenges) {
        Map<Long, Long> participantCounts = participants.stream()
                .collect(Collectors.groupingBy(RoomParticipant::getRoomId, Collectors.counting()));
        Map<Long, Long> messageCounts = messages.stream()
                .collect(Collectors.groupingBy(LanguageMessage::getRoomId, Collectors.counting()));
        Map<Long, Long> challengeCounts = challenges.stream()
                .collect(Collectors.groupingBy(LinguisticChallenge::getRoomId, Collectors.counting()));
        Map<Long, Long> messageIdToRoomId = messages.stream()
                .collect(Collectors.toMap(LanguageMessage::getId, LanguageMessage::getRoomId));

        Map<Long, Long> correctionCounts = corrections.stream()
                .collect(Collectors.groupingBy(correction -> messageIdToRoomId.getOrDefault(correction.getMessageId(), -1L), Collectors.counting()));

        Map<Long, LocalDateTime> latestActivity = new HashMap<>();
        rooms.forEach(room -> latestActivity.put(room.getId(), room.getCreatedAt()));
        messages.forEach(message -> latestActivity.merge(message.getRoomId(), message.getSentAt(), this::latest));
        challenges.forEach(challenge -> latestActivity.merge(challenge.getRoomId(), challenge.getDeadline(), this::latest));
        corrections.forEach(correction -> {
            Long roomId = messageIdToRoomId.get(correction.getMessageId());
            if (roomId != null) {
                latestActivity.merge(roomId, correction.getCorrectedAt(), this::latest);
            }
        });

        return rooms.stream()
                .map(room -> AdminRoomOverviewResponse.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .topic(room.getTopic())
                        .type(room.getType())
                        .status(room.getStatus())
                        .createdBy(room.getCreatedBy())
                        .courseId(room.getCourseId())
                        .createdAt(room.getCreatedAt())
                        .latestActivityAt(latestActivity.get(room.getId()))
                        .participantCount(participantCounts.getOrDefault(room.getId(), 0L))
                        .messageCount(messageCounts.getOrDefault(room.getId(), 0L))
                        .correctionCount(correctionCounts.getOrDefault(room.getId(), 0L))
                        .challengeCount(challengeCounts.getOrDefault(room.getId(), 0L))
                        .build())
                .sorted(Comparator.comparing(AdminRoomOverviewResponse::getCreatedAt).reversed())
                .toList();
    }

    private LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }
}
