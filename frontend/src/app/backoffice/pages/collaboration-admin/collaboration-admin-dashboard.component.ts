import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CollaborationService } from '../../../core/services/collaboration.service';

@Component({
  selector: 'app-collaboration-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="users-page" *ngIf="summary$ | async as summary">
      <div class="page-header">
        <div>
          <h2>Collaboration Supervision</h2>
          <p class="state-msg">A quick admin view of rooms, activity, and learner participation.</p>
        </div>
      </div>

      <div class="admin-metric-grid">
        <article class="admin-metric-card">
          <span>Rooms</span>
          <strong>{{ summary.totalRooms }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Active rooms</span>
          <strong>{{ summary.activeRooms }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Participants</span>
          <strong>{{ summary.totalParticipants }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Messages</span>
          <strong>{{ summary.totalMessages }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Corrections</span>
          <strong>{{ summary.totalCorrections }}</strong>
        </article>
        <article class="admin-metric-card">
          <span>Challenges</span>
          <strong>{{ summary.totalChallenges }}</strong>
        </article>
      </div>

      <div class="table-wrapper">
        <div class="page-header page-header--compact">
          <h3>Top active rooms</h3>
          <a routerLink="/admin/collaboration/rooms" class="btn-view admin-link-button">Open room monitor -></a>
        </div>
        <table class="users-table">
          <thead>
          <tr>
            <th>Room</th>
            <th>Status</th>
            <th>Participants</th>
            <th>Messages</th>
            <th>Challenges</th>
          </tr>
          </thead>
          <tbody>
          <tr *ngFor="let room of summary.topRooms">
            <td>{{ room.title }}</td>
            <td>{{ room.status }}</td>
            <td>{{ room.participantCount }}</td>
            <td>{{ room.messageCount }}</td>
            <td>{{ room.challengeCount }}</td>
          </tr>
          </tbody>
        </table>
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
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }

    .page-header--compact {
      margin: 0;
      border-bottom: none;
      padding: 1rem 1rem 0.8rem;
    }

    .page-header h2,
    .page-header h3 {
      margin: 0;
      color: #0f172a;
      font-weight: 700;
    }

    .page-header h2 {
      font-size: 1.375rem;
      letter-spacing: -0.01em;
    }

    .page-header h3 {
      font-size: 1.05rem;
    }

    .state-msg {
      color: #94a3b8;
      font-style: italic;
      font-size: 0.9rem;
    }

    .btn-view {
      background: transparent;
      border: 1px solid #6366f1;
      color: #6366f1;
      padding: 0.45rem 0.8rem;
      border-radius: 10px;
      font-size: 0.8rem;
      font-weight: 700;
      cursor: pointer;
      transition: background 0.16s ease, color 0.16s ease, transform 0.16s ease;
      white-space: nowrap;
    }

    .btn-view:hover {
      background: #6366f1;
      color: #fff;
      transform: translateY(-1px);
    }

    .admin-link-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      text-decoration: none;
      min-height: 40px;
      border-radius: 12px;
      padding: 0.55rem 0.95rem;
      font-weight: 700;
      box-shadow: 0 10px 20px rgba(99, 102, 241, 0.14);
    }

    .admin-metric-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .admin-metric-card {
      background: linear-gradient(180deg, #ffffff, #f8fafc);
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 1rem 1.1rem;
      box-shadow: 0 18px 38px rgba(15, 23, 42, 0.06);
    }

    .admin-metric-card span {
      display: block;
      color: #64748b;
      font-size: 0.85rem;
      margin-bottom: 0.35rem;
    }

    .admin-metric-card strong {
      font-size: 1.9rem;
      color: #0f172a;
    }

    .table-wrapper {
      overflow-x: auto;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
      background: #fff;
    }

    .users-table {
      width: 100%;
      border-collapse: collapse;
      background: #fff;
      font-size: 0.875rem;
    }

    .users-table thead {
      background: #1e293b;
      color: #e2e8f0;
    }

    .users-table th {
      text-align: left;
      padding: 0.8rem 1.1rem;
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.07em;
      white-space: nowrap;
      color: #94a3b8;
    }

    .users-table tbody tr {
      border-bottom: 1px solid #f1f5f9;
    }

    .users-table td {
      padding: 0.8rem 1.1rem;
      color: #334155;
      vertical-align: middle;
    }
  `]
})
export class CollaborationAdminDashboardComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly summary$ = this.collaborationService.getAdminDashboard();
}
