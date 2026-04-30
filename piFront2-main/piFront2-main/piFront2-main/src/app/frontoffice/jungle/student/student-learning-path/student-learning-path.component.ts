import { Component, inject } from '@angular/core';
import { LearningPath } from '../../../../core/models/ai.model';
import { CourseCatalogItem } from '../../../../core/models/course.model';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { CourseCatalogService } from '../../../../core/services/course-catalog.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-learning-path',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero learning-hero">
        <div class="learning-hero__copy">
          <p class="fd-eyebrow">Study Plan</p>
          <h1>Follow a step-by-step route for each course</h1>
          <p class="fd-muted">This page shows the lesson order the AI suggests for each course. Progress stays tied to your AI performance updates.</p>
        </div>
      </header>

      <section class="learning-summary" *ngIf="paths.length">
        <article class="learning-summary__item">
          <span>Plans</span>
          <strong>{{ paths.length }}</strong>
        </article>
        <article class="learning-summary__item">
          <span>Top focus</span>
          <strong>{{ primaryFocusSkill }}</strong>
        </article>
        <article class="learning-summary__item">
          <span>Average progress</span>
          <strong>{{ averageProgress }}%</strong>
        </article>
      </section>

      <div class="learning-layout">
        <section class="learning-panel">
          <div class="learning-panel__head">
            <div>
              <h2>Create a study plan</h2>
              <p>Choose a course and let the app build the recommended learning order.</p>
            </div>
          </div>

          <form class="learning-create-form" (ngSubmit)="generatePath()">
            <mat-form-field appearance="outline" class="learning-create-form__field">
              <mat-label>Course</mat-label>
              <mat-select [(ngModel)]="courseId" name="courseId">
                <mat-option *ngFor="let course of courses" [value]="course.id">
                  {{ course.title }} - {{ course.level }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit">Create plan</button>
          </form>

          <div class="learning-course-note" *ngIf="selectedCourse as course">
            <strong>{{ course.title }}</strong>
            <p>{{ course.description || ('Level: ' + course.level) }}</p>
          </div>
        </section>

        <section class="learning-panel">
          <div class="learning-panel__head">
            <div>
              <h2>How to use it</h2>
              <p>Keep the plan short and relevant to the current course.</p>
            </div>
          </div>

          <div class="learning-guide-list">
            <article class="learning-guide-item">
              <span>Generate</span>
              <strong>when a course starts or your scores shift noticeably</strong>
            </article>
            <article class="learning-guide-item">
              <span>Progress</span>
              <strong>updates automatically as your AI scores move</strong>
            </article>
            <article class="learning-guide-item">
              <span>Delete</span>
              <strong>plans for courses that are no longer active</strong>
            </article>
          </div>
        </section>
      </div>

      <section class="learning-panel" *ngIf="paths.length; else emptyPaths">
        <div class="learning-panel__head">
          <div>
            <h2>Your learning paths</h2>
            <p>Each plan shows a course, its focus skill, and the recommended lesson order.</p>
          </div>
          <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
        </div>

        <div class="learning-path-list">
          <article class="learning-path-row" *ngFor="let path of paths">
            <div class="learning-path-row__head">
              <div>
                <strong>{{ getCourseLabel(path.courseId) }}</strong>
                <p>{{ formatLabel(path.focusSkill) }} focus | Target {{ formatLabel(path.targetLevel) }}</p>
              </div>
              <span class="learning-progress-pill">{{ path.progress }}%</span>
            </div>

            <div class="learning-progress-track">
              <div class="learning-progress-track__fill" [style.width.%]="path.progress"></div>
            </div>

            <ol class="learning-step-list">
              <li *ngFor="let step of splitSteps(path.lessonOrder)">{{ step }}</li>
            </ol>

            <div class="learning-path-row__footer">
              <span>Updated {{ path.updatedAt | date: 'mediumDate' }}</span>
              <button mat-button type="button" color="warn" (click)="deletePath(path.id)">Delete plan</button>
            </div>
          </article>
        </div>
      </section>

      <ng-template #emptyPaths>
        <div class="fd-empty-state">No study plan yet. Create one for a course to begin.</div>
      </ng-template>
    </section>
  `,
  styles: [`
    .learning-hero__copy {
      max-width: 50rem;
    }

    .learning-summary {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0;
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      overflow: hidden;
      background: #fffdfa;
    }

    .learning-summary__item {
      padding: 0.95rem 1rem;
      border-right: 1px solid rgba(212, 196, 184, 0.62);
    }

    .learning-summary__item:last-child {
      border-right: 0;
    }

    .learning-summary__item span {
      display: block;
      margin-bottom: 0.3rem;
      color: #61717d;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .learning-summary__item strong {
      display: block;
      color: #0f172a;
      font-size: 1.05rem;
      line-height: 1.3;
      overflow-wrap: anywhere;
    }

    .learning-layout {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 1rem;
    }

    .learning-panel {
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      background: #fffdfa;
      padding: 1rem;
    }

    .learning-panel__head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.9rem;
    }

    .learning-panel__head h2 {
      margin: 0;
      color: #0f172a;
    }

    .learning-panel__head p {
      margin: 0.25rem 0 0;
      color: #61717d;
      line-height: 1.45;
    }

    .learning-create-form {
      display: flex;
      gap: 0.85rem;
      align-items: flex-start;
      flex-wrap: wrap;
    }

    .learning-create-form__field {
      flex: 1 1 280px;
      min-width: 220px;
    }

    .learning-course-note {
      margin-top: 0.85rem;
      padding: 0.85rem 0.95rem;
      border: 1px solid rgba(212, 196, 184, 0.7);
      border-radius: 10px;
      background: #ffffff;
    }

    .learning-course-note strong {
      color: #0f172a;
    }

    .learning-course-note p {
      margin: 0.35rem 0 0;
      color: #61717d;
      line-height: 1.45;
    }

    .learning-guide-list,
    .learning-path-list {
      display: grid;
      gap: 0.75rem;
    }

    .learning-guide-item,
    .learning-path-row {
      border: 1px solid rgba(212, 196, 184, 0.68);
      border-radius: 10px;
      background: #ffffff;
      padding: 0.95rem 1rem;
    }

    .learning-guide-item span {
      display: block;
      margin-bottom: 0.3rem;
      color: #61717d;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .learning-guide-item strong {
      color: #0f172a;
      line-height: 1.45;
    }

    .learning-path-row__head,
    .learning-path-row__footer {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .learning-path-row__head strong {
      color: #0f172a;
      font-size: 0.98rem;
    }

    .learning-path-row__head p {
      margin: 0.3rem 0 0;
      color: #61717d;
      line-height: 1.45;
    }

    .learning-progress-pill {
      display: inline-flex;
      align-items: center;
      min-height: 32px;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      background: #f7efe6;
      color: #7a6859;
      font-size: 0.78rem;
      font-weight: 700;
    }

    .learning-progress-track {
      margin: 0.85rem 0;
      width: 100%;
      height: 8px;
      border-radius: 999px;
      background: #efe5d8;
      overflow: hidden;
    }

    .learning-progress-track__fill {
      height: 100%;
      border-radius: inherit;
      background: linear-gradient(90deg, #c99753, #b7791f);
    }

    .learning-step-list {
      margin: 0;
      padding-left: 1.15rem;
      display: grid;
      gap: 0.45rem;
      color: #334155;
    }

    .learning-step-list li {
      line-height: 1.45;
    }

    .learning-path-row__footer {
      margin-top: 0.9rem;
      color: #61717d;
      font-size: 0.82rem;
    }

    @media (max-width: 900px) {
      .learning-layout {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 640px) {
      .learning-summary {
        grid-template-columns: 1fr;
      }

      .learning-summary__item {
        border-right: 0;
        border-bottom: 1px solid rgba(212, 196, 184, 0.62);
      }

      .learning-summary__item:last-child {
        border-bottom: 0;
      }
    }
  `]
})
export class StudentLearningPathComponent {
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly courseCatalogService = inject(CourseCatalogService);

  paths: LearningPath[] = [];
  courses: CourseCatalogItem[] = [];
  courseId = 101;
  feedback = '';

  constructor() {
    this.loadCourses();
    this.loadPaths();
  }

  loadCourses(): void {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses.filter(course => course.active);
      if (this.courses.length) {
        this.courseId = this.courses[0].id;
      }
    });
  }

  loadPaths(): void {
    this.aiAssistantService.getLearningPaths().subscribe(paths => {
      this.paths = paths;
    });
  }

  generatePath(): void {
    this.aiAssistantService.generateLearningPath(this.courseId).subscribe(() => {
      this.feedback = 'Study plan created.';
      this.loadPaths();
    });
  }

  deletePath(id: number): void {
    this.aiAssistantService.deleteLearningPath(id).subscribe(() => {
      this.feedback = 'Study plan deleted.';
      this.loadPaths();
    });
  }

  splitSteps(value: string): string[] {
    return value.split('->').map(step => step.trim()).filter(Boolean);
  }

  get selectedCourse(): CourseCatalogItem | undefined {
    return this.courses.find(course => course.id === this.courseId);
  }

  getCourseLabel(courseId: number): string {
    const course = this.courses.find(item => item.id === courseId);
    if (!course) {
      return `Course ${courseId}`;
    }
    return `${course.title} - ${course.level}`;
  }

  get primaryFocusSkill(): string {
    return this.paths.length ? this.formatLabel(this.paths[0].focusSkill) : 'None';
  }

  get averageProgress(): number {
    if (!this.paths.length) {
      return 0;
    }
    const total = this.paths.reduce((sum, path) => sum + path.progress, 0);
    return Math.round(total / this.paths.length);
  }

  formatLabel(value: string | null | undefined): string {
    if (!value) {
      return 'None';
    }

    return value
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}
