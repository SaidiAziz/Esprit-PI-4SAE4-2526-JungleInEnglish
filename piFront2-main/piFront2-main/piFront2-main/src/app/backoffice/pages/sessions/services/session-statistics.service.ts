import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  SessionStats,
  TutorSessionStats,
  MonthlySessionStats
} from '../models/session-statistics.model';

@Injectable({
  providedIn: 'root'
})
export class SessionStatisticsService {

  private readonly baseUrl = 'http://localhost:8222/admin/api/admin/sessions/statistics';

  constructor(private http: HttpClient) {}

  getGlobalStatistics(): Observable<SessionStats> {
    return this.http.get<SessionStats>(`${this.baseUrl}/global`);
  }

  getSessionsByTutor(): Observable<TutorSessionStats[]> {
    return this.http.get<TutorSessionStats[]>(`${this.baseUrl}/by-tutor`);
  }

  getSessionsByMonth(): Observable<MonthlySessionStats[]> {
    return this.http.get<MonthlySessionStats[]>(`${this.baseUrl}/by-month`);
  }
}