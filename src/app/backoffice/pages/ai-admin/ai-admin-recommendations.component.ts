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
    <section class="users-page">
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
  `
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
    return `${course.title} · ${course.level}`;
  }
}
