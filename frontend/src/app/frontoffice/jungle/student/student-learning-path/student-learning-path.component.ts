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
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Study Plan</p>
          <h1>Follow a simple step-by-step route for each course</h1>
          <p class="fd-muted">This page shows the order the AI suggests for lessons. Your progress automatically syncs with your AI scores.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Create a study plan</h2>
              <p>Choose a course and let the app build the recommended order.</p>
            </div>
          </div>
          <form class="fd-form-grid" (ngSubmit)="generatePath()">
            <mat-form-field appearance="outline">
              <mat-label>Course</mat-label>
              <mat-select [(ngModel)]="courseId" name="courseId">
                <mat-option *ngFor="let course of courses" [value]="course.id">
                  {{ course.title }} · {{ course.level }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit">Create plan</button>
          </form>
          <p class="fd-muted" *ngIf="selectedCourse as course">
            Selected course: <strong>{{ course.title }}</strong><span *ngIf="course.description"> | {{ course.description }}</span>
          </p>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>How to use it</h2>
              <p>Keep the path lean so it stays useful.</p>
            </div>
          </div>
          <div class="fd-analytics-list">
            <div><span>Generate</span><strong>when a course starts or scores change significantly</strong></div>
            <div><span>Auto-Synced</span><strong>Progress updates automatically as you collaborate</strong></div>
            <div><span>Delete</span><strong>when a course is no longer active</strong></div>
          </div>
        </mat-card>
      </div>

      <div class="fd-grid fd-grid--two" *ngIf="paths.length; else emptyPaths">
        <mat-card class="fd-surface-card" *ngFor="let path of paths">
          <div class="fd-section-head">
            <div>
              <h2>{{ getCourseLabel(path.courseId) }}</h2>
              <p>{{ path.focusSkill }} focus | target {{ path.targetLevel }}</p>
            </div>
            <span class="fd-pill">{{ path.progress }}%</span>
          </div>
          <div class="fd-room-stack">
            <mat-progress-bar mode="determinate" [value]="path.progress"></mat-progress-bar>
            <ol class="fd-step-list">
              <li *ngFor="let step of splitSteps(path.lessonOrder)">{{ step }}</li>
            </ol>
            <div class="fd-form-grid fd-form-grid--compact">
              <p class="fd-muted-inline" style="font-size: 0.85rem;">Progress auto-syncs with AI average score.</p>
              <div class="fd-chip-row">
                <button mat-button type="button" color="warn" (click)="deletePath(path.id)">Delete plan</button>
              </div>
            </div>
          </div>
        </mat-card>
      </div>

      <div class="fd-feedback" *ngIf="feedback">{{ feedback }}</div>

      <ng-template #emptyPaths>
        <div class="fd-empty-state">No study plan yet. Create one for a course to begin.</div>
      </ng-template>
    </section>
  `
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
    return `${course.title} · ${course.level}`;
  }
}
