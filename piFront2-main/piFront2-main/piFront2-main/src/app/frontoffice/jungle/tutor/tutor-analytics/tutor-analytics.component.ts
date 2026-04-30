import { Component, inject } from '@angular/core';
import { Room, RoomAnalytics } from '../../../../core/models/collaboration.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-analytics',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell analytics-shell">
      <header class="fd-page-hero analytics-hero">
        <div class="analytics-hero-copy">
          <p class="fd-eyebrow">Room Analytics</p>
          <h1>Track room activity and intervention points</h1>
          <p class="fd-muted">Review how each collaboration room is performing, then decide whether the tutor should push activity, improve correction quality, or simplify challenges.</p>
        </div>
        <div class="analytics-hero-strip">
          <div class="analytics-hero-strip__item">
            <span>Rooms</span>
            <strong>{{ rooms.length }}</strong>
          </div>
          <div class="analytics-hero-strip__item">
            <span>Selected</span>
            <strong>{{ selectedRoomLabel }}</strong>
          </div>
          <div class="analytics-hero-strip__item">
            <span>Messages</span>
            <strong>{{ analytics?.totalMessages ?? 0 }}</strong>
          </div>
        </div>
      </header>

      <div class="analytics-layout">
        <aside class="analytics-sidebar">
          <section class="analytics-panel">
            <div class="analytics-panel__head">
              <div>
                <h2>Rooms</h2>
                <p>Select one room to inspect.</p>
              </div>
            </div>

            <mat-form-field appearance="outline" class="analytics-filter-field">
              <mat-label>Room</mat-label>
              <mat-select [(ngModel)]="selectedRoomId" name="selectedRoom" (ngModelChange)="onRoomChange($event)">
                <mat-option *ngFor="let room of rooms" [value]="room.id">
                  {{ room.title }} - {{ room.type }} - {{ room.level }}
                </mat-option>
              </mat-select>
            </mat-form-field>

            <div class="analytics-room-list" *ngIf="rooms.length; else noRooms">
              <button
                class="analytics-room-item"
                type="button"
                *ngFor="let room of rooms"
                [class.analytics-room-item--active]="room.id === selectedRoomId"
                (click)="onRoomChange(room.id)">
                <div class="analytics-room-item__top">
                  <strong>{{ room.title }}</strong>
                  <span class="analytics-room-status">{{ room.status }}</span>
                </div>
                <p>{{ room.topic }}</p>
                <div class="analytics-room-item__meta">
                  <span>{{ room.type }}</span>
                  <span>{{ room.level }}</span>
                  <span>{{ room.maxParticipants }} seats</span>
                </div>
              </button>
            </div>
          </section>
        </aside>

        <div class="analytics-main">
          <section class="analytics-panel" *ngIf="selectedRoom as room">
            <div class="analytics-panel__head">
              <div>
                <h2>{{ room.title }}</h2>
                <p>{{ room.topic }}</p>
              </div>
              <div class="analytics-room-summary">
                <span>{{ room.nativeLanguage }} -> {{ room.targetLanguage }}</span>
                <span>{{ room.type }}</span>
                <span>{{ room.level }}</span>
              </div>
            </div>

            <div class="analytics-empty" *ngIf="!analytics">
              Loading analytics...
            </div>

            <ng-container *ngIf="analytics">
              <div class="analytics-metric-grid">
                <article class="analytics-metric">
                  <span>Total messages</span>
                  <strong>{{ analytics.totalMessages }}</strong>
                </article>
                <article class="analytics-metric">
                  <span>Avg response time</span>
                  <strong>{{ analytics.avgResponseTimeMinutes }} min</strong>
                </article>
                <article class="analytics-metric">
                  <span>Correction acceptance</span>
                  <strong>{{ analytics.correctionsAcceptanceRate }}%</strong>
                </article>
                <article class="analytics-metric">
                  <span>Challenge completion</span>
                  <strong>{{ analytics.challengeCompletionRate }}%</strong>
                </article>
                <article class="analytics-metric">
                  <span>Top contributor</span>
                  <strong>{{ analytics.topContributorUserId ? 'User #' + analytics.topContributorUserId : 'None' }}</strong>
                </article>
                <article class="analytics-metric">
                  <span>Most active language</span>
                  <strong>{{ analytics.mostActiveLanguage ?? 'None' }}</strong>
                </article>
                <article class="analytics-metric">
                  <span>Peak hour</span>
                  <strong>{{ analytics.peakHour != null ? analytics.peakHour + ':00' : 'None' }}</strong>
                </article>
              </div>

              <div class="analytics-detail-grid">
                <section class="analytics-subpanel">
                  <div class="analytics-subpanel__head">
                    <h3>Operational readout</h3>
                  </div>
                  <div class="analytics-summary-list">
                    <div class="analytics-summary-row">
                      <span>Message intensity</span>
                      <strong>{{ analytics.totalMessages >= 20 ? 'Strong activity' : 'Needs more activity' }}</strong>
                    </div>
                    <div class="analytics-summary-row">
                      <span>Correction quality</span>
                      <strong>{{ analytics.correctionsAcceptanceRate >= 60 ? 'Working' : 'Needs revision' }}</strong>
                    </div>
                    <div class="analytics-summary-row">
                      <span>Challenge format</span>
                      <strong>{{ analytics.challengeCompletionRate >= 50 ? 'Working' : 'Too difficult or too long' }}</strong>
                    </div>
                  </div>
                </section>

                <section class="analytics-subpanel">
                  <div class="analytics-subpanel__head">
                    <h3>Recommended action</h3>
                  </div>
                  <div class="analytics-action-block">
                    <strong>{{ recommendedActionTitle }}</strong>
                    <p>{{ recommendedActionText }}</p>
                  </div>
                </section>
              </div>
            </ng-container>
          </section>
        </div>
      </div>

      <ng-template #noRooms>
        <div class="analytics-empty">
          No tutor room exists yet. Create one first from the rooms page.
        </div>
      </ng-template>
    </section>
  `,
  styles: [`
    .analytics-shell {
      gap: 1rem;
    }

    .analytics-hero {
      display: grid;
      gap: 1rem;
      align-items: start;
      padding: 1.1rem 1.2rem;
    }

    .analytics-hero-copy {
      max-width: 52rem;
    }

    .analytics-hero-strip {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 0;
      border: 1px solid rgba(212, 196, 184, 0.75);
      border-radius: 12px;
      overflow: hidden;
      background: rgba(255, 255, 255, 0.8);
    }

    .analytics-hero-strip__item {
      padding: 0.85rem 1rem;
      border-right: 1px solid rgba(212, 196, 184, 0.75);
    }

    .analytics-hero-strip__item:last-child {
      border-right: 0;
    }

    .analytics-hero-strip__item span {
      display: block;
      margin-bottom: 0.2rem;
      font-size: 0.73rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #61717d;
    }

    .analytics-hero-strip__item strong {
      display: block;
      color: #0f172a;
      font-size: 1rem;
      overflow-wrap: anywhere;
    }

    .analytics-layout {
      display: grid;
      grid-template-columns: 320px minmax(0, 1fr);
      gap: 1rem;
      align-items: start;
    }

    .analytics-sidebar,
    .analytics-main {
      min-width: 0;
    }

    .analytics-panel,
    .analytics-subpanel {
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 12px;
      background: #fffdfa;
    }

    .analytics-panel {
      padding: 1rem;
    }

    .analytics-panel__head,
    .analytics-subpanel__head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 0.9rem;
    }

    .analytics-panel__head h2,
    .analytics-subpanel__head h3 {
      margin: 0;
      color: #0f172a;
    }

    .analytics-panel__head p {
      margin: 0.25rem 0 0;
      color: #61717d;
      line-height: 1.45;
    }

    .analytics-room-summary {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      justify-content: flex-end;
      color: #61717d;
      font-size: 0.8rem;
    }

    .analytics-room-summary span {
      padding: 0.28rem 0.55rem;
      border-radius: 999px;
      background: #f8f0e8;
      border: 1px solid rgba(212, 196, 184, 0.72);
    }

    .analytics-filter-field {
      width: 100%;
      margin-bottom: 0.8rem;
    }

    .analytics-room-list {
      display: grid;
      gap: 0.65rem;
    }

    .analytics-room-item {
      width: 100%;
      padding: 0.85rem 0.9rem;
      border: 1px solid rgba(212, 196, 184, 0.72);
      border-radius: 10px;
      background: #ffffff;
      text-align: left;
      cursor: pointer;
      transition: border-color 0.16s ease, background 0.16s ease;
    }

    .analytics-room-item:hover,
    .analytics-room-item--active {
      border-color: rgba(183, 121, 31, 0.45);
      background: #fff8ef;
    }

    .analytics-room-item__top,
    .analytics-room-item__meta {
      display: flex;
      justify-content: space-between;
      gap: 0.5rem;
      flex-wrap: wrap;
      align-items: center;
    }

    .analytics-room-item__top strong {
      color: #0f172a;
      font-size: 0.95rem;
    }

    .analytics-room-item p {
      margin: 0.35rem 0 0.45rem;
      color: #61717d;
      font-size: 0.84rem;
      line-height: 1.45;
    }

    .analytics-room-item__meta {
      color: #61717d;
      font-size: 0.76rem;
    }

    .analytics-room-status {
      padding: 0.2rem 0.5rem;
      border-radius: 999px;
      background: #f4eadf;
      color: #7a6859;
      font-size: 0.72rem;
      font-weight: 700;
    }

    .analytics-metric-grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 0;
      border: 1px solid rgba(212, 196, 184, 0.72);
      border-radius: 12px;
      overflow: hidden;
      background: #ffffff;
    }

    .analytics-metric {
      padding: 0.95rem 1rem;
      border-right: 1px solid rgba(212, 196, 184, 0.55);
      border-bottom: 1px solid rgba(212, 196, 184, 0.55);
      min-height: 92px;
    }

    .analytics-metric:nth-child(4n) {
      border-right: 0;
    }

    .analytics-metric span {
      display: block;
      margin-bottom: 0.3rem;
      color: #61717d;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .analytics-metric strong {
      color: #0f172a;
      font-size: 1.15rem;
      line-height: 1.3;
      overflow-wrap: anywhere;
    }

    .analytics-detail-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 1rem;
      margin-top: 1rem;
    }

    .analytics-subpanel {
      padding: 1rem;
    }

    .analytics-summary-list {
      display: grid;
      gap: 0.7rem;
    }

    .analytics-summary-row {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.75rem 0;
      border-bottom: 1px solid rgba(212, 196, 184, 0.55);
      align-items: start;
    }

    .analytics-summary-row:last-child {
      border-bottom: 0;
      padding-bottom: 0;
    }

    .analytics-summary-row span {
      color: #61717d;
      font-size: 0.82rem;
    }

    .analytics-summary-row strong {
      color: #0f172a;
      font-size: 0.9rem;
      text-align: right;
    }

    .analytics-action-block {
      padding: 0.85rem 0.9rem;
      border-radius: 10px;
      background: #f8f0e8;
      border: 1px solid rgba(212, 196, 184, 0.72);
    }

    .analytics-action-block strong {
      display: block;
      margin-bottom: 0.35rem;
      color: #0f172a;
    }

    .analytics-action-block p {
      margin: 0;
      color: #61717d;
      line-height: 1.5;
    }

    .analytics-empty {
      padding: 0.95rem 1rem;
      border: 1px dashed rgba(212, 196, 184, 0.85);
      border-radius: 10px;
      color: #61717d;
      background: #fffdfa;
    }

    @media (max-width: 1080px) {
      .analytics-layout {
        grid-template-columns: 1fr;
      }

      .analytics-metric-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .analytics-metric:nth-child(4n) {
        border-right: 1px solid rgba(212, 196, 184, 0.55);
      }

      .analytics-metric:nth-child(2n) {
        border-right: 0;
      }

      .analytics-detail-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 680px) {
      .analytics-hero-strip {
        grid-template-columns: 1fr;
      }

      .analytics-hero-strip__item {
        border-right: 0;
        border-bottom: 1px solid rgba(212, 196, 184, 0.75);
      }

      .analytics-hero-strip__item:last-child {
        border-bottom: 0;
      }

      .analytics-metric-grid {
        grid-template-columns: 1fr;
      }

      .analytics-metric,
      .analytics-metric:nth-child(2n),
      .analytics-metric:nth-child(4n) {
        border-right: 0;
      }

      .analytics-panel__head,
      .analytics-room-item__top,
      .analytics-summary-row {
        flex-direction: column;
        align-items: flex-start;
      }

      .analytics-summary-row strong {
        text-align: left;
      }
    }
  `]
})
export class TutorAnalyticsComponent {
  private readonly collaborationService = inject(CollaborationService);

  rooms: Room[] = [];
  analytics: RoomAnalytics | null = null;
  selectedRoomId: number | null = null;

  constructor() {
    this.collaborationService.getMyRooms('TUTOR').subscribe(rooms => {
      this.rooms = rooms;
      if (rooms.length && !this.selectedRoomId) {
        this.onRoomChange(rooms[0].id);
      }
    });
  }

  onRoomChange(roomId: number): void {
    this.selectedRoomId = roomId;
    this.analytics = null;
    this.collaborationService.getAnalytics(roomId).subscribe(analytics => {
      this.analytics = analytics;
    });
  }

  get selectedRoom(): Room | undefined {
    return this.rooms.find(item => item.id === this.selectedRoomId);
  }

  get selectedRoomLabel(): string {
    if (!this.selectedRoomId) {
      return 'None';
    }
    const room = this.selectedRoom;
    return room?.title ?? `#${this.selectedRoomId}`;
  }

  get recommendedActionTitle(): string {
    if (!this.analytics) {
      return 'Waiting for analytics';
    }
    if (this.analytics.avgResponseTimeMinutes > 5) {
      return 'Room needs a stronger tutor prompt';
    }
    if (this.analytics.correctionsAcceptanceRate < 60) {
      return 'Review how corrections are being given';
    }
    if (this.analytics.challengeCompletionRate < 50) {
      return 'Reduce challenge scope or difficulty';
    }
    return 'Room is operating well';
  }

  get recommendedActionText(): string {
    if (!this.analytics) {
      return 'Select a room and wait for its metrics.';
    }
    if (this.analytics.avgResponseTimeMinutes > 5) {
      return 'Learners are waiting too long between replies. Add a clearer prompt and keep the tutor more visible in the flow.';
    }
    if (this.analytics.correctionsAcceptanceRate < 60) {
      return 'Students are not consistently applying corrections. Shorter corrections and more direct examples should help.';
    }
    if (this.analytics.challengeCompletionRate < 50) {
      return 'Challenges are likely too long or too difficult for this room. Trim the task and lower the entry barrier.';
    }
    return 'The room shows solid momentum, correction uptake, and challenge completion. Keep the current format and monitor for drift.';
  }
}
