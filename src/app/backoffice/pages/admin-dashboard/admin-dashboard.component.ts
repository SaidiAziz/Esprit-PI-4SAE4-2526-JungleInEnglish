import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],  // ✅ indispensable pour routerLink dans le HTML
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'] // ✅ (styleUrl -> styleUrls)
})
export class AdminDashboardComponent {}