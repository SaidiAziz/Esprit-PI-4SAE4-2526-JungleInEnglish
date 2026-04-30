import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CollaborationService } from '../../../core/services/collaboration.service';

@Component({
  selector: 'app-collaboration-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="admin-page-shell" *ngIf="summary$ | async as summary">
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
          <a routerLink="/admin/collaboration/rooms" class="admin-link-button">Open room monitor</a>
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

    .page-header--compact {
      padding-bottom: 0;
      border-bottom: 0;
      margin-bottom: 0.8rem;
    }

    .page-header h2,
    .page-header h3 {
      margin: 0;
      color: #0f172a;
    }

    .state-msg {
      margin-top: 0.35rem;
      color: #61717d;
      line-height: 1.45;
    }

    .admin-metric-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
      gap: 1rem;
    }

    .admin-metric-card,
    .table-wrapper {
      border: 1px solid rgba(212, 196, 184, 0.8);
      border-radius: 16px;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
      box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
    }

    .admin-metric-card {
      padding: 1rem 1.05rem;
    }

    .admin-metric-card span {
      display: block;
      color: #61717d;
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .admin-metric-card strong {
      display: block;
      margin-top: 0.3rem;
      font-size: 1.8rem;
      color: #0f172a;
    }

    .table-wrapper {
      overflow: auto;
      padding: 1rem;
    }

    .users-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .users-table thead {
      background: #f7efe6;
    }

    .users-table th,
    .users-table td {
      padding: 0.85rem 0.9rem;
      border-bottom: 1px solid rgba(212, 196, 184, 0.55);
      text-align: left;
      vertical-align: top;
    }

    .users-table th {
      color: #7a6859;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .admin-link-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 38px;
      padding: 0.55rem 0.9rem;
      border-radius: 10px;
      border: 1px solid #b7791f;
      color: #8a5a12;
      text-decoration: none;
      font-weight: 700;
      background: #fffdf9;
    }
  `]
})
export class CollaborationAdminDashboardComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly summary$ = this.collaborationService.getAdminDashboard();
}
