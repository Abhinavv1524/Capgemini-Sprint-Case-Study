import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Session, SessionResponse } from '../../../core/services/session';
import { DatePipe } from '@angular/common';
import { AppRole } from '../../../core/services/auth';
import { Permission } from '../../../core/services/permission';
import { Activity } from '../../../core/services/activity';
import { Mentor } from '../../../core/services/mentor';
import { User } from '../../../core/services/user';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

type MentorPick = {
  mentorId: number;
  userName: string;
  userEmail?: string;
};

@Component({
  selector: 'app-session-page',
  imports: [FormsModule, DatePipe],
  templateUrl: './session-page.html',
  styleUrl: './session-page.css',
})
export class SessionPage implements OnInit {
  mentorId = '';
  // ISO string sent to backend (derived from selected date+slot)
  sessionTime = '';

  // Booking UI (frontend-only)
  selectedDate = '';
  selectedSlot = '';
  selectedDurationMinutes = 60;
  slotOptions: Array<{ value: string; label: string; disabled: boolean }> = [];
  minDate = '';
  maxDate = '';
  slotHint = '';
  role = signal<AppRole | null>(null);

  sessions = signal<SessionResponse[]>([]);
  loading = signal<boolean>(true);
  submitting = false;

  mentorNameByMentorId = signal<Record<number, string>>({});
  userNameByAuthUserId = signal<Record<number, string>>({});

  // Booking mentor picker
  approvedMentors = signal<MentorPick[]>([]);
  mentorSearch = signal('');

  // List view controls
  view = signal<'requests' | 'history'>('requests');
  pageSize = 5;
  currentPage = signal(1);

  successMessage = '';
  errorMessage = '';

  constructor(
    private sessionService: Session,
    private route: ActivatedRoute,
    private router: Router,
    private permissionService: Permission,
    private activityService: Activity,
    private mentorService: Mentor,
    private userService: User,
  ) {}

  goToAddReview(item: SessionResponse): void {
    // Only allow for completed sessions (UI-computed completed is ok).
    if (this.effectiveStatus(item) !== 'COMPLETED') return;

    const rawMentorId = Number(item.mentorId);

    // Session.mentorId is the mentor's auth user id (not profile id).
    // Reviews expect the same auth user id, so pass it directly.
    this.router.navigate(['/reviews'], {
      queryParams: {
        mentorId: rawMentorId,
        sessionId: item.id,
      },
    });
  }

  canAddReview(item: SessionResponse): boolean {
    return this.permissionService.can('SUBMIT_REVIEW') && this.effectiveStatus(item) === 'COMPLETED';
  }

  ngOnInit(): void {
    const now = new Date();
    this.minDate = this.formatDateOnly(now);
    this.maxDate = this.formatDateOnly(this.addDays(now, 60));

    this.permissionService.refreshAccessContext().subscribe((role) => {
      this.role.set(role);
      this.loadSessions();
      if (this.permissionService.can('BOOK_SESSION')) {
        this.loadApprovedMentors();
      }
    });

    this.route.queryParamMap.subscribe((params) => {
      const mentorId = params.get('mentorId');
      if (mentorId) {
        this.mentorId = mentorId;
      }

      const view = String(params.get('view') || '').toLowerCase();
      if (view === 'completed' || view === 'history') {
        this.view.set('history');
      } else {
        this.view.set('requests');
      }
      this.currentPage.set(1);
    });
  }

  filteredMentorOptions(): MentorPick[] {
    const list = this.approvedMentors();
    const q = this.mentorSearch().trim().toLowerCase();
    if (!q) return list;

    return list.filter((m) => {
      const hay = `${m.userName} ${m.userEmail ?? ''} ${m.mentorId}`.toLowerCase();
      return hay.includes(q);
    });
  }

