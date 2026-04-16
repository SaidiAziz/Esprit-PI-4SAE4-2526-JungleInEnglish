import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './frontoffice/layout/public-layout/public-layout.component';
import { DashboardLayoutComponent } from './frontoffice/layout/dashboard-layout/dashboard-layout.component';
import { AdminLayoutComponent } from './backoffice/layout/admin-layout/admin-layout.component';
import { LandingpageComponent } from './frontoffice/pages/landingpage/landingpage.component';
import { LoginComponent } from './frontoffice/pages/login/login.component';
import { SignupComponent } from './frontoffice/pages/signup/signup.component';
import { ForgotPasswordComponent } from './frontoffice/pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './frontoffice/pages/reset-password/reset-password.component';
import { Verify2FAComponent } from './frontoffice/pages/verify-2fa/verify-2fa.component';
import { StudentDashboardComponent } from './frontoffice/jungle/student/student-dashboard/student-dashboard.component';
import { StudentSessionsComponent } from './frontoffice/jungle/student/student-sessions/student-sessions.component';
import { StudentBookingsComponent } from './frontoffice/jungle/student/student-bookings/student-bookings.component';
import { StudentBookingHistoryComponent } from './frontoffice/jungle/student/student-booking-history/student-booking-history.component';
import { TutorDashboardComponent } from './frontoffice/jungle/tutor/tutor-dashboard/tutor-dashboard.component';
import { TutorQuizStatsComponent } from './frontoffice/jungle/tutor/Quiz/Tutor quiz stats.component';
import { TutorListComponent } from './frontoffice/jungle/student/tutor-list/tutor-list.component';
import { BookingFormComponent } from './frontoffice/jungle/student/booking-form/booking-form.component';
import { ProfileComponent } from './frontoffice/jungle/profile/profile.component';
import { SettingsComponent } from './frontoffice/jungle/settings/settings.component';
import { EventCardsComponent } from './frontoffice/pages/events/event-cards.component';
import { EventDashboardFrontComponent } from './frontoffice/pages/event-dashboard-front/event-dashboard-front.component';
import { LoyaltyComponent } from './frontoffice/pages/loyalty/loyalty.component';
import { ParticipateSessionComponent } from './frontoffice/pages/participate-session/participate-session.component';

