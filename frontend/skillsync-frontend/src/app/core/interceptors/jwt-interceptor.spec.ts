import { TestBed } from '@angular/core/testing';
import { HttpInterceptorFn } from '@angular/common/http';
import { HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Auth } from '../services/auth';
import { throwError, of, firstValueFrom } from 'rxjs';
import { vi } from 'vitest';

import { jwtInterceptor } from './jwt-interceptor';

describe('jwtInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req, next) =>
    TestBed.runInInjectionContext(() => jwtInterceptor(req, next));
  let auth: any;
  let router: any;

  beforeEach(() => {
    auth = {
      getToken: vi.fn(() => null),
      logout: vi.fn(),
    };
    router = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        { provide: Auth, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add Authorization header when token exists', async () => {
    auth.getToken.mockReturnValue('abc');
    const req = new HttpRequest('GET', '/api/test');
    await firstValueFrom(interceptor(req, (nextReq) => {
      expect(nextReq.headers.get('Authorization')).toBe('Bearer abc');
      return of({} as any);
    }));
  });

  it('should logout and redirect on 401', async () => {
    auth.getToken.mockReturnValue('abc');
    const req = new HttpRequest('GET', '/api/test');
    await expect(firstValueFrom(
      interceptor(req, () => throwError(() => new HttpErrorResponse({ status: 401 }))),
    )).rejects.toBeTruthy();
    expect(auth.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
