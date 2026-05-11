import { NotificationPage } from './notification-page';
import { Activity } from '../../../core/services/activity';
import { vi } from 'vitest';

describe('NotificationPage', () => {
  let component: NotificationPage;

  beforeEach(() => {
    const activity = {
      items: vi.fn(() => []),
    } as unknown as Activity;
    component = new NotificationPage(activity);
  });

  it('should format session requested message with bold tags', () => {
    const text = 'Session requested by learner Alice with mentor Bob. Scheduled time: 4/30/2026, 3:30:00 PM.';
    const html = component.formattedDescription(text);
    expect(html).toContain('<strong>Alice</strong>');
    expect(html).toContain('<strong>Bob</strong>');
    expect(html).toContain('<strong>4/30/2026, 3:30:00 PM</strong>');
  });
});
