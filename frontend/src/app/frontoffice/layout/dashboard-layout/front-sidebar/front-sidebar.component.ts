import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { NavItem } from '../../../../core/models/nav-item.model';

@Component({
  selector: 'app-front-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './front-sidebar.component.html',
  styleUrl: './front-sidebar.component.css'
})
export class FrontSidebarComponent {
  @Input() navItems: NavItem[] = [];

  constructor(private sanitizer: DomSanitizer) {}

  getIcon(item: NavItem): SafeHtml | null {
    return item.svgIcon ? this.sanitizer.bypassSecurityTrustHtml(item.svgIcon) : null;
  }
}
