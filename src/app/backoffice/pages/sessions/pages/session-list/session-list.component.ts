import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, startWith } from 'rxjs/operators';

import { SessionService } from '../../services/session.service';
import { Session } from '../../models/session.model';
import { ToastService } from '../../../../../shared/toast/toast.service';

@Component({
  selector: 'app-session-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './session-list.component.html',
  styleUrls: ['./session-list.component.css']
})
export class SessionListComponent implements OnInit {

  sessions: Session[] = [];
  filteredSessions: Session[] = [];

  loading = false;
  error = '';

  // ✅ options filtres
  statuses: Array<'ALL' | 'PLANNED' | 'COMPLETED' | 'CANCELED'> = ['ALL', 'PLANNED', 'COMPLETED', 'CANCELED'];

  // ✅ form filtres (déclaré seulement)
  filters!: ReturnType<FormBuilder['group']>;

  constructor(
    private sessionService: SessionService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    // ✅ initialiser ici (après injection de fb)
    this.filters = this.fb.group({
      q: [''],
      status: ['ALL'],
      tutorId: [''],
      courseId: [''],
      dateFrom: [''],
      dateTo: ['']
    });

    this.loadSessions();

    // ✅ auto-filter à chaque changement (avec debounce)
    this.filters.valueChanges
      .pipe(startWith(this.filters.value), debounceTime(150))
      .subscribe(() => this.applyFilters());
  }

  private extractBackendMessage(err: any): string {
    return (
      err?.error?.details ||
      err?.error?.message ||
      err?.error?.error ||
      (typeof err?.error === 'string' ? err.error : '') ||
      `HTTP ${err?.status ?? ''}`
    );
  }

  loadSessions() {
    this.loading = true;
    this.error = '';

    this.sessionService.getAll().subscribe({
      next: (data) => {
        this.sessions = data ?? [];
        this.loading = false;
        this.applyFilters();
      },
      error: (err) => {
        console.error('loadSessions error', err);
        this.error = 'Erreur lors du chargement des sessions';
        this.toast.error(`Chargement échoué: ${this.extractBackendMessage(err)}`);
        this.loading = false;
      }
    });
  }

  resetFilters() {
    this.filters.reset({
      q: '',
      status: 'ALL',
      tutorId: '',
      courseId: '',
      dateFrom: '',
      dateTo: ''
    });
  }

  private applyFilters() {
    const v: any = this.filters.value;

    const q = (v.q ?? '').toString().trim().toLowerCase();
    const status = (v.status ?? 'ALL') as string;

    const tutorId = v.tutorId ? Number(v.tutorId) : null;
    const courseId = v.courseId ? Number(v.courseId) : null;

    const dateFrom = (v.dateFrom ?? '').toString().trim();
    const dateTo = (v.dateTo ?? '').toString().trim();

    this.filteredSessions = (this.sessions ?? []).filter((s) => {
      if (status !== 'ALL' && s.status !== status) return false;

      if (tutorId !== null && !Number.isNaN(tutorId) && Number(s.tutorId) !== tutorId) return false;

      if (courseId !== null && !Number.isNaN(courseId) && Number(s.courseId) !== courseId) return false;

      const d = (s.sessionDate ?? '').toString();
      if (dateFrom && d < dateFrom) return false;
      if (dateTo && d > dateTo) return false;

      if (q) {
        const hay = [
          s.id,
          s.tutorId,
          s.courseId,
          s.sessionDate,
          this.formatTime((s as any).startTime),
          this.formatTime((s as any).endTime),
          s.status
        ].join(' ').toLowerCase();

        if (!hay.includes(q)) return false;
      }

      return true;
    });

    this.filteredSessions.sort((a, b) => {
      const d = (a.sessionDate ?? '').localeCompare(b.sessionDate ?? '');
      if (d !== 0) return d;
      return this.formatTime((a as any).startTime).localeCompare(this.formatTime((b as any).startTime));
    });
  }

  formatTime(t: any): string {
    if (!t) return '-';
    if (typeof t === 'string') return t.length >= 5 ? t.slice(0, 5) : t;

    const hh = String(t.hour ?? 0).padStart(2, '0');
    const mm = String(t.minute ?? 0).padStart(2, '0');
    return `${hh}:${mm}`;
  }

  canComplete(s: Session): boolean {
    return s.status !== 'COMPLETED' && s.status !== 'CANCELED';
  }

  canCancel(s: Session): boolean {
    return s.status !== 'CANCELED' && s.status !== 'COMPLETED';
  }

  completeSession(id?: number) {
    if (!id) return;

    this.sessionService.complete(id).subscribe({
      next: () => {
        this.toast.success('Session terminée (COMPLETED).');
        this.loadSessions();
      },
      error: (err) => {
        console.error('complete error', err);
        this.toast.error(`Complete échoué: ${this.extractBackendMessage(err)}`);
      }
    });
  }

  cancelSession(id?: number) {
    if (!id) return;

    this.sessionService.cancel(id).subscribe({
      next: () => {
        this.toast.success('Session annulée (CANCELED).');
        this.loadSessions();
      },
      error: (err) => {
        console.error('cancel error', err);
        this.toast.error(`Cancel échoué: ${this.extractBackendMessage(err)}`);
      }
    });
  }

  deleteSession(id?: number) {
    if (!id) return;

    this.toast.info('Suppression en cours...');

    this.sessionService.delete(id).subscribe({
      next: () => {
        this.toast.success('Session supprimée.');
        this.loadSessions();
      },
      error: (err) => {
        console.error('delete error', err);
        this.toast.error(`Suppression échouée: ${this.extractBackendMessage(err)}`);
      }
    });
  }
}