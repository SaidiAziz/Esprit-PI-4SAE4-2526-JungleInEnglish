import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  EventService,
  EventModel,
} from '../../../../core/services/event.service';
import { SessioneventService } from '../../../../core/services/sessionevent.service';
import { Session } from '../../../../core/models/sessionevent.model';

@Component({
  selector: 'app-event-session',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './event-session.component.html',
  styleUrl: './event-session.component.css',
})
export class EventSessionComponent implements OnInit {
  eventId!: number;
  event: EventModel | null = null;
  sessions: Session[] = [];
  loading = true;
  error = '';

  // ── Formulaire inline de session ─────────────────────────────────────
  showForm = false;
  sessionForm!: FormGroup;
  formLoading = false;
  formError = '';
  editingSessionId: number | null = null;
  sessionTypes = ['ONLINE', 'PRESENTIEL', 'HYBRID'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private eventService: EventService,
    private sessionService: SessioneventService,
  ) {}

  // ── Getters ──────────────────────────────────────────────────────────

  get f() {
    return this.sessionForm.controls;
  }

  /** Afficher le champ Salle selon le type sélectionné dans le formulaire */
  get showRoom(): boolean {
    const type = this.sessionForm?.get('type')?.value || '';
    return type === 'PRESENTIEL' || type === 'HYBRID';
  }

  /** Afficher le lien réunion selon le type sélectionné dans le formulaire */
  get showMeetingLink(): boolean {
    const type = this.sessionForm?.get('type')?.value || '';
    return type === 'ONLINE' || type === 'HYBRID';
  }

