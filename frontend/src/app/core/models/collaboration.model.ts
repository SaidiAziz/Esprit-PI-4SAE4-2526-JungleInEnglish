export interface Room {
  id: number;
  title: string;
  nativeLanguage: string;
  targetLanguage: string;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  type: 'TEXT_CHAT' | 'VOICE' | 'MIXED';
  maxParticipants: number;
  isPublic: boolean;
  topic: string;
  status: 'ACTIVE' | 'FULL' | 'CLOSED';
  courseId: number | null;
  createdBy: number;
  createdAt: string;
}

export interface RoomParticipant {
  id: number;
  roomId: number;
  userId: number;
  role: 'HOST' | 'NATIVE_SPEAKER' | 'LEARNER' | 'OBSERVER';
  joinedAt: string;
  lastActiveAt: string;
  messagesCount: number;
  correctionsGiven: number;
  correctionsReceived: number;
  reputationScore: number;
}

export interface RoomMessage {
  id: number;
  roomId: number;
  senderId: number;
  content: string;
  language: string;
  translationRequest: boolean;
  autoTranslation: string | null;
  correctionRequest: boolean;
  mediaUrl: string | null;
  sentAt: string;
}

export interface PeerCorrection {
  id: number;
  messageId: number;
  correctorId: number;
  originalText: string;
  correctedText: string;
  explanation: string;
  errorType: 'GRAMMAR' | 'VOCABULARY' | 'PRONUNCIATION' | 'SPELLING';
  accepted: boolean;
  helpfulVotes: number;
  correctedAt: string;
}

export interface Challenge {
  id: number;
  roomId: number;
  creatorId: number;
  type: 'TRANSLATION_RACE' | 'WORD_CHAIN' | 'DESCRIBE_IMAGE' | 'FILL_BLANK' | 'STORY_BUILDER';
  prompt: string;
  correctAnswer: string;
  deadline: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward: number;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
}

export interface Badge {
  id: number;
  userId: number;
  badgeType: 'FIRST_CHAT' | 'HELPFUL_CORRECTOR' | 'POLYGLOT' | 'CHALLENGE_WINNER' | 'ROOM_CREATOR' | 'STREAK_7DAYS';
  roomId: number | null;
  earnedAt: string;
}

export interface BadgeLeaderboardEntry {
  userId: number;
  badgeCount: number;
}

export interface RoomMatch {
  room: Room;
  recommendationReason: string;
  priorityScore: number;
}

export interface RoomAnalytics {
  roomId: number;
  totalMessages: number;
  avgResponseTimeMinutes: number;
  topContributorUserId: number | null;
  mostActiveLanguage: string | null;
  correctionsAcceptanceRate: number;
  challengeCompletionRate: number;
  peakHour: number | null;
}

export interface AdminRoomOverview {
  id: number;
  title: string;
  topic: string | null;
  type: Room['type'];
  status: Room['status'];
  createdBy: number;
  courseId: number | null;
  createdAt: string;
  latestActivityAt: string | null;
  participantCount: number;
  messageCount: number;
  correctionCount: number;
  challengeCount: number;
}

export interface AdminDashboardSummary {
  totalRooms: number;
  activeRooms: number;
  totalParticipants: number;
  totalMessages: number;
  totalCorrections: number;
  totalChallenges: number;
  totalChallengeSubmissions: number;
  topRooms: AdminRoomOverview[];
}

export interface AdminActivityItem {
  type: string;
  roomId: number | null;
  entityId: number;
  actorUserId: number | null;
  description: string;
  createdAt: string;
}

export interface AdminRoomDetail {
  overview: AdminRoomOverview;
  participants: RoomParticipant[];
  recentMessages: RoomMessage[];
  recentCorrections: PeerCorrection[];
  recentChallenges: Challenge[];
}

export interface CreateRoomRequest {
  title: string;
  nativeLanguage: string;
  targetLanguage: string;
  level: Room['level'];
  type: Room['type'];
  maxParticipants: number;
  isPublic: boolean;
  topic: string;
  courseId: number | null;
}

export interface UpdateRoomRequest extends CreateRoomRequest {}

export interface SendMessageRequest {
  content: string;
  language: string;
  translationRequest: boolean;
  correctionRequest: boolean;
  mediaUrl: string | null;
}

export interface SubmitCorrectionRequest {
  correctedText: string;
  explanation: string;
  errorType: PeerCorrection['errorType'];
}

export interface CreateChallengeRequest {
  type: Challenge['type'];
  prompt: string;
  correctAnswer: string;
  deadline: string;
  difficulty: Challenge['difficulty'];
  pointsReward: number;
}

export interface ChallengeAnswerRequest {
  answer: string;
}

export interface ChallengeAnswerResponse {
  challengeId: number;
  userId: number;
  correct: boolean;
  feedback: string;
  pointsAwarded: number;
}

export interface ChallengeSubmission {
  id: number;
  challengeId: number;
  userId: number;
  answer: string;
  correct: boolean;
  feedback: string;
  pointsAwarded: number;
  submittedAt: string;
}
