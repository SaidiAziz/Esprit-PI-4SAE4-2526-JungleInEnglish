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
import { StudentAiInsightsComponent } from './frontoffice/jungle/student/student-ai-insights/student-ai-insights.component';
import { StudentLearningPathComponent } from './frontoffice/jungle/student/student-learning-path/student-learning-path.component';
import { StudentRecommendationsComponent } from './frontoffice/jungle/student/student-recommendations/student-recommendations.component';
import { StudentCollaborationHubComponent } from './frontoffice/jungle/student/student-collaboration-hub/student-collaboration-hub.component';
import { StudentBadgesComponent } from './frontoffice/jungle/student/student-badges/student-badges.component';
import { TutorDashboardComponent } from './frontoffice/jungle/tutor/tutor-dashboard/tutor-dashboard.component';
import { TutorRoomsComponent } from './frontoffice/jungle/tutor/tutor-rooms/tutor-rooms.component';
import { TutorChallengesComponent } from './frontoffice/jungle/tutor/tutor-challenges/tutor-challenges.component';
import { TutorAnalyticsComponent } from './frontoffice/jungle/tutor/tutor-analytics/tutor-analytics.component';
import { CollaborationRoomViewComponent } from './frontoffice/jungle/shared/collaboration-room-view/collaboration-room-view.component';
import { AdminDashboardComponent } from './backoffice/pages/admin-dashboard/admin-dashboard.component';
import { UsersComponent } from './backoffice/pages/users/users.component';
import { UserDetailComponent } from './backoffice/pages/users/user-detail/user-detail.component';
import { CollaborationAdminDashboardComponent } from './backoffice/pages/collaboration-admin/collaboration-admin-dashboard.component';
import { CollaborationAdminRoomsComponent } from './backoffice/pages/collaboration-admin/collaboration-admin-rooms.component';
import { CollaborationAdminActivityComponent } from './backoffice/pages/collaboration-admin/collaboration-admin-activity.component';
import { AiAdminDashboardComponent } from './backoffice/pages/ai-admin/ai-admin-dashboard.component';
import { AiAdminStudentsComponent } from './backoffice/pages/ai-admin/ai-admin-students.component';
import { AiAdminRecommendationsComponent } from './backoffice/pages/ai-admin/ai-admin-recommendations.component';
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
        path: 'student/ai-insights',
        component: StudentAiInsightsComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/courses',
        loadComponent: () => import('./frontoffice/jungle/student/student-courses/student-courses.component').then(m => m.StudentCoursesComponent),
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/forum',
        loadComponent: () => import('./frontoffice/jungle/student/student-forum/student-forum.component').then(m => m.StudentForumComponent),
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/forum/new',
        loadComponent: () => import('./frontoffice/jungle/student/student-forum-new/student-forum-new.component').then(m => m.StudentForumNewComponent),
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/forum/:id',
        loadComponent: () => import('./frontoffice/jungle/student/student-forum-detail/student-forum-detail.component').then(m => m.StudentForumDetailComponent),
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/learning-path',
        component: StudentLearningPathComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/recommendations',
        component: StudentRecommendationsComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/collaboration',
        component: StudentCollaborationHubComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/collaboration/room/:id',
        component: CollaborationRoomViewComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'student/badges',
        component: StudentBadgesComponent,
        canActivate: [roleGuard('STUDENT')]
      },
      {
        path: 'tutor/dashboard',
        component: TutorDashboardComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/rooms',
        component: TutorRoomsComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/challenges',
        component: TutorChallengesComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/analytics',
        component: TutorAnalyticsComponent,
        canActivate: [roleGuard('TUTOR')]
      },
      {
        path: 'tutor/collaboration/room/:id',
        component: CollaborationRoomViewComponent,
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
      { path: 'courses', loadComponent: () => import('./backoffice/pages/courses/admin-courses.component').then(m => m.AdminCoursesComponent) },
      { path: 'courses/new', loadComponent: () => import('./backoffice/pages/courses/admin-course-form.component').then(m => m.AdminCourseFormComponent) },
      { path: 'courses/:id', loadComponent: () => import('./backoffice/pages/courses/admin-course-form.component').then(m => m.AdminCourseFormComponent) },
      { path: 'forum', loadComponent: () => import('./backoffice/pages/forum-moderation/forum-moderation.component').then(m => m.ForumModerationComponent) },
      { path: 'users',     component: UsersComponent },
      { path: 'users/:id', component: UserDetailComponent },
      { path: 'collaboration', component: CollaborationAdminDashboardComponent },
      { path: 'collaboration/rooms', component: CollaborationAdminRoomsComponent },
      { path: 'collaboration/activity', component: CollaborationAdminActivityComponent },
      { path: 'ai', component: AiAdminDashboardComponent },
      { path: 'ai/students', component: AiAdminStudentsComponent },
      { path: 'ai/recommendations', component: AiAdminRecommendationsComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