  // ── Lifecycle ────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.eventId = +this.route.snapshot.params['eventId'];
    if (!this.eventId || isNaN(this.eventId)) {
      this.router.navigate(['/admin/events']);
      return;
    }
    this.buildForm();
    this.loadEvent();
    this.loadSessions();
  }

  // ── Formulaire ───────────────────────────────────────────────────────

  private buildForm(): void {
    this.sessionForm = this.fb.group({
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      type: ['', Validators.required],
      availableSeats: [1, [Validators.required, Validators.min(1)]],
      room: [''],
      meetingLink: [''],
    });
    // Pas de valueChanges ici — on gère le changement via (change) dans le template
    // pour éviter les cycles d'événements qui bloquent la mise à jour du select.
  }

  /**
   * Appelée par (change) sur le select type dans le template.
   * Met à jour validateurs ET vide le champ non pertinent.
   */
  onTypeChange(): void {
    const type = this.sessionForm.get('type')!.value as string;

    const roomCtrl = this.sessionForm.get('room')!;
    const meetCtrl = this.sessionForm.get('meetingLink')!;

    roomCtrl.clearValidators();
    meetCtrl.clearValidators();

    if (type === 'PRESENTIEL') {
      roomCtrl.setValidators([Validators.required]);
      meetCtrl.setValue('', { emitEvent: false });
    } else if (type === 'ONLINE') {
      meetCtrl.setValidators([Validators.required]);
      roomCtrl.setValue('', { emitEvent: false });
    } else if (type === 'HYBRID') {
      roomCtrl.setValidators([Validators.required]);
      meetCtrl.setValidators([Validators.required]);
    }

    roomCtrl.updateValueAndValidity({ emitEvent: false });
    meetCtrl.updateValueAndValidity({ emitEvent: false });
  }

  /** Applique les validateurs selon le type déjà présent dans le form (sans vider les champs). */
  private applyFormatValidators(): void {
    const type = (this.sessionForm.get('type')?.value as string) || '';
    if (!type) return;

    const roomCtrl = this.sessionForm.get('room')!;
    const meetCtrl = this.sessionForm.get('meetingLink')!;

    roomCtrl.clearValidators();
    meetCtrl.clearValidators();

    if (type === 'PRESENTIEL') {
      roomCtrl.setValidators([Validators.required]);
    } else if (type === 'ONLINE') {
      meetCtrl.setValidators([Validators.required]);
    } else if (type === 'HYBRID') {
      roomCtrl.setValidators([Validators.required]);
      meetCtrl.setValidators([Validators.required]);
    }

    roomCtrl.updateValueAndValidity({ emitEvent: false });
    meetCtrl.updateValueAndValidity({ emitEvent: false });
  }

  private preselectType(): void {
    const typeMap: Record<string, string> = {
      Présentiel: 'PRESENTIEL',
      'En Ligne': 'ONLINE',
      Hybride: 'HYBRID',
    };
    const preselected = typeMap[this.event?.format || ''];
    if (preselected) this.sessionForm.patchValue({ type: preselected });
  }

  /** Ouvrir le formulaire en mode ajout */
  openAddForm(): void {
    this.editingSessionId = null;
    this.sessionForm.reset({
      date: '',
      startTime: '',
      endTime: '',
      type: '',
      availableSeats: 1,
      room: '',
      meetingLink: '',
    });
    this.preselectType();
    this.applyFormatValidators();
    this.formError = '';
    this.showForm = true;
    // Scroll vers le formulaire
    setTimeout(() => {
      document
        .getElementById('inline-session-form')
        ?.scrollIntoView({ behavior: 'smooth' });
    }, 50);
  }

  /** Ouvrir le formulaire en mode édition */
  openEditForm(session: Session): void {
    this.editingSessionId = session.id!;
    this.sessionForm.patchValue(session);
    this.applyFormatValidators();
    this.formError = '';
    this.showForm = true;
    setTimeout(() => {
      document
        .getElementById('inline-session-form')
        ?.scrollIntoView({ behavior: 'smooth' });
    }, 50);
  }

  cancelForm(): void {
    this.showForm = false;
    this.formError = '';
    this.editingSessionId = null;
  }

  submitSession(): void {
    if (this.sessionForm.invalid) {
      this.sessionForm.markAllAsTouched();
      return;
    }

    this.formLoading = true;
    this.formError = '';

    const payload: Session = {
      ...this.sessionForm.getRawValue(),
      eventId: this.eventId,
    };

    const request$ = this.editingSessionId
      ? this.sessionService.updateSession(this.editingSessionId, payload)
      : this.sessionService.addSession(payload);

    request$.subscribe({
      next: () => {
        this.showForm = false;
        this.editingSessionId = null;
        this.formLoading = false;
        this.loadSessions();
      },
      error: (err) => {
        this.formError = err.message || 'Erreur lors de la sauvegarde.';
        this.formLoading = false;
      },
    });
  }

  // ── Données ──────────────────────────────────────────────────────────

  loadEvent(): void {
    this.eventService.getById(this.eventId).subscribe({
      next: (data) => {
        this.event = data;
        this.applyFormatValidators();
        this.preselectType();
      },
      error: () => {
        this.error = "Impossible de charger l'événement.";
      },
    });
  }

  loadSessions(): void {
    this.loading = true;
    this.sessionService.getSessionsByEvent(this.eventId).subscribe({
      next: (data) => {
        this.sessions = data;
        this.loading = false;
      },
      error: () => {
        this.sessions = [];
        this.loading = false;
      },
    });
  }

  deleteSession(sessionId: number): void {
    if (!confirm('Supprimer cette session ?')) return;
    this.sessionService.deleteSession(sessionId).subscribe({
      next: () => this.loadSessions(),
      error: () => {
        this.error = 'Erreur lors de la suppression.';
      },
    });
  }

  // ── Navigation ───────────────────────────────────────────────────────

  /** Terminer : retour à la liste des événements */
  save(): void {
    this.router.navigate(['/admin/events']);
  }

  /** Retour au formulaire de l'événement (mode édition) */
  back(): void {
    this.router.navigate(['/admin/events/edit', this.eventId]);
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'ONLINE':
        return '🌐 En ligne';
      case 'PRESENTIEL':
        return '📍 Présentiel';
      case 'HYBRID':
        return '🔀 Hybride';
      default:
        return type;
    }
  }
}
