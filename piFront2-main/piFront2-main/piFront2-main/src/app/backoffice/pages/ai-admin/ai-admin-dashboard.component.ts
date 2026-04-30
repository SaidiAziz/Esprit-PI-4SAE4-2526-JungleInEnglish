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
    <section class="admin-page-shell" *ngIf="dashboard$ | async as dashboard">
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
            <a routerLink="/admin/ai/students" class="admin-link-button">Open student monitor</a>
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
            <a routerLink="/admin/ai/recommendations" class="admin-link-button">Open recommendation monitor</a>
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
  `,
  styles: [`
    .admin-page-shell {
      display: grid;
      gap: 1.25rem;
    }

    .page-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      padding: 0 0 1rem;
      border-bottom: 1px solid rgba(212, 196, 184, 0.7);
    }

    .page-header--compact {
      padding-bottom: 0;
      border-bottom: 0;
      margin-bottom: 0.8rem;
    }

    .page-header h2,
    .page-header h3 {
      margin: 0;
      color: #0f172a;
    }

    .page-header h2 {
      font-size: 1.45rem;
    }

    .page-header h3 {
      font-size: 1.05rem;
    }

    .state-msg {
      margin-top: 0.35rem;
      color: #61717d;
      line-height: 1.45;
    }

    .admin-metric-grid,
    .admin-two-col {
      display: grid;
      gap: 1rem;
    }

    .admin-metric-grid {
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
    }

    .admin-two-col {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .admin-metric-card,
    .detail-panel,
    .table-wrapper {
      border: 1px solid rgba(212, 196, 184, 0.8);
      border-radius: 16px;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
      box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
    }

    .admin-metric-card {
      padding: 1rem 1.05rem;
    }

    .admin-metric-card span {
      display: block;
      color: #61717d;
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .admin-metric-card strong {
      display: block;
      margin-top: 0.3rem;
      font-size: 1.8rem;
      color: #0f172a;
    }

    .detail-panel {
      padding: 1rem;
    }

    .metric-stack {
      display: grid;
      gap: 0.75rem;
    }

    .metric-row {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.85rem 0.95rem;
      border-radius: 12px;
      border: 1px solid rgba(212, 196, 184, 0.74);
      background: rgba(255, 255, 255, 0.72);
      color: #43515b;
    }

    .metric-row strong {
      color: #0f172a;
    }

    .table-wrapper {
      overflow: auto;
      padding: 1rem;
    }

    .users-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .users-table thead {
      background: #f7efe6;
    }

    .users-table th,
    .users-table td {
      padding: 0.85rem 0.9rem;
      border-bottom: 1px solid rgba(212, 196, 184, 0.55);
      text-align: left;
      vertical-align: top;
    }

    .users-table th {
      color: #7a6859;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .admin-link-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 38px;
      padding: 0.55rem 0.9rem;
      border-radius: 10px;
      border: 1px solid #b7791f;
      color: #8a5a12;
      text-decoration: none;
      font-weight: 700;
      background: #fffdf9;
    }

    @media (max-width: 900px) {
      .admin-two-col {
        grid-template-columns: 1fr;
      }
    }
  `]
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
    return `${course.title} - ${course.level}`;
  }
}
