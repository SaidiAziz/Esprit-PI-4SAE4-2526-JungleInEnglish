import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollaborationService } from '../../../core/services/collaboration.service';

@Component({
  selector: 'app-collaboration-admin-activity',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="users-page" *ngIf="activity$ | async as activity">
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
    .users-page {
      padding: 0;
    }

    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.4rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }

    .page-header h2 {
      margin: 0;
      font-size: 1.375rem;
      font-weight: 700;
      color: #0f172a;
      letter-spacing: -0.01em;
    }

    .state-msg {
      color: #94a3b8;
      font-style: italic;
      font-size: 0.9rem;
    }

    .user-count {
      background: #ede9fe;
      color: #4f46e5;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.2rem 0.65rem;
      border-radius: 999px;
      letter-spacing: 0.03em;
    }

    .admin-activity-feed {
      display: grid;
      gap: 0.9rem;
    }

    .admin-activity-card {
      background: linear-gradient(180deg, #ffffff, #f8fafc);
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 1rem 1.1rem;
      box-shadow: 0 18px 38px rgba(15, 23, 42, 0.06);
      transition: transform 0.16s ease, box-shadow 0.16s ease;
    }

    .admin-activity-card:hover {
      transform: translateY(-1px);
      box-shadow: 0 18px 34px rgba(15, 23, 42, 0.08);
    }

    .admin-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem;
      margin-bottom: 0.55rem;
    }

    .admin-activity-card p {
      margin: 0.35rem 0 0.45rem;
      color: #0f172a;
      line-height: 1.5;
    }

    .admin-activity-card small {
      color: #64748b;
    }
  `]
})
export class CollaborationAdminActivityComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly activity$ = this.collaborationService.getAdminActivity();
}
