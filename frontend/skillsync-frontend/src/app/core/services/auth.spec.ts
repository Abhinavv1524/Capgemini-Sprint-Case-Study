import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Auth } from './auth';

describe('Auth', () => {
  let service: Auth;
  const originalAtob = globalThis.atob;

  const makeToken = (payload: object) => {
    const base64 = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
    return `a.${base64}.c`;
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Auth);
  });

  afterAll(() => {
    globalThis.atob = originalAtob;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should parse user id and role from jwt token', () => {
    const token = makeToken({ userId: 99, role: 'ROLE_LEARNER' });
    service.setToken(token);

    expect(service.getUserId()).toBe(99);
    expect(service.getUserRole()).toBe('ROLE_LEARNER');
  });

  it('should resolve default route based on role', () => {
    expect(service.getDefaultRouteForRole('ROLE_ADMIN')).toBe('/admin');
    expect(service.getDefaultRouteForRole('ROLE_LEARNER')).toBe('/dashboard');
  });
});
