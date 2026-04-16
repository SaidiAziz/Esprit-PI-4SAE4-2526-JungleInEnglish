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
        <div>
          <p class="fd-eyebrow">Challenge Studio</p>
          <h1>Create or generate challenges for active rooms</h1>
          <p class="fd-muted">This view uses both the standard challenge endpoints and the advanced challenge generator endpoint.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Create a challenge</h2>
              <p>Manual challenge creation for a selected room.</p>
            </div>
          </div>
          <form class="fd-form-grid" (ngSubmit)="createChallenge()">
            <mat-form-field appearance="outline">
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
            <mat-form-field appearance="outline"><mat-label>Prompt</mat-label><textarea matInput rows="3" [(ngModel)]="challengeForm.prompt" name="prompt"></textarea></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Correct answer</mat-label><input matInput [(ngModel)]="challengeForm.correctAnswer" name="correctAnswer"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Deadline</mat-label><input matInput type="datetime-local" [(ngModel)]="challengeForm.deadline" name="deadline"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Points</mat-label><input matInput type="number" [(ngModel)]="challengeForm.pointsReward" name="pointsReward"></mat-form-field>
            <div class="fd-chip-row">
              <button mat-flat-button color="primary" type="submit">Create challenge</button>
              <button mat-stroked-button type="button" (click)="generateChallenge()">Generate automatically</button>
            </div>
          </form>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Room challenge list</h2>
              <p>Update status or delete outdated challenges.</p>
            </div>
            <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
          </div>
          <div class="fd-room-stack">
            <article class="fd-room-row" *ngFor="let challenge of challenges">
              <div>
                <strong>{{ challenge.type }}</strong>
                <p>{{ challenge.prompt }}</p>
              </div>
              <div class="fd-room-meta">
                <button mat-button type="button" (click)="reviewSubmissions(challenge)">Review answers</button>
                <mat-form-field appearance="outline">
                  <mat-label>Status</mat-label>
                  <mat-select [(ngModel)]="statusDrafts[challenge.id]" [name]="'challenge-status-' + challenge.id">
                    <mat-option *ngFor="let status of challengeStatuses" [value]="status">{{ status }}</mat-option>
                  </mat-select>
                </mat-form-field>
                <button mat-button type="button" (click)="updateStatus(challenge)">Save status</button>
                <button mat-button type="button" (click)="deleteChallenge(challenge.id)">Delete</button>
              </div>
            </article>
          </div>

          <div class="fd-room-stack" *ngIf="selectedChallenge">
            <div class="fd-section-head fd-section-head--embedded">
              <div>
                <h2>Student Answers</h2>
                <p>Challenge #{{ selectedChallenge.id }} | {{ selectedChallenge.prompt }}</p>
              </div>
            </div>
            <div class="fd-empty-state" *ngIf="!selectedSubmissions.length">No student answer yet.</div>
            <article class="fd-room-row" *ngFor="let submission of selectedSubmissions">
              <div>
                <div class="fd-chip-row">
                  <strong>Student #{{ submission.userId }}</strong>
                  <span class="fd-status-pill" [class.fd-status-pill--accepted]="submission.correct">
                    {{ submission.correct ? 'Correct' : 'Needs retry' }}
                  </span>
                </div>
                <p>{{ submission.answer }}</p>
                <small>{{ submission.feedback }}</small>
              </div>
              <div class="fd-room-meta">
                <span>{{ submission.pointsAwarded }} pts</span>
              </div>
            </article>
          </div>
        </mat-card>
      </div>
    </section>
  `
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