  private loadApprovedMentors(): void {
    this.mentorService.getAllMentors().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : [];
        const approved = list.filter((m: any) => String(m?.status || '').toUpperCase() === 'APPROVED');
        const rows: Array<{ mentorId: number; userId: number }> = approved
          .map((m: any) => ({ mentorId: Number(m?.id), userId: Number(m?.userId) }))
          .filter(
            (x: any) =>
              Number.isFinite(x.mentorId) && x.mentorId > 0 && Number.isFinite(x.userId) && x.userId > 0,
          );

        if (rows.length === 0) {
          this.approvedMentors.set([]);
          return;
        }

        const requests: Array<Observable<MentorPick>> = rows.map((r) =>
          this.userService.getUserById(r.userId).pipe(
            map(
              (uRes: any) =>
                ({
                  mentorId: r.mentorId,
                  userName: (uRes?.data?.name as string | undefined) ?? `Mentor #${r.mentorId}`,
                  userEmail: uRes?.data?.email as string | undefined,
                }) as MentorPick,
            ),
            catchError(() => of({ mentorId: r.mentorId, userName: `Mentor #${r.mentorId}` } as MentorPick)),
          ),
        );

        forkJoin(requests).subscribe({
          next: (enriched) => {
            const unique = new Map<number, MentorPick>();
            for (const item of enriched) {
              if (item?.mentorId) {
                unique.set(item.mentorId, item);
              }
            }
            const sorted = Array.from(unique.values()).sort((a, b) => a.userName.localeCompare(b.userName));
            this.approvedMentors.set(sorted);
          },
          error: () => this.approvedMentors.set([]),
        });
      },
      error: () => this.approvedMentors.set([]),
    });
  }

  filteredSessions(): SessionResponse[] {
    const sorted = [...this.sessions()].sort((a, b) => {
      const ta = Date.parse(String(a.sessionTime || ''));
      const tb = Date.parse(String(b.sessionTime || ''));
      return (Number.isFinite(tb) ? tb : 0) - (Number.isFinite(ta) ? ta : 0);
    });
    if (this.view() === 'requests') {
      return sorted.filter((s) => this.effectiveStatus(s) === 'REQUESTED');
    }
    return sorted.filter((s) => this.effectiveStatus(s) !== 'REQUESTED');
  }

  totalPages(): number {
    const total = this.filteredSessions().length;
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  paginatedSessions(): SessionResponse[] {
    const page = this.currentPage();
    const start = (page - 1) * this.pageSize;
    return this.filteredSessions().slice(start, start + this.pageSize);
  }

  goToPage(page: number): void {
    const bounded = Math.max(1, Math.min(page, this.totalPages()));
    this.currentPage.set(bounded);
  }

  previousPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  pageNumbers(): number[] {
    const pages = this.totalPages();
    return Array.from({ length: pages }, (_, idx) => idx + 1);
  }

  onSelectedDateChange(value: string): void {
    this.selectedDate = value;
    this.selectedSlot = '';
    this.sessionTime = '';
    this.slotHint = '';
    this.buildSlots();
  }

  pickSlot(value: string): void {
    if (!value) return;
    const start = this.computeStartDate(this.selectedDate, value);
    if (!start) return;

    // Guard against picking disabled slots via keyboard/devtools.
    if (this.isSlotDisabled(start)) return;

    this.selectedSlot = value;
    this.sessionTime = this.formatLocalDateTime(start);
  }

  setDuration(minutes: number): void {
    this.selectedDurationMinutes = minutes;
  }

  sessionStart(): Date | null {
    if (!this.sessionTime) return null;
    const t = Date.parse(this.sessionTime);
    if (!Number.isFinite(t)) return null;
    return new Date(t);
  }

  sessionEnd(): Date | null {
    const start = this.sessionStart();
    if (!start) return null;
    return new Date(start.getTime() + this.selectedDurationMinutes * 60_000);
  }

  stepHasDate(): boolean {
    return !!this.selectedDate && !this.isWeekendDate(this.selectedDate);
  }

  stepHasSlot(): boolean {
    return this.stepHasDate() && !!this.selectedSlot && !!this.sessionTime;
  }

  stepCanConfirm(): boolean {
    return this.stepHasSlot() && Number(this.mentorId) > 0;
  }

  private buildSlots(): void {
    this.slotOptions = [];

    if (!this.selectedDate) return;

    if (this.isWeekendDate(this.selectedDate)) {
      this.slotHint = 'Please pick a weekday (Mon-Fri).';
      return;
    }

    const day = this.parseDateOnly(this.selectedDate);
    if (!day) {
      this.slotHint = 'Invalid date.';
      return;
    }

    const BUSINESS_START_HOUR = 9;
    const BUSINESS_END_HOUR = 18;
    const INTERVAL_MINUTES = 30;

    const slots: Array<{ value: string; label: string; disabled: boolean }> = [];
    for (let minutes = BUSINESS_START_HOUR * 60; minutes < BUSINESS_END_HOUR * 60; minutes += INTERVAL_MINUTES) {
      const hh = Math.floor(minutes / 60);
      const mm = minutes % 60;
      const value = `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`;
      const start = new Date(day.getFullYear(), day.getMonth(), day.getDate(), hh, mm, 0, 0);

      slots.push({
        value,
        label: start.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' }),
        disabled: this.isSlotDisabled(start),
      });
    }

    this.slotOptions = slots;
    if (slots.every((s) => s.disabled)) {
      this.slotHint = 'No slots available for the selected date.';
    }
  }

  private isSlotDisabled(start: Date): boolean {
    // Standard UX: enforce a short lead time so users can't book immediate past/near-now.
    const LEAD_TIME_MINUTES = 30;
    return start.getTime() < Date.now() + LEAD_TIME_MINUTES * 60_000;
  }

  private computeStartDate(dateOnly: string, timeHHmm: string): Date | null {
    const day = this.parseDateOnly(dateOnly);
    if (!day) return null;
    const m = /^([0-1]\d|2[0-3]):([0-5]\d)$/.exec(timeHHmm);
    if (!m) return null;
    const hh = Number(m[1]);
    const mm = Number(m[2]);
    return new Date(day.getFullYear(), day.getMonth(), day.getDate(), hh, mm, 0, 0);
  }

  private isWeekendDate(dateOnly: string): boolean {
    const d = this.parseDateOnly(dateOnly);
    if (!d) return false;
    const day = d.getDay();
    return day === 0 || day === 6;
  }

  private parseDateOnly(dateOnly: string): Date | null {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateOnly);
    if (!m) return null;
    const y = Number(m[1]);
    const mo = Number(m[2]);
    const d = Number(m[3]);
    if (!Number.isFinite(y) || !Number.isFinite(mo) || !Number.isFinite(d)) return null;
    return new Date(y, mo - 1, d, 0, 0, 0, 0);
  }

  private formatDateOnly(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private addDays(d: Date, days: number): Date {
    const copy = new Date(d.getTime());
    copy.setDate(copy.getDate() + days);
    return copy;
  }

  loadSessions(): void {
    this.loading.set(true);
    this.sessionService.getMySessions().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        this.sessions.set(list);
        this.currentPage.set(1);
        const mentorIds: number[] = Array.from(
          new Set(list.map((s: any) => Number(s?.mentorId)).filter((x: number) => Number.isFinite(x) && x > 0)),
        );
        const learnerIds: number[] = Array.from(
          new Set(list.map((s: any) => Number(s?.learnerId)).filter((x: number) => Number.isFinite(x) && x > 0)),
        );
        const currentUserId = Number(this.permissionService.getCurrentUserId());
        if (Number.isFinite(currentUserId) && currentUserId > 0) {
          learnerIds.push(currentUserId);
        }
        this.prefetchMentorNames(mentorIds);
        this.prefetchUserNamesByAuthIds(learnerIds);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error fetching sessions', err);
        this.errorMessage = 'Failed to load sessions.';
        this.loading.set(false);
        this.mentorNameByMentorId.set({});
      },
    });
  }

  mentorDisplay(mentorId: number): string {
    const name = this.mentorNameByMentorId()[mentorId];
    return name ?? `Mentor #${mentorId}`;
  }

  effectiveStatus(item: SessionResponse): SessionResponse['status'] | 'EXPIRED' {
    // If a requested session time is already in the past, mark as expired in UI and disable actions.
    if (item.status === 'REQUESTED') {
      const t = Date.parse(item.sessionTime);
      if (Number.isFinite(t) && t < Date.now()) {
        return 'EXPIRED';
      }
    }
    // If an accepted session time is in the past, treat it as completed for learner review flow.
    if (item.status === 'ACCEPTED') {
      const t = Date.parse(item.sessionTime);
      if (Number.isFinite(t) && t < Date.now()) {
        return 'COMPLETED';
      }
    }
    return item.status;
  }

  private prefetchMentorNames(mentorIds: number[]) {
    if (mentorIds.length === 0) {
      this.mentorNameByMentorId.set({});
      return;
    }

    // Try auth-user lookup first (common path), then mentor profile lookup fallback.
    forkJoin(
      mentorIds.map((mentorId) =>
        this.userService.getUserByAuthUserId(mentorId).pipe(
          map(() => ({ mentorId, authUserId: mentorId })),
          catchError(() =>
            this.mentorService.getByMentorId(mentorId).pipe(
              map((mRes: any) => ({ mentorId, authUserId: Number(mRes?.data?.userId) })),
              catchError(() => of({ mentorId, authUserId: mentorId })),
            ),
          ),
        ),
      ),
    ).subscribe({
      next: (mentorRows) => {
        const authUserIds = Array.from(
          new Set(mentorRows.map((r) => r.authUserId).filter((x) => Number.isFinite(x) && x > 0)),
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

            const nameByMentorId: Record<number, string> = {};
            for (const mRow of mentorRows) {
              const name = nameByAuthUserId[mRow.authUserId];
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

  onSubmit(): void {
    if (!this.canBook()) {
      this.errorMessage = 'Only learners can request sessions.';
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    const parsedMentorId = Number(this.mentorId);
    if (!parsedMentorId) {
      this.errorMessage = 'Please select a mentor.';
      return;
    }

    if (!this.sessionTime) {
      this.errorMessage = 'Please select a date and time slot.';
      return;
    }

    this.submitting = true;
    this.sessionService
      .createSession({
        mentorId: parsedMentorId,
        // Frontend-only slot picker derives ISO; backend contract remains unchanged.
        sessionTime: this.sessionTime,
      })
      .subscribe({
        next: () => {
          const learnerId = Number(this.permissionService.getCurrentUserId());
          const learnerName = this.userNameByAuthUserId()[learnerId] ?? 'Learner';
          const mentorName =
            this.approvedMentors().find((m) => Number(m.mentorId) === parsedMentorId)?.userName ??
            this.mentorDisplay(parsedMentorId);
          const scheduledAt = new Date(this.sessionTime).toLocaleString();
          this.successMessage = 'Session requested successfully.';
          this.submitting = false;
          this.sessionTime = '';
          this.selectedDate = '';
          this.selectedSlot = '';
          this.slotOptions = [];
          this.activityService.log(
            'SESSION',
            'Session Requested By Learner',
            `Session requested by learner ${learnerName} with mentor ${mentorName}. Scheduled time: ${scheduledAt}.`,
          );
          this.loadSessions();
        },
        error: (err) => {
          console.error('Error creating session', err);
          this.errorMessage = 'Could not create session. Please check input and try again.';
          this.submitting = false;
        },
      });
  }

  cancelSession(sessionId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.sessionService.cancelSession(sessionId).subscribe({
      next: () => {
        this.successMessage = 'Session cancelled successfully.';
        this.loadSessions();
      },
      error: (err) => {
        console.error('Error cancelling session', err);
        this.errorMessage = 'Unable to cancel session.';
      },
    });
  }

  acceptSession(sessionId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.sessionService.acceptSession(sessionId).subscribe({
      next: () => {
        const accepted = this.sessions().find((item) => Number(item.id) === Number(sessionId));
        const currentMentorAuthId = Number(this.permissionService.getCurrentUserId());
        const learnerName = accepted
          ? this.userNameByAuthUserId()[Number(accepted.learnerId)] ?? `Learner #${accepted.learnerId}`
          : 'Learner';
        const scheduledAt = accepted?.sessionTime
          ? new Date(accepted.sessionTime).toLocaleString()
          : 'N/A';
        this.userService.getUserByAuthUserId(currentMentorAuthId).pipe(
          catchError(() => of({ data: { name: undefined } })),
        ).subscribe((res: any) => {
          const mentorName =
            (res?.data?.name as string | undefined) ??
            this.userNameByAuthUserId()[currentMentorAuthId] ??
            `Mentor #${currentMentorAuthId}`;
          this.successMessage = 'Session accepted successfully.';
          this.activityService.log(
            'SESSION',
            'Session Accepted By Mentor',
            `Session accepted by mentor ${mentorName} for learner ${learnerName}. Scheduled time: ${scheduledAt}.`,
          );
          this.loadSessions();
        });
      },
      error: (err) => {
        console.error('Error accepting session', err);
        this.errorMessage = 'Unable to accept session.';
      },
    });
  }

  rejectSession(sessionId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.sessionService.rejectSession(sessionId).subscribe({
      next: () => {
        this.successMessage = 'Session rejected successfully.';
        this.loadSessions();
      },
      error: (err) => {
        console.error('Error rejecting session', err);
        this.errorMessage = 'Unable to reject session.';
      },
    });
  }

  canCancel(item: SessionResponse): boolean {
    const status = this.effectiveStatus(item);
    return this.role() === 'ROLE_LEARNER' && (status === 'REQUESTED' || status === 'ACCEPTED');
  }

  canRespond(item: SessionResponse): boolean {
    return this.permissionService.can('RESPOND_SESSION') && this.effectiveStatus(item) === 'REQUESTED';
  }

  canBook(): boolean {
    return this.permissionService.can('BOOK_SESSION');
  }

  getStatusBadgeClass(status: SessionResponse['status'] | 'EXPIRED'): string {
    if (status === 'ACCEPTED') return 'text-bg-success';
    if (status === 'REJECTED' || status === 'CANCELLED') return 'text-bg-danger';
    if (status === 'COMPLETED') return 'text-bg-primary';
    if (status === 'EXPIRED') return 'text-bg-warning';
    return 'text-bg-secondary';
  }

  private formatLocalDateTime(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    const ss = String(d.getSeconds()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}T${hh}:${min}:${ss}`;
  }

  private prefetchUserNamesByAuthIds(authUserIds: number[]) {
    if (authUserIds.length === 0) return;

    forkJoin(
      authUserIds.map((authUserId) =>
        this.userService.getUserByAuthUserId(authUserId).pipe(
          map((uRes: any) => ({ authUserId, name: uRes?.data?.name as string | undefined })),
          catchError(() => of({ authUserId, name: undefined })),
        ),
      ),
    ).subscribe({
      next: (rows) => {
        const mapById: Record<number, string> = { ...this.userNameByAuthUserId() };
        for (const row of rows) {
          if (row?.name) {
            mapById[row.authUserId] = row.name;
          }
        }
        this.userNameByAuthUserId.set(mapById);
      },
      error: () => {},
    });
  }
}
