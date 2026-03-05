import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Course } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-admin-course-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-course-form.component.html',
  styleUrl: './admin-course-form.component.css'
})
export class AdminCourseFormComponent implements OnInit {

  courseForm: FormGroup;
  isEditing = false;
  courseId: number | null = null;

  saving = false;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
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
  }

  ngOnInit(): void {
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
    this.courseService.getById(id).subscribe({
      next: (course: Course) => {
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

    const request$ = this.isEditing && this.courseId != null
      ? this.courseService.update(this.courseId, payload)
      : this.courseService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/courses']);
      },
      error: () => {
        this.error = 'Failed to save course. Please verify your data and backend.';
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/courses']);
  }
}

