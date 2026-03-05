import { Injectable } from '@angular/core';
import { UserSummary } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class CsvExportService {

  exportUsers(users: UserSummary[], filename: string = 'users.csv'): void {
    const headers = ['ID', 'Full Name', 'Email', 'Role', 'Status', 'Joined Date'];

    const rows = users.map(u => [
      u.id,
      `${u.firstName} ${u.lastName}`,
      u.email,
      u.role,
      u.status,
      new Date(u.createdAt).toLocaleDateString('en-GB') // e.g. 04/03/2026
    ]);

    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');

    this.downloadFile(csvContent, filename);
  }

  private downloadFile(content: string, filename: string): void {
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();

    URL.revokeObjectURL(url); // cleanup
  }
}

