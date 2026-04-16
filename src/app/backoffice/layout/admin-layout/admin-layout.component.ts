import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardHeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NavItem } from '../../../core/models/nav-item.model';
import { CommonModule } from '@angular/common';

export const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/admin/dashboard' },
  { label: 'Event Dashboard', route: '/admin/event-dashboard' },
  { label: 'Users', route: '/admin/users' },
  { label: 'Courses', route: '/admin/courses' },
  { label: 'Forum Moderation', route: '/admin/forum' },
  { label: 'Collab Overview', route: '/admin/collaboration' },
  { label: 'Room Monitor', route: '/admin/collaboration/rooms' },
  { label: 'Activity', route: '/admin/collaboration/activity' },
  { label: 'AI Overview', route: '/admin/ai' },
  { label: 'AI Students', route: '/admin/ai/students' },
  { label: 'AI Recos', route: '/admin/ai/recommendations' },
  { label: 'Events', route: '/admin/events' },
  { label: 'Event Sessions', route: '/admin/sessionevents' },
  { label: 'Session Management', route: '/admin/sessions' },
  { label: 'Timeslots Calendar', route: '/admin/timeslots' },
  { label: 'Participations', route: '/admin/participations' },
  { label: 'Payments', route: '/admin/payments/dashboard' },
  { label: 'Loyalty Codes', route: '/admin/payments/loyalty-codes' },
  { label: 'Availability', route: '/admin/availability' },
  { label: 'Bookings', route: '/admin/bookings' },
  { label: 'Feedbacks', route: '/admin/feedbacks' },
  { label: 'Quizzes', route: '/admin/quiz' }
];

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, DashboardHeaderComponent, SidebarComponent],
  template: `
    <div class="dashboard-shell">
      <app-dashboard-header [navItems]="navItems"></app-dashboard-header>
      <div class="dashboard-body">
        <app-sidebar [navItems]="navItems"></app-sidebar>
        <main class="dashboard-main">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-shell { display: flex; flex-direction: column; min-height: 100vh; background: #0f172a; }
    .dashboard-body  { display: flex; flex: 1; min-height: calc(100vh - 60px); }
    .dashboard-main  { flex: 1; padding: 2rem 2.5rem; background: #f8fafc; overflow-y: auto; }
  `]
})
export class AdminLayoutComponent {
  navItems: NavItem[] = ADMIN_NAV;
}


