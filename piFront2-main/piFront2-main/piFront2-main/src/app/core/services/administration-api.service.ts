import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type OwnerType = 'PLATFORM' | 'TUTOR';

export interface TimeSlot {
  id: number;                 // ✅ backend retourne toujours id
  ownerType: OwnerType;
  ownerId?: number | null;
  dayOfWeek: number;          // 1..7
  startTime: string;          // "HH:mm" ou "HH:mm:ss"
  endTime: string;            // "HH:mm" ou "HH:mm:ss"
  validFrom?: string | null;  // "YYYY-MM-DD"
  validTo?: string | null;    // "YYYY-MM-DD"
  active?: boolean;
}

export type TimeSlotCreate = Omit<TimeSlot, 'id'>;

@Injectable({ providedIn: 'root' })
export class AdministrationApiService {
  private baseUrl = 'http://localhost:8222/admin/api/admin';

  constructor(private http: HttpClient) {}

  // TIMESLOTS
  addTimeslot(payload: TimeSlotCreate): Observable<TimeSlot> {
    return this.http.post<TimeSlot>(`${this.baseUrl}/timeslots`, payload);
  }

  getTimeslots(): Observable<TimeSlot[]> {
    return this.http.get<TimeSlot[]>(`${this.baseUrl}/timeslots`);
  }

  getTimeslotById(id: number): Observable<TimeSlot> {
    return this.http.get<TimeSlot>(`${this.baseUrl}/timeslots/${id}`);
  }

  updateTimeslot(id: number, payload: Partial<TimeSlotCreate>): Observable<TimeSlot> {
    return this.http.put<TimeSlot>(`${this.baseUrl}/timeslots/${id}`, payload);
  }

  deleteTimeslot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/timeslots/${id}`);
  }

  // SESSIONS (laisse comme tu avais)
  getSessions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/sessions`);
  }
  completeSession(id: number) { return this.http.put(`${this.baseUrl}/sessions/${id}/complete`, {}); }
  cancelSession(id: number) { return this.http.put(`${this.baseUrl}/sessions/${id}/cancel`, {}); }
  deleteSession(id: number) { return this.http.delete(`${this.baseUrl}/sessions/${id}`); }
}
