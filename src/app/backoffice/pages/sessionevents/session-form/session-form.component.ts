import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Session } from '../../../../core/models/sessionevent.model';
import { SessioneventService } from '../../../../core/services/sessionevent.service';
import {
  EventService,
  EventModel,
} from '../../../../core/services/event.service';

@Component({
  selector: 'app-session-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './session-form.component.html',
  styleUrls: ['./session-form.component.css'],
})
export class SessionFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  submitError: string | null = null;
  loading = false;
  sessionTypes = ['ONLINE', 'PRESENTIEL', 'HYBRID'];
  sessionId?: number;

  events: EventModel[] = [];
  eventsLoading = false;

  // ✅ Titre de l'événement lié (mode édition)
  linkedEventTitle: string = '';

  constructor(
    private fb: FormBuilder,
    private sessionService: SessioneventService,
    private eventService: EventService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.sessionId = this.route.snapshot.params['id'];
    this.isEdit = !!this.sessionId;

    this.form = this.fb.group({
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      type: ['', Validators.required],
      availableSeats: [0, [Validators.required, Validators.min(1)]],
      eventId: [null, Validators.required],
      room: ['', Validators.required],
      meetingLink: ['', Validators.required],
    });

    if (this.isEdit) {
      this.form.get('eventId')?.disable();
      // ✅ Charger les événements ET la session en parallèle
      this.loadEventsAndSession();
    } else {
      this.loadEvents();
    }
  }

  // ✅ Charger les événements pour le dropdown (mode création)
  loadEvents(): void {
    this.eventsLoading = true;
    this.eventService.getAll().subscribe({
      next: (data: EventModel[]) => {
        this.events = data;
        this.eventsLoading = false;
      },
      error: () => {
        this.eventsLoading = false;
      },
    });
  }

  // ✅ Charger événements + session ensemble pour résoudre le titre en mode édition
  loadEventsAndSession(): void {
    this.loading = true;
    this.eventsLoading = true;

    this.eventService.getAll().subscribe({
      next: (data: EventModel[]) => {
        this.events = data;
        this.eventsLoading = false;

        // Une fois les events chargés, charger la session
        this.loadSession();
      },
      error: () => {
        this.eventsLoading = false;
        this.loadSession(); // continuer même si events échoue
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  loadSession(): void {
    if (!this.sessionId) return;
    this.loading = true;
    this.sessionService.getSessionById(this.sessionId).subscribe({
      next: (data: Session) => {
        this.form.patchValue(data);

        // ✅ Résoudre le titre de l'événement lié
        const eventId = (data as any).eventId;
        if (eventId && this.events.length > 0) {
          const found = this.events.find((e) => e.id === eventId);
          this.linkedEventTitle = found ? found.title : `ID ${eventId}`;
        }

        this.loading = false;
      },
      error: (err) => {
        this.submitError =
          err.message || 'Erreur lors du chargement de la session';
        this.loading = false;
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const session: Session = this.form.getRawValue();

    if (this.isEdit && this.sessionId) {
      this.sessionService.updateSession(this.sessionId, session).subscribe({
        next: () => this.router.navigate(['/admin/sessionevents']),
        error: (err) => {
          this.submitError = err.message || 'Erreur lors de la mise à jour';
          this.loading = false;
        },
      });
    } else {
      this.sessionService.addSession(session).subscribe({
        next: () => this.router.navigate(['/admin/sessionevents']),
        error: (err) => {
          this.submitError = err.message || 'Erreur lors de la création';
          this.loading = false;
        },
      });
    }
  }
}
