import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AppRole } from '../../../core/services/auth';
import { Mentor } from '../../../core/services/mentor';
import { Session } from '../../../core/services/session';
import { Group } from '../../../core/services/group';
import { DatePipe } from '@angular/common';
import { Permission } from '../../../core/services/permission';
import { User } from '../../../core/services/user';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  imports: [FormsModule, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  role = signal<AppRole | null>(null);

  bio = '';
  experience: number | null = null;
  hourlyRate: number | null = null;
  applying = false;
  applyError = '';
  applySuccess = '';

  mentorStatus = signal<string | null>(null);
  sessionCount = signal(0);
  connectedMentorCount = signal(0);
  groupCount = signal(0);
  completedSessionCount = signal(0);
  pendingMentorApprovals = signal(0);
  totalUsersCount = signal(0);
  dashboardSessions = signal<any[]>([]);
  mentorNameByMentorId = signal<Record<number, string>>({});
  recommendedMentors = signal<any[]>([]);

  constructor(
    private permissionService: Permission,
    private mentorService: Mentor,
    private sessionService: Session,
    private groupService: Group,
    private userService: User,
    private router: Router,
  ) {}

  goToMySessions(): void {
    this.router.navigate(['/sessions']);
  }

  goToCompletedSessions(): void {
    this.router.navigate(['/sessions'], { queryParams: { view: 'completed' } });
  }

  goToMentors(): void {
    this.router.navigate(['/mentors']);
  }

  goToGroups(): void {
    this.router.navigate(['/groups'], { queryParams: { tab: 'my' } });
  }

  goToMentorSessions(): void {
    this.router.navigate(['/sessions']);
  }

  goToMyRatings(): void {
    this.router.navigate(['/reviews']);
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }

  goToAllSessions(): void {
    this.router.navigate(['/sessions']);
  }

  ngOnInit() {
    this.permissionService.refreshAccessContext().subscribe((role) => {
      this.role.set(role);

      if (this.isLearner()) {
        this.loadMentorStatus();
      } else {
        this.mentorStatus.set(null);
      }

      this.loadDashboardMetrics();
    });
  }

  loadMentorStatus() {
    const userId = this.permissionService.isMentorRoleDerived()
      ? null
      : this.permissionService.getCurrentUserId();
    if (!userId) {
      return;
    }

    this.mentorService.getAllMentors().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        const mine = list.find((m: any) => Number(m?.userId) === Number(userId));
        this.mentorStatus.set(mine?.status || null);
      },
      error: () => {
        this.mentorStatus.set(null);
      },
    });
  }

  applyAsMentor() {
    this.applyError = '';
    this.applySuccess = '';

    if (!this.bio.trim() || this.experience === null || this.hourlyRate === null) {
      this.applyError = 'Please fill all mentor application fields.';
      return;
    }

    this.applying = true;
    this.mentorService
      .applyAsMentor({
        bio: this.bio.trim(),
        experience: this.experience,
        hourlyRate: this.hourlyRate,
      })
      .subscribe({
        next: (res: any) => {
          this.applySuccess = res?.message || 'Mentor application submitted successfully.';
          this.mentorStatus.set(res?.data?.status || 'PENDING');
          this.applying = false;
        },
        error: (err) => {
          console.error('Error applying as mentor', err);
          this.applyError = 'Unable to submit mentor application.';
          this.applying = false;
        },
      });
  }

  loadDashboardMetrics() {
    // Load sessions and mentors for all roles
    this.sessionService.getMySessions().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        this.dashboardSessions.set(list.slice(0, 5));
        this.sessionCount.set(list.length);
        this.completedSessionCount.set(
          list.filter((item: any) => {
            const status = String(item?.status || '').toUpperCase();
            if (status === 'COMPLETED') {
              return true;
            }
            if (status === 'ACCEPTED') {
              const t = Date.parse(String(item?.sessionTime || ''));
              return Number.isFinite(t) && t < Date.now();
            }
            return false;
          }).length,
        );

        const mentorIds: number[] = Array.from(
          new Set(list.map((s: any) => Number(s?.mentorId)).filter((x: number) => Number.isFinite(x) && x > 0)),
        );
        this.prefetchMentorNames(mentorIds);
      },
      error: () => {
        this.dashboardSessions.set([]);
        this.sessionCount.set(0);
        this.completedSessionCount.set(0);
        this.mentorNameByMentorId.set({});
      },
    });

    this.mentorService.getAllMentors().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        const approved = list.filter((item: any) => String(item?.status || '').toUpperCase() === 'APPROVED');
        this.connectedMentorCount.set(approved.length);
        this.recommendedMentors.set(approved.slice(0, 3));
        this.pendingMentorApprovals.set(
          list.filter((item: any) => String(item?.status).toUpperCase() === 'PENDING').length,
        );
      },
      error: () => {
        this.connectedMentorCount.set(0);
        this.recommendedMentors.set([]);
        this.pendingMentorApprovals.set(0);
      },
    });

    this.groupService.getAllGroups().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        this.groupCount.set(list.length);
      },
      error: () => this.groupCount.set(0),
    });

    // Load total users count for admin
    if (this.isAdmin()) {
      this.userService.getAllUsers().subscribe({
        next: (res: any) => {
          const list = Array.isArray(res?.data) ? res.data : [];
          this.totalUsersCount.set(list.length);
        },
        error: () => this.totalUsersCount.set(0),
      });
    }
  }

  mentorDisplay(mentorId: number): string {
    const name = this.mentorNameByMentorId()[mentorId];
    return name ? `${name} (#${mentorId})` : `#${mentorId}`;
  }

  private prefetchMentorNames(mentorIds: number[]) {
    if (mentorIds.length === 0) {
      this.mentorNameByMentorId.set({});
      return;
    }

    // Session.mentorId can be either mentor profile id OR mentor auth user id (legacy/backward compatibility).
    // Try auth-user lookup first (common path), then fall back to mentor profile lookup.
    forkJoin(
      mentorIds.map((mentorId) =>
        this.userService.getUserByAuthUserId(mentorId).pipe(
          map(() => ({ mentorId, userId: mentorId, via: 'authUserId' as const })),
          catchError(() =>
            this.mentorService.getByMentorId(mentorId).pipe(
              map((mRes: any) => ({ mentorId, userId: Number(mRes?.data?.userId), via: 'mentorProfile' as const })),
              catchError(() => of({ mentorId, userId: mentorId, via: 'fallback' as const })),
            ),
          ),
        ),
      ),
    ).subscribe({
      next: (mentorRows) => {
        const nameByMentorId: Record<number, string> = {};

        // Fetch names via /users/auth/{id} (works whether the id is auth id or profile id mapping failed).
        const authUserIds = Array.from(
          new Set(mentorRows.map((r) => r.userId).filter((x) => Number.isFinite(x) && x > 0)),
        ) as number[];

        if (authUserIds.length === 0) {
          this.mentorNameByMentorId.set({});
          return;
        }

        forkJoin(
          authUserIds.map((authUserId) =>
            this.userService.getUserByAuthUserId(authUserId).pipe(
              map((uRes: any) => ({ authUserId, name: uRes?.data?.name as string | undefined })),
              catchError(() => of({ authUserId, name: undefined })),
            ),
          ),
        ).subscribe({
          next: (userRows) => {
            const nameByAuthUserId: Record<number, string> = {};
            for (const row of userRows) {
              if (row?.name) {
                nameByAuthUserId[row.authUserId] = row.name;
              }
            }

            for (const mRow of mentorRows) {
              const name = nameByAuthUserId[mRow.userId];
              if (name) {
                nameByMentorId[mRow.mentorId] = name;
              }
            }

            this.mentorNameByMentorId.set(nameByMentorId);
          },
          error: () => this.mentorNameByMentorId.set({}),
        });
      },
      error: () => this.mentorNameByMentorId.set({}),
    });
  }

  isLearner(): boolean {
    return this.role() === 'ROLE_LEARNER';
  }

  isMentor(): boolean {
    return this.role() === 'ROLE_MENTOR';
  }

  isAdmin(): boolean {
    return this.role() === 'ROLE_ADMIN';
  }

  dashboardTitle(): string {
    if (this.isLearner()) return 'Learner Dashboard';
    if (this.isMentor()) return 'Mentor Dashboard';
    if (this.isAdmin()) return 'Admin Dashboard';
    return 'Dashboard';
  }
}
