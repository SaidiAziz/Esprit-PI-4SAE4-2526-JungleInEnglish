export type SessionStatus = 'PLANNED' | 'CANCELED' | 'COMPLETED';
export type SessionMode = 'ONLINE' | 'ONSITE';
export type MeetingProvider = 'JITSI' | 'NONE';

export interface Session {
  id: number;
  tutorId: number;
  courseId: number;
  sessionDate: string;
  startTime: string;
  endTime: string;
  status: SessionStatus;
  timeSlotId?: number | null;
  createdAt?: string;
  updatedAt?: string;

  mode?: SessionMode;
  roomName?: string | null;
  meetingLink?: string | null;
  meetingProvider?: MeetingProvider;
}

export interface SessionPayload {
  tutorId: number;
  courseId: number;
  sessionDate: string;
  startTime: string;
  endTime: string;
  status?: SessionStatus;
  timeSlotId?: number | null;

  mode: SessionMode;
}