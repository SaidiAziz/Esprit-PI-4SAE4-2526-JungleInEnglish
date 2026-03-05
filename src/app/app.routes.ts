import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './frontoffice/layout/public-layout/public-layout.component';
import { DashboardLayoutComponent } from './frontoffice/layout/dashboard-layout/dashboard-layout.component';
import { AdminLayoutComponent } from './backoffice/layout/admin-layout/admin-layout.component';
import { LandingpageComponent } from './frontoffice/pages/landingpage/landingpage.component';
import { LoginComponent } from './frontoffice/pages/login/login.component';
import { SignupComponent } from './frontoffice/pages/signup/signup.component';
import { ForgotPasswordComponent } from './frontoffice/pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './frontoffice/pages/reset-password/reset-password.component';
import { StudentDashboardComponent } from './frontoffice/jungle/student/student-dashboard/student-dashboard.component';
import { TutorDashboardComponent } from './frontoffice/jungle/tutor/tutor-dashboard/tutor-dashboard.component';
import { ProfileComponent } from './frontoffice/jungle/profile/profile.component';
import { AdminDashboardComponent } from './backoffice/pages/admin-dashboard/admin-dashboard.component';
import { UsersComponent } from './backoffice/pages/users/users.component';
import { UserDetailComponent } from './backoffice/pages/users/user-detail/user-detail.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [

  // ── Public Shell ──────────────────────────────
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '',                 component: LandingpageComponent },
      { path: 'login',            component: LoginComponent },
      { path: 'signup',           component: SignupComponent },
      { path: 'forgot-password',  component: ForgotPasswordComponent },
      { path: 'reset-password',   component: ResetPasswordComponent },
    ]
  },

  // ── Authenticated Shell ────────────────────────
  {
    path: '',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'student/dashboard',
        component: StudentDashboardComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/profile',
        component: ProfileComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'tutor/dashboard',
        component: TutorDashboardComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/profile',
        component: ProfileComponent,
        canActivate: [roleGuard('TUTOR')]
      },
    ]
  },

  // ── Admin Shell ────────────────────────────────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users',     component: UsersComponent },
      { path: 'users/:id', component: UserDetailComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
