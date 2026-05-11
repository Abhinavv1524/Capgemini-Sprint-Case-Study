import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Review, ReviewResponse } from '../../../core/services/review';
import { Permission } from '../../../core/services/permission';
import { ActivatedRoute } from '@angular/router';
import { Mentor } from '../../../core/services/mentor';
import { of, forkJoin } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-review-page',
  imports: [FormsModule, DatePipe],
  templateUrl: './review-page.html',
  styleUrl: './review-page.css',
})
export class ReviewPage {
  mentorId = signal<number | null>(null);
  sessionId = signal<number | null>(null);
  rating = signal(5);
  comment = signal('');

  targetMentorId = signal<number | null>(null);
  reviews = signal<ReviewResponse[]>([]);
  averageRating = signal<number | null>(null);
  totalReviews = signal(0);
  reviewedSessionIds = signal<Set<number>>(new Set<number>());

  submitting = signal(false);
  loading = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  canSubmit = computed(() => this.permissionService.can('SUBMIT_REVIEW'));

  isTemporarilyUnavailable = computed(() => {
    const msg = String(this.errorMessage() || '').toLowerCase();
    return msg.includes('temporarily unavailable');
  });

  // Mentor view
  mentorProfileId = signal<number | null>(null);
  activeTab = signal<'submit' | 'myRatings'>('submit');
  ratingFilter = signal<number | 0>(0);
  sortOrder = signal<'newest' | 'oldest'>('newest');
  pageSize = 8;
  currentPage = signal(1);

