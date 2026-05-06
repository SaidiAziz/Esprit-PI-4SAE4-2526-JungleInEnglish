import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { UserServiceService } from '../../../backoffice/services/user-service.service';
import { UserResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {

  user: UserResponse | null = null;
  loading = true;

  // 2FA setup state
  selectedMethod: 'EMAIL' | 'TOTP' = 'EMAIL';
  qrCodeBase64 = '';
  totpSecret = '';
  confirmCode = '';
  step: 'idle' | 'setup-totp' | 'confirm-email' = 'idle';

  message = '';
  messageType: 'success' | 'error' = 'success';
  working = false;

  constructor(
    private authService: AuthService,
    private userService: UserServiceService
  ) {}

  ngOnInit(): void {
    this.userService.getProfile().subscribe({
      next: (u) => { this.user = u; this.loading = false; },
      error: () => { this.user = this.authService.getCurrentUser(); this.loading = false; }
    });
  }

  startEnable(): void {
    if (!this.user) return;
    this.message = '';
    this.working = true;

    if (this.selectedMethod === 'TOTP') {
      this.authService.setupTotp(this.user.email).subscribe({
        next: (res) => {
          this.qrCodeBase64 = res.qrCodeBase64;
          this.totpSecret   = res.secret;
          this.step         = 'setup-totp';
          this.working      = false;
        },
        error: () => { this.showMessage('Failed to start TOTP setup.', 'error'); this.working = false; }
      });
    } else {
      this.step    = 'confirm-email';
      this.working = false;
    }
  }

  confirmEnable(): void {
    if (!this.user) return;
    this.working = true;
    this.authService.enable2FA(
      this.user.email,
      this.selectedMethod,
      this.selectedMethod === 'TOTP' ? this.confirmCode : undefined
    ).subscribe({
      next: () => {
        this.user!.twoFactorEnabled  = true;
        this.user!.twoFactorMethod   = this.selectedMethod;
        this.authService.updateCurrentUser(this.user!);
        this.step        = 'idle';
        this.confirmCode = '';
        this.working     = false;
        this.showMessage('Two-factor authentication enabled successfully! ✅', 'success');
      },
      error: (err) => {
        this.showMessage(err.error?.message ?? 'Failed to enable 2FA.', 'error');
        this.working = false;
      }
    });
  }

  disable(): void {
    if (!this.user) return;
    this.working = true;
    this.authService.disable2FA(this.user.email).subscribe({
      next: () => {
        this.user!.twoFactorEnabled = false;
        this.user!.twoFactorMethod  = undefined;
        this.authService.updateCurrentUser(this.user!);
        this.step    = 'idle';
        this.working = false;
        this.showMessage('Two-factor authentication disabled.', 'success');
      },
      error: () => { this.showMessage('Failed to disable 2FA.', 'error'); this.working = false; }
    });
  }

  cancelSetup(): void {
    this.step        = 'idle';
    this.confirmCode = '';
    this.qrCodeBase64 = '';
    this.message     = '';
  }

  private showMessage(msg: string, type: 'success' | 'error'): void {
    this.message     = msg;
    this.messageType = type;
    setTimeout(() => this.message = '', 5000);
  }
}

