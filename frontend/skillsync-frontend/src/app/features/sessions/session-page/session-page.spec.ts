import { SessionPage } from './session-page';
import { vi } from 'vitest';

describe('SessionPage', () => {
  let component: SessionPage;

  beforeEach(() => {
    const sessionSvc = {
      createSession: vi.fn(),
      getMySessions: vi.fn(),
      cancelSession: vi.fn(),
      acceptSession: vi.fn(),
      rejectSession: vi.fn(),
    } as any;
    const route = { queryParamMap: { subscribe: () => {} } } as any;
    const router = { navigate: vi.fn() } as any;
    const permission = {
      can: vi.fn(() => false),
      refreshAccessContext: vi.fn(),
      getCurrentUserId: vi.fn(() => 1),
      isMentorRoleDerived: vi.fn(() => false),
    } as any;
    const activity = { log: vi.fn() } as any;
    const mentor = { getAllMentors: vi.fn(), getByMentorId: vi.fn() } as any;
    const user = { getUserByAuthUserId: vi.fn() } as any;
    component = new SessionPage(sessionSvc, route, router, permission, activity, mentor, user);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should mark requested past session as expired', () => {
    const past = new Date(Date.now() - 60_000).toISOString();
    const status = component.effectiveStatus({
      id: 1,
      mentorId: 2,
      learnerId: 3,
      sessionTime: past,
      status: 'REQUESTED',
      createdAt: new Date().toISOString(),
    });
    expect(status).toBe('EXPIRED');
  });

  it('should mark accepted past session as completed', () => {
    const past = new Date(Date.now() - 60_000).toISOString();
    const status = component.effectiveStatus({
      id: 1,
      mentorId: 2,
      learnerId: 3,
      sessionTime: past,
      status: 'ACCEPTED',
      createdAt: new Date().toISOString(),
    });
    expect(status).toBe('COMPLETED');
  });
});
