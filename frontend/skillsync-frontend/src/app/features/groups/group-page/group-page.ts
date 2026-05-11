import { Component, computed, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppRole } from '../../../core/services/auth';
import { Group, GroupResponse } from '../../../core/services/group';
import { Permission } from '../../../core/services/permission';
import { User } from '../../../core/services/user';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-group-page',
  imports: [FormsModule, DatePipe],
  templateUrl: './group-page.html',
  styleUrl: './group-page.css',
})
export class GroupPage {
  groups = signal<GroupResponse[]>([]);
  loading = signal(true);
  actionLoadingIds = signal<Set<number>>(new Set<number>());

  activeTab = signal<'all' | 'my'>('all');
  searchTerm = signal('');

  createName = signal('');
  createDescription = signal('');
  creating = signal(false);

  joinedGroupIds = signal<Set<number>>(new Set<number>());
  currentUserId = signal<number | null>(null);
  role = signal<AppRole | null>(null);
  private joinedKey = 'skillsync_joined_groups';

  successMessage = signal('');
  errorMessage = signal('');

  pageSize = 8;
  currentPage = signal(1);

  totalPages = computed(() => {
    const total = this.visibleGroups().length;
    return Math.max(1, Math.ceil(total / this.pageSize));
  });

  pagedGroups = computed(() => {
    const page = this.currentPage();
    const start = (page - 1) * this.pageSize;
    return this.visibleGroups().slice(start, start + this.pageSize);
  });

  visibleGroups = computed(() => {
    let data = [...this.groups()];

    if (this.activeTab() === 'my') {
      data = data.filter((group) => this.isJoined(group));
    }

    const search = this.searchTerm().trim().toLowerCase();
    if (search) {
      data = data.filter(
        (group) =>
          group.name.toLowerCase().includes(search) ||
          (group.description || '').toLowerCase().includes(search),
      );
    }

    return data;
  });

  private readonly createdByNameMap = signal<Record<number, string>>({});

  createdByLabel(userId: number): string {
    return this.createdByNameMap()[userId] || `User #${userId}`;
  }

  constructor(
    private groupService: Group,
    private permissionService: Permission,
    private userService: User,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.permissionService.refreshAccessContext().subscribe((role) => {
      this.currentUserId.set(this.permissionService.getCurrentUserId());
      this.role.set(role);
      this.restoreJoinedGroups();
      this.loadGroups();
    });

    this.route.queryParamMap.subscribe((params) => {
      const tab = params.get('tab');
      if (tab === 'my') {
        this.activeTab.set('my');
      }
    });
  }

  loadGroups(showLoader = true) {
    if (showLoader) {
      this.loading.set(true);
    }
    this.groupService.getAllGroups().subscribe({
      next: (res: any) => {
        const data = res?.data ?? [];
        const list = Array.isArray(data) ? data : [];
        this.groups.set(list);
        this.currentPage.set(1);
        this.prefetchCreatedByUsers(list);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error fetching groups', err);
        this.errorMessage.set('Failed to fetch groups.');
        this.loading.set(false);
      },
    });
  }

  private prefetchCreatedByUsers(groups: GroupResponse[]) {
    const ids = Array.from(
      new Set(groups.map((g) => Number(g.createdBy)).filter((x) => Number.isFinite(x) && x > 0)),
    );
    if (ids.length === 0) {
      this.createdByNameMap.set({});
      return;
    }

    forkJoin(
      ids.map((id) =>
        this.userService.getUserById(id).pipe(
          map((res: any) => ({ id, name: res?.data?.name as string | undefined })),
          catchError(() => of({ id, name: undefined })),
        ),
      ),
    ).subscribe({
      next: (rows) => {
        const mapObj: Record<number, string> = {};
        for (const row of rows) {
          if (row?.name) {
            mapObj[row.id] = row.name;
          }
        }
        this.createdByNameMap.set(mapObj);
      },
      error: () => this.createdByNameMap.set({}),
    });
  }

