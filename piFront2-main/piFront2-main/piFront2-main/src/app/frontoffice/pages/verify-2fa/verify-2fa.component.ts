import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserServiceService } from '../../../backoffice/services/user-service.service';

@Component({
  selector: 'app-verify-2fa',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './verify-2fa.component.html',
  styleUrl: './verify-2fa.component.css'
})
export class Verify2FAComponent implements OnInit {
  code = '';
  method = '';
  email = '';
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private userService: UserServiceService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.email  = sessionStorage.getItem('2fa_email')  ?? '';
      this.method = sessionStorage.getItem('2fa_method') ?? 'EMAIL';
      if (!this.email) this.router.navigate(['/login']);
    }
  }

  verify(): void {
    if (this.code.length !== 6 || this.loading) return;
    this.loading = true;
    this.errorMessage = '';

    this.authService.verify2FA(this.email, this.code).subscribe({
      next: (res) => {
        if (isPlatformBrowser(this.platformId)) {
          sessionStorage.removeItem('2fa_email');
          sessionStorage.removeItem('2fa_method');
          // Store token so interceptor and getProfile() can use it
          localStorage.setItem('auth_token', res.token);
        }
        // Fetch user profile to populate cache and determine redirect
        this.userService.getProfile().subscribe({
          next: (user) => {
            this.authService.updateCurrentUser(user);
            const role = user.role;
            if (role === 'STUDENT')     this.router.navigate(['/student/dashboard']);
            else if (role === 'TUTOR')  this.router.navigate(['/tutor/dashboard']);
            else if (role === 'ADMIN')  this.router.navigate(['/admin/dashboard']);
            else                        this.router.navigate(['/']);
          },
          error: () => this.router.navigate(['/'])
        });
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message ?? 'Invalid or expired code.';
        this.loading = false;
      }
    });
  }
}


