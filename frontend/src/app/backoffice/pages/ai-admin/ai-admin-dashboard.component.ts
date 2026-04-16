import { Component, inject } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';
import { CourseCatalogItem } from '../../../core/models/course.model';
import { UserDirectoryService } from '../../../core/services/user-directory.service';
import { CourseCatalogService } from '../../../core/services/course-catalog.service';
import { Observable } from 'rxjs';
import { UserResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-ai-admin-dashboard',
  standalone: true,
  imports: [CommonModule, AsyncPipe, RouterLink],
  template: `
    <section class="users-page" *ngIf="dashboard$ | async as dashboard">
      <div class="page-header">
        <div>
          <h2>AI Supervision</h2>
          <p class="state-msg">Monitor generated analyses, learning paths, and recommendation quality across students.</p>
        </div>
      </div>

      <div class="admin-metric-grid">
        <article class="admin-metric-card">
          <span>Students tracked</span>
          <strong>{{ dashboard.totalStudents }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Analyses</span>
          <strong>{{ dashboard.totalAnalyses }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Recommendations</span>
          <strong>{{ dashboard.totalRecommendations }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Learning paths</span>
          <strong>{{ dashboard.totalLearningPaths }}</strong>
        </article>
      </div>

      <div class="admin-two-col">
        <div class="detail-panel">
          <div class="page-header page-header--compact">
            <h3>Level distribution</h3>
            <a routerLink="/admin/ai/students" class="btn-view admin-link-button">Open student monitor</a>
          </div>
          <div class="metric-stack">
            <div class="metric-row" *ngFor="let item of dashboard.levelDistribution">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>

        <div class="detail-panel">
          <div class="page-header page-header--compact">
            <h3>Weak skill trends</h3>
            <a routerLink="/admin/ai/recommendations" class="btn-view admin-link-button">Open recommendation monitor</a>
          </div>
          <div class="metric-stack">
            <div class="metric-row" *ngFor="let item of dashboard.weakestSkillDistribution">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </div>

      <div class="table-wrapper">
        <div class="page-header page-header--compact">
          <h3>Recent analyses</h3>
        </div>
        <table class="users-table">
          <thead>
          <tr>
            <th>User</th>
            <th>Course</th>
            <th>Level</th>
            <th>Average</th>
            <th>Updated</th>
          </tr>
          </thead>
          <tbody>
          <tr *ngFor="let analysis of dashboard.recentAnalyses">
            <td>
              <ng-container *ngIf="getUser(analysis.userId) | async as user">
                <strong>{{ formatUser(user, analysis.userId) }}</strong>
                <div class="state-msg">ID {{ analysis.userId }}<span *ngIf="user?.email"> | {{ user?.email }}</span></div>
              </ng-container>
            </td>
            <td>{{ getCourseLabel(analysis.courseId) }}</td>
            <td>{{ analysis.level }}</td>
            <td>{{ analysis.averageScore | number: '1.0-2' }}</td>
            <td>{{ analysis.lastUpdated | date: 'medium' }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
  `
})
export class AiAdminDashboardComponent {
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly userDirectoryService = inject(UserDirectoryService);
  private readonly courseCatalogService = inject(CourseCatalogService);
  readonly dashboard$ = this.aiAssistantService.getAdminDashboard();
  courses: CourseCatalogItem[] = [];

  constructor() {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses;
    });
  }

  getUser(userId: number): Observable<UserResponse | null> {
    return this.userDirectoryService.getUserById(userId);
  }

  formatUser(user: UserResponse | null, userId: number): string {
    if (!user) {
      return `User #${userId}`;
    }
    return `${user.firstName} ${user.lastName}`.trim();
  }

  getCourseLabel(courseId: number): string {
    const course = this.courses.find(item => item.id === courseId);
    if (!course) {
      return `Course ${courseId}`;
    }
    return `${course.title} · ${course.level}`;
  }
}
