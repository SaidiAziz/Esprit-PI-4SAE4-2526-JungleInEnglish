import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { SessionAuditService } from '../../services/session-audit.service';
import { SessionAudit } from '../../models/session-audit.model';

@Component({
  selector: 'app-session-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './session-audit.component.html',
  styleUrls: ['./session-audit.component.css']
})
export class SessionAuditComponent implements OnInit {

  audits: SessionAudit[] = [];
  loading = false;
  errorMessage = '';
  sessionIdFilter: number | null = null;

  constructor(private sessionAuditService: SessionAuditService) {}

  ngOnInit(): void {
    this.loadAllAudits();
  }

  loadAllAudits(): void {
    this.loading = true;
    this.errorMessage = '';

    this.sessionAuditService.getAllAudits().subscribe({
      next: (data) => {
        this.audits = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement audit', err);
        this.errorMessage = 'Impossible de charger l’historique des sessions.';
        this.loading = false;
      }
    });
  }

  searchBySessionId(): void {
    if (this.sessionIdFilter === null || this.sessionIdFilter === undefined) {
      this.loadAllAudits();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.sessionAuditService.getAuditBySessionId(this.sessionIdFilter).subscribe({
      next: (data) => {
        this.audits = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement audit par session', err);
        this.errorMessage = 'Impossible de charger l’historique de cette session.';
        this.loading = false;
      }
    });
  }

  clearFilter(): void {
    this.sessionIdFilter = null;
    this.loadAllAudits();
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString();
  }

  getActionClass(actionType: string): string {
    switch (actionType) {
      case 'CREATED':
        return 'badge created';
      case 'UPDATED':
        return 'badge updated';
      case 'COMPLETED':
        return 'badge completed';
      case 'CANCELED':
        return 'badge canceled';
      case 'DELETED':
        return 'badge deleted';
      default:
        return 'badge';
    }
  }
}