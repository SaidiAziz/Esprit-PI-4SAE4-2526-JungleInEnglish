import { Component, inject } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { LearningPath, Recommendation } from '../../../core/models/ai.model';
import { CourseCatalogItem } from '../../../core/models/course.model';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';
import { CourseCatalogService } from '../../../core/services/course-catalog.service';
import { UserDirectoryService } from '../../../core/services/user-directory.service';
import { Observable } from 'rxjs';
import { UserResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-ai-admin-recommendations',
  standalone: true,
  imports: [CommonModule, AsyncPipe],
  template: `
    <section class="admin-page-shell">
      <div class="page-header">
        <div>
          <h2>Recommendation Monitor</h2>
          <p class="state-msg">Review generated recommendations and learning paths, then regenerate them when they look stale.</p>
        </div>
      </div>

      <div class="admin-two-col">
        <div class="table-wrapper">
          <div class="page-header page-header--compact">
            <h3>Recommendations</h3>
          </div>
          <table class="users-table">
            <thead>
            <tr>
              <th>User</th>
              <th>Type</th>
              <th>Skill</th>
              <th>Content</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let recommendation of recommendations">
              <td>
                <ng-container *ngIf="getUser(recommendation.userId) | async as user">
                  <strong>{{ formatUser(user, recommendation.userId) }}</strong>
                  <div class="state-msg">ID {{ recommendation.userId }}</div>
                </ng-container>
              </td>
              <td>{{ recommendation.type }}</td>
              <td>{{ recommendation.focusSkill }}</td>
              <td>{{ recommendation.contentTitle || recommendation.contentId }}</td>
              <td class="col-action">
                <button class="btn-view" (click)="regenerateRecommendations(recommendation.userId)">Regenerate</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="detail-panel">
          <div class="page-header page-header--compact">
            <h3>Learning paths</h3>
          </div>
          <div class="admin-list-card" *ngFor="let path of learningPaths">
            <ng-container *ngIf="getUser(path.userId) | async as user">
              <div>
                <strong>{{ formatUser(user, path.userId) }} | {{ getCourseLabel(path.courseId) }}</strong>
                <p>ID {{ path.userId }}<span *ngIf="user?.email"> | {{ user?.email }}</span></p>
                <p>{{ path.focusSkill }} | {{ path.targetLevel }} | Progress {{ path.progress }}%</p>
              </div>
            </ng-container>
            <button class="btn-view" (click)="regeneratePath(path)">Regenerate path</button>
          </div>
        </div>
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
      margin-bottom: 0.85rem;
    }

    .page-header h2,
    .page-header h3 {
      margin: 0;
      color: #0f172a;
    }

    .state-msg {
      margin-top: 0.35rem;
      color: #61717d;
      line-height: 1.45;
    }

    .admin-two-col {
      display: grid;
      grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
      gap: 1rem;
      align-items: start;
    }

    .table-wrapper,
    .detail-panel {
      border: 1px solid rgba(212, 196, 184, 0.8);
      border-radius: 16px;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
      box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
    }

    .table-wrapper {
      overflow: auto;
      padding: 1rem;
    }

    .detail-panel {
      padding: 1rem;
      display: grid;
      gap: 0.85rem;
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

    .col-action {
      text-align: right;
    }

    .admin-list-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.95rem 1rem;
      border-radius: 12px;
      border: 1px solid rgba(212, 196, 184, 0.74);
      background: rgba(255, 255, 255, 0.72);
    }

    .admin-list-card p {
      color: #61717d;
    }

    .btn-view {
      min-height: 36px;
      padding: 0.5rem 0.85rem;
      border-radius: 10px;
      border: 1px solid #b7791f;
      background: #fffdf9;
      color: #8a5a12;
      font-weight: 700;
      cursor: pointer;
    }

    @media (max-width: 980px) {
      .admin-two-col {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 720px) {
      .admin-list-card {
        flex-direction: column;
        align-items: stretch;
      }
    }
  `]
})
export class AiAdminRecommendationsComponent {
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly courseCatalogService = inject(CourseCatalogService);
  private readonly userDirectoryService = inject(UserDirectoryService);

  recommendations: Recommendation[] = [];
  learningPaths: LearningPath[] = [];
  courses: CourseCatalogItem[] = [];

  constructor() {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses;
    });
    this.loadRecommendations();
    this.loadLearningPaths();
  }

  regenerateRecommendations(userId: number): void {
    this.aiAssistantService.regenerateAdminRecommendations(userId).subscribe(() => this.loadRecommendations());
  }

  regeneratePath(path: LearningPath): void {
    this.aiAssistantService.regenerateAdminLearningPath(path.userId, path.courseId).subscribe(() => this.loadLearningPaths());
  }

  private loadRecommendations(): void {
    this.aiAssistantService.getAdminRecommendations().subscribe(recommendations => {
      this.recommendations = recommendations;
    });
  }

  private loadLearningPaths(): void {
    this.aiAssistantService.getAdminLearningPaths().subscribe(paths => {
      this.learningPaths = paths;
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
