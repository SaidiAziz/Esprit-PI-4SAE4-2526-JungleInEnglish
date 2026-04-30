import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { SessionStatisticsService } from '../../services/session-statistics.service';
import {
  SessionStats,
  TutorSessionStats,
  MonthlySessionStats
} from '../../models/session-statistics.model';

@Component({
  selector: 'app-session-statistics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './session-statistics.component.html',
  styleUrls: ['./session-statistics.component.css']
})
export class SessionStatisticsComponent implements OnInit {

  globalStats: SessionStats | null = null;
  tutorStats: TutorSessionStats[] = [];
  monthStats: MonthlySessionStats[] = [];
  loading = false;
  errorMessage = '';

  constructor(private statisticsService: SessionStatisticsService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.errorMessage = '';

    let loadedCount = 0;
    const totalRequests = 3;

    const finishLoading = () => {
      loadedCount++;
      if (loadedCount === totalRequests) {
        this.loading = false;
      }
    };

    this.statisticsService.getGlobalStatistics().subscribe({
      next: (data) => {
        this.globalStats = data;
        finishLoading();
      },
      error: (err) => {
        console.error('Erreur chargement statistiques globales', err);
        this.errorMessage = 'Impossible de charger les statistiques globales.';
        finishLoading();
      }
    });

    this.statisticsService.getSessionsByTutor().subscribe({
      next: (data) => {
        this.tutorStats = data;
        finishLoading();
      },
      error: (err) => {
        console.error('Erreur chargement statistiques par tuteur', err);
        this.errorMessage = 'Impossible de charger les statistiques par tuteur.';
        finishLoading();
      }
    });

    this.statisticsService.getSessionsByMonth().subscribe({
      next: (data) => {
        this.monthStats = data;
        finishLoading();
      },
      error: (err) => {
        console.error('Erreur chargement statistiques par mois', err);
        this.errorMessage = 'Impossible de charger les statistiques par mois.';
        finishLoading();
      }
    });
  }

  getMonthName(month: number): string {
    const months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return months[month - 1] || `Month ${month}`;
  }

  getMaxMonthCount(): number {
    if (!this.monthStats.length) return 1;
    return Math.max(...this.monthStats.map(item => item.sessionCount), 1);
  }

  getMonthBarWidth(count: number): number {
    return (count / this.getMaxMonthCount()) * 100;
  }

  getStatusPercent(value: number): number {
    if (!this.globalStats || this.globalStats.totalSessions === 0) return 0;
    return (value / this.globalStats.totalSessions) * 100;
  }
}