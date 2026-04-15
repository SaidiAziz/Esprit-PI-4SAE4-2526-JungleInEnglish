export interface SessionStats {
  totalSessions: number;
  plannedSessions: number;
  completedSessions: number;
  canceledSessions: number;
  completionRate: number;
}

export interface TutorSessionStats {
  tutorId: number;
  sessionCount: number;
}

export interface MonthlySessionStats {
  month: number;
  sessionCount: number;
}