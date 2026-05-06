import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ParticipationService } from '../../../core/services/participation.service';
import { PaymentService } from '../../../core/services/payment.service';
@Component({
  selector: 'app-event-dashboard-front',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './event-dashboard-front.component.html',
  styleUrl: './event-dashboard-front.component.css',
})
export class EventDashboardFrontComponent implements OnInit {
  currentUser: any = null;
  participations: any[] = [];
  payments: any[] = [];
  loading = true;

  // Stats
  totalPaid = 0;
  upcomingSessions = 0;
  totalEvents = 0;
  pendingPayments = 0;

  // Timeline unifiée
  timeline: any[] = [];

  // Filtre paiements
  paymentFilter: 'ALL' | 'PAID' | 'PENDING' = 'ALL';

  constructor(
    private authService: AuthService,
    private participationService: ParticipationService,
    private paymentService: PaymentService,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) return;
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    const email = this.currentUser.email;

    forkJoin({
      participations: this.participationService.getAllParticipations(),
      payments: this.paymentService.getAllPayments(),
    }).subscribe({
      next: ({ participations, payments }) => {
        // ✅ Filtrer par email du user connecté
        this.participations = participations.filter(
          (p: any) => p.userEmail === email,
        );
        this.payments = payments.filter(
          (p: any) => p.participantEmail === email,
        );

        this.computeStats();
        this.buildTimeline();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  computeStats(): void {
    const today = new Date();

    this.totalEvents = this.participations.length;

    this.totalPaid = this.payments
      .filter((p) => p.status === 'PAID')
      .reduce((sum, p) => sum + (p.amount || 0), 0);

    this.pendingPayments = this.payments.filter(
      (p) => p.status === 'PENDING',
    ).length;

    // Sessions à venir (registrationDate >= aujourd'hui)
    this.upcomingSessions = this.participations.filter((p) => {
      if (!p.registrationDate) return false;
      return new Date(p.registrationDate) >= today;
    }).length;
  }

  buildTimeline(): void {
    const events = this.participations.map((p) => ({
      type: 'participation',
      date: p.registrationDate,
      title: p.eventTitle || 'Événement',
      subtitle: `Session · ${p.sessionId}`,
      status: p.status,
      paymentStatus: p.paymentStatus,
      amount: p.amountPaid,
      method: p.paymentMethod,
      raw: p,
    }));

    const pays = this.payments.map((p) => ({
      type: 'payment',
      date: p.paymentDate,
      title: `Paiement — ${p.participantName || ''}`,
      subtitle: p.reference || '',
      status: p.status,
      amount: p.amount,
      method: p.paymentMethod,
      raw: p,
    }));

    this.timeline = [...events, ...pays].sort(
      (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime(),
    );
  }

  get filteredPayments(): any[] {
    if (this.paymentFilter === 'ALL') return this.payments;
    return this.payments.filter((p) => p.status === this.paymentFilter);
  }

  setPaymentFilter(f: 'ALL' | 'PAID' | 'PENDING'): void {
    this.paymentFilter = f;
  }

  getProgressStep(p: any): number {
    if (p.status === 'CANCELLED') return 0;
    if (p.paymentStatus === 'PAID' && p.status === 'REGISTERED') return 2;
    if (p.status === 'REGISTERED') return 1;
    if (p.status === 'ATTENDED') return 3;
    return 1;
  }

  cancelParticipation(id: number): void {
    if (!confirm("Confirmer l'annulation ?")) return;
    this.participationService.deleteParticipation(id).subscribe({
      next: () => this.loadData(),
      error: () => alert("Erreur lors de l'annulation"),
    });
  }

  downloadReceipt(payment: any): void {
    const paymentDateFull = payment.paymentDate
      ? new Date(payment.paymentDate).toLocaleDateString('fr-FR', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        })
      : '—';
    const paymentTime = payment.paymentDate
      ? new Date(payment.paymentDate).toLocaleTimeString('fr-FR', {
          hour: '2-digit',
          minute: '2-digit',
        })
      : '';

    const statusLabel = (s: string) => {
      switch ((s || '').toUpperCase()) {
        case 'PAID':
          return 'Payé';
        case 'PENDING':
          return 'En attente';
        case 'FAILED':
          return 'Échoué';
        default:
          return s;
      }
    };
    const methodLabel = (m: string) => {
      switch ((m || '').toUpperCase()) {
        case 'CREDIT_CARD':
          return 'Carte bancaire';
        case 'BANK_TRANSFER':
          return 'Virement bancaire';
        case 'CASH':
          return 'Espèces';
        case 'ONLINE_PAYMENT':
          return 'Paiement en ligne';
        default:
          return m || '—';
      }
    };
    const methodIcon = (m: string) => {
      switch ((m || '').toUpperCase()) {
        case 'CREDIT_CARD':
          return '💳';
        case 'BANK_TRANSFER':
          return '🏦';
        case 'CASH':
          return '💵';
        case 'ONLINE_PAYMENT':
          return '🌐';
        default:
          return '💳';
      }
    };

    const statusColor =
      payment.status === 'PAID'
        ? '#16a34a'
        : payment.status === 'PENDING'
          ? '#d97706'
          : '#dc2626';
    const statusBg =
      payment.status === 'PAID'
        ? '#dcfce7'
        : payment.status === 'PENDING'
          ? '#fef3c7'
          : '#fee2e2';
    const participantName =
      payment.participantName ||
      (this.currentUser
        ? this.currentUser.firstName + ' ' + this.currentUser.lastName
        : '—');

    // Look for event title from participation list
    const participation = this.participations.find(
      (p: any) =>
        p.sessionId === payment.sessionId || p.eventId === payment.eventId,
    );
    const eventTitle =
      payment.eventTitle ||
      participation?.eventTitle ||
      (payment.eventId ? `Événement #${payment.eventId}` : '—');

    const sessionLabel = participation?.registrationDate
      ? new Date(participation.registrationDate).toLocaleDateString('fr-FR', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        })
      : payment.sessionId
        ? `Session #${payment.sessionId}`
        : '—';

    const html = `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8"/>
  <title>Reçu ${payment.reference || payment.id}</title>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Inter', Arial, sans-serif; background: #f8fafc; color: #1e293b; }
    .page { width: 680px; margin: 40px auto; background: #fff; border-radius: 16px; box-shadow: 0 4px 32px rgba(0,0,0,.10); overflow: hidden; }

    .header { background: linear-gradient(135deg, #1a3c2e 0%, #2d6a4f 60%, #40916c 100%); padding: 36px 40px 28px; color: #fff; }
    .header-top { display: flex; justify-content: space-between; align-items: flex-start; }
    .brand { display: flex; align-items: center; gap: 12px; }
    .brand-logo { width: 44px; height: 44px; background: rgba(255,255,255,.15); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
    .brand-name { font-size: 22px; font-weight: 700; letter-spacing: .5px; }
    .brand-sub  { font-size: 12px; color: rgba(255,255,255,.7); margin-top: 2px; }
    .receipt-badge { background: rgba(255,255,255,.15); border: 1px solid rgba(255,255,255,.25); border-radius: 8px; padding: 6px 14px; font-size: 12px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; }
    .header-bottom { margin-top: 24px; display: flex; justify-content: space-between; align-items: flex-end; }
    .ref-label { font-size: 11px; color: rgba(255,255,255,.6); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
    .ref-value { font-size: 20px; font-weight: 700; letter-spacing: 1px; }
    .amount-block { text-align: right; }
    .amount-label { font-size: 11px; color: rgba(255,255,255,.6); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
    .amount-value { font-size: 36px; font-weight: 700; }
    .amount-currency { font-size: 16px; font-weight: 400; opacity: .8; }

    .status-bar { background: ${statusBg}; border-bottom: 2px solid ${statusColor}; padding: 12px 40px; display: flex; align-items: center; justify-content: space-between; }
    .status-pill { display: inline-flex; align-items: center; gap: 8px; background: ${statusColor}; color: #fff; border-radius: 20px; padding: 4px 16px; font-size: 13px; font-weight: 600; }
    .status-dot { width: 8px; height: 8px; background: #fff; border-radius: 50%; }
    .status-date { font-size: 12px; color: ${statusColor}; font-weight: 500; }

    .body { padding: 32px 40px; }
    .section { margin-bottom: 28px; }
    .section-title { font-size: 11px; text-transform: uppercase; letter-spacing: 1.5px; color: #94a3b8; font-weight: 600; margin-bottom: 14px; padding-bottom: 8px; border-bottom: 1px solid #f1f5f9; }
    .row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px dashed #f1f5f9; }
    .row:last-child { border-bottom: none; }
    .row-key { font-size: 13px; color: #64748b; font-weight: 500; }
    .row-val { font-size: 13px; color: #1e293b; font-weight: 600; text-align: right; max-width: 60%; }

    .event-box { background: linear-gradient(135deg, #f0fdf4, #dcfce7); border: 1px solid #86efac; border-radius: 10px; padding: 16px 20px; margin-bottom: 28px; }
    .event-box-label { font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #16a34a; font-weight: 600; margin-bottom: 6px; }
    .event-box-title { font-size: 17px; font-weight: 700; color: #14532d; }
    .event-box-session { font-size: 13px; color: #166534; margin-top: 4px; }

    .method-badge { display: inline-flex; align-items: center; gap: 6px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 4px 12px; font-size: 13px; font-weight: 600; }

    .footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; }
    .footer-note { font-size: 11px; color: #94a3b8; line-height: 1.6; max-width: 380px; }
    .footer-logo { font-size: 20px; font-weight: 800; color: #2d6a4f; letter-spacing: .5px; opacity: .6; }

    .print-bar { display: flex; justify-content: center; padding: 20px; background: #f1f5f9; }
    #btn-print { display: inline-flex; align-items: center; gap: 10px; background: linear-gradient(135deg,#2d6a4f,#40916c); color: #fff; border: none; border-radius: 10px; padding: 12px 28px; font-size: 15px; font-weight: 600; cursor: pointer; box-shadow: 0 4px 14px rgba(45,106,79,.35); transition: transform .15s; }
    #btn-print:hover { transform: translateY(-2px); }
    @media print {
      body { background: #fff; }
      .page { box-shadow: none; margin: 0; border-radius: 0; width: 100%; }
      .print-bar { display: none !important; }
    }
  </style>
</head>
<body>
<div class="print-bar">
  <button id="btn-print">🖨️ Imprimer / Enregistrer en PDF</button>
</div>
<div class="page">

  <div class="header">
    <div class="header-top">
      <div class="brand">
        <div class="brand-logo">🌿</div>
        <div>
          <div class="brand-name">Jungle</div>
          <div class="brand-sub">Plateforme d'apprentissage</div>
        </div>
      </div>
      <div class="receipt-badge">Reçu officiel</div>
    </div>
    <div class="header-bottom">
      <div>
        <div class="ref-label">Référence</div>
        <div class="ref-value">${payment.reference || 'PAY-' + payment.id}</div>
      </div>
      <div class="amount-block">
        <div class="amount-label">Montant</div>
        <div class="amount-value">${(payment.amount || 0).toLocaleString('fr-FR')} <span class="amount-currency">TND</span></div>
      </div>
    </div>
  </div>

  <div class="status-bar">
    <span class="status-pill"><span class="status-dot"></span>${statusLabel(payment.status)}</span>
    <span class="status-date">Paiement effectué le ${paymentDateFull} à ${paymentTime}</span>
  </div>

  <div class="body">

    <div class="event-box">
      <div class="event-box-label">🎯 Événement</div>
      <div class="event-box-title">${eventTitle}</div>
      <div class="event-box-session">📅 Session : ${sessionLabel}</div>
    </div>

    <div class="section">
      <div class="section-title">Informations du participant</div>
      <div class="row"><span class="row-key">Nom complet</span><span class="row-val">${participantName}</span></div>
      <div class="row"><span class="row-key">Adresse e-mail</span><span class="row-val">${payment.participantEmail || '—'}</span></div>
    </div>

    <div class="section">
      <div class="section-title">Détails du paiement</div>
      <div class="row"><span class="row-key">Référence</span><span class="row-val">${payment.reference || '—'}</span></div>
      <div class="row"><span class="row-key">Mode de paiement</span><span class="row-val"><span class="method-badge">${methodIcon(payment.paymentMethod)} ${methodLabel(payment.paymentMethod)}</span></span></div>
      <div class="row"><span class="row-key">Date et heure</span><span class="row-val">${paymentDateFull}, ${paymentTime}</span></div>
      <div class="row"><span class="row-key">Statut</span><span class="row-val" style="color:${statusColor};font-weight:700;">${statusLabel(payment.status)}</span></div>
      <div class="row" style="margin-top:8px;padding-top:14px;border-top:2px solid #e2e8f0;"><span class="row-key" style="font-size:15px;font-weight:700;color:#1e293b;">Total réglé</span><span class="row-val" style="font-size:18px;color:${statusColor};">${(payment.amount || 0).toLocaleString('fr-FR')} TND</span></div>
    </div>

  </div>

  <div class="footer">
    <div class="footer-note">
      Ce document est un reçu officiel généré automatiquement par la plateforme Jungle.<br/>
      Conservez-le comme preuve de paiement. Réf. : ${payment.reference || payment.id}
    </div>
    <div class="footer-logo">JUNGLE</div>
  </div>

</div>
<script>
  document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('btn-print').addEventListener('click', function() {
      window.print();
    });
  });
</script>
</body>
</html>`;

    const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    const blobUrl = URL.createObjectURL(blob);
    window.open(blobUrl, '_blank');
    setTimeout(() => URL.revokeObjectURL(blobUrl), 10000);
  }

  getInitials(): string {
    if (!this.currentUser) return '?';
    return `${this.currentUser.firstName?.[0] || ''}${this.currentUser.lastName?.[0] || ''}`.toUpperCase();
  }
}