import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Booking } from '../../core/models/booking';
import { Session } from '../../core/models/Session';
import { SessionFeedback } from '../../core/models/SessionFeedback';
import { StaticTutor, STATIC_TUTORS } from '../../core/models/static-tutors';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private bookingApi = 'http://localhost:8080/api/bookings';
  private sessionApi = 'http://localhost:8080/api/sessions';
  private feedbackApi = 'http://localhost:8080/api/feedback';

  constructor(private http: HttpClient) {}

  getByStudent(id: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApi}/student/${id}`);
  }

  getByTutor(id: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApi}/tutor/${id}`);
  }

  getStudentSessions(studentId: number): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.sessionApi}/student/${studentId}`);
  }

  startSession(sessionId: number): Observable<Session> {
    return this.http.patch<Session>(`${this.sessionApi}/${sessionId}/start`, {});
  }

  endSession(sessionId: number): Observable<Session> {
    return this.http.patch<Session>(`${this.sessionApi}/${sessionId}/end`, {});
  }

  submitFeedback(sessionId: number, studentId: number, rating: number, comment: string): Observable<SessionFeedback> {
    return this.http.post<SessionFeedback>(`${this.feedbackApi}/session/${sessionId}`, {
      studentId,
      rating,
      comment
    });
  }

  getStaticTutors(): Observable<StaticTutor[]> {
    return of(STATIC_TUTORS);
  }

  getTutorById(id: number): Observable<StaticTutor | null> {
    const tutor = STATIC_TUTORS.find(t => t.id === id) ?? null;
    return of(tutor);
  }

  getAvailableSlots(tutorId: number, date: string): { startTime: string; endTime: string }[] {
    const tutor = STATIC_TUTORS.find(t => t.id === tutorId);
    if (!tutor || !date) return [];

    const day = new Date(`${date}T12:00:00`).toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();
    return tutor.availability
      .filter(slot => slot.available && slot.dayOfWeek === day)
      .map(slot => ({ startTime: slot.startTime, endTime: slot.endTime }));
  }

  createBooking(booking: Omit<Booking, 'id' | 'createdAt' | 'updatedAt'>): Observable<Booking> {
    return this.http.post<Booking>(this.bookingApi, booking);
  }

  confirm(id: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.bookingApi}/${id}/confirm`, {});
  }

  reject(id: number, reason: string): Observable<Booking> {
    const params = new HttpParams().set('reason', reason);
    return this.http.put<Booking>(`${this.bookingApi}/${id}/reject`, {}, { params });
  }

  cancel(id: number, cancelledBy: string, reason: string): Observable<Booking> {
    const params = new HttpParams().set('cancelledBy', cancelledBy).set('reason', reason);
    return this.http.put<Booking>(`${this.bookingApi}/${id}/cancel`, {}, { params });
  }
}

