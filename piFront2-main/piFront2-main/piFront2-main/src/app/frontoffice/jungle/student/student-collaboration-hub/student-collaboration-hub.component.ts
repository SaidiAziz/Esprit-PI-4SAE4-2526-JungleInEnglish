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
        <div class="hub-hero-copy">
          <p class="fd-eyebrow">Practice Rooms</p>
          <h1>Pick a room, join it, then practice with messages and challenges</h1>
          <p class="fd-muted">Start here when you want real practice. Recommended rooms are based on your current weak skill.</p>
        </div>
        <div class="hub-hero-stats">
          <div class="hub-hero-stat">
            <span>Recommended</span>
            <strong>{{ matches.length }}</strong>
          </div>
          <div class="hub-hero-stat">
            <span>Public rooms</span>
            <strong>{{ rooms.length }}</strong>
          </div>
          <div class="hub-hero-stat">
            <span>Joined</span>
            <strong>{{ joinedRoomCount }}</strong>
          </div>
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
        <div class="hub-card-grid" *ngIf="matches.length; else noMatches">
          <article class="hub-room-card" *ngFor="let match of matches">
            <div class="hub-room-card__head">
              <div>
                <strong>{{ match.room.title }}</strong>
                <p>{{ match.room.topic }}</p>
              </div>
              <div class="fd-chip-row">
                <span class="fd-pill">{{ match.room.type }}</span>
                <span class="fd-pill">{{ match.room.level }}</span>
              </div>
            </div>
            <p class="hub-room-card__reason">{{ match.recommendationReason }}</p>
            <div class="hub-room-card__meta">
              <span>{{ match.room.nativeLanguage }} -> {{ match.room.targetLanguage }}</span>
              <span>{{ match.room.maxParticipants }} seats</span>
            </div>
            <div class="hub-room-card__actions">
              <button mat-stroked-button type="button" (click)="joinRoom(match.room.id)">Join</button>
              <a mat-button [routerLink]="['/student/collaboration/room', match.room.id]">Open room</a>
            </div>
          </article>
        </div>
        <ng-template #noMatches>
          <div class="fd-empty-state">No recommendation yet. Browse the full room list below.</div>
        </ng-template>
      </mat-card>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <div>
            <h2>All public rooms</h2>
            <p>Browse and join directly from the backend room list.</p>
          </div>
        </div>
        <div class="hub-room-list" *ngIf="rooms.length; else noRooms">
          <article class="hub-list-row" *ngFor="let room of rooms">
            <div class="hub-list-row__main">
              <strong>{{ room.title }}</strong>
              <p>{{ room.topic }}</p>
            </div>
            <div class="hub-list-row__meta">
              <span>{{ room.type }}</span>
              <span>{{ room.level }}</span>
              <span>{{ room.nativeLanguage }} -> {{ room.targetLanguage }}</span>
              <span>{{ room.maxParticipants }} seats</span>
            </div>
            <div class="hub-list-row__actions">
              <button mat-stroked-button type="button" (click)="joinRoom(room.id)">Join</button>
              <a mat-button [routerLink]="['/student/collaboration/room', room.id]">Enter</a>
            </div>
          </article>
        </div>
        <ng-template #noRooms>
          <div class="fd-empty-state">No public room available right now.</div>
        </ng-template>
      </mat-card>
    </section>
  `,
  styles: [`
    .hub-hero-copy {
      max-width: 48rem;
    }

    .hub-hero-stats {
      display: grid;
      grid-template-columns: repeat(3, minmax(94px, 1fr));
      gap: 0.75rem;
      min-width: 300px;
    }

    .hub-hero-stat {
      padding: 0.9rem 1rem;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(212, 196, 184, 0.84);
      text-align: center;
    }

    .hub-hero-stat span {
      display: block;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #61717d;
    }

    .hub-hero-stat strong {
      display: block;
      margin-top: 0.2rem;
      font-size: 1.2rem;
      color: #0f172a;
    }

    .fd-surface-card {
      padding: 1rem !important;
    }

    .hub-card-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 1rem;
    }

    .hub-room-card,
    .hub-list-row {
      border: 1px solid rgba(212, 196, 184, 0.78);
      border-radius: 14px;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .hub-room-card {
      padding: 1rem;
      display: grid;
      gap: 0.8rem;
    }

    .hub-room-card__head {
      display: grid;
      gap: 0.7rem;
    }

    .hub-room-card__head strong,
    .hub-list-row__main strong {
      color: #0f172a;
      font-size: 1rem;
    }

    .hub-room-card__head p,
    .hub-list-row__main p,
    .hub-room-card__reason {
      margin: 0;
      color: #61717d;
      line-height: 1.45;
    }

    .hub-room-card__meta,
    .hub-list-row__meta {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem 1rem;
      color: #43515b;
      font-size: 0.83rem;
    }

    .hub-room-card__actions,
    .hub-list-row__actions {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
      align-items: center;
    }

    .hub-room-list {
      display: grid;
      gap: 0.9rem;
    }

    .hub-list-row {
      padding: 1rem;
      display: grid;
      grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr) auto;
      gap: 0.7rem;
      align-items: center;
    }

    @media (max-width: 960px) {
      .hub-list-row {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 720px) {
      .hub-hero-stats {
        min-width: 0;
        width: 100%;
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class StudentCollaborationHubComponent {
  private readonly collaborationService = inject(CollaborationService);
  private readonly snackBar = inject(MatSnackBar);

  matches: RoomMatch[] = [];
  rooms: Room[] = [];
  feedback = '';
  private readonly joinedRooms = new Set<number>();

  constructor() {
    this.loadData();
  }

  loadData(): void {
    this.collaborationService.getRecommendations().subscribe(matches => this.matches = matches);
    this.collaborationService.getPublicRooms().subscribe(rooms => this.rooms = rooms);
  }

  joinRoom(roomId: number): void {
    this.collaborationService.joinRoom(roomId).subscribe(() => {
      this.joinedRooms.add(roomId);
      this.feedback = `Joined room #${roomId}. Open it to message and take challenges.`;
      this.snackBar.open('Room joined. You can open it now.', 'Close', {
        duration: 2600,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
    });
  }

  get joinedRoomCount(): number {
    return this.joinedRooms.size;
  }
}
