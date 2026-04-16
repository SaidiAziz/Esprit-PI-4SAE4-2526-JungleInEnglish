import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastComponent } from './shared/toast/toast.component';
import { AppNotificationCenterComponent } from './frontoffice/shared/app-notification-center/app-notification-center.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastComponent, AppNotificationCenterComponent],
  template: `
    <router-outlet></router-outlet>
    <app-toast></app-toast>
    <app-notification-center></app-notification-center>
  `
})
export class AppComponent {
  title = 'PiFront';
}
