import { Component, inject } from '@angular/core';
import { Room, RoomAnalytics } from '../../../../core/models/collaboration.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-analytics',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Room Analytics</p>
          <h1>See whether collaboration is producing useful language practice</h1>
          <p class="fd-muted">Select a room to view its statistics: activity volume, response speed, correction quality, and challenge completion.</p>
        </div>
      </header>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>Select a room</h2>
            <p>Choose a room to analyse from the list below.</p>
          </div>
        </div>

        <mat-form-field appearance="outline" class="fd-filter-field">
          <mat-label>Room</mat-label>
          <mat-select [(ngModel)]="selectedRoomId" name="selectedRoom" (ngModelChange)="onRoomChange($event)">
            <mat-option *ngFor="let room of rooms" [value]="room.id">
              {{ room.title }} — {{ room.type }} · {{ room.level }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <div class="fd-empty-state" *ngIf="!selectedRoomId">
          No room selected. Pick one from the dropdown above.
        </div>

        <ng-container *ngIf="analytics && selectedRoomId">
          <div class="fd-grid fd-grid--four" style="margin-top: 1.5rem;">
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Total messages</span>
              <strong>{{ analytics.totalMessages }}</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Avg response time</span>
              <strong>{{ analytics.avgResponseTimeMinutes }} min</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Correction acceptance</span>
              <strong>{{ analytics.correctionsAcceptanceRate }}%</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Challenge completion</span>
              <strong>{{ analytics.challengeCompletionRate }}%</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Top contributor</span>
              <strong>{{ analytics.topContributorUserId ? 'User #' + analytics.topContributorUserId : '—' }}</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Most active language</span>
              <strong>{{ analytics.mostActiveLanguage ?? '—' }}</strong>
            </mat-card>
            <mat-card class="fd-metric-card">
              <span class="fd-metric-label">Peak hour</span>
              <strong>{{ analytics.peakHour != null ? analytics.peakHour + ':00' : '—' }}</strong>
            </mat-card>
          </div>

          <div class="fd-grid fd-grid--two" style="margin-top: 1.5rem;">
            <mat-card class="fd-surface-card">
              <div class="fd-section-head">
                <h2>Interpretation guide</h2>
                <p>Simple thresholds so tutors know when to intervene.</p>
              </div>
              <div class="fd-room-stack">
                <article class="fd-room-row">
                  <div><strong>Low response speed</strong><p>Above 5 min usually means the room lacks momentum or a clear prompt.</p></div>
                </article>
                <article class="fd-room-row">
                  <div><strong>Weak acceptance rate</strong><p>If corrections are ignored, challenge format or correction quality should be revised.</p></div>
                </article>
                <article class="fd-room-row">
                  <div><strong>Low completion</strong><p>Reduce challenge scope when completion drops below 50%.</p></div>
                </article>
              </div>
            </mat-card>
          </div>
        </ng-container>
      </mat-card>
    </section>
  `
})
export class TutorAnalyticsComponent {
  private readonly collaborationService = inject(CollaborationService);

  rooms: Room[] = [];
  analytics: RoomAnalytics | null = null;
  selectedRoomId: number | null = null;

  constructor() {
    this.collaborationService.getMyRooms('TUTOR').subscribe(rooms => {
      this.rooms = rooms;
    });
  }

  onRoomChange(roomId: number): void {
    this.selectedRoomId = roomId;
    this.analytics = null;
    this.collaborationService.getAnalytics(roomId).subscribe(analytics => {
      this.analytics = analytics;
    });
  }
}
