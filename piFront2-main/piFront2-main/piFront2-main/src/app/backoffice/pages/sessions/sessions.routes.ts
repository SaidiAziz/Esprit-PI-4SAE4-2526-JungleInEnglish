import { Routes } from '@angular/router';

import { SessionListComponent } from './pages/session-list/session-list.component';
import { SessionFormComponent } from './pages/session-form/session-form.component';
import { SessionDetailsComponent } from './pages/session-details/session-details.component';
import { SessionStatisticsComponent } from './pages/session-statistics/session-statistics.component';
import { SessionAuditComponent } from './pages/session-audit/session-audit.component';

export const sessionsRoutes: Routes = [
  { path: '', component: SessionListComponent },
  { path: 'new', component: SessionFormComponent },

  // ✅ IMPORTANT : mettre avant :id
  { path: 'statistics', component: SessionStatisticsComponent },
  { path: 'audit', component: SessionAuditComponent },

  { path: ':id/edit', component: SessionFormComponent },
  { path: ':id', component: SessionDetailsComponent }
];