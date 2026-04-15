import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Session } from '../models/session.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  // ✅ adapte host/port (gateway ou service)
  private baseUrl = 'http://localhost:8090/admin/api/admin/sessions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Session[]> {
    return this.http.get<Session[]>(this.baseUrl);
  }

  getById(id: number): Observable<Session> {
    return this.http.get<Session>(`${this.baseUrl}/${id}`);
  }

  create(payload: Session): Observable<Session> {
    return this.http.post<Session>(this.baseUrl, payload);
  }

  update(id: number, payload: Session): Observable<Session> {
    return this.http.put<Session>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // ✅ endpoints spécifiques
  complete(id: number): Observable<Session> {
    return this.http.put<Session>(`${this.baseUrl}/${id}/complete`, {});
  }

  cancel(id: number): Observable<Session> {
    return this.http.put<Session>(`${this.baseUrl}/${id}/cancel`, {});
  }

  // ⚠️ adapte si ton endpoint attend un param (?tutorId=) ou un body
  getByTutor(tutorId: number): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.baseUrl}/by-tutor?tutorId=${tutorId}`);
  }
  private extractBackendMessage(err: any): string {
  return (
    err?.error?.details ||
    err?.error?.message ||
    err?.error?.error ||
    (typeof err?.error === 'string' ? err.error : '') ||
    `HTTP ${err?.status ?? ''}`
  );
}
}