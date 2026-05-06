// src/app/participation-list/participation-list.component.ts
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Participation, ParticipationStatus } from '../../../../core/models/participation.model';
import { ParticipationService } from '../../../../core/services/participation.service';
import { PaymentService } from '../../../../core/services/payment.service';

@Component({
  selector: 'app-participation-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './participation-list.component.html',
  styleUrls: ['./participation-list.component.css'],
})
export class ParticipationListComponent implements OnInit {
  participations: Participation[] = [];
  loading = false;
  error: string | null = null;

  // Toast
  toast: { message: string; type: 'success' | 'error' | 'info' } | null = null;
  private toastTimer: any;

  /** email → "Prénom Nom" résolu depuis les paiements */
  private nameMap = new Map<string, string>();

  constructor(
    private participationService: ParticipationService,
    private paymentService: PaymentService,
  ) {}

  ngOnInit(): void {
    this.fetchParticipations();
  }

  fetchParticipations(): void {
    this.loading = true;
    forkJoin({
      participations: this.participationService.getAllParticipations(),
      payments: this.paymentService.getAllPayments(),
    }).subscribe({
      next: ({ participations, payments }) => {
        // Construire map email → nom réel depuis les paiements
        (payments ?? []).forEach((pay: any) => {
          if (pay.participantEmail && pay.participantName) {
            this.nameMap.set(pay.participantEmail, pay.participantName);
          }
        });
        this.participations = participations;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors du chargement';
        this.loading = false;
      },
    });
  }

  /** Résout le nom réel depuis les paiements, sinon dérive depuis l'email */
  getParticipantName(p: Participation): string {
    if (p.userEmail && this.nameMap.has(p.userEmail)) {
      return this.nameMap.get(p.userEmail)!;
    }
    if (p.userName && p.userName.trim()) return p.userName.trim();
    return p.userEmail || '—';
  }

  // ── STATUS INLINE ──────────────────────────────────────────────

  onStatusChange(p: Participation, newStatus: string): void {
    if (!p.id) return;
    const previousStatus = p.status;
    const updated: Participation = { ...p, status: newStatus as ParticipationStatus };

    this.participationService.updateParticipation(p.id, updated).subscribe({
      next: (saved) => {
        // Update local list
        const idx = this.participations.findIndex((x) => x.id === p.id);
        if (idx !== -1) this.participations[idx] = { ...this.participations[idx], status: saved.status ?? (newStatus as ParticipationStatus) };

        if (newStatus === ParticipationStatus.COMPLETED) {
          // Toujours utiliser p (original) qui contient userEmail et eventTitle
          this.sendCertificate({ ...p, status: newStatus as ParticipationStatus });
        } else {
          this.showToast(`Statut mis à jour : ${this.statusLabel(newStatus)}`, 'success');
        }
      },
      error: () => {
        // Revert on error
        const idx = this.participations.findIndex((x) => x.id === p.id);
        if (idx !== -1) this.participations[idx] = { ...this.participations[idx], status: previousStatus };
        this.showToast('Erreur lors de la mise à jour du statut', 'error');
      },
    });
  }

  // ── CERTIFICATE ────────────────────────────────────────────────

  private sendCertificate(p: Participation): void {
    const name = this.getParticipantName(p);
    const email = p.userEmail || '';
    const eventTitle = p.eventTitle || 'Événement';
    const sessionDate = p.registrationDate
      ? new Date(p.registrationDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
      : new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });

    if (!email) {
      this.showToast('Statut mis à jour — email participant introuvable, certificat non envoyé', 'info');
      return;
    }

    this.showToast(`Envoi du certificat à ${email}…`, 'info');

    this.participationService
      .sendCertificate(email, name, eventTitle, sessionDate, p.sessionId ?? 0)
      .subscribe({
        next: () => this.showToast(`✅ Certificat envoyé avec succès à ${email}`, 'success'),
        error: () => this.showToast(`❌ Échec de l'envoi du certificat à ${email}`, 'error'),
      });
  }

  // ── HELPERS ────────────────────────────────────────────────────

  statusLabel(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'REGISTERED': return 'Inscrit';
      case 'CANCELLED':  return 'Annulé';
      case 'ATTENDED':   return 'Présent';
      default:           return status;
    }
  }

  statusClass(status: string | undefined): string {
    switch ((status || '').toUpperCase()) {
      case 'REGISTERED': return 'status-registered';
      case 'CANCELLED':  return 'status-cancelled';
      case 'ATTENDED':   return 'status-attended';
      default:           return '';
    }
  }

  showToast(message: string, type: 'success' | 'error' | 'info'): void {
    clearTimeout(this.toastTimer);
    this.toast = { message, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4500);
  }

  deleteParticipation(id: number): void {
    if (confirm('Voulez-vous vraiment supprimer cette participation ?')) {
      this.participationService.deleteParticipation(id).subscribe({
        next: () => {
          this.participations = this.participations.filter((p) => p.id !== id);
          this.showToast('Participation supprimée', 'success');
        },
        error: (err) => {
          this.showToast('Erreur lors de la suppression : ' + (err.message || err), 'error');
        },
      });
    }
  }
}