import { Component, inject } from '@angular/core';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { PerformanceAnalysis, UpdatePerformanceAnalysisRequest } from '../../../../core/models/ai.model';
import { CourseCatalogItem } from '../../../../core/models/course.model';
import { CourseCatalogService } from '../../../../core/services/course-catalog.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-ai-insights',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">My Progress</p>
          <h1>See how the app evaluates your current level</h1>
          <p class="fd-muted">This page shows the score records the AI uses to understand your grammar, listening, and speaking level. Manual entry is only for demo/testing until real assessments are connected.</p>
        </div>
        <mat-chip-set *ngIf="summary">
          <mat-chip>{{ summary.currentLevel }}</mat-chip>
          <mat-chip highlighted>Weakest: {{ summary.weakestSkill }}</mat-chip>
        </mat-chip-set>
      </header>

      <div class="fd-grid fd-grid--three" *ngIf="summary">
        <mat-card class="fd-metric-card">
          <span class="fd-metric-label">Overall average</span>
          <strong>{{ summary.overallAverage }}%</strong>
        </mat-card>
        <mat-card class="fd-metric-card">
          <span class="fd-metric-label">Tracked analyses</span>
          <strong>{{ summary.totalAnalyses }}</strong>
        </mat-card>
        <mat-card class="fd-metric-card">
          <span class="fd-metric-label">Priority skill</span>
          <strong>{{ summary.weakestSkill }}</strong>
        </mat-card>
      </div>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Add score data for demo</h2>
              <p>Create one score record for a course so the AI can generate level, weak skill, and recommendations.</p>
            </div>
          </div>
          <form class="fd-form-grid" (ngSubmit)="createAnalysis()">
            <mat-form-field appearance="outline">
              <mat-label>Course</mat-label>
              <mat-select [(ngModel)]="createForm.courseId" name="courseId">
                <mat-option *ngFor="let course of courses" [value]="course.id">
                  {{ course.title }} · {{ course.level }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Grammar score</mat-label>
              <input matInput type="number" [(ngModel)]="createForm.grammarScore" name="grammarScore">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Listening score</mat-label>
              <input matInput type="number" [(ngModel)]="createForm.listeningScore" name="listeningScore">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Speaking score</mat-label>
              <input matInput type="number" [(ngModel)]="createForm.speakingScore" name="speakingScore">
            </mat-form-field>
            <button mat-flat-button color="primary" type="submit">Create analysis</button>
          </form>
          <p class="fd-muted" *ngIf="selectedCreateCourse as course">
            Selected course: <strong>{{ course.title }}</strong><span *ngIf="course.description"> | {{ course.description }}</span>
          </p>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Assistant meaning</h2>
              <p>How the backend interprets your scores.</p>
            </div>
          </div>
          <div class="fd-analytics-list">
            <div><span>Beginner</span><strong>Needs guided practice and structured support</strong></div>
            <div><span>Intermediate</span><strong>Can alternate between drills and live collaboration rooms</strong></div>
            <div><span>Advanced</span><strong>Should be pushed with open-ended speaking and challenge play</strong></div>
          </div>
        </mat-card>
      </div>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>Your score records</h2>
            <p>These records are what the AI reads before creating recommendations and a study plan.</p>
          </div>
          <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
        </div>
        <div class="fd-insight-list" *ngIf="analyses.length; else emptyAnalyses">
          <article class="fd-insight-item" *ngFor="let analysis of analyses">
            <div class="fd-section-inline">
              <div class="fd-insight-title">
                <strong>{{ getCourseLabel(analysis.courseId) }}</strong>
                <span>{{ analysis.level }}</span>
              </div>
              <div class="fd-chip-row">
                <span class="fd-pill">{{ analysis.averageScore | number: '1.0-1' }}%</span>
                <button mat-button type="button" (click)="selectAnalysis(analysis)">Edit</button>
                <button mat-button type="button" (click)="deleteAnalysis(analysis.id)">Delete</button>
              </div>
            </div>
            <div class="fd-progress-stack">
              <label>Grammar {{ analysis.grammarScore }}%</label>
              <mat-progress-bar mode="determinate" [value]="analysis.grammarScore"></mat-progress-bar>
              <label>Listening {{ analysis.listeningScore }}%</label>
              <mat-progress-bar mode="determinate" [value]="analysis.listeningScore"></mat-progress-bar>
              <label>Speaking {{ analysis.speakingScore }}%</label>
              <mat-progress-bar mode="determinate" [value]="analysis.speakingScore"></mat-progress-bar>
            </div>
          </article>
        </div>
      </mat-card>

      <mat-card class="fd-surface-card" *ngIf="selectedAnalysis">
        <div class="fd-section-head">
          <div>
            <h2>Edit score record</h2>
            <p>Adjust the stored scores and let the service recompute the level.</p>
          </div>
        </div>
        <form class="fd-form-grid" (ngSubmit)="updateAnalysis()">
          <mat-form-field appearance="outline">
            <mat-label>Course</mat-label>
            <mat-select [(ngModel)]="editForm.courseId" name="editCourseId">
              <mat-option *ngFor="let course of courses" [value]="course.id">
                {{ course.title }} · {{ course.level }}
              </mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Grammar score</mat-label>
            <input matInput type="number" [(ngModel)]="editForm.grammarScore" name="editGrammarScore">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Listening score</mat-label>
            <input matInput type="number" [(ngModel)]="editForm.listeningScore" name="editListeningScore">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Speaking score</mat-label>
            <input matInput type="number" [(ngModel)]="editForm.speakingScore" name="editSpeakingScore">
          </mat-form-field>
          <div class="fd-chip-row">
            <button mat-flat-button color="primary" type="submit">Save changes</button>
            <button mat-stroked-button type="button" (click)="cancelEdit()">Cancel</button>
          </div>
        </form>
      </mat-card>

      <ng-template #emptyAnalyses>
        <div class="fd-empty-state">No analyses yet. Add one to activate the assistant logic.</div>
      </ng-template>
    </section>
  `
})
export class StudentAiInsightsComponent {
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly courseCatalogService = inject(CourseCatalogService);

  summary: any = null;
  analyses: PerformanceAnalysis[] = [];
  courses: CourseCatalogItem[] = [];
  selectedAnalysis: PerformanceAnalysis | null = null;
  feedback = '';

  createForm = {
    courseId: 101,
    grammarScore: 70,
    listeningScore: 68,
    speakingScore: 60
  };

  editForm: UpdatePerformanceAnalysisRequest = {
    courseId: 101,
    grammarScore: 70,
    listeningScore: 68,
    speakingScore: 60
  };

  constructor() {
    this.loadCourses();
    this.loadData();
  }

  loadData(): void {
    this.aiAssistantService.getSummary().subscribe(summary => this.summary = summary);
    this.aiAssistantService.getAnalyses().subscribe(analyses => this.analyses = analyses);
  }

  loadCourses(): void {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses.filter(course => course.active);
      if (this.courses.length) {
        this.createForm.courseId = this.courses[0].id;
        if (!this.selectedAnalysis) {
          this.editForm.courseId = this.courses[0].id;
        }
      }
    });
  }

  createAnalysis(): void {
    this.aiAssistantService.createAnalysis(this.createForm).subscribe(() => {
      this.feedback = 'Performance analysis created.';
      this.loadData();
    });
  }

  selectAnalysis(analysis: PerformanceAnalysis): void {
    this.selectedAnalysis = analysis;
    this.editForm = {
      courseId: analysis.courseId,
      grammarScore: analysis.grammarScore,
      listeningScore: analysis.listeningScore,
      speakingScore: analysis.speakingScore
    };
  }

  updateAnalysis(): void {
    if (!this.selectedAnalysis) {
      return;
    }

    this.aiAssistantService.updateAnalysis(this.selectedAnalysis.id, this.editForm).subscribe(() => {
      this.feedback = 'Performance analysis updated.';
      this.selectedAnalysis = null;
      this.loadData();
    });
  }

  deleteAnalysis(id: number): void {
    this.aiAssistantService.deleteAnalysis(id).subscribe(() => {
      this.feedback = 'Performance analysis deleted.';
      this.selectedAnalysis = this.selectedAnalysis?.id === id ? null : this.selectedAnalysis;
      this.loadData();
    });
  }

  cancelEdit(): void {
    this.selectedAnalysis = null;
  }

  get selectedCreateCourse(): CourseCatalogItem | undefined {
    return this.courses.find(course => course.id === this.createForm.courseId);
  }

  getCourseLabel(courseId: number): string {
    const course = this.courses.find(item => item.id === courseId);
    if (!course) {
      return `Course ${courseId}`;
    }
    return `${course.title} · ${course.level}`;
  }
}
