import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserServiceService } from '../../../backoffice/services/user-service.service';
import { UpdateProfileRequest, UserResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  user: UserResponse | null = null;
  loading = true;
  errorMessage = '';

  editMode = false;
  saving = false;
  saveSuccess = false;

  form: UpdateProfileRequest = {};

  constructor(
    private authService: AuthService,
    private userService: UserServiceService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = '';
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.user = data;
        this.loading = false;
      },
      error: () => {
        // Fallback to cached user if /me endpoint not yet available
        this.user = this.authService.getCurrentUser();
        this.loading = false;
      }
    });
  }

  openEdit(): void {
    if (!this.user) return;
    this.form = {
      firstName:       this.user.firstName,
      lastName:        this.user.lastName,
      email:           this.user.email,
      level:           this.user.level           ?? '',
      learningGoals:   this.user.learningGoals   ?? '',
      bio:             this.user.bio             ?? '',
      specialization:  this.user.specialization  ?? '',
      experienceYears: this.user.experienceYears,
      hourlyRate:      this.user.hourlyRate,
    };
    this.editMode    = true;
    this.saveSuccess = false;
    this.errorMessage = '';
  }

  cancelEdit(): void {
    this.editMode = false;
    this.errorMessage = '';
  }

  saveEdit(): void {
    this.saving = true;
    this.errorMessage = '';
    this.userService.updateProfile(this.form).subscribe({
      next: (updated) => {
        this.user = updated;
        this.authService.updateCurrentUser(updated);
        this.saving      = false;
        this.editMode    = false;
        this.saveSuccess = true;
        setTimeout(() => (this.saveSuccess = false), 3000);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage =
          err.status === 403
            ? 'Access denied.'
            : 'Failed to update profile. Please try again.';
      }
    });
  }

  getInitials(): string {
    if (!this.user) return '?';
    return `${this.user.firstName?.[0] ?? ''}${this.user.lastName?.[0] ?? ''}`.toUpperCase();
  }

  isStudent(): boolean { return this.user?.role === 'STUDENT'; }
  isTutor():   boolean { return this.user?.role === 'TUTOR'; }
}



