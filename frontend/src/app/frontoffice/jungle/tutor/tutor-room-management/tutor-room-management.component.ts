import { Component, inject } from '@angular/core';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-tutor-room-management',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Tutor View</p>
          <h1>Room manager</h1>
          <p class="fd-muted">A focused view of the collaboration rooms you are supervising or reviewing.</p>
        </div>
      </header>

      <mat-card class="fd-surface-card">
        <table mat-table [dataSource]="rooms$ | async" class="fd-table">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Room</th>
            <td mat-cell *matCellDef="let room">{{ room.title }}</td>
          </ng-container>
          <ng-container matColumnDef="topic">
            <th mat-header-cell *matHeaderCellDef>Topic</th>
            <td mat-cell *matCellDef="let room">{{ room.topic }}</td>
          </ng-container>
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let room">{{ room.type }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let room">{{ room.status }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </mat-card>
    </section>
  `
})
export class TutorRoomManagementComponent {
  private readonly collaborationService = inject(CollaborationService);
  readonly columns = ['title', 'topic', 'type', 'status'];
  readonly rooms$ = this.collaborationService.getMyRooms('TUTOR');
}
