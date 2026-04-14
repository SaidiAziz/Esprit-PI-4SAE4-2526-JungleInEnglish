import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { NavItem } from '../../../core/models/nav-item.model';
import { filter } from 'rxjs/operators';
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent implements OnInit {
  @Input() navItems: NavItem[] = [];
  openMenus: { [key: string]: boolean } = {};

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.autoOpenCurrent();
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        this.autoOpenCurrent();
      });
  }

  private autoOpenCurrent(): void {
    const url = this.router.url;
    this.navItems.forEach((item) => {
      if (item.children) {
        const hasActive = item.children.some(
          (c) => c.route && url.startsWith(c.route),
        );
        if (hasActive) this.openMenus[item.label] = true;
      }
    });
  }

  toggle(label: string): void {
    this.openMenus[label] = !this.openMenus[label];
  }

  isOpen(label: string): boolean {
    return !!this.openMenus[label];
  }
}
