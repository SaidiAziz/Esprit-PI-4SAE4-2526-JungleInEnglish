import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CourseCatalogItem } from '../../../../core/models/course.model';
import { CourseCatalogService } from '../../../../core/services/course-catalog.service';

@Component({
  selector: 'app-student-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="sc-page">
      <div class="sc-hero">
        <div class="sc-hero-text">
          <h2>Available courses</h2>
          <p>Browse live English courses and pick the one that fits your level and schedule.</p>
          <div class="sc-tags">
            <span>Course microservice</span>
            <span>Live catalog</span>
            <span>Gateway connected</span>
          </div>
        </div>
        <div class="sc-hero-tag">
          <div class="tag-pill">Student View</div>
          <p>Real courses loaded from the shared course catalog.</p>
        </div>
      </div>

      <div class="sc-filters">
        <div class="filter-group">
          <label for="levelSelect">Level</label>
          <select
            id="levelSelect"
            [(ngModel)]="selectedLevel"
            (change)="applyFilters()">
            <option value="">All levels</option>
            <option *ngFor="let level of levels" [value]="level">{{ level }}</option>
          </select>
        </div>

        <div class="filter-group checkbox">
          <label>
            <input type="checkbox" [(ngModel)]="onlyActive" (change)="applyFilters()">
            Only active courses
          </label>
        </div>

        <button type="button" class="btn-ghost" (click)="clearFilters()">Reset</button>
      </div>

      <div *ngIf="loading" class="banner info">Loading courses...</div>
      <div *ngIf="error" class="banner error">{{ error }}</div>

      <div *ngIf="!loading && !error" class="sc-grid">
        <article *ngFor="let course of pagedCourses" class="sc-card">
          <header class="sc-card-header">
            <div class="level-pill">{{ course.level }}</div>
            <h3>{{ course.title }}</h3>
          </header>

          <p class="sc-card-desc">
            {{ course.description || 'No detailed description has been added yet for this course.' }}
          </p>

          <ul class="sc-meta">
            <li>
              <span class="label">Duration</span>
              <span class="value">{{ course.durationHours }} hours</span>
            </li>
            <li>
              <span class="label">Starts</span>
              <span class="value">{{ course.startDate | date: 'mediumDate' }}</span>
            </li>
            <li>
              <span class="label">Ends</span>
              <span class="value">{{ course.endDate ? (course.endDate | date: 'mediumDate') : 'Flexible' }}</span>
            </li>
            <li>
              <span class="label">Max students</span>
              <span class="value">{{ course.maxStudents }}</span>
            </li>
          </ul>

          <footer class="sc-footer">
            <div class="price-block">
              <span class="label">Price</span>
              <span class="price" *ngIf="course.price !== null && course.price !== undefined; else contact">
                {{ course.price }} USD
              </span>
              <ng-template #contact>
                <span class="price muted">Contact us</span>
              </ng-template>
            </div>
            <div class="status-pill" [class.inactive]="!course.active">
              {{ course.active ? 'Open' : 'Closed' }}
            </div>
          </footer>
        </article>

        <div *ngIf="filteredCourses.length === 0" class="empty-state">
          <h3>No course matches your filters</h3>
          <p>Try another level or remove the active-only filter.</p>
        </div>
      </div>

      <div *ngIf="!loading && !error && filteredCourses.length > 0" class="pagination">
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
    .sc-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .sc-hero {
      display: flex;
      justify-content: space-between;
      gap: 1.5rem;
      align-items: center;
      padding: 1.6rem 1.8rem;
      border-radius: 22px;
      color: #f8fafc;
      background:
        radial-gradient(circle at top right, rgba(246, 189, 96, 0.35), transparent 30%),
        linear-gradient(135deg, #2d5757 0%, #3d3d60 55%, #1f2f3f 100%);
      box-shadow: 0 16px 36px rgba(45, 87, 87, 0.2);
    }

    .sc-hero-text h2 {
      margin: 0 0 0.35rem;
      font-size: 1.55rem;
    }

    .sc-hero-text p {
      margin: 0 0 0.8rem;
      font-size: 0.95rem;
      color: rgba(248, 250, 252, 0.82);
      max-width: 720px;
    }

    .sc-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.45rem;
    }

    .sc-tags span {
      font-size: 0.75rem;
      padding: 0.28rem 0.72rem;
      border-radius: 999px;
      border: 1px solid rgba(247, 237, 226, 0.28);
      background: rgba(247, 237, 226, 0.12);
    }

    .sc-hero-tag {
      min-width: 220px;
      background: rgba(247, 237, 226, 0.12);
      border-radius: 16px;
      padding: 1rem 1.05rem;
      border: 1px solid rgba(247, 237, 226, 0.24);
    }

    .tag-pill {
      display: inline-block;
      padding: 0.2rem 0.7rem;
      border-radius: 999px;
      background: #f6bd60;
      color: #3d3d60;
      font-size: 0.68rem;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      margin-bottom: 0.45rem;
      font-weight: 700;
    }

    .sc-hero-tag p {
      margin: 0;
      font-size: 0.84rem;
      color: rgba(248, 250, 252, 0.86);
    }

    .sc-filters {
      display: flex;
      align-items: end;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
    }

    .filter-group label {
      font-size: 0.76rem;
      font-weight: 700;
      color: #5b6478;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .filter-group select {
      min-width: 180px;
      padding: 0.55rem 0.8rem;
      border-radius: 999px;
      border: 1px solid rgba(61, 61, 96, 0.16);
      background: #fff;
      font-size: 0.9rem;
      color: #1f2937;
    }

    .filter-group.checkbox {
      flex-direction: row;
      align-items: center;
      gap: 0.45rem;
      margin-left: auto;
    }

    .filter-group.checkbox label {
      text-transform: none;
      letter-spacing: normal;
      font-size: 0.88rem;
      color: #475569;
      font-weight: 600;
    }

    .btn-ghost {
      padding: 0.55rem 0.95rem;
      border-radius: 999px;
      border: 1px solid #d6d9e1;
      background: #fff;
      font-size: 0.85rem;
      color: #475569;
      cursor: pointer;
    }

    .banner {
      padding: 0.75rem 0.95rem;
      border-radius: 12px;
      font-size: 0.88rem;
    }

    .banner.info {
      background: #eef6f5;
      color: #2d5757;
    }

    .banner.error {
      background: #fee2e2;
      color: #b91c1c;
    }

    .sc-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 1.1rem;
    }

    .sc-card {
      background: linear-gradient(180deg, #ffffff, #fbfaf8);
      border-radius: 18px;
      padding: 1.1rem 1rem 1rem;
      border: 1px solid rgba(61, 61, 96, 0.1);
      box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
      display: flex;
      flex-direction: column;
      gap: 0.7rem;
      color: #1f2937;
    }

    .sc-card-header {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }

    .level-pill {
      display: inline-block;
      width: fit-content;
      padding: 0.2rem 0.65rem;
      border-radius: 999px;
      background: rgba(45, 87, 87, 0.12);
      color: #2d5757;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }

    .sc-card-header h3 {
      margin: 0;
      font-size: 1rem;
      color: #1e293b;
    }

    .sc-card-desc {
      margin: 0;
      font-size: 0.85rem;
      color: #64748b;
      min-height: 3.2rem;
    }

    .sc-meta {
      list-style: none;
      margin: 0;
      padding: 0;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.5rem 0.75rem;
      font-size: 0.79rem;
    }

    .sc-meta .label {
      display: block;
      font-size: 0.68rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: #94a3b8;
      margin-bottom: 0.15rem;
    }

    .sc-meta .value {
      font-weight: 600;
      color: #334155;
    }

    .sc-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 0.2rem;
    }

    .price-block .label {
      display: block;
      font-size: 0.68rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: #94a3b8;
    }

    .price-block .price {
      font-size: 1rem;
      font-weight: 700;
      color: #1f2937;
    }

    .price-block .price.muted {
      font-size: 0.85rem;
      font-weight: 500;
      color: #94a3b8;
    }

    .status-pill {
      padding: 0.28rem 0.72rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 700;
      background: rgba(34, 197, 94, 0.14);
      color: #15803d;
    }

    .status-pill.inactive {
      background: rgba(200, 70, 48, 0.12);
      color: #c84630;
    }

    .empty-state {
      grid-column: 1 / -1;
      text-align: center;
      padding: 1.8rem 1rem;
      background: linear-gradient(180deg, #fffaf5, #f7efe7);
      border-radius: 18px;
      border: 1px solid rgba(212, 196, 184, 0.45);
      color: #5b6478;
    }

    .empty-state h3 {
      margin: 0 0 0.35rem;
      color: #2d5757;
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.7rem;
      margin-top: 0.3rem;
    }

    .pagination button {
      padding: 0.35rem 0.8rem;
      border-radius: 999px;
      border: 1px solid #d6d9e1;
      background: #fff;
      font-size: 0.8rem;
      cursor: pointer;
    }

    .pagination .pages {
      display: flex;
      gap: 0.4rem;
    }

    .pagination .pages button.active {
      background: #3d3d60;
      color: #f9fafb;
      border-color: transparent;
    }

    @media (max-width: 1024px) {
      .sc-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (max-width: 768px) {
      .sc-hero {
        flex-direction: column;
        align-items: flex-start;
      }

      .filter-group.checkbox {
        margin-left: 0;
      }
    }

    @media (max-width: 600px) {
      .sc-grid {
        grid-template-columns: minmax(0, 1fr);
      }
    }
  `]
})
export class StudentCoursesComponent {
  private readonly courseCatalogService = inject(CourseCatalogService);

  courses: CourseCatalogItem[] = [];
  filteredCourses: CourseCatalogItem[] = [];
  levels: string[] = [];
  selectedLevel = '';
  onlyActive = true;
  readonly pageSize = 6;
  page = 1;
  totalPages = 1;
  loading = false;
  error: string | null = null;

  constructor() {
    this.loadCourses();
  }

  private loadCourses(): void {
    this.loading = true;
    this.error = null;
    this.courseCatalogService.getCourses().subscribe({
      next: (courses) => {
        this.courses = courses;
        this.levels = Array.from(new Set(courses.map(course => course.level).filter(Boolean)));
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load courses. Please try again later.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.courses];

    if (this.selectedLevel) {
      result = result.filter(course => course.level.toLowerCase() === this.selectedLevel.toLowerCase());
    }

    if (this.onlyActive) {
      result = result.filter(course => course.active);
    }

    this.filteredCourses = result;
    this.updatePagination();
  }

  clearFilters(): void {
    this.selectedLevel = '';
    this.onlyActive = true;
    this.applyFilters();
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
