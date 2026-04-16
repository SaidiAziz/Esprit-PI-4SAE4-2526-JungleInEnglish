import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

export type AppNotificationKind = 'info' | 'success' | 'warning' | 'achievement';

export interface AppNotification {
  id: number;
  title: string;
  message: string;
  kind: AppNotificationKind;
  createdAt: number;
}

@Injectable({ providedIn: 'root' })
export class AppNotificationService {
  private readonly notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  private nextId = 1;

  readonly notifications$ = this.notificationsSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private readonly platformId: object) {}

  info(title: string, message: string): void {
    this.push(title, message, 'info');
  }

  success(title: string, message: string): void {
    this.push(title, message, 'success');
  }

  warning(title: string, message: string): void {
    this.push(title, message, 'warning');
  }

  achievement(title: string, message: string): void {
    this.push(title, message, 'achievement');
  }

  dismiss(id: number): void {
    this.notificationsSubject.next(this.notificationsSubject.value.filter(notification => notification.id !== id));
  }

  private push(title: string, message: string, kind: AppNotificationKind): void {
    const notification: AppNotification = {
      id: this.nextId++,
      title,
      message,
      kind,
      createdAt: Date.now()
    };

    this.notificationsSubject.next([notification, ...this.notificationsSubject.value].slice(0, 5));
    this.playSound(kind);

    if (this.isBrowser()) {
      window.setTimeout(() => this.dismiss(notification.id), kind === 'achievement' ? 5200 : 3600);
    }
  }

  private playSound(kind: AppNotificationKind): void {
    if (!this.isBrowser()) {
      return;
    }

    try {
      const AudioContextCtor = window.AudioContext || (window as typeof window & {
        webkitAudioContext?: typeof AudioContext;
      }).webkitAudioContext;

      if (!AudioContextCtor) {
        return;
      }

      const context = new AudioContextCtor();
      const gain = context.createGain();
      gain.connect(context.destination);

      const notes =
        kind === 'achievement' ? [660, 880, 1100] :
        kind === 'success' ? [720, 920] :
        kind === 'warning' ? [420, 320] :
        [560];

      notes.forEach((frequency, index) => {
        const oscillator = context.createOscillator();
        oscillator.type = kind === 'warning' ? 'triangle' : 'sine';
        oscillator.frequency.setValueAtTime(frequency, context.currentTime + index * 0.08);
        oscillator.connect(gain);
        oscillator.start(context.currentTime + index * 0.08);
        oscillator.stop(context.currentTime + index * 0.08 + 0.12);
      });

      gain.gain.setValueAtTime(0.0001, context.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.05, context.currentTime + 0.03);
      gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + notes.length * 0.08 + 0.16);

      window.setTimeout(() => {
        void context.close();
      }, 900);
    } catch {
      // Ignore notification sound failures.
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
