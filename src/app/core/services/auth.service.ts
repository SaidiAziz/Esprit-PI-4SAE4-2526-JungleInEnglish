import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  UserResponse,
  TutorProfile,
  ForgotPasswordRequest,
  ResetPasswordRequest
} from '../models/user.model';

// Re-export so existing imports from auth.service still work
export type { LoginRequest, LoginResponse, RegisterRequest, UserResponse, TutorProfile, ForgotPasswordRequest, ResetPasswordRequest };

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, request).pipe(
      tap(response => {
        if (this.isBrowser()) {
          localStorage.setItem(this.TOKEN_KEY, response.token);
          localStorage.setItem(this.USER_KEY, JSON.stringify(response.user));
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.API_URL}/register`, request);
  }

  updateCurrentUser(updatedUser: UserResponse): void {
    if (!this.isBrowser()) return;
    localStorage.setItem(this.USER_KEY, JSON.stringify(updatedUser));
  }

  verify2FA(email: string, code: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.API_URL}/verify-2fa`, { email, code });
  }

  setupTotp(email: string): Observable<{ qrCodeBase64: string; secret: string }> {
    return this.http.post<{ qrCodeBase64: string; secret: string }>(`${this.API_URL}/2fa/setup-totp`, { email });
  }

  enable2FA(
    email: string,
    method: 'EMAIL' | 'TOTP',
    code?: string
  ): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/2fa/enable`, {
      email,
      method,
      ...(code ? { code } : {})
    });
  }

  disable2FA(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/2fa/disable`, { email });
  }

  logout(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
  }

  getToken(): string | null {
    if (!this.isBrowser()) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): UserResponse | null {
    if (!this.isBrowser()) return null;
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUserRole(): 'STUDENT' | 'TUTOR' | 'ADMIN' | null {
    const user = this.getCurrentUser();
    return user ? user.role : null;
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/reset-password`, request);
  }
}

