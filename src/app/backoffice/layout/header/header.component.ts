import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { NavItem } from '../../../core/models/nav-item.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'] // ✅ (styleUrl -> styleUrls)
})
export class DashboardHeaderComponent {
  @Input() navItems: NavItem[] = [];

  constructor(public authService: AuthService, private router: Router) {}

  get currentUser() {
    return this.authService.getCurrentUser();
  }

  // ✅ Si le parent ne passe rien, on met un menu par défaut
  get computedNavItems(): NavItem[] {
    if (this.navItems && this.navItems.length) return this.navItems;

    const role = this.currentUser?.role;

    if (role === 'ADMIN') {
      return [
        { label: 'Dashboard', route: '/admin/dashboard' },
        { label: 'Users', route: '/admin/users' },
        { label: 'Timeslots', route: '/admin/timeslots' } // ✅ ajout
      ];
    }

    // fallback (tutor/student) - adapte si besoin
    return [{ label: 'Dashboard', route: '/' }];
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}