import { AdminDashboardComponent } from './backoffice/pages/admin-dashboard/admin-dashboard.component';
import { EventDashboardComponent } from './backoffice/pages/event-dashboard/event-dashboard.component';
import { UsersComponent } from './backoffice/pages/users/users.component';
import { UserDetailComponent } from './backoffice/pages/users/user-detail/user-detail.component';
import { EventListComponent } from './backoffice/pages/events/event-list/event-list.component';
import { EventFormComponent } from './backoffice/pages/events/event-form/event-form.component';
import { EventSessionComponent } from './backoffice/pages/events/event-session/event-session.component';
import { SessionListComponent } from './backoffice/pages/sessionevents/session-list/session-list.component';
import { SessionFormComponent } from './backoffice/pages/sessionevents/session-form/session-form.component';
import { ParticipationListComponent } from './backoffice/pages/participations/participation-list/participation-list.component';
import { ParticipationFormComponent } from './backoffice/pages/participations/participation-form/participation-form.component';
import { PaymentDashboardComponent } from './backoffice/pages/payments/payment-dashboard/payment-dashboard.component';
import { PaymentEventsComponent } from './backoffice/pages/payments/payment-events/payment-events.component';
import { PaymentCoursComponent } from './backoffice/pages/payments/payment-cours/payment-cours.component';
import { LoyaltyCodesComponent } from './backoffice/pages/payments/loyalty-codes/loyalty-codes.component';
import { AvailabilityComponent } from './backoffice/pages/availability-page/Availability.component';
import { AdminFeedbacksComponent } from './backoffice/pages/admin-feedbacks/admin-feedbacks.component';
import { BookingListComponent } from './backoffice/pages/bookings-page/Booking-list.component';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [

  // -- Public Shell --
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', component: LandingpageComponent },
      { path: 'login', component: LoginComponent },
      { path: 'signup', component: SignupComponent },
      { path: 'forgot-password', component: ForgotPasswordComponent },
      { path: 'reset-password', component: ResetPasswordComponent },
      { path: 'verify-2fa', component: Verify2FAComponent },
    ]
  },

  // -- Authenticated Shell --
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
        path: 'student/settings',
        component: SettingsComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      { path: 'student/tutors', component: TutorListComponent, canActivate: [roleGuard('STUDENT')] },
      { path: 'student/sessions', component: StudentSessionsComponent, canActivate: [roleGuard('STUDENT')] },
      { path: 'student/booking-history', component: StudentBookingHistoryComponent, canActivate: [roleGuard('STUDENT')] },
      { path: 'student/bookings/new', component: BookingFormComponent, canActivate: [roleGuard('STUDENT')] },
      {
        path: 'student/bookings/edit/:id',
        canActivate: [roleGuard('STUDENT')],
        loadComponent: () => import('./backoffice/pages/bookings-page/Booking-form.component').then(m => m.BookingFormComponent)
      },
      { path: 'student/bookings', component: StudentBookingsComponent, canActivate: [roleGuard('STUDENT')] },
      {
        path: 'student/quiz',
        canActivate: [roleGuard('STUDENT')],
        loadComponent: () => import('./frontoffice/jungle/student/Quiz/student-quiz-list.component').then(m => m.StudentQuizListComponent)
      },
      {
        path: 'student/quiz/:id/take',
        canActivate: [roleGuard('STUDENT')],
        loadComponent: () => import('./frontoffice/jungle/student/Quiz/student-quiz-take.component').then(m => m.StudentQuizTakeComponent)
      },
      {
        path: 'student/results',
        canActivate: [roleGuard('STUDENT')],
        loadComponent: () => import('./frontoffice/jungle/student/Quiz/student-quiz-history.component').then(m => m.StudentQuizHistoryComponent)
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
      {
        path: 'tutor/settings',
        component: SettingsComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/sessions',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Session/tutor-sessions.component').then(m => m.TutorSessionsComponent)
      },
      {
        path: 'tutor/bookings',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Booking/Tutor-bookings.component').then(m => m.TutorBookingsComponent)
      },
      {
        path: 'tutor/availability',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./backoffice/pages/availability-page/Availability.component').then(m => m.AvailabilityComponent)
      },
      {
        path: 'tutor/students',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Student/tutor-students.component').then(m => m.TutorStudentsComponent)
      },
      {
        path: 'tutor/quiz',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Quiz/Tutor quiz list.component').then(m => m.TutorQuizListComponent)
      },
      {
        path: 'tutor/quiz/new',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Quiz/Tutor quiz form.component').then(m => m.TutorQuizFormComponent)
      },
      {
        path: 'tutor/quiz/edit/:id',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Quiz/Tutor quiz form.component').then(m => m.TutorQuizFormComponent)
      },
      {
        path: 'tutor/quiz/:id/results',
        canActivate: [roleGuard('TUTOR')],
        loadComponent: () => import('./frontoffice/jungle/tutor/Quiz/Tutor quiz results component').then(m => m.TutorQuizResultsComponent)
      },
      { path: 'tutor/quiz/:id/stats', component: TutorQuizStatsComponent, canActivate: [roleGuard('TUTOR')] },

      // Shared event features for authenticated learners
      { path: 'events', component: EventCardsComponent },
      { path: 'events/:id/participate', component: ParticipateSessionComponent },
      { path: 'stats-events', component: EventDashboardFrontComponent },
      { path: 'loyalty', component: LoyaltyComponent },
    ]
  },

  // -- Admin Shell --
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'event-dashboard', component: EventDashboardComponent },

      { path: 'users', component: UsersComponent },
      { path: 'users/:id', component: UserDetailComponent },

      { path: 'sessions', component: SessionListComponent },
      { path: 'availability', component: AvailabilityComponent },
      { path: 'feedbacks', component: AdminFeedbacksComponent },

      { path: 'events', component: EventListComponent },
      { path: 'events/add', component: EventFormComponent },
      { path: 'events/edit/:id', component: EventFormComponent },
      { path: 'events/:id/sessions', component: EventSessionComponent },
      { path: 'events/:eventId/sessions/add', component: SessionFormComponent },
      { path: 'events/:eventId/sessions/edit/:id', component: SessionFormComponent },

      { path: 'sessionevents', component: SessionListComponent },
      { path: 'sessionevents/add', redirectTo: 'events', pathMatch: 'full' },
      { path: 'sessionevents/edit/:id', redirectTo: 'events', pathMatch: 'full' },

      { path: 'participations', component: ParticipationListComponent },
      { path: 'participations/add', component: ParticipationFormComponent },
      { path: 'participations/edit/:id', component: ParticipationFormComponent },

      { path: 'bookings/new', loadComponent: () => import('./backoffice/pages/bookings-page/Booking-form.component').then(m => m.BookingFormComponent) },
      { path: 'bookings/edit/:id', loadComponent: () => import('./backoffice/pages/bookings-page/Booking-form.component').then(m => m.BookingFormComponent) },
      { path: 'bookings', component: BookingListComponent },
      { path: 'quiz', loadComponent: () => import('./backoffice/pages/AdminQuiz/admin-quiz.component').then(m => m.AdminQuizComponent) },
      { path: 'quiz/:id/questions', loadComponent: () => import('./backoffice/pages/Question/question-list/question-list.component').then(m => m.QuestionListComponent) },
      { path: 'quiz/:id/take', loadComponent: () => import('./backoffice/pages/Quiz/quiz-take/quiz-take.component').then(m => m.QuizTakeComponent) },

      { path: 'payments', redirectTo: 'payments/dashboard', pathMatch: 'full' },
      { path: 'payments/dashboard', component: PaymentDashboardComponent },
      { path: 'payments/events', component: PaymentEventsComponent },
      { path: 'payments/courses', component: PaymentCoursComponent },
      { path: 'payments/loyalty-codes', component: LoyaltyCodesComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
