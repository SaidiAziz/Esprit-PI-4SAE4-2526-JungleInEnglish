import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseCatalogItem } from '../../../core/models/course.model';
import { CourseCatalogService } from '../../../core/services/course-catalog.service';

@Component({
  selector: 'app-admin-course-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="course-form-page">
      <div class="page-header">
        <div>
          <h2>{{ isEditing ? 'Edit course' : 'Create course' }}</h2>
          <p class="state-msg">{{ isEditing ? 'Update the course details.' : 'Add a new course to the shared catalog.' }}</p>
        </div>
        <button type="button" class="btn-secondary" (click)="cancel()">Back to list</button>
      </div>

      <section class="form-panel">
        <div *ngIf="loading" class="state-msg">Loading course...</div>
        <div *ngIf="error" class="error-banner">{{ error }}</div>

        <form [formGroup]="courseForm" (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label for="title">Title</label>
              <input id="title" type="text" formControlName="title">
              <div class="error" *ngIf="courseForm.get('title')?.touched && courseForm.get('title')?.invalid">Title is required.</div>
            </div>
            <div class="form-group">
              <label for="level">Level</label>
              <input id="level" type="text" formControlName="level" placeholder="Beginner, Intermediate...">
              <div class="error" *ngIf="courseForm.get('level')?.touched && courseForm.get('level')?.invalid">Level is required.</div>
            </div>
          </div>

          <div class="form-group">
            <label for="description">Description</label>
            <textarea id="description" rows="3" formControlName="description"></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="durationHours">Duration (hours)</label>
              <input id="durationHours" type="number" min="1" formControlName="durationHours">
            </div>
            <div class="form-group">
              <label for="price">Price</label>
              <input id="price" type="number" min="0" step="0.01" formControlName="price">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="startDate">Start date</label>
              <input id="startDate" type="date" formControlName="startDate">
            </div>
            <div class="form-group">
              <label for="endDate">End date</label>
              <input id="endDate" type="date" formControlName="endDate">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="maxStudents">Max students</label>
              <input id="maxStudents" type="number" min="1" formControlName="maxStudents">
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" formControlName="active">
                Active
              </label>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" [disabled]="saving">{{ isEditing ? 'Update course' : 'Create course' }}</button>
            <button type="button" class="btn-ghost" (click)="cancel()">Cancel</button>
          </div>
        </form>
      </section>
    </section>
  `,
  styles: [`
    .course-form-page {
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

    .form-panel {
      background: #fff;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      padding: 1.25rem 1.5rem;
      box-shadow: 0 1px 6px rgba(15, 23, 42, 0.06);
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

    form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .form-group label {
      font-size: 0.8rem;
      font-weight: 600;
      color: #64748b;
    }

    .form-group input,
    .form-group textarea {
      padding: 0.45rem 0.7rem;
      border-radius: 10px;
      border: 1px solid #cbd5e1;
      font-size: 0.85rem;
    }

    .checkbox-group {
      justify-content: center;
    }

    .error {
      font-size: 0.73rem;
      color: #b91c1c;
    }

    .form-actions {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.4rem;
    }

    .form-actions button[type='submit'] {
      padding: 0.45rem 0.95rem;
      border-radius: 999px;
      border: none;
      background: #4f46e5;
      color: #fff;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-secondary,
    .btn-ghost {
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

    @media (max-width: 600px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class AdminCourseFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly courseCatalogService = inject(CourseCatalogService);

  readonly courseForm = this.fb.group({
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

  isEditing = false;
  courseId: number | null = null;
  saving = false;
  loading = false;
  error: string | null = null;

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditing = true;
      this.courseId = Number(idParam);
      this.loadCourse(this.courseId);
    }
  }

  private loadCourse(id: number): void {
    this.loading = true;
    this.error = null;
    this.courseCatalogService.getCourseById(id).subscribe({
      next: (course) => {
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
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load course details.';
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

    const formValue = this.courseForm.getRawValue();
    const payload: Omit<CourseCatalogItem, 'id'> = {
      title: formValue.title ?? '',
      level: formValue.level ?? '',
      description: formValue.description || null,
      durationHours: formValue.durationHours ?? 1,
      startDate: formValue.startDate ?? '',
      endDate: formValue.endDate || null,
      price: formValue.price == null ? null : Number(formValue.price),
      maxStudents: formValue.maxStudents ?? 1,
      active: !!formValue.active
    };

    const request$ = this.isEditing && this.courseId != null
      ? this.courseCatalogService.updateCourse(this.courseId, payload)
      : this.courseCatalogService.createCourse(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/admin/courses']);
      },
      error: () => {
        this.error = 'Failed to save course.';
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/courses']);
  }
}
