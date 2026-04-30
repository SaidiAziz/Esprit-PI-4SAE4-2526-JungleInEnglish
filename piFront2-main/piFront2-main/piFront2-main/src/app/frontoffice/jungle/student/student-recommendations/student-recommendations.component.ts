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
      <header class="fd-page-hero recommendations-hero">
        <div class="recommendations-hero__copy">
          <p class="fd-eyebrow">Recommendations</p>
          <h1>See what the app suggests you should focus on next</h1>
          <p class="fd-muted">Use this page after new scores or room activity to refresh your next study direction.</p>
        </div>
        <button mat-flat-button color="primary" type="button" (click)="regenerate()">Refresh suggestions</button>
      </header>

      <section class="recommendations-summary" *ngIf="recommendations.length">
        <article class="recommendations-summary__item">
          <span>Suggestions</span>
          <strong>{{ recommendations.length }}</strong>
        </article>
        <article class="recommendations-summary__item">
          <span>Top focus</span>
          <strong>{{ primarySkill }}</strong>
        </article>
        <article class="recommendations-summary__item">
          <span>Main type</span>
          <strong>{{ primaryType }}</strong>
        </article>
      </section>

      <section class="recommendations-panel">
        <div class="recommendations-panel__head">
          <div>
            <h2>Your next study suggestions</h2>
            <p>The app chooses these suggestions from your weakest skill and current level.</p>
          </div>
          <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
        </div>

        <div class="recommendations-list" *ngIf="recommendations.length; else emptyState">
          <article class="recommendation-row" *ngFor="let rec of recommendations">
            <div class="recommendation-row__main">
              <div class="recommendation-row__top">
                <strong>{{ rec.contentTitle || (formatLabel(rec.type) + ' suggestion') }}</strong>
                <div class="recommendation-row__tags">
                  <span class="recommendation-tag">{{ formatLabel(rec.type) }}</span>
                  <span class="recommendation-tag recommendation-tag--accent">{{ formatLabel(rec.focusSkill) }}</span>
                  <span class="recommendation-tag" *ngIf="rec.contentLevel">{{ rec.contentLevel }}</span>
                </div>
              </div>
              <p *ngIf="rec.contentDescription" class="recommendation-row__description">{{ rec.contentDescription }}</p>
              <p class="recommendation-row__reason">{{ rec.reason }}</p>
            </div>

            <div class="recommendation-row__side">
              <span class="recommendation-price" *ngIf="rec.contentPrice !== null && rec.contentPrice !== undefined">
                {{ rec.contentPrice === 0 ? 'Free' : ('Price: ' + rec.contentPrice) }}
              </span>
              <span class="recommendation-price" *ngIf="rec.contentPrice === null || rec.contentPrice === undefined">
                Suggested next step
              </span>
            </div>
          </article>
        </div>
      </section>

      <ng-template #emptyState>
        <div class="fd-empty-state">No suggestions yet. Click "Refresh suggestions" to generate them.</div>
      </ng-template>
    </section>
  `,
  styles: [`
    .recommendations-hero {
      align-items: center;
    }

    .recommendations-hero__copy {
      max-width: 50rem;
    }

    .recommendations-summary {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0;
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      overflow: hidden;
      background: #fffdfa;
    }

    .recommendations-summary__item {
      padding: 0.95rem 1rem;
      border-right: 1px solid rgba(212, 196, 184, 0.62);
    }

    .recommendations-summary__item:last-child {
      border-right: 0;
    }

    .recommendations-summary__item span {
      display: block;
      margin-bottom: 0.3rem;
      color: #61717d;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .recommendations-summary__item strong {
      display: block;
      color: #0f172a;
      font-size: 1.05rem;
      line-height: 1.3;
      overflow-wrap: anywhere;
    }

    .recommendations-panel {
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      background: #fffdfa;
      padding: 1rem;
    }

    .recommendations-panel__head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.9rem;
    }

    .recommendations-panel__head h2 {
      margin: 0;
      color: #0f172a;
    }

    .recommendations-panel__head p {
      margin: 0.25rem 0 0;
      color: #61717d;
      line-height: 1.45;
    }

    .recommendations-list {
      display: grid;
      gap: 0.75rem;
    }

    .recommendation-row {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 1rem;
      align-items: start;
      padding: 0.95rem 1rem;
      border: 1px solid rgba(212, 196, 184, 0.68);
      border-radius: 10px;
      background: #ffffff;
    }

    .recommendation-row__main {
      min-width: 0;
    }

    .recommendation-row__top {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.45rem;
    }

    .recommendation-row__top strong {
      color: #0f172a;
      font-size: 0.98rem;
      line-height: 1.35;
    }

    .recommendation-row__tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.4rem;
      justify-content: flex-end;
    }

    .recommendation-tag {
      display: inline-flex;
      align-items: center;
      min-height: 28px;
      padding: 0.18rem 0.6rem;
      border-radius: 999px;
      border: 1px solid rgba(212, 196, 184, 0.72);
      background: #f8f0e8;
      color: #7a6859;
      font-size: 0.72rem;
      font-weight: 700;
    }

    .recommendation-tag--accent {
      background: #fff3d8;
      color: #8a5a12;
    }

    .recommendation-row__description,
    .recommendation-row__reason {
      margin: 0;
      color: #61717d;
      line-height: 1.5;
    }

    .recommendation-row__description {
      margin-bottom: 0.35rem;
    }

    .recommendation-row__side {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      min-width: 120px;
    }

    .recommendation-price {
      display: inline-flex;
      align-items: center;
      min-height: 32px;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      background: #f7efe6;
      color: #7a6859;
      font-size: 0.76rem;
      font-weight: 700;
      text-align: center;
    }

    @media (max-width: 900px) {
      .recommendation-row,
      .recommendation-row__top {
        grid-template-columns: 1fr;
      }

      .recommendation-row__tags,
      .recommendation-row__side {
        justify-content: flex-start;
      }
    }

    @media (max-width: 640px) {
      .recommendations-summary {
        grid-template-columns: 1fr;
      }

      .recommendations-summary__item {
        border-right: 0;
        border-bottom: 1px solid rgba(212, 196, 184, 0.62);
      }

      .recommendations-summary__item:last-child {
        border-bottom: 0;
      }
    }
  `]
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

  get primarySkill(): string {
    return this.recommendations.length ? this.formatLabel(this.recommendations[0].focusSkill) : 'None';
  }

  get primaryType(): string {
    return this.recommendations.length ? this.formatLabel(this.recommendations[0].type) : 'None';
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
