import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionAudit } from '../models/session-audit.model';

@Injectable({
  providedIn: 'root'
})
export class SessionAuditService {

  private readonly baseUrl = 'http://localhost:8090/admin/api/admin/sessions/audit';

  constructor(private http: HttpClient) {}

  getAllAudits(): Observable<SessionAudit[]> {
    return this.http.get<SessionAudit[]>(this.baseUrl);
  }

  getAuditBySessionId(sessionId: number): Observable<SessionAudit[]> {
    return this.http.get<SessionAudit[]>(`${this.baseUrl}/${sessionId}`);
  }
}