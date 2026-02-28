import { Routes } from '@angular/router';
import {LandingpageComponent} from './frontoffice/pages/landingpage/landingpage.component';
import {LoginComponent} from './frontoffice/pages/login/login.component';
import {SignupComponent} from './frontoffice/pages/signup/signup.component';
import {StudentDashboardComponent} from './frontoffice/jungle/student/student-dashboard/student-dashboard.component';
import {TutorDashboardComponent} from './frontoffice/jungle/tutor/tutor-dashboard/tutor-dashboard.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {path: '', component: LandingpageComponent},
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: SignupComponent},

  {path: 'student/dashboard', component: StudentDashboardComponent, canActivate: [authGuard]},
  {path: 'tutor/dashboard', component: TutorDashboardComponent, canActivate: [authGuard]},

  {path: '**', redirectTo: ''}
];
