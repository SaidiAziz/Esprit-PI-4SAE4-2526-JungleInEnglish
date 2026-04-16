import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './frontoffice/layout/public-layout/public-layout.component';
import { DashboardLayoutComponent } from './frontoffice/layout/dashboard-layout/dashboard-layout.component';
import { AdminLayoutComponent } from './backoffice/layout/admin-layout/admin-layout.component';
import { LandingpageComponent } from './frontoffice/pages/landingpage/landingpage.component';
import { LoginComponent } from './frontoffice/pages/login/login.component';
import { SignupComponent } from './frontoffice/pages/signup/signup.component';
import { StudentDashboardComponent } from './frontoffice/jungle/student/student-dashboard/student-dashboard.component';
import { TutorDashboardComponent } from './frontoffice/jungle/tutor/tutor-dashboard/tutor-dashboard.component';
import { AdminDashboardComponent } from './backoffice/pages/admin-dashboard/admin-dashboard.component';
import { UsersComponent } from './backoffice/pages/users/users.component';
import { UserDetailComponent } from './backoffice/pages/users/user-detail/user-detail.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
//events
import { EventListComponent } from './backoffice/pages/events/event-list/event-list.component';
import { EventFormComponent } from './backoffice/pages/events/event-form/event-form.component';
import { EventSessionComponent } from './backoffice/pages/events/event-session/event-session.component';
// ── Sessions ──
import { SessionListComponent } from './backoffice/pages/sessionevents/session-list/session-list.component';
import { SessionFormComponent } from './backoffice/pages/sessionevents/session-form/session-form.component';
// ── Participations ──
import { ParticipationListComponent } from './backoffice/pages/participations/participation-list/participation-list.component';
import { ParticipationFormComponent } from './backoffice/pages/participations/participation-form/participation-form.component';

// ── Front-office-Events ──
import { EventCardsComponent } from './frontoffice/pages/events/event-cards.component';
import { ParticipateSessionComponent } from './frontoffice/pages/participate-session/participate-session.component';
// ── Payments & dashboard ──
import { PaymentDashboardComponent } from './backoffice/pages/payments/payment-dashboard/payment-dashboard.component';
import { PaymentEventsComponent } from './backoffice/pages/payments/payment-events/payment-events.component';
import { PaymentCoursComponent } from './backoffice/pages/payments/payment-cours/payment-cours.component';
import { EventDashboardComponent } from './backoffice/pages/event-dashboard/event-dashboard.component';
import { EventDashboardFrontComponent } from './frontoffice/pages/event-dashboard-front/event-dashboard-front.component';
import { LoyaltyComponent } from './frontoffice/pages/loyalty/loyalty.component';
import { PaymentDashboardFrontComponent } from './frontoffice/pages/payment-dashboard-front/payment-dashboard-front.component';
export const routes: Routes = [
  // ── Public Shell ──────────────────────────────
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', component: LandingpageComponent },
      { path: 'login', component: LoginComponent },
      { path: 'signup', component: SignupComponent },
    ],
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
        canActivate: [roleGuard('STUDENT')],
      },
      {
        path: 'tutor/dashboard',
        component: TutorDashboardComponent,
        canActivate: [roleGuard('TUTOR')],
      },
      {
        path: 'events',
        component: EventCardsComponent,
      },
      {
        path: 'events/:id/participate',
        component: ParticipateSessionComponent,
      },
      {
        path: 'stats-events',
        component: EventDashboardFrontComponent,
      },
      {
        path: 'loyalty',
        component: LoyaltyComponent,
      },
      {
        path: 'payments',
        component: PaymentDashboardFrontComponent,
      },
    ],
  },

  // ── Admin Shell ────────────────────────────────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: UsersComponent },
      { path: 'users/:id', component: UserDetailComponent },

      // ── Events ──
      { path: 'events', component: EventListComponent },
      { path: 'events/add', component: EventFormComponent },
      { path: 'events/edit/:id', component: EventFormComponent },
      // ── Events-dashboard ──
      { path: 'events-dashboard', component: EventDashboardComponent },

      // ── Sessions liées à un event (nouveau flow) ─────────────────
      // /admin/events/:eventId/sessions       → liste + gestion des sessions
      // /admin/events/:eventId/sessions/add   → créer une session
      // /admin/events/:eventId/sessions/edit/:id → éditer une session
      { path: 'events/:eventId/sessions', component: EventSessionComponent },
      { path: 'events/:eventId/sessions/add', component: SessionFormComponent },
      {
        path: 'events/:eventId/sessions/edit/:id',
        component: SessionFormComponent,
      },

      // ── Sessions ──
      { path: 'sessionevents', component: SessionListComponent },
      { path: 'sessionevents/add', component: SessionFormComponent },
      { path: 'sessionevents/edit/:id', component: SessionFormComponent },

      // ── Participations ──
      { path: 'participations', component: ParticipationListComponent },
      { path: 'participations/add', component: ParticipationFormComponent },
      {
        path: 'participations/edit/:id',
        component: ParticipationFormComponent,
      },
      { path: 'participations/:id', component: ParticipationFormComponent },
      //payment
      {
        path: 'payments/dashboard',
        loadComponent: () =>
          import('./backoffice/pages/payments/payment-dashboard/payment-dashboard.component').then(
            (m) => m.PaymentDashboardComponent,
          ),
      },
      { path: 'payments/events', component: PaymentEventsComponent },
      { path: 'payments/cours', component: PaymentCoursComponent },
      {
        path: 'payments/loyalty',
        loadComponent: () =>
          import('./backoffice/pages/payments/loyalty-codes/loyalty-codes.component').then(
            (m) => m.LoyaltyCodesComponent,
          ),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
