import { Component, inject } from '@angular/core';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-challenge-studio',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Challenge Studio</p>
          <h1>Shape the room rhythm with smart prompts</h1>
          <p class="fd-muted">Review the current challenge lineup and use the backend generator when you need a level-appropriate prompt.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Current room challenges</h2>
            <p>These come from the collaboration backend.</p>
          </div>
          <mat-accordion>
            <mat-expansion-panel *ngFor="let challenge of challenges$ | async">
              <mat-expansion-panel-header>
                <mat-panel-title>{{ challenge.type }}</mat-panel-title>
                <mat-panel-description>{{ challenge.status }} • {{ challenge.difficulty }}</mat-panel-description>
              </mat-expansion-panel-header>
              <p>{{ challenge.prompt }}</p>
            </mat-expansion-panel>
          </mat-accordion>
        </mat-card>

        <mat-card class="fd-surface-card fd-action-card">
          <div class="fd-section-head">
            <h2>Generator guidance</h2>
            <p>Use generated prompts when you want the room level to dictate the activity automatically.</p>
          </div>
          <ul class="fd-step-list">
            <li>Beginner rooms prefer fill-in-the-blank prompts.</li>
            <li>Intermediate rooms favor fast translation races.</li>
            <li>Advanced rooms work best with story-building prompts.</li>
          </ul>
          <button mat-flat-button color="primary">Generate from backend</button>
        </mat-card>
      </div>
    </section>
  `
})
export class TutorChallengeStudioComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly challenges$ = this.collaborationService.getRoomChallenges(1);
}
