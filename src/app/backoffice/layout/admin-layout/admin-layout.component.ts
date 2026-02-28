import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardHeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NavItem } from '../../../core/models/nav-item.model';
import { CommonModule } from '@angular/common';

export const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard',    route: '/admin/dashboard', icon: '🏠' },
  { label: 'Users',        route: '/admin/users',     icon: '👥' },
  { label: 'Courses',      route: '#',                icon: '📚' },
  { label: 'Enrollments',  route: '#',                icon: '📋' },
  { label: 'Payments',     route: '#',                icon: '💳' },
  { label: 'Reports',      route: '#',                icon: '📊' },
  { label: 'Settings',     route: '#',                icon: '⚙️' },
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
    .dashboard-shell { display: flex; flex-direction: column; min-height: 100vh; }
    .dashboard-body  { display: flex; flex: 1; }
    .dashboard-main  { flex: 1; padding: 2rem; background: #fff; }
  `]
})
export class AdminLayoutComponent {
  navItems: NavItem[] = ADMIN_NAV;
}


