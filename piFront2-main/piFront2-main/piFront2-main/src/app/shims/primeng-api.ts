import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
  confirm(config: {
    message?: string;
    target?: EventTarget | null;
    header?: string;
    icon?: string;
    accept?: () => void;
    reject?: () => void;
    [key: string]: unknown;
  }): void {
    const prompt = config.message || 'Please confirm this action.';
    if (window.confirm(prompt)) {
      config.accept?.();
    } else {
      config.reject?.();
    }
  }
}

@Injectable({ providedIn: 'root' })
export class MessageService {
  add(_message: unknown): void {}
}
