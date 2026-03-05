import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { UserServiceService } from '../../services/user-service.service';
import { UserSummary } from '../../../core/models/user.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {

  users: UserSummary[] = [];

  // Filters
  searchTerm = '';
  selectedRole = 'ALL';
  sortDir = 'desc';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  loading = false;
  errorMessage = '';

  private searchSubject = new Subject<string>();

  constructor(private userService: UserServiceService) {}

  ngOnInit(): void {
    this.loadUsers();

    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.currentPage = 0;
      this.loadUsers();
    });
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.userService.searchUsers(
      this.searchTerm,
      this.selectedRole,
      'createdAt',
      this.sortDir,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (res) => {
        this.users = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403) {
          this.errorMessage = 'Access denied. You do not have permission to view users.';
        } else {
          this.errorMessage = 'Failed to load users. Please try again.';
        }
      }
    });
  }

  onSearch(term: string): void {
    this.searchSubject.next(term);
  }

  onRoleChange(role: string): void {
    this.selectedRole = role;
    this.currentPage = 0;
    this.loadUsers();
  }

  toggleSortDir(): void {
    this.sortDir = this.sortDir === 'desc' ? 'asc' : 'desc';
    this.loadUsers();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadUsers();
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
