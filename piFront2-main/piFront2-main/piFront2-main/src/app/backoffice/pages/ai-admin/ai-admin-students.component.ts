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
    <section class="admin-page-shell">
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
      margin-bottom: 0.9rem;
    }

    .page-header h2,
    .page-header h3,
    .admin-section h4 {
      margin: 0;
      color: #0f172a;
    }

    .state-msg {
      margin-top: 0.35rem;
      color: #61717d;
      line-height: 1.45;
    }

    .admin-split-grid {
      display: grid;
      grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
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

    .users-table tbody tr {
      transition: background 0.16s ease;
    }

    .users-table tbody tr:hover,
    .selected-row {
      background: rgba(248, 240, 232, 0.72);
    }

    .col-action {
      text-align: right;
    }

    .detail-panel {
      padding: 1rem;
      display: grid;
      gap: 1rem;
    }

    .admin-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 0.6rem;
    }

    .user-count {
      display: inline-flex;
      align-items: center;
      min-height: 32px;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      background: #fff3d8;
      color: #8a5a12;
      font-size: 0.78rem;
      font-weight: 700;
    }

    .admin-section {
      display: grid;
      gap: 0.75rem;
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

    .admin-list-card p,
    .admin-list-card small {
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
      .admin-split-grid {
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
    return `${course.title} - ${course.level}`;
  }
}
