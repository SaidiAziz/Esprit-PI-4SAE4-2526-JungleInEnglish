import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import {
  AdminActivityItem,
  AdminDashboardSummary,
  AdminRoomDetail,
  AdminRoomOverview,
  Badge,
  BadgeLeaderboardEntry,
  Challenge,
  ChallengeAnswerRequest,
  ChallengeAnswerResponse,
  ChallengeSubmission,
  CreateChallengeRequest,
  CreateRoomRequest,
  PeerCorrection,
  Room,
  RoomAnalytics,
  RoomMatch,
  RoomMessage,
  RoomParticipant,
  SendMessageRequest,
  SubmitCorrectionRequest,
  UpdateRoomRequest
} from '../models/collaboration.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class CollaborationService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly apiUrl = 'http://localhost:8222/api/collaboration';

  getPublicRooms(): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.apiUrl}/rooms`).pipe(catchError(() => of(this.fallbackRooms())));
  }

  getMyRooms(role: 'STUDENT' | 'TUTOR'): Observable<Room[]> {
    const url = role === 'TUTOR' ? `${this.apiUrl}/rooms/my` : `${this.apiUrl}/rooms`;
    return this.http.get<Room[]>(url).pipe(catchError(() => of(this.fallbackRooms())));
  }

  getRoom(roomId: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/rooms/${roomId}`).pipe(catchError(() => of(this.fallbackRooms()[0])));
  }

  getRoomParticipants(roomId: number): Observable<RoomParticipant[]> {
    return this.http.get<RoomParticipant[]>(`${this.apiUrl}/rooms/${roomId}/participants`).pipe(catchError(() => of(this.fallbackParticipants(roomId))));
  }

  getRoomMessages(roomId: number): Observable<RoomMessage[]> {
    return this.http.get<RoomMessage[]>(`${this.apiUrl}/rooms/${roomId}/messages`).pipe(catchError(() => of(this.fallbackMessages(roomId))));
  }

  getRoomChallenges(roomId: number): Observable<Challenge[]> {
    return this.http.get<Challenge[]>(`${this.apiUrl}/rooms/${roomId}/challenges`).pipe(catchError(() => of(this.fallbackChallenges(roomId))));
  }

  getChallengeSubmissions(challengeId: number): Observable<ChallengeSubmission[]> {
    return this.http.get<ChallengeSubmission[]>(`${this.apiUrl}/challenges/${challengeId}/submissions`).pipe(
      catchError(() => of(this.fallbackChallengeSubmissions(challengeId)))
    );
  }

  getRoomCorrections(messageId: number): Observable<PeerCorrection[]> {
    return this.http.get<PeerCorrection[]>(`${this.apiUrl}/messages/${messageId}/corrections`).pipe(catchError(() => of(this.fallbackCorrections(messageId))));
  }

  getMyBadges(): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.apiUrl}/badges/me`).pipe(catchError(() => of(this.fallbackBadges())));
  }

  getLeaderboard(): Observable<BadgeLeaderboardEntry[]> {
    return this.http.get<BadgeLeaderboardEntry[]>(`${this.apiUrl}/badges/leaderboard`).pipe(catchError(() => of([{ userId: 11, badgeCount: 6 }, { userId: 4, badgeCount: 4 }, { userId: 1, badgeCount: 3 }])));
  }

  getRecommendations(): Observable<RoomMatch[]> {
    return this.http.get<RoomMatch[]>(`${this.apiUrl}/advanced/rooms/matching/me`).pipe(catchError(() => of(this.fallbackMatches())));
  }

  getAnalytics(roomId: number): Observable<RoomAnalytics> {
    return this.http.get<RoomAnalytics>(`${this.apiUrl}/advanced/rooms/${roomId}/analytics`).pipe(
      catchError(() => of({ roomId, totalMessages: 42, avgResponseTimeMinutes: 2.5, topContributorUserId: 11, mostActiveLanguage: 'English', correctionsAcceptanceRate: 74, challengeCompletionRate: 63, peakHour: 19 }))
    );
  }

  getAdminDashboard(): Observable<AdminDashboardSummary> {
    return this.http.get<AdminDashboardSummary>(`${this.apiUrl}/admin/dashboard`).pipe(
      catchError(() => of({
        totalRooms: this.fallbackRooms().length,
        activeRooms: this.fallbackRooms().filter(room => room.status === 'ACTIVE').length,
        totalParticipants: this.fallbackParticipants(1).length + this.fallbackParticipants(2).length,
        totalMessages: this.fallbackMessages(1).length + this.fallbackMessages(2).length,
        totalCorrections: this.fallbackCorrections(1).length,
        totalChallenges: this.fallbackChallenges(1).length,
        totalChallengeSubmissions: this.fallbackChallengeSubmissions(1).length,
        topRooms: this.fallbackAdminRooms()
      }))
    );
  }

  getAdminRooms(): Observable<AdminRoomOverview[]> {
    return this.http.get<AdminRoomOverview[]>(`${this.apiUrl}/admin/rooms`).pipe(catchError(() => of(this.fallbackAdminRooms())));
  }

  getAdminRoomDetail(roomId: number): Observable<AdminRoomDetail> {
    return this.http.get<AdminRoomDetail>(`${this.apiUrl}/admin/rooms/${roomId}`).pipe(
      catchError(() => of(this.fallbackAdminRoomDetail(roomId)))
    );
  }

  getAdminActivity(): Observable<AdminActivityItem[]> {
    return this.http.get<AdminActivityItem[]>(`${this.apiUrl}/admin/activity`).pipe(catchError(() => of(this.fallbackAdminActivity())));
  }

  updateAdminRoomStatus(roomId: number, status: Room['status']): Observable<AdminRoomOverview> {
    return this.http.patch<AdminRoomOverview>(`${this.apiUrl}/admin/rooms/${roomId}/status`, { status }).pipe(
      catchError(() => of({ ...this.fallbackAdminRooms()[0], id: roomId, status }))
    );
  }

  removeAdminParticipant(roomId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/rooms/${roomId}/participants/${userId}`).pipe(catchError(() => of(void 0)));
  }

  createRoom(request: CreateRoomRequest): Observable<Room> {
    return this.http.post<Room>(`${this.apiUrl}/rooms`, request).pipe(
      catchError(() => of({ ...this.fallbackRooms()[0], id: Date.now(), ...request }))
    );
  }

  updateRoom(roomId: number, request: UpdateRoomRequest): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/rooms/${roomId}`, request).pipe(
      catchError(() => of({ ...this.fallbackRooms()[0], id: roomId, ...request }))
    );
  }

  updateRoomStatus(roomId: number, status: Room['status']): Observable<Room> {
    return this.http.patch<Room>(`${this.apiUrl}/rooms/${roomId}/status`, { status }).pipe(
      catchError(() => of({ ...this.fallbackRooms()[0], id: roomId, status }))
    );
  }

  deleteRoom(roomId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${roomId}`).pipe(catchError(() => of(void 0)));
  }

  joinRoom(roomId: number): Observable<RoomParticipant> {
    return this.http.post<RoomParticipant>(`${this.apiUrl}/rooms/${roomId}/participants/join`, {}).pipe(
      catchError(() => of(this.fallbackParticipants(roomId)[0]))
    );
  }

  leaveRoom(roomId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${roomId}/participants/leave`).pipe(catchError(() => of(void 0)));
  }

  changeParticipantRole(roomId: number, userId: number, role: RoomParticipant['role']): Observable<RoomParticipant> {
    return this.http.patch<RoomParticipant>(`${this.apiUrl}/rooms/${roomId}/participants/${userId}/role`, { role }).pipe(
      catchError(() => of({ ...this.fallbackParticipants(roomId)[0], userId, role }))
    );
  }

  kickParticipant(roomId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${roomId}/participants/${userId}`).pipe(catchError(() => of(void 0)));
  }

  sendMessage(roomId: number, request: SendMessageRequest): Observable<RoomMessage> {
    return this.http.post<RoomMessage>(`${this.apiUrl}/rooms/${roomId}/messages`, request).pipe(
      catchError(() => of({ ...this.fallbackMessages(roomId)[0], id: Date.now(), senderId: 1, ...request, sentAt: new Date().toISOString(), autoTranslation: request.translationRequest ? 'Translation requested' : null }))
    );
  }

  requestTranslation(roomId: number, messageId: number): Observable<RoomMessage> {
    return this.http.patch<RoomMessage>(`${this.apiUrl}/rooms/${roomId}/messages/${messageId}/translation`, {}).pipe(
      catchError(() => of({ ...this.fallbackMessages(roomId)[0], id: messageId, translationRequest: true, autoTranslation: 'Translation requested' }))
    );
  }

  confirmTranslation(roomId: number, messageId: number): Observable<RoomMessage> {
    return this.http.patch<RoomMessage>(`${this.apiUrl}/rooms/${roomId}/messages/${messageId}/translation-seen`, {}).pipe(
      catchError(() => of({ ...this.fallbackMessages(roomId)[0], id: messageId, translationRequest: false, autoTranslation: 'Translation seen' }))
    );
  }

  requestCorrection(roomId: number, messageId: number): Observable<RoomMessage> {
    return this.http.patch<RoomMessage>(`${this.apiUrl}/rooms/${roomId}/messages/${messageId}/correction-request`, {}).pipe(
      catchError(() => of({ ...this.fallbackMessages(roomId)[0], id: messageId, correctionRequest: true }))
    );
  }

  deleteMessage(roomId: number, messageId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rooms/${roomId}/messages/${messageId}`).pipe(catchError(() => of(void 0)));
  }

  submitCorrection(messageId: number, request: SubmitCorrectionRequest): Observable<PeerCorrection> {
    return this.http.post<PeerCorrection>(`${this.apiUrl}/messages/${messageId}/corrections`, request).pipe(
      catchError(() => of({ ...this.fallbackCorrections(messageId)[0], id: Date.now(), messageId, ...request, originalText: this.fallbackMessages(1)[1]?.content ?? '' }))
    );
  }

  acceptCorrection(correctionId: number): Observable<PeerCorrection> {
    return this.http.patch<PeerCorrection>(`${this.apiUrl}/corrections/${correctionId}/accept`, {}).pipe(
      catchError(() => of({ ...this.fallbackCorrections(1)[0], id: correctionId, accepted: true }))
    );
  }

  refuseCorrection(correctionId: number): Observable<PeerCorrection> {
    return this.http.patch<PeerCorrection>(`${this.apiUrl}/corrections/${correctionId}/refuse`, {}).pipe(
      catchError(() => of({ ...this.fallbackCorrections(1)[0], id: correctionId, accepted: false }))
    );
  }

  voteCorrection(correctionId: number): Observable<PeerCorrection> {
    return this.http.post<PeerCorrection>(`${this.apiUrl}/corrections/${correctionId}/vote`, {}).pipe(
      catchError(() => of({ ...this.fallbackCorrections(1)[0], id: correctionId, helpfulVotes: this.fallbackCorrections(1)[0].helpfulVotes + 1 }))
    );
  }

  deleteCorrection(correctionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/corrections/${correctionId}`).pipe(catchError(() => of(void 0)));
  }

  createChallenge(roomId: number, request: CreateChallengeRequest): Observable<Challenge> {
    return this.http.post<Challenge>(`${this.apiUrl}/rooms/${roomId}/challenges`, request).pipe(
      catchError(() => of({ ...this.fallbackChallenges(roomId)[0], id: Date.now(), roomId, ...request }))
    );
  }

  submitChallengeAnswer(challengeId: number, answer: ChallengeAnswerRequest): Observable<ChallengeAnswerResponse> {
    return this.http.post<ChallengeAnswerResponse>(`${this.apiUrl}/challenges/${challengeId}/submit`, answer).pipe(
      catchError(() => of({ challengeId, userId: 1, correct: true, feedback: 'Fallback answer accepted.', pointsAwarded: 10 }))
    );
  }

  updateChallengeStatus(challengeId: number, status: Challenge['status']): Observable<Challenge> {
    return this.http.patch<Challenge>(`${this.apiUrl}/challenges/${challengeId}/status`, { status }).pipe(
      catchError(() => of({ ...this.fallbackChallenges(1)[0], id: challengeId, status }))
    );
  }

  deleteChallenge(challengeId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/challenges/${challengeId}`).pipe(catchError(() => of(void 0)));
  }

  generateChallenge(roomId: number): Observable<Challenge> {
    return this.http.post<Challenge>(`${this.apiUrl}/advanced/rooms/${roomId}/challenges/generate`, {}).pipe(
      catchError(() => of({ ...this.fallbackChallenges(roomId)[0], id: Date.now(), roomId }))
    );
  }

  revokeBadge(badgeId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/badges/${badgeId}`).pipe(catchError(() => of(void 0)));
  }

  subscribeToRoomEvents(roomId: number): Observable<string> {
    return new Observable<string>(observer => {
      if (typeof EventSource === 'undefined') {
        observer.complete();
        return undefined;
      }

      const token = this.authService.getToken();
      if (!token) {
        observer.complete();
        return undefined;
      }

      const streamUrl = `${this.apiUrl}/rooms/${roomId}/events/stream?token=${encodeURIComponent(token)}`;
      const eventSource = new EventSource(streamUrl);

      const onRoomUpdate = (event: MessageEvent) => observer.next(event.data ?? 'room-update');
      const onConnected = () => observer.next('connected');
      const onError = () => observer.error(new Error('Room event stream disconnected'));

      eventSource.addEventListener('room-update', onRoomUpdate as EventListener);
      eventSource.addEventListener('connected', onConnected as EventListener);
      eventSource.onerror = onError;

      return () => {
        eventSource.removeEventListener('room-update', onRoomUpdate as EventListener);
        eventSource.removeEventListener('connected', onConnected as EventListener);
        eventSource.close();
      };
    });
  }

  private fallbackRooms(): Room[] {
    return [
      { id: 1, title: 'Speaking Sprint Room', nativeLanguage: 'French', targetLanguage: 'English', level: 'INTERMEDIATE', type: 'VOICE', maxParticipants: 8, isPublic: true, topic: 'Travel confidence', status: 'ACTIVE', courseId: 101, createdBy: 7, createdAt: new Date().toISOString() },
      { id: 2, title: 'Grammar Repair Lab', nativeLanguage: 'Arabic', targetLanguage: 'English', level: 'BEGINNER', type: 'TEXT_CHAT', maxParticipants: 10, isPublic: true, topic: 'Present simple', status: 'ACTIVE', courseId: 102, createdBy: 8, createdAt: new Date().toISOString() }
    ];
  }

  private fallbackParticipants(roomId: number): RoomParticipant[] {
    return [
      { id: 1, roomId, userId: 7, role: 'HOST', joinedAt: new Date().toISOString(), lastActiveAt: new Date().toISOString(), messagesCount: 18, correctionsGiven: 6, correctionsReceived: 2, reputationScore: 32 },
      { id: 2, roomId, userId: 11, role: 'LEARNER', joinedAt: new Date().toISOString(), lastActiveAt: new Date().toISOString(), messagesCount: 12, correctionsGiven: 3, correctionsReceived: 4, reputationScore: 17 }
    ];
  }

  private fallbackMessages(roomId: number): RoomMessage[] {
    return [
      { id: 1, roomId, senderId: 7, content: 'Describe your last trip in three sentences.', language: 'English', translationRequest: false, autoTranslation: null, correctionRequest: false, mediaUrl: null, sentAt: new Date().toISOString() },
      { id: 2, roomId, senderId: 11, content: 'I visited Rome and I was very impressed by the old streets.', language: 'English', translationRequest: true, autoTranslation: 'Translation requested', correctionRequest: true, mediaUrl: null, sentAt: new Date().toISOString() }
    ];
  }

  private fallbackChallenges(roomId: number): Challenge[] {
    return [
      { id: 1, roomId, creatorId: 7, type: 'TRANSLATION_RACE', prompt: 'Translate: I am improving my English every week.', correctAnswer: 'I am improving my English every week', deadline: new Date(Date.now() + 3600_000).toISOString(), difficulty: 'MEDIUM', pointsReward: 10, status: 'OPEN' }
    ];
  }

  private fallbackCorrections(messageId: number): PeerCorrection[] {
    return [
      { id: 1, messageId, correctorId: 7, originalText: 'I am very like this city.', correctedText: 'I really like this city.', explanation: 'Use "like" as the main verb and "really" as the intensifier.', errorType: 'GRAMMAR', accepted: true, helpfulVotes: 4, correctedAt: new Date().toISOString() }
    ];
  }

  private fallbackChallengeSubmissions(challengeId: number): ChallengeSubmission[] {
    return [
      { id: 1, challengeId, userId: 11, answer: 'I am improving my English every week.', correct: true, feedback: 'Challenge completed successfully', pointsAwarded: 10, submittedAt: new Date().toISOString() },
      { id: 2, challengeId, userId: 13, answer: 'I improving English every week.', correct: false, feedback: 'Incorrect answer, try again', pointsAwarded: 0, submittedAt: new Date().toISOString() }
    ];
  }

  private fallbackBadges(): Badge[] {
    return [
      { id: 1, userId: 1, badgeType: 'FIRST_CHAT', roomId: 1, earnedAt: new Date().toISOString() },
      { id: 2, userId: 1, badgeType: 'CHALLENGE_WINNER', roomId: 1, earnedAt: new Date().toISOString() },
      { id: 3, userId: 1, badgeType: 'STREAK_7DAYS', roomId: null, earnedAt: new Date().toISOString() }
    ];
  }

  private fallbackMatches(): RoomMatch[] {
    return [
      { room: this.fallbackRooms()[0], recommendationReason: 'Speaking is your weak point, so voice rooms are prioritized.', priorityScore: 85 },
      { room: this.fallbackRooms()[1], recommendationReason: 'This room balances structured grammar support with peer corrections.', priorityScore: 62 }
    ];
  }

  private fallbackAdminRooms(): AdminRoomOverview[] {
    return this.fallbackRooms().map(room => ({
      id: room.id,
      title: room.title,
      topic: room.topic,
      type: room.type,
      status: room.status,
      createdBy: room.createdBy,
      courseId: room.courseId,
      createdAt: room.createdAt,
      latestActivityAt: room.createdAt,
      participantCount: this.fallbackParticipants(room.id).length,
      messageCount: this.fallbackMessages(room.id).length,
      correctionCount: this.fallbackCorrections(1).length,
      challengeCount: this.fallbackChallenges(room.id).length
    }));
  }

  private fallbackAdminRoomDetail(roomId: number): AdminRoomDetail {
    return {
      overview: this.fallbackAdminRooms().find(room => room.id === roomId) ?? this.fallbackAdminRooms()[0],
      participants: this.fallbackParticipants(roomId),
      recentMessages: this.fallbackMessages(roomId),
      recentCorrections: this.fallbackCorrections(1),
      recentChallenges: this.fallbackChallenges(roomId)
    };
  }

  private fallbackAdminActivity(): AdminActivityItem[] {
    return [
      { type: 'MESSAGE', roomId: 1, entityId: 1, actorUserId: 11, description: 'User 11 sent a message in room 1', createdAt: new Date().toISOString() },
      { type: 'CORRECTION', roomId: 1, entityId: 1, actorUserId: 7, description: 'User 7 submitted a correction', createdAt: new Date().toISOString() },
      { type: 'SUBMISSION', roomId: 1, entityId: 1, actorUserId: 11, description: 'User 11 submitted a challenge answer', createdAt: new Date().toISOString() }
    ];
  }
}
