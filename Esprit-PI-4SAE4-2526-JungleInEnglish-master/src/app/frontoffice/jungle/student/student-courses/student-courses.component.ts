import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Course } from '../../../../core/models/course.model';
import { CourseService } from '../../../../core/services/course.service';

@Component({
  selector: 'app-student-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './student-courses.component.html',
  styleUrl: './student-courses.component.css'
})
export class StudentCoursesComponent {

  courses: Course[] = [];
  filteredCourses: Course[] = [];

  levels: string[] = [];
  selectedLevel = '';
  onlyActive = true;

  readonly pageSize = 3;
  page = 1;
  totalPages = 1;

  loading = false;
  error: string | null = null;

  chatOpen = false;

  constructor(private courseService: CourseService) {
    this.loadCourses();
  }

  private loadCourses(): void {
    this.loading = true;
    this.error = null;
    this.courseService.getAll().subscribe({
      next: (courses: Course[]) => {
        this.courses = courses;
        this.levels = Array.from(
          new Set(
            courses
              .map((c: Course) => c.level)
              .filter((l: string) => !!l)
          )
        );
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
      result = result.filter(c => c.level.toLowerCase() === this.selectedLevel.toLowerCase());
    }

    if (this.onlyActive) {
      result = result.filter(c => c.active);
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

  get pagedCourses(): Course[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredCourses.slice(start, start + this.pageSize);
  }

  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages) {
      return;
    }
    this.page = p;
  }

  nextPage(): void {
    this.goToPage(this.page + 1);
  }

  prevPage(): void {
    this.goToPage(this.page - 1);
  }

  getCourseQrUrl(course: Course): string {
    const base = 'https://api.qrserver.com/v1/create-qr-code/';
    const payload = {
      title: course.title,
      level: course.level,
      startDate: course.startDate,
      durationHours: course.durationHours
    };
    const data = encodeURIComponent(JSON.stringify(payload));
    return `${base}?size=140x140&data=${data}`;
  }

  toggleChat(): void {
    this.chatOpen = !this.chatOpen;
  }
}

