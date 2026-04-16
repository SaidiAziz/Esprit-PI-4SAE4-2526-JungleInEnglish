import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { combineLatest, map } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { AiAssistantService } from '../../../../core/services/ai-assistant.service';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS, RouterLink],
  template: `
    <section class="fd-page-shell" *ngIf="vm$ | async as vm">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Student Dashboard</p>
          <h1>Welcome back, {{ vm.user?.firstName }}</h1>
          <p class="fd-muted">Simple flow: check what to study next, join a practice room, then track how your level improves.</p>
        </div>
        <mat-chip-set>
          <mat-chip>{{ vm.summary.currentLevel }}</mat-chip>
          <mat-chip highlighted>{{ vm.summary.weakestSkill }} priority</mat-chip>
        </mat-chip-set>
      </header>

      <div class="fd-grid fd-grid--four">
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Average score</span><strong>{{ vm.summary.overallAverage }}%</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Recommendations</span><strong>{{ vm.summary.recommendations.length }}</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Learning paths</span><strong>{{ vm.summary.learningPaths.length }}</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Badges earned</span><strong>{{ vm.badges.length }}</strong></mat-card>
      </div>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>What To Do Next</h2>
          </div>
          <div class="fd-analytics-list">
            <div><span>1. Read your recommendation</span><strong>Open the recommendations page and see your next study focus</strong></div>
            <div><span>2. Practice in a room</span><strong>Join a room and send messages or answer a challenge</strong></div>
            <div><span>3. Check progress</span><strong>Return to My Progress and Study Plan to see your next route</strong></div>
          </div>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Next AI moves</h2>
            <a mat-button routerLink="/student/recommendations">See all</a>
          </div>
          <div class="fd-room-stack">
            <article class="fd-room-row" *ngFor="let recommendation of vm.summary.recommendations.slice(0, 3)">
              <div>
                <strong>{{ recommendation.type }}</strong>
                <p>{{ recommendation.reason }}</p>
              </div>
              <span class="fd-pill">{{ recommendation.focusSkill }}</span>
            </article>
          </div>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Recommended Practice Room</h2>
            <a mat-button routerLink="/student/collaboration">Open rooms</a>
          </div>
          <article class="fd-highlight-panel" *ngIf="vm.matches[0] as match">
            <h3>{{ match.room.title }}</h3>
            <p>{{ match.recommendationReason }}</p>
            <div class="fd-chip-row">
              <mat-chip>{{ match.room.type }}</mat-chip>
              <mat-chip>{{ match.room.level }}</mat-chip>
            </div>
            <a mat-flat-button color="primary" [routerLink]="['/student/collaboration/room', match.room.id]">Open room</a>
          </article>
        </mat-card>
      </div>
    </section>
  `
})
export class StudentDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly aiAssistantService = inject(AiAssistantService);
  private readonly collaborationService = inject(CollaborationService);

  readonly vm$ = combineLatest([
    this.aiAssistantService.getSummary(),
    this.collaborationService.getRecommendations(),
    this.collaborationService.getMyBadges()
  ]).pipe(
    map(([summary, matches, badges]) => ({
      user: this.authService.getCurrentUser(),
      summary,
      matches,
      badges
    }))
  );
}
