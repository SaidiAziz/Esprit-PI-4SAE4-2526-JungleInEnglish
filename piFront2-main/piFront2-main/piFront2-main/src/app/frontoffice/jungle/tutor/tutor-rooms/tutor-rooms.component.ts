import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Room, UpdateRoomRequest } from '../../../../core/models/collaboration.model';
import { CourseCatalogItem } from '../../../../core/models/course.model';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { CourseCatalogService } from '../../../../core/services/course-catalog.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-rooms',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS, RouterLink],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div class="rooms-hero-copy">
          <p class="fd-eyebrow">Rooms</p>
          <h1>Create and manage student practice rooms</h1>
          <p class="fd-muted">Create a room first, then students can join it and practice through chat, corrections, and challenges.</p>
        </div>
        <div class="rooms-hero-stats">
          <div class="rooms-hero-stat">
            <span>Rooms</span>
            <strong>{{ rooms.length }}</strong>
          </div>
          <div class="rooms-hero-stat">
            <span>Active</span>
            <strong>{{ activeRoomsCount }}</strong>
          </div>
          <div class="rooms-hero-stat">
            <span>Courses</span>
            <strong>{{ courses.length }}</strong>
          </div>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card rooms-panel">
          <div class="fd-section-head">
            <div>
              <h2>{{ editingRoom ? 'Edit this room' : 'Create a new room' }}</h2>
              <p>Use a clear topic and level so students know why they should join this room.</p>
            </div>
          </div>

          <form class="fd-form-grid" (ngSubmit)="saveRoom()">
            <div class="rooms-form-grid">
              <mat-form-field appearance="outline" class="rooms-form-span-2">
                <mat-label>Title</mat-label>
                <input matInput [(ngModel)]="roomForm.title" name="title">
              </mat-form-field>

              <mat-form-field appearance="outline" class="rooms-form-span-2">
                <mat-label>Topic</mat-label>
                <input matInput [(ngModel)]="roomForm.topic" name="topic">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Native language</mat-label>
                <input matInput [(ngModel)]="roomForm.nativeLanguage" name="nativeLanguage">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Target language</mat-label>
                <input matInput [(ngModel)]="roomForm.targetLanguage" name="targetLanguage">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Level</mat-label>
                <mat-select [(ngModel)]="roomForm.level" name="level">
                  <mat-option *ngFor="let level of levels" [value]="level">{{ level }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Type</mat-label>
                <mat-select [(ngModel)]="roomForm.type" name="type">
                  <mat-option *ngFor="let type of types" [value]="type">{{ type }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Max participants</mat-label>
                <input matInput type="number" [(ngModel)]="roomForm.maxParticipants" name="maxParticipants">
              </mat-form-field>

              <mat-form-field appearance="outline" class="rooms-form-span-2">
                <mat-label>Linked course</mat-label>
                <mat-select [(ngModel)]="roomForm.courseId" name="courseId">
                  <mat-option [value]="null">No linked course</mat-option>
                  <mat-option *ngFor="let course of courses" [value]="course.id">
                    {{ course.title }} - {{ course.level }}
                  </mat-option>
                </mat-select>
              </mat-form-field>
            </div>

            <div class="rooms-form-footer">
              <mat-checkbox [(ngModel)]="roomForm.isPublic" name="isPublic">Visible to students</mat-checkbox>
              <div class="fd-chip-row">
                <button mat-flat-button color="primary" type="submit">{{ editingRoom ? 'Save changes' : 'Create room' }}</button>
                <button mat-stroked-button type="button" *ngIf="editingRoom" (click)="resetForm()">Cancel</button>
              </div>
            </div>
          </form>

          <div class="rooms-course-note" *ngIf="selectedCourse as course">
            <span class="rooms-course-note__label">Linked course</span>
            <strong>{{ course.title }}</strong>
            <p *ngIf="course.description">{{ course.description }}</p>
          </div>
        </mat-card>

        <mat-card class="fd-surface-card rooms-panel">
          <div class="fd-section-head">
            <div>
              <h2>Your existing rooms</h2>
              <p>Open a room to watch activity, or update it here.</p>
            </div>
            <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
          </div>

          <div class="fd-room-stack" *ngIf="rooms.length; else emptyRooms">
            <article class="rooms-card" *ngFor="let room of rooms">
              <div class="rooms-card__head">
                <div class="rooms-card__title">
                  <strong>{{ room.title }}</strong>
                  <p>{{ room.topic }}</p>
                </div>

                <div class="rooms-card__badges">
                  <span class="fd-pill">{{ room.type }}</span>
                  <span class="fd-pill">{{ room.level }}</span>
                  <span class="fd-status-pill" [class.fd-status-pill--accepted]="room.status === 'ACTIVE'">{{ room.status }}</span>
                </div>
              </div>

              <div class="rooms-card__meta">
                <span>Participants: {{ room.maxParticipants }}</span>
                <span *ngIf="room.courseId">Course #{{ room.courseId }}</span>
                <span>{{ room.isPublic ? 'Visible to students' : 'Private room' }}</span>
              </div>

              <div class="rooms-card__controls">
                <mat-form-field appearance="outline" class="rooms-card__status">
                  <mat-label>Status</mat-label>
                  <mat-select [(ngModel)]="statusDrafts[room.id]" [name]="'status-' + room.id">
                    <mat-option *ngFor="let status of statuses" [value]="status">{{ status }}</mat-option>
                  </mat-select>
                </mat-form-field>

                <div class="rooms-card__actions">
                  <button mat-button type="button" (click)="editRoom(room)">Edit</button>
                  <button mat-button type="button" (click)="applyStatus(room)">Update status</button>
                  <button mat-button type="button" color="warn" (click)="deleteRoom(room.id)">Delete</button>
                  <a mat-stroked-button [routerLink]="['/tutor/collaboration/room', room.id]">Open room</a>
                </div>
              </div>
            </article>
          </div>

          <ng-template #emptyRooms>
            <div class="fd-empty-state">
              No rooms yet. Create your first room to start inviting students.
            </div>
          </ng-template>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .rooms-hero-copy {
      max-width: 44rem;
    }

    .rooms-hero-stats {
      display: grid;
      grid-template-columns: repeat(3, minmax(88px, 1fr));
      gap: 0.75rem;
      min-width: 280px;
    }

    .rooms-hero-stat {
      padding: 0.85rem 0.95rem;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.78);
      border: 1px solid rgba(212, 196, 184, 0.8);
      text-align: center;
    }

    .rooms-hero-stat span {
      display: block;
      font-size: 0.76rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #61717d;
    }

    .rooms-hero-stat strong {
      display: block;
      margin-top: 0.2rem;
      font-size: 1.45rem;
      color: #0f172a;
    }

    .rooms-panel {
      padding: 1rem !important;
    }

    .rooms-form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.85rem;
    }

    .rooms-form-span-2 {
      grid-column: span 2;
    }

    .rooms-form-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .rooms-course-note {
      margin-top: 1rem;
      padding: 0.9rem 1rem;
      border: 1px solid rgba(212, 196, 184, 0.85);
      border-radius: 12px;
      background: linear-gradient(180deg, #fffdf9, #f8f0e8);
    }

    .rooms-course-note__label {
      display: block;
      margin-bottom: 0.3rem;
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: #61717d;
    }

    .rooms-course-note strong {
      color: #0f172a;
    }

    .rooms-course-note p {
      margin-top: 0.35rem;
      color: #61717d;
    }

    .rooms-card {
      border: 1px solid rgba(212, 196, 184, 0.75);
      border-radius: 14px;
      padding: 1rem;
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .rooms-card__head,
    .rooms-card__controls,
    .rooms-card__actions,
    .rooms-card__badges,
    .rooms-card__meta {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .rooms-card__head,
    .rooms-card__controls {
      align-items: flex-start;
      justify-content: space-between;
    }

    .rooms-card__title strong {
      display: block;
      font-size: 1rem;
      color: #0f172a;
    }

    .rooms-card__title p {
      margin-top: 0.25rem;
      color: #61717d;
    }

    .rooms-card__badges {
      justify-content: flex-end;
    }

    .rooms-card__meta {
      margin-top: 0.75rem;
      color: #61717d;
      font-size: 0.84rem;
    }

    .rooms-card__controls {
      margin-top: 0.9rem;
      padding-top: 0.9rem;
      border-top: 1px solid rgba(212, 196, 184, 0.75);
    }

    .rooms-card__status {
      min-width: 170px;
      flex: 0 0 170px;
    }

    .rooms-card__actions {
      align-items: center;
      justify-content: flex-end;
      flex: 1 1 320px;
    }

    @media (max-width: 1100px) {
      .rooms-hero-stats,
      .rooms-form-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 760px) {
      .rooms-hero-stats,
      .rooms-form-grid {
        grid-template-columns: 1fr;
      }

      .rooms-form-span-2 {
        grid-column: span 1;
      }

      .rooms-card__actions,
      .rooms-card__badges {
        justify-content: flex-start;
      }

      .rooms-card__status {
        min-width: 100%;
        flex-basis: 100%;
      }
    }
  `]
})
export class TutorRoomsComponent {
  private readonly collaborationService = inject(CollaborationService);
  private readonly courseCatalogService = inject(CourseCatalogService);
  private readonly snackBar = inject(MatSnackBar);

  rooms: Room[] = [];
  courses: CourseCatalogItem[] = [];
  editingRoom: Room | null = null;
  feedback = '';
  readonly levels: Room['level'][] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  readonly types: Room['type'][] = ['TEXT_CHAT', 'VOICE', 'MIXED'];
  readonly statuses: Room['status'][] = ['ACTIVE', 'FULL', 'CLOSED'];
  readonly statusDrafts: Record<number, Room['status']> = {};

  roomForm: UpdateRoomRequest = this.buildInitialForm();

  constructor() {
    this.loadCourses();
    this.loadRooms();
  }

  loadCourses(): void {
    this.courseCatalogService.getCourses().subscribe(courses => {
      this.courses = courses.filter(course => course.active);
      if (!this.editingRoom && this.roomForm.courseId === undefined && this.courses.length) {
        this.roomForm.courseId = this.courses[0].id;
      }
    });
  }

  loadRooms(): void {
    this.collaborationService.getMyRooms('TUTOR').subscribe(rooms => {
      this.rooms = rooms;
      rooms.forEach(room => this.statusDrafts[room.id] = room.status);
    });
  }

  saveRoom(): void {
    const request = { ...this.roomForm, courseId: this.roomForm.courseId || null };
    const action$ = this.editingRoom
      ? this.collaborationService.updateRoom(this.editingRoom.id, request)
      : this.collaborationService.createRoom(request);

    action$.subscribe(() => {
      this.feedback = this.editingRoom ? 'Room updated.' : 'Room created.';
      this.snackBar.open(this.feedback, 'Close', {
        duration: 2600,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.resetForm();
      this.loadRooms();
    });
  }

  editRoom(room: Room): void {
    this.editingRoom = room;
    this.roomForm = {
      title: room.title,
      nativeLanguage: room.nativeLanguage,
      targetLanguage: room.targetLanguage,
      level: room.level,
      type: room.type,
      maxParticipants: room.maxParticipants,
      isPublic: room.isPublic,
      topic: room.topic,
      courseId: room.courseId
    };
  }

  applyStatus(room: Room): void {
    this.collaborationService.updateRoomStatus(room.id, this.statusDrafts[room.id]).subscribe(() => {
      this.feedback = `Room #${room.id} status updated.`;
      this.snackBar.open('Room status updated.', 'Close', {
        duration: 2400,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.loadRooms();
    });
  }

  deleteRoom(roomId: number): void {
    this.collaborationService.deleteRoom(roomId).subscribe(() => {
      this.feedback = `Room #${roomId} deleted.`;
      this.snackBar.open('Room deleted.', 'Close', {
        duration: 2400,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      this.loadRooms();
    });
  }

  resetForm(): void {
    this.editingRoom = null;
    this.roomForm = this.buildInitialForm();
  }

  private buildInitialForm(): UpdateRoomRequest {
    return {
      title: '',
      nativeLanguage: 'French',
      targetLanguage: 'English',
      level: 'INTERMEDIATE',
      type: 'VOICE',
      maxParticipants: 8,
      isPublic: true,
      topic: '',
      courseId: null
    };
  }

  get selectedCourse(): CourseCatalogItem | undefined {
    if (!this.roomForm.courseId) {
      return undefined;
    }
    return this.courses.find(course => course.id === this.roomForm.courseId);
  }

  get activeRoomsCount(): number {
    return this.rooms.filter(room => room.status === 'ACTIVE').length;
  }
}
