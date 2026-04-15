import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Session } from '../../models/session.model';

@Component({
  selector: 'app-tutor-sessions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tutor-sessions.component.html',
  styleUrls: ['./tutor-sessions.component.css']
})
export class TutorSessionsComponent implements OnInit {

  sessions: Session[] = [];
  loading = false;
  error = '';

  // mets ici l'id réel du tutor à tester
  tutorId = 20;

  constructor(private sessionService: SessionService) {}

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions(): void {
    this.loading = true;
    this.error = '';

    this.sessionService.getByTutor(this.tutorId).subscribe({
      next: (data) => {
        this.sessions = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement sessions tutor', err);
        this.error = 'Impossible de charger les sessions.';
        this.loading = false;
      }
    });
  }

  joinSession(link?: string | null): void {
    if (!link) return;
    window.open(link, '_blank');
  }

  formatTime(t: any): string {
    if (!t) return '-';

    if (typeof t === 'string') {
      return t.length >= 5 ? t.slice(0, 5) : t;
    }

    const hh = String(t.hour ?? 0).padStart(2, '0');
    const mm = String(t.minute ?? 0).padStart(2, '0');
    return `${hh}:${mm}`;
  }

  trackBySession(index: number, session: Session): number {
    return session.id ?? index;
  }
}