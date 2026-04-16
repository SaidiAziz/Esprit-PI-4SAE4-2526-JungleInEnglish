import { Component, inject } from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { AiSummary, AiAdminStudentOverview } from '../../../core/models/ai.model';
import { CourseCatalogItem } from '../../../core/models/course.model';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';
import { CourseCatalogService } from '../../../core/services/course-catalog.service';
import { UserDirectoryService } from '../../../core/services/user-directory.service';
import { Observable } from 'rxjs';
import { UserResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-ai-admin-students',
  standalone: true,
  imports: [CommonModule, AsyncPipe],
  template: `
    <section class="users-page">
      <div class="page-header">
        <div>
          <h2>AI Student Monitor</h2>
          <p class="state-msg">Inspect each student's level, weak skill, analyses, recommendations, and learning paths.</p>
        </div>
      </div>

      <div class="admin-split-grid">
        <div class="table-wrapper">
          <table class="users-table">
            <thead>
            <tr>
              <th>User</th>
              <th>Level</th>
              <th>Average</th>
              <th>Weak skill</th>
              <th>Analyses</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let student of students" [class.selected-row]="selectedStudentId === student.userId">
              <td>
                <ng-container *ngIf="getUser(student.userId) | async as user">
                  <strong>{{ formatUser(user, student.userId) }}</strong>
                  <div class="state-msg">ID {{ student.userId }}<span *ngIf="user?.email"> | {{ user?.email }}</span></div>
                </ng-container>
              </td>
              <td>{{ student.currentLevel }}</td>
              <td>{{ student.overallAverage | number: '1.0-2' }}</td>
              <td>{{ student.weakestSkill }}</td>
              <td>{{ student.analysisCount }}</td>
              <td class="col-action">
                <button class="btn-view" (click)="selectStudent(student)">Inspect</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="detail-panel" *ngIf="selectedSummary as summary">
          <div class="page-header page-header--compact">
            <ng-container *ngIf="getUser(summary.userId) | async as user">
              <div>
                <h3>{{ formatUser(user, summary.userId) }}</h3>
                <p class="state-msg">ID {{ summary.userId }}<span *ngIf="user?.email"> | {{ user?.email }}</span></p>
                <p class="state-msg">Current level {{ summary.currentLevel }} | Weakest skill {{ summary.weakestSkill }}</p>
              </div>
            </ng-container>
          </div>

          <div class="admin-chip-row">
            <span class="user-count">{{ summary.performanceAnalyses.length }} analyses</span>
            <span class="user-count">{{ summary.recommendations.length }} recommendations</span>
            <span class="user-count">{{ summary.learningPaths.length }} learning paths</span>
          </div>

          <div class="admin-section">
            <h4>Analyses</h4>
            <div class="admin-list-card" *ngFor="let analysis of summary.performanceAnalyses">
              <div>
                <strong>{{ getCourseLabel(analysis.courseId) }}</strong>
                <p>{{ analysis.level }} | Avg {{ analysis.averageScore | number: '1.0-2' }}</p>
              </div>
              <small>{{ analysis.lastUpdated | date: 'medium' }}</small>
            </div>
          </div>

          <div class="admin-section">
            <h4>Recommendations</h4>
            <div class="admin-list-card" *ngFor="let recommendation of summary.recommendations">
              <div>
                <strong>{{ recommendation.contentTitle || recommendation.type }}</strong>
                <p>{{ recommendation.focusSkill }} | {{ recommendation.reason }}</p>
              </div>
            </div>
          </div>

          <div class="admin-section">
            <h4>Learning paths</h4>
            <div class="admin-list-card" *ngFor="let path of summary.learningPaths">
              <div>
                <strong>{{ getCourseLabel(path.courseId) }}</strong>
                <p>{{ path.focusSkill }} | Progress {{ path.progress }}%</p>
              </div>
              <button class="btn-view" (click)="regeneratePath(summary.userId, path.courseId)">Regenerate path</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  `
})
export class AiAdminStudentsComponent {
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly courseCatalogService = inject(CourseCatalogService);
  private readonly userDirectoryService = inject(UserDirectoryService);

  students: AiAdminStudentOverview[] = [];
  selectedStudentId: number | null = null;
  selectedSummary: AiSummary | null = null;
  courses: CourseCatalogItem[] = [];

  constructor() {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses;
    });
    this.aiAssistantService.getAdminStudents().subscribe(students => {
      this.students = students;
      if (students.length && this.selectedStudentId === null) {
        this.selectStudent(students[0]);
      }
    });
  }

  selectStudent(student: AiAdminStudentOverview): void {
    this.selectedStudentId = student.userId;
    this.aiAssistantService.getAdminStudentDetail(student.userId).subscribe(summary => {
      this.selectedSummary = summary;
    });
  }

  regeneratePath(userId: number, courseId: number): void {
    this.aiAssistantService.regenerateAdminLearningPath(userId, courseId).subscribe(path => {
      if (!this.selectedSummary) {
        return;
      }
      this.selectedSummary = {
        ...this.selectedSummary,
        learningPaths: this.selectedSummary.learningPaths.map(existing => existing.id === path.id ? path : existing)
      };
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
