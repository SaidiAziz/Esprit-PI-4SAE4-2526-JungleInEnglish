import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { Session } from '../models/session.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly baseUrl = `${environment.adminApiUrl}/sessions`;

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

  complete(id: number): Observable<Session> {
    return this.http.put<Session>(`${this.baseUrl}/${id}/complete`, {});
  }

  cancel(id: number): Observable<Session> {
    return this.http.put<Session>(`${this.baseUrl}/${id}/cancel`, {});
  }

  getByTutor(tutorId: number): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.baseUrl}/by-tutor?tutorId=${tutorId}`);
  }
}
