import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NavItem } from '../../../core/models/nav-item.model';
import { STUDENT_NAV } from '../../jungle/student/student-nav';
import { TUTOR_NAV } from '../../jungle/tutor/tutor-nav';
import { DashboardHeaderComponent } from '../../../backoffice/layout/header/header.component';
import { SidebarComponent } from '../../../backoffice/layout/sidebar/sidebar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-layout',
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
export class DashboardLayoutComponent {
  navItems: NavItem[] = [];

  constructor(private authService: AuthService) {
    const role = this.authService.getUserRole();
    this.navItems = role === 'TUTOR' ? TUTOR_NAV : STUDENT_NAV;
  }
}

