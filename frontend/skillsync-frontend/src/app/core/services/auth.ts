import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type AppRole = 'ROLE_LEARNER' | 'ROLE_MENTOR' | 'ROLE_ADMIN';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: AppRole;
}

interface TokenPayload {
  sub?: string | number;
  userId?: number;
  id?: number;
  role?: AppRole;
  authorities?: Array<string | { authority?: string }>;
  exp?: number;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly tokenStorageKey = 'token';

  private baseUrl: string = environment.apiUrl;
  private token: string | null = null;

  constructor(private http: HttpClient) {}

  login(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, data);
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, data);
  }

  setToken(token: string) {
    this.token = token;
    localStorage.setItem(this.tokenStorageKey, token);
  }

  getToken(): string | null {
    if (!this.token) {
      this.token = localStorage.getItem(this.tokenStorageKey);
    }
    return this.token;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    if (this.isTokenExpired()) {
      this.logout();
      return false;
    }

    return true;
  }

  logout() {
    this.token = null;
    localStorage.removeItem(this.tokenStorageKey);
  }

  getUserId(): number | null {
    const payload = this.getTokenPayload();
    const value = payload?.userId ?? payload?.id ?? payload?.sub;
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  getUserRole(): AppRole | null {
    const payload = this.getTokenPayload();
    const authorities = payload?.authorities ?? [];
    const firstAuthority = authorities[0];
    const authorityValue =
      typeof firstAuthority === 'string' ? firstAuthority : firstAuthority?.authority;

    const role = payload?.role ?? authorityValue ?? null;
    if (role === 'ROLE_LEARNER' || role === 'ROLE_MENTOR' || role === 'ROLE_ADMIN') {
      return role;
    }
    return null;
  }

  hasRole(role: AppRole): boolean {
    return this.getUserRole() === role;
  }

  getDefaultRouteForRole(role: AppRole | null = this.getUserRole()): string {
    if (role === 'ROLE_ADMIN') {
      return '/admin';
    }
    return '/dashboard';
  }

  private isTokenExpired(): boolean {
    const payload = this.getTokenPayload();
    if (!payload?.exp) {
      return false;
    }

    const nowInSeconds = Math.floor(Date.now() / 1000);
    return payload.exp <= nowInSeconds;
  }

  private getTokenPayload(): TokenPayload | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const part = token.split('.')[1];
      if (!part) {
        return null;
      }

      const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      return JSON.parse(atob(padded)) as TokenPayload;
    } catch {
      return null;
    }
  }
}
