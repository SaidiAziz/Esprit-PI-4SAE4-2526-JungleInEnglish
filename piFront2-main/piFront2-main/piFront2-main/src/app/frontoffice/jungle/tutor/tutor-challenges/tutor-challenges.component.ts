import { Component, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Challenge, ChallengeSubmission, CreateChallengeRequest, Room } from '../../../../core/models/collaboration.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-challenges',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div class="challenge-hero-copy">
          <p class="fd-eyebrow">Challenge Studio</p>
          <h1>Create or generate challenges for active rooms</h1>
          <p class="fd-muted">Use manual prompts for targeted exercises, or let the advanced generator create a challenge from the selected room context.</p>
        </div>
        <div class="challenge-hero-stats">
          <div class="challenge-hero-stat">
            <span>Rooms</span>
            <strong>{{ rooms.length }}</strong>
          </div>
          <div class="challenge-hero-stat">
            <span>Challenges</span>
            <strong>{{ challenges.length }}</strong>
          </div>
          <div class="challenge-hero-stat">
            <span>Answers</span>
            <strong>{{ selectedSubmissions.length }}</strong>
          </div>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card challenge-panel">
          <div class="fd-section-head">
            <div>
              <h2>Create a challenge</h2>
              <p>Choose a room, define the task, and publish it with a deadline.</p>
            </div>
          </div>

          <form class="fd-form-grid" (ngSubmit)="createChallenge()">
            <div class="challenge-form-grid">
              <mat-form-field appearance="outline" class="challenge-form-span-2">
                <mat-label>Room</mat-label>
                <mat-select [(ngModel)]="roomId" name="roomId" (ngModelChange)="loadChallenges()">
                  <mat-option *ngFor="let room of rooms" [value]="room.id">{{ room.title }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Type</mat-label>
                <mat-select [(ngModel)]="challengeForm.type" name="type">
                  <mat-option *ngFor="let type of challengeTypes" [value]="type">{{ type }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Difficulty</mat-label>
                <mat-select [(ngModel)]="challengeForm.difficulty" name="difficulty">
                  <mat-option *ngFor="let difficulty of challengeDifficulties" [value]="difficulty">{{ difficulty }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="challenge-form-span-2">
                <mat-label>Prompt</mat-label>
                <textarea matInput rows="4" [(ngModel)]="challengeForm.prompt" name="prompt"></textarea>
              </mat-form-field>

              <mat-form-field appearance="outline" class="challenge-form-span-2">
                <mat-label>Correct answer</mat-label>
                <input matInput [(ngModel)]="challengeForm.correctAnswer" name="correctAnswer">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Deadline</mat-label>
                <input matInput type="datetime-local" [(ngModel)]="challengeForm.deadline" name="deadline">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Points</mat-label>
                <input matInput type="number" [(ngModel)]="challengeForm.pointsReward" name="pointsReward">
              </mat-form-field>
            </div>

            <div class="challenge-form-footer">
              <div class="challenge-form-hint">
                <strong>Generator</strong>
                <p>Auto-generation uses the room’s active learning context and defaults.</p>
              </div>
              <div class="fd-chip-row">
                <button mat-flat-button color="primary" type="submit">Create challenge</button>
                <button mat-stroked-button type="button" (click)="generateChallenge()">Generate automatically</button>
              </div>
            </div>
          </form>
        </mat-card>

        <mat-card class="fd-surface-card challenge-panel">
          <div class="fd-section-head">
            <div>
              <h2>Room challenge list</h2>
              <p>Review, close, or remove outdated challenges.</p>
            </div>
            <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
          </div>

          <div class="fd-room-stack" *ngIf="challenges.length; else noChallenges">
            <article class="challenge-card" *ngFor="let challenge of challenges">
              <div class="challenge-card__head">
                <div class="challenge-card__title">
                  <strong>{{ challenge.type }}</strong>
                  <p>{{ challenge.prompt }}</p>
                </div>
                <div class="challenge-card__badges">
                  <span class="fd-pill">{{ challenge.difficulty }}</span>
                  <span class="fd-pill">{{ challenge.pointsReward }} pts</span>
                  <span class="fd-status-pill" [class.fd-status-pill--accepted]="challenge.status === 'OPEN'">{{ challenge.status }}</span>
                </div>
              </div>

              <div class="challenge-card__meta">
                <span>Deadline: {{ challenge.deadline | date:'medium' }}</span>
                <span>Room #{{ challenge.roomId }}</span>
              </div>

              <div class="challenge-card__controls">
                <button mat-button type="button" (click)="reviewSubmissions(challenge)">Review answers</button>

                <mat-form-field appearance="outline" class="challenge-card__status">
                  <mat-label>Status</mat-label>
                  <mat-select [(ngModel)]="statusDrafts[challenge.id]" [name]="'challenge-status-' + challenge.id">
                    <mat-option *ngFor="let status of challengeStatuses" [value]="status">{{ status }}</mat-option>
                  </mat-select>
                </mat-form-field>

                <div class="challenge-card__actions">
                  <button mat-button type="button" (click)="updateStatus(challenge)">Save status</button>
                  <button mat-button type="button" color="warn" (click)="deleteChallenge(challenge.id)">Delete</button>
                </div>
              </div>
            </article>
          </div>

          <ng-template #noChallenges>
            <div class="fd-empty-state">
              No challenges yet for this room. Create one or generate one automatically.
            </div>
          </ng-template>

          <section class="challenge-review" *ngIf="selectedChallenge">
            <div class="fd-section-head fd-section-head--embedded">
              <div>
                <h2>Student answers</h2>
                <p>Challenge #{{ selectedChallenge.id }} | {{ selectedChallenge.prompt }}</p>
              </div>
            </div>

            <div class="fd-empty-state" *ngIf="!selectedSubmissions.length">No student answer yet.</div>

            <div class="challenge-submission-list" *ngIf="selectedSubmissions.length">
              <article class="challenge-submission-card" *ngFor="let submission of selectedSubmissions">
                <div class="challenge-submission-card__head">
                  <div class="fd-chip-row">
                    <strong>Student #{{ submission.userId }}</strong>
                    <span class="fd-status-pill" [class.fd-status-pill--accepted]="submission.correct">
                      {{ submission.correct ? 'Correct' : 'Needs retry' }}
                    </span>
                  </div>
                  <span class="fd-pill">{{ submission.pointsAwarded }} pts</span>
                </div>
                <p>{{ submission.answer }}</p>
                <small>{{ submission.feedback }}</small>
              </article>
            </div>
          </section>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .challenge-hero-copy {
      max-width: 46rem;
    }

    .challenge-hero-stats {
      display: grid;
      grid-template-columns: repeat(3, minmax(88px, 1fr));
      gap: 0.75rem;
      min-width: 280px;
    }

    .challenge-hero-stat {
      padding: 0.85rem 0.95rem;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.78);
      border: 1px solid rgba(212, 196, 184, 0.8);
      text-align: center;
    }

    .challenge-hero-stat span {
      display: block;
      font-size: 0.76rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #61717d;
    }

    .challenge-hero-stat strong {
      display: block;
      margin-top: 0.2rem;
      font-size: 1.45rem;
      color: #0f172a;
    }

    .challenge-panel {
      padding: 1rem !important;
    }

    .challenge-form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.85rem;
    }

    .challenge-form-span-2 {
      grid-column: span 2;
    }

    .challenge-form-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .challenge-form-hint {
      max-width: 20rem;
      padding: 0.85rem 0.95rem;
      border-radius: 12px;
      border: 1px solid rgba(212, 196, 184, 0.8);
      background: linear-gradient(180deg, #fffdf9, #f8f0e8);
    }

    .challenge-form-hint strong {
      display: block;
      color: #0f172a;
      margin-bottom: 0.2rem;
    }

    .challenge-form-hint p {
      color: #61717d;
      font-size: 0.84rem;
    }

    .challenge-card,
    .challenge-submission-card {
      border: 1px solid rgba(212, 196, 184, 0.75);
      border-radius: 14px;
      padding: 1rem;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .challenge-card__head,
    .challenge-card__controls,
    .challenge-card__badges,
    .challenge-card__meta,
    .challenge-card__actions,
    .challenge-submission-card__head {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .challenge-card__head,
    .challenge-card__controls,
    .challenge-submission-card__head {
      align-items: flex-start;
      justify-content: space-between;
    }

    .challenge-card__title strong {
      display: block;
      color: #0f172a;
      font-size: 1rem;
    }

    .challenge-card__title p,
    .challenge-submission-card p {
      margin-top: 0.25rem;
      color: #61717d;
    }

    .challenge-card__badges {
      justify-content: flex-end;
    }

    .challenge-card__meta {
      margin-top: 0.75rem;
      color: #61717d;
      font-size: 0.84rem;
    }

    .challenge-card__controls {
      margin-top: 0.9rem;
      padding-top: 0.9rem;
      border-top: 1px solid rgba(212, 196, 184, 0.75);
    }

    .challenge-card__status {
      min-width: 180px;
      flex: 0 0 180px;
    }

    .challenge-card__actions {
      justify-content: flex-end;
      align-items: center;
      flex: 1 1 220px;
    }

    .challenge-review {
      margin-top: 1rem;
      padding-top: 1rem;
      border-top: 1px solid rgba(212, 196, 184, 0.75);
    }

    .challenge-submission-list {
      display: grid;
      gap: 0.75rem;
    }

    .challenge-submission-card small {
      display: block;
      margin-top: 0.45rem;
      color: #61717d;
    }

    @media (max-width: 1100px) {
      .challenge-hero-stats,
      .challenge-form-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 760px) {
      .challenge-hero-stats,
      .challenge-form-grid {
        grid-template-columns: 1fr;
      }

      .challenge-form-span-2 {
        grid-column: span 1;
      }

      .challenge-card__badges,
      .challenge-card__actions {
        justify-content: flex-start;
      }

      .challenge-card__status {
        min-width: 100%;
        flex-basis: 100%;
      }
    }
  `]
})
export class TutorChallengesComponent {
  private readonly collaborationService = inject(CollaborationService);
  private readonly snackBar = inject(MatSnackBar);

  rooms: Room[] = [];
  challenges: Challenge[] = [];
  selectedChallenge: Challenge | null = null;
  selectedSubmissions: ChallengeSubmission[] = [];
  roomId = 1;
  feedback = '';
  readonly statusDrafts: Record<number, Challenge['status']> = {};
  readonly challengeTypes: Challenge['type'][] = ['TRANSLATION_RACE', 'WORD_CHAIN', 'DESCRIBE_IMAGE', 'FILL_BLANK', 'STORY_BUILDER'];
  readonly challengeDifficulties: Challenge['difficulty'][] = ['EASY', 'MEDIUM', 'HARD'];
  readonly challengeStatuses: Challenge['status'][] = ['OPEN', 'CLOSED', 'CANCELLED'];
  readonly challengeForm: CreateChallengeRequest = {
    type: 'TRANSLATION_RACE',
    prompt: '',
    correctAnswer: '',
    deadline: this.buildDeadline(),
    difficulty: 'MEDIUM',
    pointsReward: 10
  };

  constructor() {
    this.collaborationService.getMyRooms('TUTOR').subscribe(rooms => {
      this.rooms = rooms;
      this.roomId = rooms[0]?.id ?? 0;
      if (this.roomId) {
        this.loadChallenges();
      } else {
        this.challenges = [];
      }
    });
  }

  loadChallenges(): void {
    if (!this.roomId) {
      this.challenges = [];
      return;
    }
    this.collaborationService.getRoomChallenges(this.roomId).subscribe(challenges => {
      this.challenges = challenges;
      challenges.forEach(challenge => this.statusDrafts[challenge.id] = challenge.status);
    });
  }

  createChallenge(): void {
    if (!this.roomId) {
      this.feedback = 'Create a room first.';
      return;
    }
    this.collaborationService.createChallenge(this.roomId, this.challengeForm).subscribe(() => {
      this.feedback = 'Challenge created.';
      this.snackBar.open('Challenge created for the room.', 'Close', {
        duration: 2600,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.challengeForm.prompt = '';
      this.challengeForm.correctAnswer = '';
      this.challengeForm.deadline = this.buildDeadline();
      this.loadChallenges();
    });
  }

  generateChallenge(): void {
    if (!this.roomId) {
      this.feedback = 'Create a room first.';
      return;
    }
    this.collaborationService.generateChallenge(this.roomId).subscribe(() => {
      this.feedback = 'Challenge generated from advanced service.';
      this.snackBar.open('Challenge generated automatically.', 'Close', {
        duration: 2600,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.loadChallenges();
    });
  }

  updateStatus(challenge: Challenge): void {
    this.collaborationService.updateChallengeStatus(challenge.id, this.statusDrafts[challenge.id]).subscribe(() => {
      this.feedback = `Challenge #${challenge.id} updated.`;
      this.snackBar.open('Challenge status updated.', 'Close', {
        duration: 2400,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.loadChallenges();
    });
  }

  deleteChallenge(challengeId: number): void {
    this.collaborationService.deleteChallenge(challengeId).subscribe(() => {
      this.feedback = `Challenge #${challengeId} deleted.`;
      this.snackBar.open('Challenge deleted.', 'Close', {
        duration: 2400,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.loadChallenges();
    });
  }

  reviewSubmissions(challenge: Challenge): void {
    this.selectedChallenge = challenge;
    this.collaborationService.getChallengeSubmissions(challenge.id).subscribe(submissions => {
      this.selectedSubmissions = submissions;
      this.snackBar.open('Student answers loaded.', 'Close', {
        duration: 2200,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
    });
  }

  private buildDeadline(): string {
    const target = new Date(Date.now() + 2 * 60 * 60 * 1000);
    const localTarget = new Date(target.getTime() - target.getTimezoneOffset() * 60000);
    return localTarget.toISOString().slice(0, 16);
  }
}
