import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { Session, SessionStatus } from '../../models/session.model';

type SessionMode = 'ONLINE' | 'ONSITE';

@Component({
  selector: 'app-session-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './session-form.component.html',
  styleUrls: ['./session-form.component.css']
})
export class SessionFormComponent implements OnInit {

  id?: number;
  isEdit = false;

  loading = false;
  error = '';

  statuses: SessionStatus[] = ['PLANNED', 'CANCELED', 'COMPLETED'];
  modes: SessionMode[] = ['ONLINE', 'ONSITE'];

  form: Session & { mode: SessionMode } = {
    tutorId: 0,
    courseId: 0,
    sessionDate: new Date().toISOString().slice(0, 10),
    status: 'PLANNED',
    timeSlotId: null as any,
    mode: 'ONSITE'
  } as Session & { mode: SessionMode };

  startTimeStr = '18:00';
  endTimeStr = '19:00';

  constructor(
    private api: SessionService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.id = Number(idParam);
      this.loadById(this.id);
    }
  }

  private normalizeTimeToHM(value: any): string {
    if (!value) return '00:00';

    if (typeof value === 'string') {
      return value.length >= 5 ? value.slice(0, 5) : value;
    }

    const hh = String(value?.hour ?? 0).padStart(2, '0');
    const mm = String(value?.minute ?? 0).padStart(2, '0');
    return `${hh}:${mm}`;
  }

  private toHHmmss(value: string): string {
    if (!value) return '00:00:00';
    return value.length === 5 ? `${value}:00` : value;
  }

  private normalizeDateToYMD(value: any): string {
    if (!value) return value;

    if (typeof value === 'string') {
      if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return value;

      if (/^\d{2}\/\d{2}\/\d{4}$/.test(value)) {
        const [dd, mm, yyyy] = value.split('/');
        return `${yyyy}-${mm}-${dd}`;
      }

      if (value.includes('T')) return value.slice(0, 10);
    }

    return value;
  }

  loadById(id: number) {
    this.loading = true;
    this.error = '';

    this.api.getById(id).subscribe({
      next: (data: any) => {
        this.form = {
          id: data.id,
          tutorId: data.tutorId,
          courseId: data.courseId,
          sessionDate: this.normalizeDateToYMD(data.sessionDate),
          status: data.status,
          timeSlotId: data.timeSlotId ?? null,
          mode: data.mode ?? 'ONSITE'
        } as Session & { mode: SessionMode };

        this.startTimeStr = this.normalizeTimeToHM(data.startTime);
        this.endTimeStr = this.normalizeTimeToHM(data.endTime);

        this.loading = false;
      },
      error: (err) => {
        console.error('loadById error', err);
        this.error = 'Impossible de charger la session.';
        this.loading = false;
      }
    });
  }

  submit() {
    this.error = '';

    if (!this.form.tutorId || !this.form.courseId || !this.form.sessionDate) {
      this.error = 'Veuillez remplir tutorId, courseId et sessionDate.';
      return;
    }

    if (!this.startTimeStr || !this.endTimeStr) {
      this.error = 'Veuillez remplir startTime et endTime.';
      return;
    }

    if (!this.form.mode) {
      this.error = 'Veuillez choisir le mode de la session.';
      return;
    }

    const payload: any = {
      tutorId: this.form.tutorId,
      courseId: this.form.courseId,
      sessionDate: this.normalizeDateToYMD(this.form.sessionDate),
      startTime: this.toHHmmss(this.startTimeStr),
      endTime: this.toHHmmss(this.endTimeStr),
      status: this.form.status,
      timeSlotId: this.form.timeSlotId ?? null,
      mode: this.form.mode
    };

    console.log('PAYLOAD SENT =>', JSON.stringify(payload, null, 2));

    this.loading = true;

    const request$ = (this.isEdit && this.id)
      ? this.api.update(this.id, payload)
      : this.api.create(payload);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/admin/sessions']);
      },
      error: (err) => {
        console.error('save error', err);
        this.loading = false;

        const backendMsg =
          err?.error?.details ||
          err?.error?.message ||
          err?.error?.error ||
          (typeof err?.error === 'string' ? err.error : '');

        this.error = backendMsg
          ? `Enregistrement échoué: ${backendMsg}`
          : 'Enregistrement échoué (voir console).';
      }
    });
  }

  cancel() {
    this.router.navigate(['/admin/sessions']);
  }
}