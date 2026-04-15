import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { TimeslotsCalendarComponent } from '../timeslots-calendar/timeslots-calendar.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, TimeslotsCalendarComponent],
  templateUrl: './student-dashboard.component.html',
  styleUrl: './student-dashboard.component.css'
})
export class StudentDashboardComponent {
  constructor(public authService: AuthService) {}
  get user() { return this.authService.getCurrentUser(); }
}
