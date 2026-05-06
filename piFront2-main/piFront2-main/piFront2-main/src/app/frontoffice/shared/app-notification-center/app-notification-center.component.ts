import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AppNotificationService } from '../../../core/services/app-notification.service';

@Component({
  selector: 'app-notification-center',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="fd-notification-center" *ngIf="notifications$ | async as notifications">
      <article
        class="fd-notification-card"
        [class.fd-notification-card--success]="notification.kind === 'success'"
        [class.fd-notification-card--warning]="notification.kind === 'warning'"
        [class.fd-notification-card--achievement]="notification.kind === 'achievement'"
        *ngFor="let notification of notifications">
        <div class="fd-notification-card__icon">
          <ng-container [ngSwitch]="notification.kind">
            <svg *ngSwitchCase="'achievement'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M12 3l2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.2 6.4 20.2l1.1-6.2L3 9.6l6.2-.9L12 3z"/>
            </svg>
            <svg *ngSwitchCase="'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M20 6L9 17l-5-5"/>
            </svg>
            <svg *ngSwitchCase="'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M12 9v4"/>
              <path d="M12 17h.01"/>
              <path d="M10.3 3.9L1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/>
            </svg>
            <svg *ngSwitchDefault viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M12 8h.01"/>
              <path d="M11 12h1v4h1"/>
              <circle cx="12" cy="12" r="9"/>
            </svg>
          </ng-container>
        </div>
        <div class="fd-notification-card__content">
          <strong>{{ notification.title }}</strong>
          <p>{{ notification.message }}</p>
        </div>
        <button type="button" class="fd-notification-card__close" (click)="dismiss(notification.id)" aria-label="Dismiss notification">&times;</button>
      </article>
    </section>
  `,
  styles: [`
    .fd-notification-center {
      position: fixed;
      top: 90px;
      right: 24px;
      z-index: 1200;
      display: grid;
      gap: 0.9rem;
      width: min(360px, calc(100vw - 32px));
      pointer-events: none;
    }

    .fd-notification-card {
      display: grid;
      grid-template-columns: auto 1fr auto;
      gap: 0.9rem;
      align-items: start;
      padding: 1rem 1rem 0.95rem;
      border-radius: 22px;
      border: 1px solid rgba(212, 196, 184, 0.82);
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(247, 237, 226, 0.98));
      box-shadow: 0 18px 40px rgba(45, 87, 87, 0.18);
      pointer-events: auto;
      animation: fd-notification-slide 260ms ease, fd-notification-glow 1500ms ease;
    }

    .fd-notification-card--success {
      border-color: rgba(45, 87, 87, 0.26);
      background: linear-gradient(180deg, rgba(125, 185, 181, 0.2), rgba(255, 255, 255, 0.98));
    }

    .fd-notification-card--warning {
      border-color: rgba(200, 70, 48, 0.26);
      background: linear-gradient(180deg, rgba(246, 201, 187, 0.3), rgba(255, 255, 255, 0.98));
    }

    .fd-notification-card--achievement {
      border-color: rgba(246, 189, 96, 0.52);
      background:
        radial-gradient(circle at top right, rgba(246, 189, 96, 0.34), transparent 32%),
        linear-gradient(180deg, rgba(246, 189, 96, 0.24), rgba(255, 255, 255, 0.98));
    }

    .fd-notification-card__icon {
      width: 2.4rem;
      height: 2.4rem;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16px;
      background: rgba(45, 87, 87, 0.08);
      color: var(--color-dark-teal);
      flex-shrink: 0;
    }

    .fd-notification-card__icon svg {
      width: 1.25rem;
      height: 1.25rem;
      display: block;
    }

    .fd-notification-card__content strong {
      display: block;
      margin-bottom: 0.2rem;
      color: var(--color-dark-teal);
      font-size: 0.95rem;
    }

    .fd-notification-card__content p {
      margin: 0;
      color: var(--fd-muted-text);
      font-size: 0.86rem;
      line-height: 1.45;
    }

    .fd-notification-card__close {
      border: none;
      background: transparent;
      color: var(--fd-muted-text);
      font-size: 1.2rem;
      line-height: 1;
      padding: 0.1rem;
      cursor: pointer;
    }

    @keyframes fd-notification-slide {
      from {
        transform: translate3d(18px, -12px, 0) scale(0.96);
        opacity: 0;
      }
      to {
        transform: translate3d(0, 0, 0) scale(1);
        opacity: 1;
      }
    }

    @keyframes fd-notification-glow {
      0% { box-shadow: 0 0 0 rgba(246, 189, 96, 0); }
      30% { box-shadow: 0 20px 44px rgba(246, 189, 96, 0.18); }
      100% { box-shadow: 0 18px 40px rgba(45, 87, 87, 0.18); }
    }

    @media (max-width: 820px) {
      .fd-notification-center {
        top: 78px;
        right: 12px;
        left: 12px;
        width: auto;
      }
    }
  `]
})
export class AppNotificationCenterComponent {
  private readonly notificationService = inject(AppNotificationService);

  readonly notifications$ = this.notificationService.notifications$;

  dismiss(id: number): void {
    this.notificationService.dismiss(id);
  }
}
