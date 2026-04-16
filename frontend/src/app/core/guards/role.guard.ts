import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Usage in routes:
 *   canActivate: [authGuard, roleGuard('STUDENT')]
 *
 * Reads the required role from route.data['role'] and compares
 * against the logged-in user's role. Redirects to the correct
 * dashboard if the role doesn't match.
 */
export const roleGuard = (requiredRole: 'STUDENT' | 'TUTOR' | 'ADMIN'): CanActivateFn => {
  return (_route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);

    if (!isPlatformBrowser(platformId)) {
      return true;
    }

    const userRole = authService.getUserRole();

    if (userRole === requiredRole) {
      return true;
    }

    // Redirect to their own dashboard instead of letting them in
    if (userRole === 'STUDENT') {
      router.navigate(['/student/dashboard']);
    } else if (userRole === 'TUTOR') {
      router.navigate(['/tutor/dashboard']);
    } else if (userRole === 'ADMIN') {
      router.navigate(['/admin/dashboard']);
    } else {
      router.navigate(['/login']);
    }

    return false;
  };
};
