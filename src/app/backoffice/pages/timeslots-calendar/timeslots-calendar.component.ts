import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdministrationApiService } from '../../../core/services/administration-api.service';

type TimeSlot = {
  id?: number; // ✅ important
  ownerType: 'PLATFORM' | 'TUTOR';
  ownerId?: number | null;
  dayOfWeek: number;     // 1..7
  startTime: string;     // "HH:mm:ss" ou "HH:mm"
  endTime: string;
};

type CalendarEvent = TimeSlot & { top: number; height: number };

@Component({
  selector: 'app-timeslots-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './timeslots-calendar.component.html',
  styleUrls: ['./timeslots-calendar.component.scss']
})
export class TimeslotsCalendarComponent implements OnInit {

  startHour = 0;
  endHour = 24;
  pxPerHour = 60;

  hours: number[] = [];
  days = [
    { dayOfWeek: 1, label: 'Mon' },
    { dayOfWeek: 2, label: 'Tue' },
    { dayOfWeek: 3, label: 'Wed' },
    { dayOfWeek: 4, label: 'Thu' },
    { dayOfWeek: 5, label: 'Fri' },
    { dayOfWeek: 6, label: 'Sat' },
    { dayOfWeek: 7, label: 'Sun' },
  ];

  eventsByDay: Record<number, CalendarEvent[]> = {1:[],2:[],3:[],4:[],5:[],6:[],7:[]};

  // modal state
  modalOpen = false;
  editingId: number | null = null;

  form: any = {
    ownerType: 'PLATFORM',
    ownerId: null,     // ✅
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '10:00'
  };

  constructor(private api: AdministrationApiService) {}

  ngOnInit(): void {
    this.hours = Array.from({ length: this.endHour - this.startHour }, (_, i) => this.startHour + i);
    this.loadTimeslots();
  }

  loadTimeslots() {
    this.api.getTimeslots().subscribe({
      next: (data: TimeSlot[]) => this.mapToCalendar(data),
      error: (err: any) => console.error('GET error:', err),
    });
  }

  // ✅ si on change ownerType
  onOwnerTypeChange() {
    if (this.form.ownerType === 'PLATFORM') {
      this.form.ownerId = null;
    }
  }

  // clique sur zone vide -> add
  onDayClick(event: MouseEvent, dayOfWeek: number) {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const y = event.clientY - rect.top;

    const minutes = Math.round((y / this.pxPerHour) * 60);
    const startMinutes = this.startHour * 60 + minutes;
    const endMinutes = startMinutes + 60;

    this.openAdd(dayOfWeek, this.minToTime(startMinutes), this.minToTime(endMinutes));
  }

  openAdd(dayOfWeek: number, startTime: string, endTime: string) {
    this.editingId = null;
    this.form = {
      ownerType: 'PLATFORM',
      ownerId: null,
      dayOfWeek,
      startTime,
      endTime
    };
    this.modalOpen = true;
  }

  // clique sur event -> edit
  openEdit(event: MouseEvent, e: TimeSlot) {
    event.stopPropagation();

    this.editingId = e.id ?? null;
    this.form = {
      ownerType: e.ownerType,
      ownerId: e.ownerId ?? null, // ✅
      dayOfWeek: Number(e.dayOfWeek),
      startTime: this.normalizeTime(e.startTime),
      endTime: this.normalizeTime(e.endTime)
    };
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
  }

  save() {
    // ✅ validation côté front
    if (this.form.ownerType === 'TUTOR' && !this.form.ownerId) {
      alert('Tutor ID is required when owner type is TUTOR');
      return;
    }

    const payload: any = {
      ownerType: this.form.ownerType,
      ownerId: this.form.ownerType === 'TUTOR' ? Number(this.form.ownerId) : null, // ✅
      dayOfWeek: Number(this.form.dayOfWeek),
      startTime: this.form.startTime,
      endTime: this.form.endTime
    };

    if (this.editingId) {
      this.api.updateTimeslot(this.editingId, payload).subscribe({
        next: () => { this.closeModal(); this.loadTimeslots(); },
        error: (err: any) => console.error('PUT error:', err)
      });
    } else {
      this.api.addTimeslot(payload).subscribe({
        next: () => { this.closeModal(); this.loadTimeslots(); },
        error: (err: any) => console.error('POST error:', err)
      });
    }
  }

  remove() {
    if (!this.editingId) return;
    this.api.deleteTimeslot(this.editingId).subscribe({
      next: () => { this.closeModal(); this.loadTimeslots(); },
      error: (err: any) => console.error('DELETE error:', err)
    });
  }

  // mapping data -> blocs
  private mapToCalendar(timeslots: TimeSlot[]) {
    for (const k of Object.keys(this.eventsByDay)) this.eventsByDay[Number(k)] = [];

    for (const t of timeslots) {
      const startMin = this.toMinutes(t.startTime);
      const endMin = this.toMinutes(t.endTime);

      const dayStartMin = this.startHour * 60;
      if (startMin < dayStartMin) continue;

      const topMin = startMin - dayStartMin;
      const durMin = Math.max(15, endMin - startMin);

      const top = (topMin / 60) * this.pxPerHour;
      const height = (durMin / 60) * this.pxPerHour;

      const ev: CalendarEvent = { ...t, top, height };
      if (this.eventsByDay[t.dayOfWeek]) this.eventsByDay[t.dayOfWeek].push(ev);
    }
  }

  private normalizeTime(t: string): string {
    return t?.length >= 5 ? t.substring(0, 5) : t;
  }

  private toMinutes(time: string): number {
    const parts = time.split(':').map(Number);
    return (parts[0] ?? 0) * 60 + (parts[1] ?? 0);
  }

  private minToTime(min: number): string {
    const h = Math.floor(min / 60);
    const m = min % 60;
    return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}`;
  }
}