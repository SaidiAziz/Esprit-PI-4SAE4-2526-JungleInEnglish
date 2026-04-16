import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { map, switchMap, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-dashboard',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS, RouterLink],
  template: `
    <section class="fd-page-shell" *ngIf="vm$ | async as vm">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Tutor Dashboard</p>
          <h1>Welcome back, {{ vm.user?.firstName }}</h1>
          <p class="fd-muted">Simple flow: create a room, add a challenge, then watch activity and corrections through analytics.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--four">
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Visible rooms</span><strong>{{ vm.rooms.length }}</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Open challenges</span><strong>{{ vm.challengeCount }}</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Top room messages</span><strong>{{ vm.analytics.totalMessages }}</strong></mat-card>
        <mat-card class="fd-metric-card"><span class="fd-metric-label">Peak hour</span><strong>{{ vm.analytics.peakHour }}h</strong></mat-card>
      </div>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Tutor Workflow</h2>
          </div>
          <div class="fd-analytics-list">
            <div><span>1. Create a room</span><strong>Set topic, level, type, and participant limit</strong></div>
            <div><span>2. Add a challenge</span><strong>Use the challenge page to create or auto-generate practice</strong></div>
            <div><span>3. Monitor the room</span><strong>Watch messages, corrections, and completion rates</strong></div>
          </div>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Your Rooms</h2>
            <a mat-button routerLink="/tutor/rooms">Open rooms</a>
          </div>
          <div class="fd-room-stack">
            <article class="fd-room-row" *ngFor="let room of vm.rooms.slice(0, 3)">
              <div>
                <strong>{{ room.title }}</strong>
                <p>{{ room.topic }}</p>
              </div>
              <div class="fd-room-meta">
                <span>{{ room.type }}</span>
                <span>{{ room.status }}</span>
              </div>
            </article>
          </div>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <h2>Room Snapshot</h2>
            <a mat-button routerLink="/tutor/analytics">Deep dive</a>
          </div>
          <div class="fd-analytics-list">
            <div><span>Top contributor</span><strong>#{{ vm.analytics.topContributorUserId }}</strong></div>
            <div><span>Most active language</span><strong>{{ vm.analytics.mostActiveLanguage }}</strong></div>
            <div><span>Correction acceptance</span><strong>{{ vm.analytics.correctionsAcceptanceRate }}%</strong></div>
            <div><span>Challenge completion</span><strong>{{ vm.analytics.challengeCompletionRate }}%</strong></div>
          </div>
        </mat-card>
      </div>
    </section>
  `
})
export class TutorDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly collaborationService = inject(CollaborationService);

  readonly vm$ = this.collaborationService.getMyRooms('TUTOR').pipe(
    switchMap(rooms => {
      const primaryRoomId = rooms[0]?.id;
      if (!primaryRoomId) {
        return of({
          user: this.authService.getCurrentUser(),
          rooms,
          analytics: {
            roomId: 0,
            totalMessages: 0,
            avgResponseTimeMinutes: 0,
            topContributorUserId: null,
            mostActiveLanguage: null,
            correctionsAcceptanceRate: 0,
            challengeCompletionRate: 0,
            peakHour: null
          },
          challengeCount: 0
        });
      }

      return this.collaborationService.getAnalytics(primaryRoomId).pipe(
        switchMap(analytics =>
          this.collaborationService.getRoomChallenges(primaryRoomId).pipe(
            map(challenges => ({
              user: this.authService.getCurrentUser(),
              rooms,
              analytics,
              challengeCount: challenges.length
            }))
          )
        )
      );
    }),
    map(vm => ({
      user: this.authService.getCurrentUser(),
      rooms: vm.rooms,
      analytics: vm.analytics,
      challengeCount: vm.challengeCount
    }))
  );
}
