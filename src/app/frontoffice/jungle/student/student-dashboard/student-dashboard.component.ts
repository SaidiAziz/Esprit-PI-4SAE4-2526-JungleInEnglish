import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { PaymentService } from '../../../../core/services/payment.service';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './student-dashboard.component.html',
  styleUrl: './student-dashboard.component.css',
})
export class StudentDashboardComponent implements OnInit {
  loyalty: any = null;
  loyaltyLoading = true;

  constructor(
    public authService: AuthService,
    private paymentService: PaymentService,
  ) {}

  get user() {
    return this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    const email = this.user?.email;
    if (!email) {
      this.loyaltyLoading = false;
      return;
    }
    this.paymentService.getLoyaltyAccount(email).subscribe({
      next: (acc) => {
        this.loyalty = acc;
        this.loyaltyLoading = false;
      },
      error: () => {
        this.loyaltyLoading = false;
      },
    });
  }

  get tierIcon(): string {
    if (this.loyalty?.tier === 'GOLD') return '🏆';
    if (this.loyalty?.tier === 'SILVER') return '🥈';
    return '🥉';
  }

  get progressPercent(): number {
    if (!this.loyalty) return 0;
    const p = this.loyalty.totalPoints;
    if (this.loyalty.tier === 'GOLD') return 100;
    if (this.loyalty.tier === 'SILVER')
      return Math.min(((p - 3) / 3) * 100, 100);
    return Math.min((p / 3) * 100, 100);
  }

  get nextMilestone(): string {
    if (!this.loyalty) return '2 events to first reward!';
    const p = this.loyalty.totalPoints;
    if (this.loyalty.tier === 'GOLD') return 'All rewards unlocked!';
    if (this.loyalty.tier === 'SILVER')
      return `${6 - p} events to GOLD & 40% code`;
    if (p >= 2) return `${4 - p} events to 30% code`;
    return `${2 - p} events to first 10% code`;
  }
}
