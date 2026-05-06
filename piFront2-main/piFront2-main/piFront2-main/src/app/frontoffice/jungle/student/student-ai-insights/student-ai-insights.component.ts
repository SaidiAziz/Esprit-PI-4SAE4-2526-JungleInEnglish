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
        <div class="insights-hero-copy">
          <p class="fd-eyebrow">My Progress</p>
          <h1>See how the app evaluates your current level</h1>
          <p class="fd-muted">This page shows the score records the AI uses to understand your grammar, listening, and speaking level. Manual entry is only for demo/testing until real assessments are connected.</p>
        </div>
        <div class="insights-hero-stats" *ngIf="summary">
          <div class="insights-hero-stat">
            <span>Level</span>
            <strong>{{ summary.currentLevel }}</strong>
          </div>
          <div class="insights-hero-stat">
            <span>Weakest skill</span>
            <strong>{{ summary.weakestSkill }}</strong>
          </div>
          <div class="insights-hero-stat">
            <span>Analyses</span>
            <strong>{{ summary.totalAnalyses }}</strong>
          </div>
        </div>
      </header>

      <section class="insights-summary-strip" *ngIf="summary">
        <article class="insights-summary-item">
          <span>Overall average</span>
          <strong>{{ summary.overallAverage }}%</strong>
        </article>
        <article class="insights-summary-item">
          <span>Tracked analyses</span>
          <strong>{{ summary.totalAnalyses }}</strong>
        </article>
        <article class="insights-summary-item">
          <span>Priority skill</span>
          <strong>{{ formatSkill(summary.weakestSkill) }}</strong>
        </article>
      </section>

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
                  {{ course.title }} - {{ course.level }}
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
            <div class="insight-record__footer">
              <span>Updated {{ analysis.lastUpdated | date: 'medium' }}</span>
              <span>{{ analysis.level }}</span>
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
                {{ course.title }} - {{ course.level }}
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
  `,
  styles: [`
    .insights-hero-copy {
      max-width: 48rem;
    }

    .insights-hero-stats {
      display: grid;
      grid-template-columns: repeat(3, minmax(100px, 1fr));
      gap: 0.75rem;
      min-width: 320px;
    }

    .insights-hero-stat {
      padding: 0.9rem 1rem;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(212, 196, 184, 0.84);
      text-align: center;
    }

    .insights-hero-stat span {
      display: block;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #61717d;
    }

    .insights-hero-stat strong {
      display: block;
      margin-top: 0.2rem;
      font-size: 1.05rem;
      color: #0f172a;
    }

    .fd-surface-card {
      padding: 1rem !important;
    }

    .insights-summary-strip {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0;
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      overflow: hidden;
      background: #fffdfa;
    }

    .insights-summary-item {
      padding: 0.95rem 1rem;
      border-right: 1px solid rgba(212, 196, 184, 0.62);
    }

    .insights-summary-item:last-child {
      border-right: 0;
    }

    .insights-summary-item span {
      display: block;
      margin-bottom: 0.3rem;
      color: #61717d;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .insights-summary-item strong {
      display: block;
      color: #0f172a;
      font-size: 1.1rem;
      line-height: 1.3;
      overflow-wrap: anywhere;
    }

    .fd-form-grid {
      gap: 0.9rem;
    }

    .fd-analytics-list {
      display: grid;
      gap: 0.85rem;
    }

    .fd-analytics-list div {
      padding: 0.95rem 1rem;
      border-radius: 12px;
      border: 1px solid rgba(212, 196, 184, 0.8);
      background: linear-gradient(180deg, #fffdf9, #f8f0e8);
    }

    .fd-analytics-list span {
      display: block;
      margin-bottom: 0.3rem;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.07em;
      color: #7a6859;
    }

    .fd-analytics-list strong {
      color: #0f172a;
      line-height: 1.45;
    }

    .fd-insight-list {
      display: grid;
      gap: 1rem;
    }

    .fd-insight-item {
      padding: 1rem;
      border-radius: 14px;
      border: 1px solid rgba(212, 196, 184, 0.78);
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .fd-insight-title {
      display: grid;
      gap: 0.2rem;
    }

    .fd-insight-title strong {
      color: #0f172a;
      font-size: 1rem;
    }

    .fd-insight-title span {
      color: #61717d;
      font-size: 0.85rem;
    }

    .fd-progress-stack {
      display: grid;
      gap: 0.45rem;
      margin-top: 0.9rem;
    }

    .fd-progress-stack label {
      color: #43515b;
      font-size: 0.83rem;
      font-weight: 600;
    }

    .insight-record__footer {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
      margin-top: 0.95rem;
      color: #61717d;
      font-size: 0.82rem;
    }

    @media (max-width: 900px) {
      .insights-hero-stats {
        min-width: 0;
        width: 100%;
      }
    }

    @media (max-width: 640px) {
      .insights-hero-stats {
        grid-template-columns: 1fr;
      }

      .insights-summary-strip {
        grid-template-columns: 1fr;
      }

      .insights-summary-item {
        border-right: 0;
        border-bottom: 1px solid rgba(212, 196, 184, 0.62);
      }

      .insights-summary-item:last-child {
        border-bottom: 0;
      }
    }
  `]
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
    return `${course.title} - ${course.level}`;
  }

  formatSkill(value: string | null | undefined): string {
    if (!value) {
      return 'None';
    }
    return value
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}
