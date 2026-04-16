import { Component, inject } from '@angular/core';
import { Room, RoomAnalytics } from '../../../../core/models/collaboration.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-room-analytics',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Advanced Metrics</p>
          <h1>Room analytics</h1>
          <p class="fd-muted">Select a room to view its detailed statistics.</p>
        </div>
      </header>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>Select a room</h2>
            <p>Choose any of your rooms from the list below.</p>
          </div>
        </div>

        <mat-form-field appearance="outline" class="fd-filter-field">
          <mat-label>Room</mat-label>
          <mat-select [(ngModel)]="selectedRoomId" name="selectedRoom" (ngModelChange)="onRoomChange($event)">
            <mat-option *ngFor="let room of rooms" [value]="room.id">
              {{ room.title }} ({{ room.type }} · {{ room.level }})
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
        </ng-container>
      </mat-card>
    </section>
  `
})
export class TutorRoomAnalyticsComponent {
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
