import { Component, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, switchMap, tap } from 'rxjs';
import { UserResponse } from '../../../../core/models/user.model';
import { AppNotificationService } from '../../../../core/services/app-notification.service';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { UserDirectoryService } from '../../../../core/services/user-directory.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-student-badges',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell" *ngIf="vm$ | async as vm">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Motivation Layer</p>
          <h1>Badges should reinforce the behavior the platform wants more of</h1>
          <p class="fd-muted">Track your earned badges and compare your activity with the leaderboard.</p>
        </div>
      </header>

      <div class="fd-grid fd-grid--three">
        <mat-card class="fd-surface-card fd-badge-card" [class.fd-badge-card--new]="isNewBadge(badge.id)" *ngFor="let badge of vm.badges">
          <div class="fd-badge-mark">{{ badge.badgeType.slice(0, 2) }}</div>
          <h2>{{ badge.badgeType.replaceAll('_', ' ') }}</h2>
          <p>Earned on {{ badge.earnedAt | date: 'mediumDate' }}</p>
          <span class="fd-badge-new" *ngIf="isNewBadge(badge.id)">New</span>
        </mat-card>
      </div>

      <mat-card class="fd-surface-card">
        <div class="fd-section-head">
          <h2>Leaderboard</h2>
          <p>A quick signal of who is most active inside the collaboration module.</p>
        </div>
        <table mat-table [dataSource]="vm.leaderboard" class="fd-table">
          <ng-container matColumnDef="userId">
            <th mat-header-cell *matHeaderCellDef>User</th>
            <td mat-cell *matCellDef="let row">
              <ng-container *ngIf="getUser(row.userId) | async as user">
                <strong>{{ formatUser(user, row.userId) }}</strong>
                <div class="fd-muted" style="font-size: 0.82rem;">ID {{ row.userId }}<span *ngIf="user?.email"> | {{ user?.email }}</span></div>
              </ng-container>
            </td>
          </ng-container>
          <ng-container matColumnDef="badgeCount">
            <th mat-header-cell *matHeaderCellDef>Badges</th>
            <td mat-cell *matCellDef="let row">{{ row.badgeCount }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>
      </mat-card>
    </section>
  `,
  styles: [`
    .fd-badge-card {
      position: relative;
      overflow: hidden;
    }

    .fd-badge-card--new {
      border-color: rgba(246, 189, 96, 0.7);
      box-shadow: 0 20px 45px rgba(246, 189, 96, 0.18);
      animation: fd-badge-pop 850ms ease;
    }

    .fd-badge-card--new::after {
      content: '';
      position: absolute;
      inset: -40% auto auto -20%;
      width: 60%;
      height: 180%;
      transform: rotate(24deg);
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.45), transparent);
      animation: fd-badge-shine 1700ms ease;
      pointer-events: none;
    }

    .fd-badge-new {
      display: inline-flex;
      margin-top: 0.75rem;
      padding: 0.28rem 0.7rem;
      border-radius: 999px;
      background: rgba(246, 189, 96, 0.2);
      color: var(--color-accent-purple);
      font-size: 0.76rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }

    @keyframes fd-badge-pop {
      0% { transform: scale(0.94) translateY(8px); opacity: 0.2; }
      60% { transform: scale(1.03) translateY(-2px); opacity: 1; }
      100% { transform: scale(1) translateY(0); opacity: 1; }
    }

    @keyframes fd-badge-shine {
      from { transform: translateX(-160%) rotate(24deg); opacity: 0; }
      30% { opacity: 1; }
      to { transform: translateX(260%) rotate(24deg); opacity: 0; }
    }
  `]
})
export class StudentBadgesComponent {
  private readonly collaborationService = inject(CollaborationService);
  private readonly notificationService = inject(AppNotificationService);
  private readonly userDirectoryService = inject(UserDirectoryService);
  private readonly newBadgeIds = new Set<number>();

  readonly displayedColumns = ['userId', 'badgeCount'];
  readonly vm$ = this.collaborationService.getMyBadges().pipe(
    tap(badges => {
      if (typeof window === 'undefined') {
        return;
      }

      const storageKey = 'seen_badge_ids';
      const seenIds = new Set(
        (window.localStorage.getItem(storageKey) ?? '')
          .split(',')
          .map(value => Number(value))
          .filter(value => Number.isFinite(value) && value > 0)
      );

      this.newBadgeIds.clear();
      badges.forEach(badge => {
        if (!seenIds.has(badge.id)) {
          this.newBadgeIds.add(badge.id);
          this.notificationService.achievement('Badge achieved', badge.badgeType.replaceAll('_', ' '));
          seenIds.add(badge.id);
        }
      });

      window.localStorage.setItem(storageKey, [...seenIds].join(','));
    }),
    switchMap(badges => this.collaborationService.getLeaderboard().pipe(
      map(leaderboard => ({ badges, leaderboard }))
    ))
  );

  isNewBadge(badgeId: number): boolean {
    return this.newBadgeIds.has(badgeId);
  }

  getUser(userId: number): Observable<UserResponse | null> {
    return this.userDirectoryService.getUserById(userId);
  }

  formatUser(user: UserResponse | null, userId: number): string {
    if (!user) {
      return `User #${userId}`;
    }
    return `${user.firstName} ${user.lastName}`.trim();
  }
}
