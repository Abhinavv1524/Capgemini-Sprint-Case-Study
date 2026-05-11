import { firstValueFrom, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Permission } from './permission';
import { Auth } from './auth';
import { Mentor } from './mentor';

describe('Permission', () => {
  let authSpy: Auth;
  let mentorSpy: Mentor;
  let service: Permission;

  beforeEach(() => {
    authSpy = {
      isLoggedIn: vi.fn(),
      getUserRole: vi.fn(),
      getUserId: vi.fn(),
    } as unknown as Auth;
    mentorSpy = {
      getAllMentors: vi.fn(),
    } as unknown as Mentor;
    service = new Permission(authSpy, mentorSpy);
  });

  it('should return admin role directly without mentor lookup', async () => {
    (authSpy.isLoggedIn as any).mockReturnValue(true);
    (authSpy.getUserRole as any).mockReturnValue('ROLE_ADMIN');
    (authSpy.getUserId as any).mockReturnValue(1);

    const role = await firstValueFrom(service.refreshAccessContext());
    expect(role).toBe('ROLE_ADMIN');
    expect((mentorSpy.getAllMentors as any).mock.calls.length).toBe(0);
  });

  it('should derive mentor role when approved mentor exists for learner user', async () => {
    (authSpy.isLoggedIn as any).mockReturnValue(true);
    (authSpy.getUserRole as any).mockReturnValue('ROLE_LEARNER');
    (authSpy.getUserId as any).mockReturnValue(7);
    (mentorSpy.getAllMentors as any).mockReturnValue(of({ data: [{ userId: 7, status: 'APPROVED' }] }));

    const role = await firstValueFrom(service.refreshAccessContext());
    expect(role).toBe('ROLE_MENTOR');
    expect(service.isMentorRoleDerived()).toBeTruthy();
  });

  it('should fall back to token role when mentor lookup fails', async () => {
    (authSpy.isLoggedIn as any).mockReturnValue(true);
    (authSpy.getUserRole as any).mockReturnValue('ROLE_LEARNER');
    (authSpy.getUserId as any).mockReturnValue(7);
    (mentorSpy.getAllMentors as any).mockReturnValue(throwError(() => new Error('network')));

    const role = await firstValueFrom(service.refreshAccessContext());
    expect(role).toBe('ROLE_LEARNER');
    expect(service.isMentorRoleDerived()).toBeFalsy();
  });
});
