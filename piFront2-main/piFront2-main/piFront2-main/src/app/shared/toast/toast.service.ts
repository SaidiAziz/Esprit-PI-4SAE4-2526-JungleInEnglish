import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastState {
  visible: boolean;
  message: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _state = new BehaviorSubject<ToastState>({
    visible: false,
    message: '',
    type: 'info',
  });

  state$ = this._state.asObservable();
  private timer?: any;

  show(message: string, type: ToastType = 'info', durationMs = 2500) {
    if (this.timer) clearTimeout(this.timer);

    this._state.next({ visible: true, message, type });

    this.timer = setTimeout(() => {
      this._state.next({ visible: false, message: '', type });
    }, durationMs);
  }

  success(msg: string) { this.show(msg, 'success'); }
  error(msg: string) { this.show(msg, 'error', 3500); }
  info(msg: string) { this.show(msg, 'info'); }

  hide() {
    if (this.timer) clearTimeout(this.timer);
    this._state.next({ visible: false, message: '', type: 'info' });
  }
}