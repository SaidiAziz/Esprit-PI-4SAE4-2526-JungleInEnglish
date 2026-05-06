import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { NavItem } from '../../../../core/models/nav-item.model';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-front-dashboard-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './front-dashboard-header.component.html',
  styleUrl: './front-dashboard-header.component.css'
})
export class FrontDashboardHeaderComponent {
  @Input() navItems: NavItem[] = [];

  constructor(
    public authService: AuthService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  get currentUser() {
    return this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getIcon(item: NavItem): SafeHtml | null {
    return item.svgIcon ? this.sanitizer.bypassSecurityTrustHtml(item.svgIcon) : null;
  }
}
