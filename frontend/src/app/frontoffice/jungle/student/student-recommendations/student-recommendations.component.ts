import { Component, inject } from '@angular/core';
import { Recommendation } from '../../../../core/models/ai.model';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-recommendations',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Recommendations</p>
          <h1>See what the app suggests you should focus on next</h1>
          <p class="fd-muted">Use this page after new scores or room activity to refresh your next study direction.</p>
        </div>
        <button mat-flat-button color="primary" type="button" (click)="regenerate()">Refresh suggestions</button>
      </header>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>Your next study suggestions</h2>
            <p>The app chooses these suggestions from your weakest skill and current level.</p>
          </div>
          <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
        </div>
        <div class="fd-grid fd-grid--three" *ngIf="recommendations.length; else emptyState">
          <mat-card class="fd-surface-card fd-action-card" *ngFor="let rec of recommendations">
            <div class="fd-chip-row">
              <mat-chip>{{ rec.type }}</mat-chip>
              <mat-chip highlighted>{{ rec.focusSkill }}</mat-chip>
              <mat-chip *ngIf="rec.contentLevel">{{ rec.contentLevel }}</mat-chip>
            </div>
            <h2>{{ rec.contentTitle || (rec.type + ' suggestion') }}</h2>
            <p *ngIf="rec.contentDescription" class="fd-muted">{{ rec.contentDescription }}</p>
            <p>{{ rec.reason }}</p>
            <p *ngIf="rec.contentPrice !== null && rec.contentPrice !== undefined" class="fd-muted">
              {{ rec.contentPrice === 0 ? 'Free course' : ('Price: ' + rec.contentPrice) }}
            </p>
          </mat-card>
        </div>
      </mat-card>

      <ng-template #emptyState>
        <div class="fd-empty-state">No suggestions yet. Click "Refresh suggestions" to generate them.</div>
      </ng-template>
    </section>
  `
})
export class StudentRecommendationsComponent {
  private readonly aiAssistantService = inject(AiAssistantService);

  recommendations: Recommendation[] = [];
  feedback = '';

  constructor() {
    this.loadRecommendations();
  }

  loadRecommendations(): void {
    this.aiAssistantService.getRecommendations().subscribe(recommendations => this.recommendations = recommendations);
  }

  regenerate(): void {
    this.aiAssistantService.generateRecommendations().subscribe(recommendations => {
      this.recommendations = recommendations;
      this.feedback = 'Suggestions updated.';
    });
  }
}