  visibleReviews = computed(() => {
    let list = [...this.reviews()];
    const r = this.ratingFilter();
    if (r && Number.isFinite(r)) {
      list = list.filter((x) => Number(x.rating) === r);
    }
    list.sort((a, b) => {
      const ta = Date.parse(String(a.createdAt || ''));
      const tb = Date.parse(String(b.createdAt || ''));
      const da = Number.isFinite(ta) ? ta : 0;
      const db = Number.isFinite(tb) ? tb : 0;
      return this.sortOrder() === 'newest' ? db - da : da - db;
    });
    return list;
  });
  learnerVisibleReviews = computed(() => {
    const list = [...this.reviews()];
    list.sort((a, b) => {
      const ta = Date.parse(String(a.createdAt || ''));
      const tb = Date.parse(String(b.createdAt || ''));
      const da = Number.isFinite(ta) ? ta : 0;
      const db = Number.isFinite(tb) ? tb : 0;
      return db - da;
    });
    return list;
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.visibleReviews().length / this.pageSize)));
  pagedReviews = computed(() => {
    const page = this.currentPage();
    const start = (page - 1) * this.pageSize;
    return this.visibleReviews().slice(start, start + this.pageSize);
  });

  constructor(
    private reviewService: Review,
    public permissionService: Permission,
    private route: ActivatedRoute,
    private mentorService: Mentor,
  ) {}

  ngOnInit() {
    this.permissionService.refreshAccessContext().subscribe((role) => {
      if (role === 'ROLE_MENTOR') {
        this.activeTab.set('myRatings');
        const authUserId = this.permissionService.getCurrentUserId();
        if (authUserId) {
          const numericAuthId = Number(authUserId);
          if (Number.isFinite(numericAuthId) && numericAuthId > 0) {
            // Reviews API expects auth user ID (same as session.mentorId).
            this.mentorProfileId.set(numericAuthId);
            this.targetMentorId.set(numericAuthId);
            this.mentorId.set(numericAuthId);
            this.loadMentorReviews(true);
          }
        }
      }
    });

    // Support deep-linking from completed sessions (prefill IDs so the user doesn't type them).
    this.route.queryParamMap.subscribe((params) => {
      const mentorId = Number(params.get('mentorId'));
      const sessionId = Number(params.get('sessionId'));

      if (Number.isFinite(mentorId) && mentorId > 0) {
        this.mentorId.set(mentorId);
        // Don't set targetMentorId here; loadMentorReviews will resolve it.
      }
      if (Number.isFinite(sessionId) && sessionId > 0) {
        this.sessionId.set(sessionId);
      }

      if (Number.isFinite(mentorId) && mentorId > 0) {
        if (this.permissionService.getCurrentRole() !== 'ROLE_MENTOR') {
          this.activeTab.set('submit');
        }
        this.loadMentorReviews(false);
      }
    });
  }

  setRatingFilter(value: number) {
    const n = Number(value);
    this.ratingFilter.set(Number.isFinite(n) ? (n as any) : 0);
    this.currentPage.set(1);
  }

  setSort(value: 'newest' | 'oldest') {
    this.sortOrder.set(value);
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

  canSubmitReview(): boolean {
    return this.permissionService.can('SUBMIT_REVIEW');
  }

  submitReview() {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (!this.canSubmitReview()) {
      this.errorMessage.set('Only learners can submit reviews.');
      return;
    }

    const mentorIdInput = Number(this.mentorId());
    const sessionId = Number(this.sessionId());
    if (!mentorIdInput || !sessionId) {
      this.errorMessage.set('Mentor auth user ID and Session ID are required.');
      return;
    }

    this.submitting.set(true);

    this.reviewService.getReviewsByMentor(mentorIdInput).pipe(
      catchError(() => of({ data: [] })),
    ).subscribe({
      next: (existingRes: any) => {
        const existing = Array.isArray(existingRes?.data) ? existingRes.data : [];
        const alreadyReviewed = existing.some((item: any) => Number(item?.sessionId) === sessionId);
        if (alreadyReviewed) {
          this.errorMessage.set('You can submit a review only once for a session.');
          this.submitting.set(false);
          return;
        }

        // Review API expects mentor auth user ID (same as session.mentorId).
        this.reviewService
          .addReview({
            mentorId: mentorIdInput,
            sessionId,
            rating: this.rating(),
            comment: this.comment().trim(),
          })
          .subscribe({
            next: () => {
              this.successMessage.set('Review submitted successfully.');
              this.submitting.set(false);
              this.targetMentorId.set(mentorIdInput);
              this.comment.set('');
              this.loadMentorReviews(true);
            },
            error: (err: any) => {
              const rawMessage =
                err?.error?.message || err?.error?.error || err?.message || 'Unable to submit review.';
              const message = String(rawMessage);
              const normalized = message.toLowerCase();

              if (normalized.includes('temporarily unavailable')) {
                this.errorMessage.set(
                  `${message} If this keeps happening, ensure the session service is running (via gateway on :8080), then retry.`,
                );
                this.submitting.set(false);
                return;
              }

              console.error('Error submitting review', err);
              this.errorMessage.set(String(message));
              this.submitting.set(false);
            },
          });
      },
      error: () => {
        this.errorMessage.set('Unable to verify existing reviews.');
        this.submitting.set(false);
      },
    });
  }

  loadMentorReviews(showLoader = true) {
    this.successMessage.set('');
    this.errorMessage.set('');
    if (showLoader) {
      this.loading.set(true);
    }

    const rawMentorId = this.targetMentorId();
    if (!rawMentorId) {
      this.errorMessage.set('Enter mentor auth user ID to fetch reviews.');
      this.loading.set(false);
      return;
    }

    // Review API uses mentor auth user ID (same as session.mentorId). Use as-is.
    const useId = rawMentorId;
    this.targetMentorId.set(useId);

    return forkJoin({
      reviews: this.reviewService.getReviewsByMentor(useId).pipe(
        catchError(() => of({ data: [] })),
      ),
      rating: this.reviewService.getRatingByMentor(useId).pipe(
        catchError(() => of({ data: null })),
      ),
    })
      .subscribe({
        next: (res: any) => {
          const list = res?.reviews?.data ?? [];
          this.reviews.set(Array.isArray(list) ? list : []);
          const reviewedSet = new Set<number>();
          for (const item of this.reviews()) {
            const sid = Number((item as any)?.sessionId);
            if (Number.isFinite(sid) && sid > 0) {
              reviewedSet.add(sid);
            }
          }
          this.reviewedSessionIds.set(reviewedSet);
          this.averageRating.set(res?.rating?.data?.averageRating ?? null);
          this.totalReviews.set(res?.rating?.data?.totalReviews ?? 0);
          this.currentPage.set(1);
          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set('Unable to fetch reviews.');
          this.reviews.set([]);
          this.averageRating.set(null);
          this.totalReviews.set(0);
          this.currentPage.set(1);
          this.loading.set(false);
        },
      });
  }
}
