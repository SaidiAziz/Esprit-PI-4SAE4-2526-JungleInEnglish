import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Course } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-admin-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-courses.component.html',
  styleUrl: './admin-courses.component.css'
})
export class AdminCoursesComponent {

  courses: Course[] = [];
  filteredCourses: Course[] = [];

  levelFilter = '';

  readonly pageSize = 3;
  page = 1;
  totalPages = 1;

  courseForm: FormGroup;
  isEditing = false;
  selectedCourseId: number | null = null;

  loading = false;
  saving = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService
  ) {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      level: ['', [Validators.required, Validators.maxLength(50)]],
      description: [''],
      durationHours: [1, [Validators.required, Validators.min(1)]],
      startDate: ['', Validators.required],
      endDate: [''],
      price: [0, [Validators.min(0)]],
      maxStudents: [1, [Validators.required, Validators.min(1)]],
      active: [true]
    });

    this.loadAllCourses();
  }

  private loadAllCourses(): void {
    this.loading = true;
    this.error = null;
    this.courseService.getAll().subscribe({
      next: (courses: Course[]) => {
        this.courses = courses;
        this.filteredCourses = courses;
        this.updatePagination();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load courses. Please check the backend (port 8089).';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    this.saving = true;
    this.error = null;

    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      this.saving = false;
      return;
    }

    const formValue = this.courseForm.value;

    const payload: Course = {
      title: formValue.title,
      level: formValue.level,
      description: formValue.description || null,
      durationHours: formValue.durationHours,
      startDate: formValue.startDate,
      endDate: formValue.endDate || null,
      price: formValue.price === null || formValue.price === '' ? null : formValue.price,
      maxStudents: formValue.maxStudents,
      active: formValue.active
    };

    const request$ = this.isEditing && this.selectedCourseId != null
      ? this.courseService.update(this.selectedCourseId, payload)
      : this.courseService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.resetForm();
        this.loadAllCourses();
      },
      error: () => {
        this.error = 'Failed to save course. Please verify your data and backend.';
        this.saving = false;
      }
    });
  }

  editCourse(course: Course): void {
    this.isEditing = true;
    this.selectedCourseId = course.id ?? null;

    this.courseForm.patchValue({
      title: course.title,
      level: course.level,
      description: course.description ?? '',
      durationHours: course.durationHours,
      startDate: course.startDate,
      endDate: course.endDate ?? '',
      price: course.price ?? 0,
      maxStudents: course.maxStudents,
      active: course.active
    });
  }

  deleteCourse(course: Course): void {
    if (!course.id) {
      return;
    }
    this.loading = true;
    this.error = null;
    this.courseService.delete(course.id).subscribe({
      next: () => {
        this.loadAllCourses();
      },
      error: () => {
        this.error = 'Failed to delete course.';
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.courseForm.reset({
      title: '',
      level: '',
      description: '',
      durationHours: 1,
      startDate: '',
      endDate: '',
      price: 0,
      maxStudents: 1,
      active: true
    });
    this.isEditing = false;
    this.selectedCourseId = null;
  }

  searchByLevel(): void {
    const level = this.levelFilter.trim();
    this.error = null;

    if (!level) {
      this.loadAllCourses();
      return;
    }

    this.loading = true;
    this.courseService.searchByLevel(level).subscribe({
      next: (courses: Course[]) => {
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

  exportToPdf(): void {
    const doc = new jsPDF({
      orientation: 'landscape',
      unit: 'pt',
      format: 'a4'
    });

    const title = 'Courses Catalog';
    const subtitle = 'Generated from Jungle in English admin';
    const dateText = new Date().toLocaleString();

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(18);
    doc.setTextColor(15, 23, 42);
    doc.text(title, 40, 40);

    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 116, 139);
    doc.text(subtitle, 40, 60);
    doc.text(dateText, 40, 78);

    const rows = this.filteredCourses.map(c => ([
      c.title,
      c.level,
      c.durationHours?.toString() ?? '',
      c.startDate ?? '',
      c.endDate ?? '',
      c.price != null ? `${c.price}` : '',
      c.maxStudents?.toString() ?? '',
      c.active ? 'Yes' : 'No'
    ]));

    autoTable(doc, {
      startY: 95,
      head: [[
        'Title',
        'Level',
        'Duration (h)',
        'Start',
        'End',
        'Price',
        'Max students',
        'Active'
      ]],
      body: rows,
      styles: {
        fontSize: 10,
        cellPadding: 6
      },
      headStyles: {
        fillColor: [15, 23, 42],
        textColor: [248, 250, 252],
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [248, 249, 252]
      },
      bodyStyles: {
        textColor: [30, 64, 175]
      },
      theme: 'striped'
    });

    doc.save('courses.pdf');
  }
}

