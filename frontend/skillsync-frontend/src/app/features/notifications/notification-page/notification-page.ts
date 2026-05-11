import { Component, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Activity } from '../../../core/services/activity';

@Component({
  selector: 'app-notification-page',
  imports: [DatePipe],
  templateUrl: './notification-page.html',
  styleUrl: './notification-page.css',
})
export class NotificationPage {
  constructor(private activityService: Activity) {}

  notifications = computed(() => this.activityService.items());

  icon(type: string): string {
    if (type === 'SESSION') {
      return '[S]';
    }
    if (type === 'MENTOR') {
      return '[M]';
    }
    if (type === 'GROUP') {
      return '[G]';
    }
    if (type === 'SKILL') {
      return '[K]';
    }
    return '[N]';
  }

  formattedDescription(text: string): string {
    let result = String(text || '');

    result = result.replace(
      /Session requested by learner (.+?) with mentor (.+?)\. Scheduled time: (.+?)\./i,
      'Session requested by learner <strong>$1</strong> with mentor <strong>$2</strong>. Scheduled time: <strong>$3</strong>.',
    );

    result = result.replace(
      /Session accepted by mentor (.+?) for learner (.+?)\. Scheduled time: (.+?)\./i,
      'Session accepted by mentor <strong>$1</strong> for learner <strong>$2</strong>. Scheduled time: <strong>$3</strong>.',
    );

    return result;
  }
}
