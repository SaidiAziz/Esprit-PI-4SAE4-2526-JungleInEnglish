import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CourseCatalogItem } from '../../../core/models/course.model';
import { CourseCatalogService } from '../../../core/services/course-catalog.service';

@Component({
  selector: 'app-admin-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="courses-page">
      <div class="page-header">
        <div>
          <h2>Course Catalog</h2>
          <p class="state-msg">Manage courses exposed by the course microservice through the shared gateway.</p>
        </div>
        <div class="page-actions">
          <a routerLink="/admin/courses/new" class="btn-secondary">New course</a>
        </div>
      </div>

      <section class="list-panel">
        <div class="toolbar">
          <label for="levelFilter">Search by level</label>
          <input id="levelFilter" type="text" [(ngModel)]="levelFilter" placeholder="Beginner, Intermediate...">
          <button type="button" (click)="searchByLevel()">Search</button>
          <button type="button" class="btn-ghost" (click)="clearFilter()">Reset</button>
        </div>

        <div *ngIf="loading" class="state-msg">Loading courses...</div>
        <div *ngIf="error" class="error-banner">{{ error }}</div>

        <div class="table-wrapper" *ngIf="!loading && pagedCourses.length > 0">
          <table class="courses-table">
            <thead>
            <tr>
              <th>Title</th>
              <th>Level</th>
              <th>Duration</th>
              <th>Start</th>
              <th>Price</th>
              <th>Max</th>
              <th>Active</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let course of pagedCourses">
              <td class="col-title">{{ course.title }}</td>
              <td class="col-level">{{ course.level }}</td>
              <td>{{ course.durationHours }} h</td>
              <td>{{ course.startDate | date: 'mediumDate' }}</td>
              <td>{{ course.price ?? '—' }}</td>
              <td>{{ course.maxStudents }}</td>
              <td>
                <span class="status-badge" [class.status-active]="course.active" [class.status-inactive]="!course.active">
                  {{ course.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td class="col-actions">
                <a [routerLink]="['/admin/courses', course.id]" class="btn-link">Edit</a>
                <button type="button" class="btn-link danger" (click)="deleteCourse(course)">Delete</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div *ngIf="!loading && filteredCourses.length === 0" class="state-msg">
          No courses found. Try another level or create a new course.
        </div>
      </section>

      <div *ngIf="!loading && filteredCourses.length > 0" class="courses-pagination">
        <button type="button" (click)="prevPage()" [disabled]="page === 1">Prev</button>
        <div class="pages">
          <button
            type="button"
            *ngFor="let pageNumber of pageNumbers"
            [class.active]="page === pageNumber"
            (click)="goToPage(pageNumber)">
            {{ pageNumber }}
          </button>
        </div>
        <button type="button" (click)="nextPage()" [disabled]="page === totalPages">Next</button>
      </div>
    </section>
  `,
  styles: [`
    .courses-page {
      padding: 0;
    }

    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }

    .page-header h2 {
      margin: 0;
      font-size: 1.375rem;
      font-weight: 700;
      color: #0f172a;
    }

    .state-msg {
      margin-top: 0.25rem;
      font-size: 0.9rem;
      color: #94a3b8;
    }

    .list-panel {
      background: #fff;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      padding: 1.25rem 1.5rem;
      box-shadow: 0 1px 6px rgba(15, 23, 42, 0.06);
    }

    .page-actions {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }

    .toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
      align-items: center;
      margin-bottom: 1rem;
    }

    .toolbar label {
      font-size: 0.8rem;
      font-weight: 600;
      color: #64748b;
    }

    .toolbar input {
      padding: 0.45rem 0.7rem;
      border-radius: 10px;
      border: 1px solid #cbd5e1;
      font-size: 0.85rem;
    }

    .toolbar button {
      padding: 0.45rem 0.95rem;
      border-radius: 999px;
      border: none;
      background: #4f46e5;
      color: #fff;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-ghost,
    .btn-secondary {
      padding: 0.45rem 1rem;
      border-radius: 999px;
      border: 1px solid #e2e8f0;
      background: #fff;
      font-size: 0.8rem;
      font-weight: 600;
      color: #334155;
      cursor: pointer;
      text-decoration: none;
    }

    .btn-ghost {
      background: transparent;
      color: #64748b;
    }

    .error-banner {
      background: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
      border-radius: 8px;
      padding: 0.6rem 0.8rem;
      font-size: 0.83rem;
      margin-bottom: 0.75rem;
    }

    .table-wrapper {
      overflow-x: auto;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 1px 6px rgba(15, 23, 42, 0.05);
    }

    .courses-table {
      width: 100%;
      border-collapse: collapse;
      background: #fff;
      font-size: 0.83rem;
    }

    .courses-table thead {
      background: #1e293b;
    }

    .courses-table th {
      color: #e2e8f0;
      text-align: left;
      padding: 0.7rem 0.9rem;
      font-size: 0.7rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .courses-table tbody tr {
      border-bottom: 1px solid #f1f5f9;
    }

    .courses-table tbody tr:hover {
      background: #f8fafc;
    }

    .courses-table td {
      padding: 0.65rem 0.9rem;
      color: #334155;
    }

    .col-title {
      font-weight: 600;
    }

    .col-level {
      font-size: 0.8rem;
      color: #4f46e5;
    }

    .col-actions {
      text-align: right;
      white-space: nowrap;
    }

    .status-badge {
      display: inline-block;
      padding: 0.15rem 0.55rem;
      border-radius: 999px;
      font-size: 0.7rem;
      font-weight: 600;
    }

    .status-active {
      background: #dcfce7;
      color: #15803d;
    }

    .status-inactive {
      background: #fee2e2;
      color: #b91c1c;
    }

    .btn-link {
      background: transparent;
      border: none;
      color: #4f46e5;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      padding: 0 0.25rem;
      text-decoration: none;
    }

    .btn-link.danger {
      color: #b91c1c;
    }

    .courses-pagination {
      margin-top: 1rem;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.7rem;
    }

    .courses-pagination button {
      padding: 0.3rem 0.75rem;
      border-radius: 999px;
      border: 1px solid #e2e8f0;
      background: #fff;
      font-size: 0.78rem;
      cursor: pointer;
    }

    .courses-pagination .pages {
      display: flex;
      gap: 0.4rem;
    }

    .courses-pagination .pages button.active {
      background: #4f46e5;
      color: #f9fafb;
      border-color: transparent;
    }
  `]
})
export class AdminCoursesComponent {
  private readonly courseCatalogService = inject(CourseCatalogService);

  courses: CourseCatalogItem[] = [];
  filteredCourses: CourseCatalogItem[] = [];
  levelFilter = '';
  readonly pageSize = 6;
  page = 1;
  totalPages = 1;
  loading = false;
  error: string | null = null;

  constructor() {
    this.loadAllCourses();
  }

  private loadAllCourses(): void {
    this.loading = true;
    this.error = null;
    this.courseCatalogService.getCourses().subscribe({
      next: (courses) => {
        this.courses = courses;
        this.filteredCourses = courses;
        this.updatePagination();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load courses.';
        this.loading = false;
      }
    });
  }

  searchByLevel(): void {
    const level = this.levelFilter.trim();
    if (!level) {
      this.loadAllCourses();
      return;
    }

    this.loading = true;
    this.error = null;
    this.courseCatalogService.searchByLevel(level).subscribe({
      next: (courses) => {
        this.filteredCourses = courses;
        this.updatePagination();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to search courses by level.';
        this.loading = false;
      }
    });
  }

  clearFilter(): void {
    this.levelFilter = '';
    this.loadAllCourses();
  }

  deleteCourse(course: CourseCatalogItem): void {
    this.loading = true;
    this.error = null;
    this.courseCatalogService.deleteCourse(course.id).subscribe({
      next: () => this.loadAllCourses(),
      error: () => {
        this.error = 'Failed to delete course.';
        this.loading = false;
      }
    });
  }

  private updatePagination(): void {
    const total = this.filteredCourses.length;
    this.totalPages = Math.max(1, Math.ceil(total / this.pageSize));
    if (this.page > this.totalPages) {
      this.page = this.totalPages;
    }
    if (this.page < 1) {
      this.page = 1;
    }
  }

  get pagedCourses(): CourseCatalogItem[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredCourses.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.page = page;
  }

  nextPage(): void {
    this.goToPage(this.page + 1);
  }

  prevPage(): void {
    this.goToPage(this.page - 1);
  }
}
