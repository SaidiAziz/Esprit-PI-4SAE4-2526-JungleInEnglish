import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EmailService {
  async sendEmail(_payload: unknown): Promise<void> {
    return Promise.resolve();
  }
}

