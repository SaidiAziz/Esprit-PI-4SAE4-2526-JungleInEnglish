import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import {
  AiAdminDashboard,
  AiAdminStudentOverview,
  AiSummary,
  CreatePerformanceAnalysisRequest,
  GenerateLearningPathRequest,
  LearningPath,
  PerformanceAnalysis,
  Recommendation,
  UpdateLearningPathProgressRequest,
  UpdatePerformanceAnalysisRequest
} from '../models/ai.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly apiUrl = '/api/ai';

  getSummary(): Observable<AiSummary> {
    return this.http.get<AiSummary>(`${this.apiUrl}/summary/me`).pipe(catchError(() => of(this.buildFallbackSummary())));
  }

  getAnalyses(): Observable<PerformanceAnalysis[]> {
    return this.http.get<PerformanceAnalysis[]>(`${this.apiUrl}/performance-analyses/me`).pipe(
      catchError(() => of(this.buildFallbackSummary().performanceAnalyses))
    );
  }

  getRecommendations(): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.apiUrl}/recommendations/me`).pipe(
      catchError(() => of(this.buildFallbackSummary().recommendations))
    );
  }

  getLearningPaths(): Observable<LearningPath[]> {
    return this.http.get<LearningPath[]>(`${this.apiUrl}/learning-paths/me`).pipe(
      catchError(() => of(this.buildFallbackSummary().learningPaths))
    );
  }

  getAdminDashboard(): Observable<AiAdminDashboard> {
    return this.http.get<AiAdminDashboard>(`${this.apiUrl}/admin/dashboard`).pipe(
      catchError(() => of(this.buildFallbackAdminDashboard()))
    );
  }

  getAdminStudents(): Observable<AiAdminStudentOverview[]> {
    return this.http.get<AiAdminStudentOverview[]>(`${this.apiUrl}/admin/students`).pipe(
      catchError(() => of(this.buildFallbackAdminStudents()))
    );
  }

  getAdminStudentDetail(userId: number): Observable<AiSummary> {
    return this.http.get<AiSummary>(`${this.apiUrl}/admin/students/${userId}`).pipe(
      catchError(() => of({ ...this.buildFallbackSummary(), userId }))
    );
  }

  getAdminRecommendations(): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.apiUrl}/admin/recommendations`).pipe(
      catchError(() => of(this.buildFallbackSummary().recommendations))
    );
  }

  regenerateAdminRecommendations(userId: number): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(`${this.apiUrl}/admin/recommendations/${userId}/regenerate`, {}).pipe(
      catchError(() => of(this.buildFallbackSummary().recommendations.map(item => ({ ...item, userId }))))
    );
  }

  getAdminLearningPaths(): Observable<LearningPath[]> {
    return this.http.get<LearningPath[]>(`${this.apiUrl}/admin/learning-paths`).pipe(
      catchError(() => of(this.buildFallbackSummary().learningPaths))
    );
  }

  regenerateAdminLearningPath(userId: number, courseId: number): Observable<LearningPath> {
    return this.http.post<LearningPath>(`${this.apiUrl}/admin/learning-paths/${userId}/regenerate/${courseId}`, {}).pipe(
      catchError(() => of({
        ...this.buildFallbackSummary().learningPaths[0],
        userId,
        courseId,
        updatedAt: new Date().toISOString()
      }))
    );
  }

  createAnalysis(request: Omit<CreatePerformanceAnalysisRequest, 'userId'>): Observable<PerformanceAnalysis> {
    return this.http.post<PerformanceAnalysis>(`${this.apiUrl}/performance-analyses`, {
      ...request,
      userId: this.getCurrentUserId()
    }).pipe(catchError(() => of(this.buildFallbackSummary().performanceAnalyses[0])));
  }

  updateAnalysis(id: number, request: UpdatePerformanceAnalysisRequest): Observable<PerformanceAnalysis> {
    return this.http.put<PerformanceAnalysis>(`${this.apiUrl}/performance-analyses/${id}`, request).pipe(
      catchError(() => of({ ...this.buildFallbackSummary().performanceAnalyses[0], id, ...request }))
    );
  }

  deleteAnalysis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/performance-analyses/${id}`).pipe(catchError(() => of(void 0)));
  }

  generateRecommendations(): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(`${this.apiUrl}/recommendations/generate/me`, {}).pipe(
      catchError(() => of(this.buildFallbackSummary().recommendations))
    );
  }

  generateLearningPath(courseId: number): Observable<LearningPath> {
    const payload: GenerateLearningPathRequest = {
      userId: this.getCurrentUserId(),
      courseId
    };
    return this.http.post<LearningPath>(`${this.apiUrl}/learning-paths/generate`, payload).pipe(
      catchError(() =>
        of({
          ...this.buildFallbackSummary().learningPaths[0],
          id: Date.now(),
          courseId
        })
      )
    );
  }

  updateLearningPathProgress(id: number, progress: number): Observable<LearningPath> {
    const payload: UpdateLearningPathProgressRequest = { progress };
    return this.http.patch<LearningPath>(`${this.apiUrl}/learning-paths/${id}/progress`, payload).pipe(
      catchError(() => of({ ...this.buildFallbackSummary().learningPaths[0], id, progress }))
    );
  }

  deleteLearningPath(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/learning-paths/${id}`).pipe(catchError(() => of(void 0)));
  }

  private getCurrentUserId(): number {
    return this.authService.getCurrentUser()?.id ?? 1;
  }

  private buildFallbackSummary(): AiSummary {
    return {
      userId: 1,
      currentLevel: 'INTERMEDIATE',
      overallAverage: 68,
      weakestSkill: 'SPEAKING',
      totalAnalyses: 2,
      performanceAnalyses: [
        { id: 1, userId: 1, courseId: 101, grammarScore: 74, listeningScore: 70, speakingScore: 58, averageScore: 67.33, level: 'INTERMEDIATE', createdAt: new Date().toISOString(), lastUpdated: new Date().toISOString() },
        { id: 2, userId: 1, courseId: 102, grammarScore: 69, listeningScore: 72, speakingScore: 61, averageScore: 67.33, level: 'INTERMEDIATE', createdAt: new Date().toISOString(), lastUpdated: new Date().toISOString() }
      ],
      recommendations: [
        { id: 1, userId: 1, type: 'COURSE', contentId: 101, contentTitle: 'English Speaking Booster', contentDescription: 'A guided conversation course focused on fluency and pronunciation.', contentLevel: 'INTERMEDIATE', contentPrice: 0, reason: 'Recommended because speaking is currently your weakest skill.', focusSkill: 'SPEAKING', createdAt: new Date().toISOString() },
        { id: 2, userId: 1, type: 'COURSE', contentId: 102, contentTitle: 'Listening and Conversation Lab', contentDescription: 'Interactive audio-based practice to improve comprehension and speaking confidence.', contentLevel: 'INTERMEDIATE', contentPrice: 25, reason: 'A second course option that still supports your speaking progress.', focusSkill: 'SPEAKING', createdAt: new Date().toISOString() }
      ],
      learningPaths: [
        { id: 1, userId: 1, courseId: 101, lessonOrder: 'Speaking Focus -> Pronunciation Drill -> Listening Workshop -> Grammar Refresh -> Applied Practice', progress: 46, focusSkill: 'SPEAKING', targetLevel: 'INTERMEDIATE', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }
      ]
    };
  }

  private buildFallbackAdminDashboard(): AiAdminDashboard {
    return {
      totalStudents: 4,
      totalAnalyses: 8,
      totalRecommendations: 6,
      totalLearningPaths: 4,
      levelDistribution: [
        { label: 'INTERMEDIATE', value: 3 },
        { label: 'BEGINNER', value: 1 }
      ],
      weakestSkillDistribution: [
        { label: 'SPEAKING', value: 2 },
        { label: 'GRAMMAR', value: 1 },
        { label: 'LISTENING', value: 1 }
      ],
      recentAnalyses: this.buildFallbackSummary().performanceAnalyses
    };
  }

  private buildFallbackAdminStudents(): AiAdminStudentOverview[] {
    return [
      {
        userId: 1,
        currentLevel: 'INTERMEDIATE',
        overallAverage: 68,
        weakestSkill: 'SPEAKING',
        analysisCount: 2,
        recommendationCount: 2,
        learningPathCount: 1,
        lastAnalysisAt: new Date().toISOString()
      },
      {
        userId: 3,
        currentLevel: 'BEGINNER',
        overallAverage: 54,
        weakestSkill: 'GRAMMAR',
        analysisCount: 1,
        recommendationCount: 1,
        learningPathCount: 1,
        lastAnalysisAt: new Date().toISOString()
      }
    ];
  }
}
