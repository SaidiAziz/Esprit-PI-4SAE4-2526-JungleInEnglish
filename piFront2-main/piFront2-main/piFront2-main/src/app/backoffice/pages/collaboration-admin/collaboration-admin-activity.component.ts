import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollaborationService } from '../../../core/services/collaboration.service';

@Component({
  selector: 'app-collaboration-admin-activity',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="admin-page-shell" *ngIf="activity$ | async as activity">
      <div class="page-header">
        <div>
          <h2>Recent Collaboration Activity</h2>
          <p class="state-msg">Recent room supervision events across messages, corrections, challenges, and submissions.</p>
        </div>
      </div>

      <div class="admin-activity-feed">
        <article class="admin-activity-card" *ngFor="let item of activity">
          <div class="admin-chip-row">
            <span class="user-count">{{ item.type }}</span>
            <span class="user-count" *ngIf="item.roomId">Room {{ item.roomId }}</span>
            <span class="user-count" *ngIf="item.actorUserId">User {{ item.actorUserId }}</span>
          </div>
          <p>{{ item.description }}</p>
          <small>{{ item.createdAt | date: 'medium' }}</small>
        </article>
      </div>
    </section>
  `,
  styles: [`
    .admin-page-shell {
      display: grid;
      gap: 1.25rem;
    }

    .page-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 1rem;
      padding: 0 0 1rem;
      border-bottom: 1px solid rgba(212, 196, 184, 0.7);
    }

    .page-header h2 {
      margin: 0;
      color: #0f172a;
    }

    .state-msg {
      margin-top: 0.35rem;
      color: #61717d;
      line-height: 1.45;
    }

    .admin-activity-feed {
      display: grid;
      gap: 0.9rem;
    }

    .admin-activity-card {
      border: 1px solid rgba(212, 196, 184, 0.8);
      border-radius: 16px;
      padding: 1rem 1.05rem;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
      box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
    }

    .admin-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem;
      margin-bottom: 0.6rem;
    }

    .user-count {
      display: inline-flex;
      align-items: center;
      min-height: 32px;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      background: #fff3d8;
      color: #8a5a12;
      font-size: 0.78rem;
      font-weight: 700;
    }

    .admin-activity-card p {
      margin: 0;
      color: #334155;
      line-height: 1.5;
    }

    .admin-activity-card small {
      display: block;
      margin-top: 0.5rem;
      color: #61717d;
    }
  `]
})
export class CollaborationAdminActivityComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly activity$ = this.collaborationService.getAdminActivity();
}
