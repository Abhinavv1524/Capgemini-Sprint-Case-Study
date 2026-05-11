import { Injectable, signal } from '@angular/core';

export type ActivityType = 'SESSION' | 'MENTOR' | 'GROUP' | 'SKILL' | 'SYSTEM';

export interface ActivityItem {
  id: string;
  type: ActivityType;
  title: string;
  description: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class Activity {
  private readonly storageKey = 'skillsync_activity_feed';
  private readonly maxItems = 120;
  private readonly itemsSignal = signal<ActivityItem[]>(this.restoreItems());

  readonly items = this.itemsSignal.asReadonly();

  log(type: ActivityType, title: string, description: string) {
    const next: ActivityItem = {
      id: this.generateId(),
      type,
      title,
      description,
      createdAt: new Date().toISOString(),
    };

    this.itemsSignal.update((items) => {
      const updated = [next, ...items].slice(0, this.maxItems);
      this.persistItems(updated);
      return updated;
    });
  }

  recent(limit = 8): ActivityItem[] {
    return this.items().slice(0, limit);
  }

  count(): number {
    return this.items().length;
  }

  private restoreItems(): ActivityItem[] {
    const saved = localStorage.getItem(this.storageKey);
    if (!saved) {
      return [];
    }

    try {
      const parsed = JSON.parse(saved);
      if (!Array.isArray(parsed)) {
        return [];
      }

      return parsed
        .filter((item) => item && item.id && item.type && item.title && item.createdAt)
        .slice(0, this.maxItems);
    } catch {
      return [];
    }
  }

  private persistItems(items: ActivityItem[]) {
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }

  private generateId(): string {
    return `${Date.now()}_${Math.random().toString(16).slice(2)}`;
  }
}
