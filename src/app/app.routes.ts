import { Routes } from '@angular/router';
import {LandingpageComponent} from './frontoffice/pages/landingpage/landingpage.component';
import {LoginComponent} from './frontoffice/pages/login/login.component';
import {SignupComponent} from './frontoffice/pages/signup/signup.component';

export const routes: Routes = [
  {path: '', component: LandingpageComponent},
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: SignupComponent},
];
