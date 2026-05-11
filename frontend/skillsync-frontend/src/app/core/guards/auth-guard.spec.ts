import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { Auth } from '../services/auth';
import { vi } from 'vitest';

import { authGuard } from './auth-guard';

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => authGuard(...guardParameters));
  let authService: { isLoggedIn: () => boolean };
  let router: { navigate: (commands: string[]) => void };

  beforeEach(() => {
    authService = { isLoggedIn: () => false };
    router = { navigate: () => {} };
    TestBed.configureTestingModule({
      providers: [
        { provide: Auth, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('should allow navigation when logged in', () => {
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(true);
    const result = executeGuard({} as any, {} as any);
    expect(result).toBe(true);
  });

  it('should redirect to login when not logged in', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(false);
    const result = executeGuard({} as any, {} as any);
    expect(result).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
