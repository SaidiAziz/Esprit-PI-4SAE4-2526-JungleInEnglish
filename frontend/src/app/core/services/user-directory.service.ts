import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, shareReplay } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserDirectoryService {
  private readonly http = inject(HttpClient);
  private readonly cache = new Map<number, Observable<UserResponse | null>>();

  getUserById(userId: number): Observable<UserResponse | null> {
    if (!this.cache.has(userId)) {
      const request$ = this.http.get<UserResponse>(`/user/users/getUserById/${userId}`).pipe(
        catchError(() => of(null)),
        shareReplay(1)
      );
      this.cache.set(userId, request$);
    }
    return this.cache.get(userId)!;
  }
}
