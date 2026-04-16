export type SessionStatus = 'PLANNED' | 'CANCELED' | 'COMPLETED';

// ✅ Réponse backend (ce que tu reçois)
export interface Session {
  id: number;
  tutorId: number;
  courseId: number;
  sessionDate: string;   // "YYYY-MM-DD"
  startTime: string;     // "HH:mm:ss"
  endTime: string;       // "HH:mm:ss"
  status: SessionStatus;
  timeSlotId?: number | null;
  createdAt?: string;
  updatedAt?: string;
}

// ✅ Payload (ce que tu envoies en POST/PUT)
export interface SessionPayload {
  tutorId: number;
  courseId: number;
  sessionDate: string;   // "YYYY-MM-DD"
  startTime: string;     // "HH:mm:ss"
  endTime: string;       // "HH:mm:ss"
  status?: SessionStatus; // optionnel (car ton create force PLANNED)
  timeSlotId?: number | null;
}