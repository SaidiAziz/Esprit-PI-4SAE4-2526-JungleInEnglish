import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardHeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NavItem } from '../../../core/models/nav-item.model';
import { CommonModule } from '@angular/common';

export const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/admin/dashboard' },
  { label: 'Users', route: '/admin/users' },

  {
    label: 'Events',
    route: '/admin/events-dashboard', // ← route par défaut (dashboard)
    children: [
      { label: ' Dashboard', route: '/admin/events-dashboard' },
      { label: 'Événements', route: '/admin/events' },
      { label: 'Sessions', route: '/admin/sessionevents' },
      { label: 'Participations', route: '/admin/participations' },
    ],
  },
  {
    label: 'Payment',
    route: '/admin/payments/dashboard',
    children: [
      { label: 'Dashboard', route: '/admin/payments/dashboard' },
      { label: 'Events', route: '/admin/payments/events' },
      { label: 'Cours', route: '/admin/payments/cours' },
      { label: '🎁 Loyalty Codes', route: '/admin/payments/loyalty' },
    ],
  },
  //{ label: 'Courses',      route: '#' },
  //{ label: 'Enrollments',  route: '#' },
  //{ label: 'Payments',     route: '#' },
  //{ label: 'Reports',      route: '#' },
  //{ label: 'Settings',     route: '#' },
];

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    DashboardHeaderComponent,
    SidebarComponent,
  ],
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
  styles: [
    `
      .dashboard-shell {
        display: flex;
        flex-direction: column;
        min-height: 100vh;
        background: #0f172a;
      }
      .dashboard-body {
        display: flex;
        flex: 1;
        min-height: calc(100vh - 60px);
      }
      .dashboard-main {
        flex: 1;
        padding: 2rem 2.5rem;
        background: #f8fafc;
        overflow-y: auto;
      }
    `,
  ],
})
export class AdminLayoutComponent {
  navItems: NavItem[] = ADMIN_NAV;
}
