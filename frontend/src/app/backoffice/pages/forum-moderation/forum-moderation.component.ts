import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ForumService } from '../../../core/services/forum.service';
import { ReportResponse } from '../../../core/models/forum.model';

@Component({
  selector: 'app-forum-moderation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="mod-page">
      <div class="header">
        <div>
          <h2>Forum Moderation</h2>
          <p>Review reported posts and comments, then approve, reject, or remove the content.</p>
        </div>
        <div class="controls">
          <label>
            <span>Status</span>
            <select [(ngModel)]="status" (change)="changeStatus()">
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </label>
          <label>
            <span>Reviewed by</span>
            <input [(ngModel)]="reviewedBy" placeholder="admin">
          </label>
        </div>
      </div>

      <div *ngIf="loading" class="banner info">Loading reports...</div>
      <div *ngIf="error" class="banner error">{{ error }}</div>

      <div class="table" *ngIf="!loading">
        <div class="row head">
          <div>ID</div>
          <div>Target</div>
          <div>Reporter</div>
          <div>Reason</div>
          <div>Created</div>
          <div>Actions</div>
        </div>

        <div class="row" *ngFor="let report of reports">
          <div>#{{ report.id }}</div>
          <div class="target">
            <span class="pill">{{ report.targetType }}</span>
            <span>#{{ report.targetId }}</span>
          </div>
          <div>{{ report.reporterName }}</div>
          <div class="reason">{{ report.reason }}</div>
          <div>{{ report.createdAt | date:'short' }}</div>
          <div class="actions" *ngIf="status === 'PENDING'; else reviewedState">
            <button type="button" (click)="approve(report.id)">Approve</button>
            <button type="button" class="danger" (click)="reject(report.id)">Reject</button>
            <button type="button" class="danger-outline" (click)="deleteTarget(report)" [disabled]="deleting">
              Delete content
            </button>
          </div>
          <ng-template #reviewedState>
            <div class="done">{{ report.status }}</div>
          </ng-template>
        </div>

        <div class="empty" *ngIf="reports.length === 0">
          No reports in this status.
        </div>
      </div>

      <div class="pager" *ngIf="!loading && totalPages > 1">
        <button type="button" (click)="prev()" [disabled]="page === 0">Prev</button>
        <span>Page {{ page + 1 }} / {{ totalPages }}</span>
        <button type="button" (click)="next()" [disabled]="page >= totalPages - 1">Next</button>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .mod-page {
      max-width: 1280px;
      margin: 1.2rem auto 2.2rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      color: #0f172a;
    }

    .header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
    }

    .header h2 {
      margin: 0 0 0.25rem;
    }

    .header p {
      margin: 0;
      color: rgba(15, 23, 42, 0.65);
    }

    .controls {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .controls label {
      display: grid;
      gap: 0.25rem;
      font-size: 0.8rem;
      font-weight: 800;
      color: rgba(15, 23, 42, 0.82);
    }

    .controls select,
    .controls input {
      padding: 0.62rem 0.85rem;
      border-radius: 14px;
      border: 1px solid rgba(15, 23, 42, 0.14);
      background: rgba(255, 255, 255, 0.92);
    }

    .banner {
      padding: 0.78rem 0.95rem;
      border-radius: 16px;
      font-size: 0.92rem;
      border: 1px solid transparent;
    }

    .banner.info {
      background: rgba(8, 28, 49, 0.05);
      color: rgba(15, 23, 42, 0.85);
      border-color: rgba(8, 28, 49, 0.08);
    }

    .banner.error {
      background: rgba(185, 28, 28, 0.10);
      color: #b91c1c;
      border-color: rgba(185, 28, 28, 0.18);
    }

    .table {
      background: rgba(255, 255, 255, 0.92);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 22px;
      overflow: hidden;
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
    }

    .row {
      display: grid;
      grid-template-columns: 0.5fr 1fr 1fr 2fr 1fr 1fr;
      gap: 0.6rem;
      padding: 0.85rem 1rem;
      border-bottom: 1px solid rgba(15, 23, 42, 0.06);
      align-items: center;
      font-size: 0.85rem;
    }

    .row.head {
      background:
        radial-gradient(900px 220px at 10% 0%, rgba(246, 189, 96, 0.18), transparent 55%),
        radial-gradient(700px 220px at 85% 0%, rgba(45, 87, 87, 0.14), transparent 60%),
        linear-gradient(135deg, #0b1120 0%, #111827 55%, #0b1120 100%);
      color: rgba(229, 231, 235, 0.96);
      font-size: 0.72rem;
      font-weight: 900;
      text-transform: uppercase;
      letter-spacing: 0.07em;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }

    .target {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
    }

    .pill {
      display: inline-flex;
      padding: 0.18rem 0.55rem;
      border-radius: 999px;
      background: rgba(246, 189, 96, 0.12);
      color: #aa8733;
      font-weight: 900;
      font-size: 0.72rem;
      border: 1px solid rgba(170, 135, 51, 0.18);
    }

    .reason {
      color: rgba(15, 23, 42, 0.82);
    }

    .actions {
      display: flex;
      gap: 0.4rem;
      justify-content: flex-end;
      flex-wrap: wrap;
    }

    .actions button {
      padding: 0.46rem 0.9rem;
      border-radius: 999px;
      border: 1px solid rgba(255, 255, 255, 0.14);
      background: linear-gradient(135deg, #f6bd60 0%, #c84630 100%);
      color: #fff;
      cursor: pointer;
      font-weight: 800;
      font-size: 0.78rem;
      letter-spacing: 0.03em;
      text-transform: uppercase;
    }

    .actions button.danger {
      background: linear-gradient(135deg, #b91c1c 0%, #991b1b 100%);
    }

    .actions button.danger-outline {
      background: rgba(255, 255, 255, 0.9);
      color: #b91c1c;
      border: 1px solid rgba(185, 28, 28, 0.22);
      box-shadow: none;
    }

    .done {
      text-align: right;
      font-weight: 800;
      color: rgba(15, 23, 42, 0.65);
    }

    .empty {
      padding: 1.2rem 1rem;
      text-align: center;
      color: rgba(15, 23, 42, 0.65);
    }

    .pager {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.8rem;
    }

    .pager button {
      padding: 0.45rem 0.9rem;
      border-radius: 999px;
      border: 1px solid rgba(15, 23, 42, 0.08);
      background: rgba(255, 255, 255, 0.92);
      cursor: pointer;
      font-weight: 800;
    }

    .pager button:disabled {
      opacity: 0.5;
      cursor: default;
    }

    @media (max-width: 980px) {
      .row {
        grid-template-columns: 0.6fr 1.2fr 1fr 2fr 1fr;
      }

      .row > :nth-child(6) {
        display: none;
      }
    }
  `]
})
export class ForumModerationComponent {
  private readonly forumService = inject(ForumService);

  status: 'PENDING' | 'APPROVED' | 'REJECTED' = 'PENDING';
  page = 0;
  size = 10;
  totalPages = 1;
  reports: ReportResponse[] = [];
  loading = false;
  error: string | null = null;
  reviewedBy = 'admin';
  deleting = false;

  constructor() {
    this.load();
  }

  load(page: number = this.page): void {
    this.loading = true;
    this.error = null;
    this.forumService.listReports({ status: this.status, page, size: this.size }).subscribe({
      next: (response) => {
        this.reports = response.content;
        this.page = response.number;
        this.totalPages = response.totalPages || 1;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load reports.';
        this.loading = false;
      }
    });
  }

  changeStatus(): void {
    this.page = 0;
    this.load(0);
  }

  approve(id: number): void {
    this.forumService.reviewReport(id, 'APPROVED', this.reviewedBy).subscribe({
      next: () => this.load(),
      error: () => {
        this.error = 'Failed to approve report.';
      }
    });
  }

  reject(id: number): void {
    this.forumService.reviewReport(id, 'REJECTED', this.reviewedBy).subscribe({
      next: () => this.load(),
      error: () => {
        this.error = 'Failed to reject report.';
      }
    });
  }

  deleteTarget(report: ReportResponse): void {
    this.error = null;
    this.deleting = true;

    const request$ = report.targetType === 'POST'
      ? this.forumService.deletePost(report.targetId)
      : this.forumService.deleteComment(report.targetId);

    request$.subscribe({
      next: () => {
        this.forumService.reviewReport(report.id, 'APPROVED', this.reviewedBy).subscribe({
          next: () => {
            this.deleting = false;
            this.load();
          },
          error: () => {
            this.deleting = false;
            this.load();
          }
        });
      },
      error: () => {
        this.deleting = false;
        this.error = 'Failed to delete the reported content.';
      }
    });
  }

  prev(): void {
    if (this.page <= 0) {
      return;
    }
    this.load(this.page - 1);
  }

  next(): void {
    if (this.page >= this.totalPages - 1) {
      return;
    }
    this.load(this.page + 1);
  }
}
