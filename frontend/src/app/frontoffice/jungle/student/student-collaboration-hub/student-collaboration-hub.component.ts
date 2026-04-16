import { Component, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { Room, RoomMatch } from '../../../../core/models/collaboration.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-collaboration-hub',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS, RouterLink],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Practice Rooms</p>
          <h1>Pick a room, join it, then practice with messages and challenges</h1>
          <p class="fd-muted">Start here when you want real practice. Recommended rooms are based on your current weak skill.</p>
        </div>
      </header>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>Best matches for you</h2>
            <p>These recommendations come from the advanced room matching service.</p>
          </div>
          <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
        </div>
        <div class="fd-room-stack">
          <article class="fd-room-row" *ngFor="let match of matches">
            <div>
              <strong>{{ match.room.title }}</strong>
              <p>{{ match.recommendationReason }}</p>
            </div>
            <div class="fd-room-meta">
              <span>{{ match.room.type }}</span>
              <span>{{ match.room.level }}</span>
              <button mat-stroked-button type="button" (click)="joinRoom(match.room.id)">Join</button>
              <a mat-button [routerLink]="['/student/collaboration/room', match.room.id]">Open room</a>
            </div>
          </article>
        </div>
      </mat-card>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>All public rooms</h2>
            <p>Browse and join directly from the backend room list.</p>
          </div>
        </div>
        <table mat-table [dataSource]="rooms" class="fd-table">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Room</th>
            <td mat-cell *matCellDef="let room">{{ room.title }}</td>
          </ng-container>
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let room">{{ room.type }}</td>
          </ng-container>
          <ng-container matColumnDef="topic">
            <th mat-header-cell *matHeaderCellDef>Topic</th>
            <td mat-cell *matCellDef="let room">{{ room.topic }}</td>
          </ng-container>
          <ng-container matColumnDef="action">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let room">
              <div class="fd-chip-row">
                <button mat-stroked-button type="button" (click)="joinRoom(room.id)">Join</button>
                <a mat-button [routerLink]="['/student/collaboration/room', room.id]">Enter</a>
              </div>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>
      </mat-card>
    </section>
  `
})
export class StudentCollaborationHubComponent {
  private readonly collaborationService = inject(CollaborationService);
  private readonly snackBar = inject(MatSnackBar);

  readonly displayedColumns = ['title', 'type', 'topic', 'action'];
  matches: RoomMatch[] = [];
  rooms: Room[] = [];
  feedback = '';

  constructor() {
    this.loadData();
  }

  loadData(): void {
    this.collaborationService.getRecommendations().subscribe(matches => this.matches = matches);
    this.collaborationService.getPublicRooms().subscribe(rooms => this.rooms = rooms);
  }

  joinRoom(roomId: number): void {
    this.collaborationService.joinRoom(roomId).subscribe(() => {
      this.feedback = `Joined room #${roomId}. Open it to message and take challenges.`;
      this.snackBar.open('Room joined. You can open it now.', 'Close', {
        duration: 2600,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
    });
  }
}
