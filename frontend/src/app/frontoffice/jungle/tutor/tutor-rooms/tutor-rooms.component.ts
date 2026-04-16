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
        <div>
          <p class="fd-eyebrow">Rooms</p>
          <h1>Create and manage student practice rooms</h1>
          <p class="fd-muted">Create a room first, then students can join it and practice through chat, corrections, and challenges.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--two">
        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>{{ editingRoom ? 'Edit this room' : 'Create a new room' }}</h2>
              <p>Use a clear topic and level so students know why they should join this room.</p>
            </div>
          </div>
          <form class="fd-form-grid" (ngSubmit)="saveRoom()">
            <mat-form-field appearance="outline"><mat-label>Title</mat-label><input matInput [(ngModel)]="roomForm.title" name="title"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Topic</mat-label><input matInput [(ngModel)]="roomForm.topic" name="topic"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Native language</mat-label><input matInput [(ngModel)]="roomForm.nativeLanguage" name="nativeLanguage"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Target language</mat-label><input matInput [(ngModel)]="roomForm.targetLanguage" name="targetLanguage"></mat-form-field>
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
            <mat-form-field appearance="outline"><mat-label>Max participants</mat-label><input matInput type="number" [(ngModel)]="roomForm.maxParticipants" name="maxParticipants"></mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Linked course</mat-label>
              <mat-select [(ngModel)]="roomForm.courseId" name="courseId">
                <mat-option [value]="null">No linked course</mat-option>
                <mat-option *ngFor="let course of courses" [value]="course.id">
                  {{ course.title }} · {{ course.level }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <mat-checkbox [(ngModel)]="roomForm.isPublic" name="isPublic">Visible to students</mat-checkbox>
            <div class="fd-chip-row">
              <button mat-flat-button color="primary" type="submit">{{ editingRoom ? 'Save changes' : 'Create room' }}</button>
              <button mat-stroked-button type="button" *ngIf="editingRoom" (click)="resetForm()">Cancel</button>
            </div>
          </form>
          <p class="fd-muted" *ngIf="selectedCourse as course">
            Linked course: <strong>{{ course.title }}</strong><span *ngIf="course.description"> | {{ course.description }}</span>
          </p>
        </mat-card>

        <mat-card class="fd-surface-card">
          <div class="fd-section-head">
            <div>
              <h2>Your existing rooms</h2>
              <p>Open a room to watch activity, or update it here.</p>
            </div>
            <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
          </div>
          <div class="fd-room-stack">
            <article class="fd-room-row" *ngFor="let room of rooms">
              <div>
                <strong>{{ room.title }}</strong>
                <p>{{ room.topic }}</p>
              </div>
              <div class="fd-room-meta">
                <span>{{ room.type }}</span>
                <span>{{ room.level }}</span>
                <mat-form-field appearance="outline">
                  <mat-label>Status</mat-label>
                  <mat-select [(ngModel)]="statusDrafts[room.id]" [name]="'status-' + room.id">
                    <mat-option *ngFor="let status of statuses" [value]="status">{{ status }}</mat-option>
                  </mat-select>
                </mat-form-field>
                <button mat-button type="button" (click)="editRoom(room)">Edit</button>
                <button mat-button type="button" (click)="applyStatus(room)">Update status</button>
                <button mat-button type="button" (click)="deleteRoom(room.id)">Delete</button>
                <a mat-stroked-button [routerLink]="['/tutor/collaboration/room', room.id]">Open room</a>
              </div>
            </article>
          </div>
        </mat-card>
      </div>
    </section>
  `
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
}
