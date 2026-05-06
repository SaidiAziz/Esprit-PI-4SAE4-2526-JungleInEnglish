import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import { CourseCatalogItem } from '../models/course.model';

@Injectable({ providedIn: 'root' })
export class CourseCatalogService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8222/api/courses';

  getCourses(): Observable<CourseCatalogItem[]> {
    return this.http.get<CourseCatalogItem[]>(this.apiUrl).pipe(
      catchError(() => of(this.buildFallbackCourses()))
    );
  }

  getCourseById(id: number): Observable<CourseCatalogItem> {
    return this.http.get<CourseCatalogItem>(`${this.apiUrl}/${id}`).pipe(
      catchError(() => of(this.buildFallbackCourses().find(course => course.id === id) ?? this.buildFallbackCourses()[0]))
    );
  }

  createCourse(course: Omit<CourseCatalogItem, 'id'>): Observable<CourseCatalogItem> {
    return this.http.post<CourseCatalogItem>(this.apiUrl, course).pipe(
      catchError(() => of({ ...course, id: Date.now() }))
    );
  }

  updateCourse(id: number, course: Omit<CourseCatalogItem, 'id'>): Observable<CourseCatalogItem> {
    return this.http.put<CourseCatalogItem>(`${this.apiUrl}/${id}`, course).pipe(
      catchError(() => of({ ...course, id }))
    );
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(() => of(void 0))
    );
  }

  searchByLevel(level: string): Observable<CourseCatalogItem[]> {
    return this.http.get<CourseCatalogItem[]>(`${this.apiUrl}/search`, { params: { level } }).pipe(
      catchError(() => of(this.buildFallbackCourses().filter(course => course.level.toLowerCase() === level.toLowerCase())))
    );
  }

  private buildFallbackCourses(): CourseCatalogItem[] {
    return [
      {
        id: 101,
        title: 'English Speaking Booster',
        level: 'INTERMEDIATE',
        description: 'Conversation practice focused on speaking confidence and fluency.',
        durationHours: 12,
        startDate: new Date().toISOString(),
        endDate: null,
        price: 0,
        maxStudents: 30,
        active: true
      },
      {
        id: 102,
        title: 'Listening and Conversation Lab',
        level: 'INTERMEDIATE',
        description: 'Listening exercises with guided conversation prompts.',
        durationHours: 10,
        startDate: new Date().toISOString(),
        endDate: null,
        price: 25,
        maxStudents: 25,
        active: true
      }
    ];
  }
}
