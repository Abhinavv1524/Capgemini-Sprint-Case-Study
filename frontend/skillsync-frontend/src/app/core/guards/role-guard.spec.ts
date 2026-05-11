import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Auth } from '../services/auth';
import { Permission } from '../services/permission';

import { roleGuard } from './role-guard';

describe('roleGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => roleGuard(...guardParameters));
  let auth: any;
  let router: any;
  let permission: any;

  beforeEach(() => {
    auth = {
      isLoggedIn: vi.fn(() => true),
      logout: vi.fn(),
      getDefaultRouteForRole: vi.fn(() => '/dashboard'),
    };
    router = { navigate: vi.fn() };
    permission = { refreshAccessContext: vi.fn(() => of('ROLE_LEARNER')) };

    TestBed.configureTestingModule({
      providers: [
        { provide: Auth, useValue: auth },
        { provide: Router, useValue: router },
        { provide: Permission, useValue: permission },
      ],
    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('should block and redirect to login when not authenticated', () => {
    auth.isLoggedIn.mockReturnValue(false);
    const result = executeGuard({ data: { roles: ['ROLE_LEARNER'] } } as any, {} as any);
    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should allow when role is permitted', async () => {
    permission.refreshAccessContext.mockReturnValue(of('ROLE_LEARNER'));
    const result$ = executeGuard({ data: { roles: ['ROLE_LEARNER'] } } as any, {} as any) as any;
    result$.subscribe((val: boolean) => expect(val).toBe(true));
  });

  it('should redirect to default route when role not permitted', async () => {
    permission.refreshAccessContext.mockReturnValue(of('ROLE_MENTOR'));
    const result$ = executeGuard({ data: { roles: ['ROLE_LEARNER'] } } as any, {} as any) as any;
    result$.subscribe((val: boolean) => expect(val).toBe(false));
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});
