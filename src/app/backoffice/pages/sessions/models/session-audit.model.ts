export interface SessionAudit {
  id: number;
  sessionId: number;
  actionType: string;
  modifiedBy: string;
  modifiedAt: string;
  oldStatus: string | null;
  newStatus: string | null;
  details: string;
}