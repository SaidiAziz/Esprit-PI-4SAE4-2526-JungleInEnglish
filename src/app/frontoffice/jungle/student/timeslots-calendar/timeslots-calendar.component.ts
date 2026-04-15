import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdministrationApiService } from '../../../../core/services/administration-api.service';

type OwnerType = 'PLATFORM' | 'TUTOR';

type TimeSlot = {
  id?: number;
  ownerType: OwnerType;
  ownerId?: number | null;
  dayOfWeek: number;   // 1..7
  startTime: string;   // "HH:mm" ou "HH:mm:ss"
  endTime: string;
};

type CalendarEvent = TimeSlot & { top: number; height: number };

@Component({
  selector: 'app-timeslots-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timeslots-calendar.component.html',
  styleUrls: ['./timeslots-calendar.component.css']
})
export class TimeslotsCalendarComponent implements OnInit {
  @Input() readonly = true;

  pxPerHour = 60;

  days = [
    { dayOfWeek: 1, label: 'Mon' },
    { dayOfWeek: 2, label: 'Tue' },
    { dayOfWeek: 3, label: 'Wed' },
    { dayOfWeek: 4, label: 'Thu' },
    { dayOfWeek: 5, label: 'Fri' },
    { dayOfWeek: 6, label: 'Sat' },
    { dayOfWeek: 7, label: 'Sun' },
  ];

  hours: number[] = Array.from({ length: 24 }, (_, i) => i);

  eventsByDay: Record<number, CalendarEvent[]> = { 1: [], 2: [], 3: [], 4: [], 5: [], 6: [], 7: [] };

  loading = false;
  error: string | null = null;

  constructor(private adminApi: AdministrationApiService) {}

  ngOnInit(): void {
    this.loadTimeSlots();
  }

  private loadTimeSlots(): void {
    this.loading = true;
    this.error = null;

    // ✅ LA BONNE METHODE
    this.adminApi.getTimeslots().subscribe({
      next: (slots: TimeSlot[]) => {
        console.log('timeslots from backend:', slots);
        this.buildCalendar(slots || []);
        this.loading = false;
      },
      error: (err: any) => {
        console.error('GET timeslots failed', err);
        this.error = err?.error?.message || err?.message || 'Failed to load timeslots';
        this.loading = false;
      }
    });
  }

  private buildCalendar(slots: TimeSlot[]): void {
    this.eventsByDay = { 1: [], 2: [], 3: [], 4: [], 5: [], 6: [], 7: [] };

    for (const s of slots) {
      if (!s?.dayOfWeek || s.dayOfWeek < 1 || s.dayOfWeek > 7) continue;

      const startMin = this.toMinutes(s.startTime);
      const endMin = this.toMinutes(s.endTime);
      if (!Number.isFinite(startMin) || !Number.isFinite(endMin) || endMin <= startMin) continue;

      const top = (startMin / 60) * this.pxPerHour;
      const height = ((endMin - startMin) / 60) * this.pxPerHour;

      this.eventsByDay[s.dayOfWeek].push({ ...s, top, height });
    }

    // tri optionnel
    for (const d of Object.keys(this.eventsByDay)) {
      this.eventsByDay[+d].sort((a, b) => this.toMinutes(a.startTime) - this.toMinutes(b.startTime));
    }
  }

  private toMinutes(t: string): number {
    if (!t) return NaN;
    const parts = t.split(':').map(Number);
    const hh = parts[0];
    const mm = parts[1] ?? 0;
    if (!Number.isFinite(hh) || !Number.isFinite(mm)) return NaN;
    return hh * 60 + mm;
  }
}