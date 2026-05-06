import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminRoomDetail, AdminRoomOverview, Room } from '../../../core/models/collaboration.model';
import { CollaborationService } from '../../../core/services/collaboration.service';

@Component({
  selector: 'app-collaboration-admin-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="users-page">
      <div class="page-header">
        <div>
          <h2>Room Monitor</h2>
          <p class="state-msg">Select a room to inspect participants, recent messages, corrections, and challenges.</p>
        </div>
      </div>

      <div class="admin-split-grid">
        <div class="table-wrapper">
          <table class="users-table">
            <thead>
            <tr>
              <th>Room</th>
              <th>Type</th>
              <th>Status</th>
              <th>Participants</th>
              <th>Messages</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let room of rooms" [class.selected-row]="selectedRoomId === room.id">
              <td>{{ room.title }}</td>
              <td>{{ room.type }}</td>
              <td>{{ room.status }}</td>
              <td>{{ room.participantCount }}</td>
              <td>{{ room.messageCount }}</td>
              <td class="col-action">
                <button class="btn-view" (click)="selectRoom(room)">Inspect</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="detail-panel" *ngIf="selectedDetail as detail">
          <div class="page-header page-header--compact">
            <div>
              <h3>{{ detail.overview.title }}</h3>
              <p class="state-msg">{{ detail.overview.topic || 'No topic set' }}</p>
            </div>
          </div>

          <div class="admin-chip-row">
            <span class="status-badge status-active">{{ detail.overview.status }}</span>
            <span class="user-count">{{ detail.overview.type }}</span>
            <span class="user-count">{{ detail.overview.participantCount }} participants</span>
          </div>

          <div class="admin-inline-actions">
            <select [(ngModel)]="selectedStatus">
              <option value="ACTIVE">ACTIVE</option>
              <option value="FULL">FULL</option>
              <option value="CLOSED">CLOSED</option>
            </select>
            <button class="btn-view" (click)="saveStatus()">Save status</button>
          </div>

          <div class="admin-section">
            <h4>Participants</h4>
            <div class="admin-list-card" *ngFor="let participant of detail.participants">
              <div>
                <strong>User {{ participant.userId }}</strong>
                <p>{{ participant.role }} | Reputation {{ participant.reputationScore }}</p>
              </div>
              <button class="btn-view btn-view--danger" (click)="removeParticipant(participant.userId)">Remove</button>
            </div>
          </div>

          <div class="admin-section">
            <h4>Recent messages</h4>
            <div class="admin-list-card" *ngFor="let message of detail.recentMessages">
              <div>
                <strong>User {{ message.senderId }}</strong>
                <p>{{ message.content }}</p>
              </div>
            </div>
          </div>

          <div class="admin-section">
            <h4>Recent corrections</h4>
            <div class="admin-list-card" *ngFor="let correction of detail.recentCorrections">
              <div>
                <strong>User {{ correction.correctorId }}</strong>
                <p>{{ correction.correctedText }}</p>
              </div>
            </div>
          </div>
        </div>
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

    .page-header--compact {
      margin-bottom: 1rem;
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

    .btn-view--danger {
      color: #b91c1c;
      border-color: rgba(239, 68, 68, 0.2);
      background: #fff5f5;
    }

    .btn-view--danger:hover {
      background: #ef4444;
      color: #fff;
    }

    .admin-split-grid {
      display: grid;
      grid-template-columns: 1.2fr 1fr;
      gap: 1.25rem;
    }

    .table-wrapper {
      background: #fff;
      border-radius: 18px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 18px 38px rgba(15, 23, 42, 0.06);
      overflow-x: auto;
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

    .selected-row {
      background: linear-gradient(90deg, rgba(99, 102, 241, 0.1), rgba(99, 102, 241, 0.03));
    }

    .users-table tbody tr {
      border-bottom: 1px solid #f1f5f9;
      transition: background 0.16s ease, transform 0.16s ease;
    }

    .users-table tbody tr:hover {
      background: #f8faff;
      transform: translateX(2px);
    }

    .users-table td {
      padding: 0.8rem 1.1rem;
      color: #334155;
      vertical-align: middle;
    }

    .col-action {
      text-align: right;
    }

    .detail-panel {
      background: linear-gradient(180deg, #ffffff, #f8fafc);
      border: 1px solid #e2e8f0;
      border-radius: 18px;
      padding: 1.05rem 1.15rem;
      box-shadow: 0 18px 38px rgba(15, 23, 42, 0.06);
    }

    .admin-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem;
      margin-bottom: 0.75rem;
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

    .status-badge {
      display: inline-block;
      padding: 0.18rem 0.6rem;
      border-radius: 999px;
      font-size: 0.72rem;
      font-weight: 700;
      text-transform: capitalize;
    }

    .status-badge.status-active {
      background: #dcfce7;
      color: #15803d;
    }

    .admin-inline-actions {
      display: flex;
      gap: 0.75rem;
      margin: 1rem 0 1.25rem;
    }

    .admin-inline-actions select {
      min-width: 150px;
      border: 1px solid #cbd5e1;
      border-radius: 12px;
      padding: 0.6rem 0.8rem;
      background: #fff;
      color: #0f172a;
    }

    .admin-inline-actions .btn-view {
      min-height: 42px;
      border-radius: 12px;
      padding: 0.6rem 1rem;
      box-shadow: 0 10px 18px rgba(99, 102, 241, 0.12);
    }

    .admin-section {
      margin-top: 1.2rem;
    }

    .admin-section h4 {
      margin: 0 0 0.7rem;
      color: #0f172a;
      font-size: 0.98rem;
    }

    .admin-list-card {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: start;
      padding: 0.9rem 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 14px;
      background: linear-gradient(180deg, #f8fafc, #ffffff);
      margin-bottom: 0.7rem;
      transition: transform 0.16s ease, box-shadow 0.16s ease;
    }

    .admin-list-card:hover {
      transform: translateY(-1px);
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
    }

    .admin-list-card p {
      margin: 0.25rem 0 0;
      color: #64748b;
    }

    @media (max-width: 1100px) {
      .admin-split-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class CollaborationAdminRoomsComponent {
  private readonly collaborationService = inject(CollaborationService);

  rooms: AdminRoomOverview[] = [];
  selectedDetail: AdminRoomDetail | null = null;
  selectedRoomId: number | null = null;
  selectedStatus: Room['status'] = 'ACTIVE';

  constructor() {
    this.loadRooms();
  }

  loadRooms(): void {
    this.collaborationService.getAdminRooms().subscribe(rooms => this.rooms = rooms);
  }

  selectRoom(room: AdminRoomOverview): void {
    this.selectedRoomId = room.id;
    this.selectedStatus = room.status;
    this.collaborationService.getAdminRoomDetail(room.id).subscribe(detail => this.selectedDetail = detail);
  }

  saveStatus(): void {
    if (!this.selectedRoomId) {
      return;
    }
    this.collaborationService.updateAdminRoomStatus(this.selectedRoomId, this.selectedStatus).subscribe(updated => {
      this.rooms = this.rooms.map(room => room.id === updated.id ? updated : room);
      if (this.selectedDetail) {
        this.selectedDetail = { ...this.selectedDetail, overview: updated };
      }
    });
  }

  removeParticipant(userId: number): void {
    if (!this.selectedRoomId) {
      return;
    }
    this.collaborationService.removeAdminParticipant(this.selectedRoomId, userId).subscribe(() => {
      const currentRoom = this.rooms.find(room => room.id === this.selectedRoomId);
      if (currentRoom) {
        this.selectRoom(currentRoom);
      }
      this.loadRooms();
    });
  }
}