  createGroup() {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (!this.createName().trim()) {
      this.errorMessage.set('Group name is required.');
      return;
    }

    this.creating.set(true);
    this.groupService
      .createGroup({
        name: this.createName().trim(),
        description: this.createDescription().trim(),
      })
      .subscribe({
        next: (res: any) => {
          const created = res?.data as GroupResponse | undefined;
          if (created?.id) {
            this.groups.set([created, ...this.groups().filter((group) => group.id !== created.id)]);
            this.joinedGroupIds.update((ids) => {
              const next = new Set(ids);
              next.add(created.id);
              return next;
            });
            this.persistJoinedGroups();
          } else {
            this.loadGroups(false);
          }

          this.successMessage.set('Group created successfully.');
          this.creating.set(false);
          this.createName.set('');
          this.createDescription.set('');
          this.activeTab.set('my');
        },
        error: (err) => {
          console.error('Error creating group', err);
          this.errorMessage.set('Could not create group.');
          this.creating.set(false);
        },
      });
  }

  joinGroup(group: GroupResponse) {
    if (this.isActionLoading(group.id)) {
      return;
    }

    this.successMessage.set('');
    this.errorMessage.set('');
    this.actionLoadingIds.update((ids) => {
      const next = new Set(ids);
      next.add(group.id);
      return next;
    });

    this.groupService.joinGroup(group.id).subscribe({
      next: () => {
        this.joinedGroupIds.update((ids) => {
          const next = new Set(ids);
          next.add(group.id);
          return next;
        });
        this.persistJoinedGroups();
        this.successMessage.set(`Joined "${group.name}" successfully.`);
        this.actionLoadingIds.update((ids) => {
          const next = new Set(ids);
          next.delete(group.id);
          return next;
        });
      },
      error: (err) => {
        console.error('Error joining group', err);
        this.errorMessage.set('Unable to join this group.');
        this.actionLoadingIds.update((ids) => {
          const next = new Set(ids);
          next.delete(group.id);
          return next;
        });
      },
    });
  }

  leaveGroup(group: GroupResponse) {
    if (this.isActionLoading(group.id)) {
      return;
    }

    this.successMessage.set('');
    this.errorMessage.set('');
    this.actionLoadingIds.update((ids) => {
      const next = new Set(ids);
      next.add(group.id);
      return next;
    });

    this.groupService.leaveGroup(group.id).subscribe({
      next: () => {
        this.joinedGroupIds.update((ids) => {
          const next = new Set(ids);
          next.delete(group.id);
          return next;
        });
        this.persistJoinedGroups();
        this.successMessage.set(`Left "${group.name}" successfully.`);
        this.actionLoadingIds.update((ids) => {
          const next = new Set(ids);
          next.delete(group.id);
          return next;
        });
      },
      error: (err) => {
        console.error('Error leaving group', err);
        this.errorMessage.set('Unable to leave this group.');
        this.actionLoadingIds.update((ids) => {
          const next = new Set(ids);
          next.delete(group.id);
          return next;
        });
      },
    });
  }

  isJoined(group: GroupResponse): boolean {
    return this.joinedGroupIds().has(group.id) || group.createdBy === this.currentUserId();
  }

  isOwner(group: GroupResponse): boolean {
    return group.createdBy === this.currentUserId();
  }

  isActionLoading(groupId: number): boolean {
    return this.actionLoadingIds().has(groupId);
  }

  canJoinLeave(): boolean {
    return this.permissionService.can('JOIN_GROUP');
  }

  setTab(tab: 'all' | 'my') {
    this.activeTab.set(tab);
    this.currentPage.set(1);
  }

  onSearchChange(value: string) {
    this.searchTerm.set(value);
    this.currentPage.set(1);
  }

  goToPage(page: number) {
    const bounded = Math.max(1, Math.min(page, this.totalPages()));
    this.currentPage.set(bounded);
  }

  previousPage() {
    this.goToPage(this.currentPage() - 1);
  }

  nextPage() {
    this.goToPage(this.currentPage() + 1);
  }

  private persistJoinedGroups() {
    const userId = this.currentUserId();
    if (!userId) {
      return;
    }
    localStorage.setItem(
      `${this.joinedKey}_${userId}`,
      JSON.stringify(Array.from(this.joinedGroupIds())),
    );
  }

  private restoreJoinedGroups() {
    const userId = this.currentUserId();
    if (!userId) {
      return;
    }

    const saved = localStorage.getItem(`${this.joinedKey}_${userId}`);
    if (!saved) {
      return;
    }

    try {
      const ids = JSON.parse(saved);
      if (Array.isArray(ids)) {
        this.joinedGroupIds.set(
          new Set(ids.map((x) => Number(x)).filter((x) => Number.isFinite(x))),
        );
      }
    } catch {
      this.joinedGroupIds.set(new Set<number>());
    }
  }
}
