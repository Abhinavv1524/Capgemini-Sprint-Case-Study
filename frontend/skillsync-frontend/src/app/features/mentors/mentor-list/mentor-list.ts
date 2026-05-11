import { Component, signal } from '@angular/core';
import { Mentor } from '../../../core/services/mentor';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Permission } from '../../../core/services/permission';
import { User } from '../../../core/services/user';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface MentorInterface {
  id: number;
  userId: number;
  bio: string;
  experience: number;
  hourlyRate: number;
  status?: string;
}

export type MentorWithUser = MentorInterface & {
  userName?: string;
  userEmail?: string;
};

@Component({
  selector: 'app-mentor-list',
  imports: [FormsModule],
  templateUrl: './mentor-list.html',
  styleUrl: './mentor-list.css',
  standalone: true
})
export class MentorList {
  mentors = signal<MentorWithUser[]>([]);
  loading = signal<boolean>(true);
  errorMessage = signal<string>('');
  searchTerm = '';
  minExperience = 0;
  maxRate = 0;
  sortBy = 'relevance';
  pageSize = 6;
  currentPage = 1;

  getFilteredMentors() {
    let data = [...this.mentors()];
    const search = this.searchTerm.trim().toLowerCase();

    if (search) {
      data = data.filter((mentor) => mentor.bio.toLowerCase().includes(search));
    }

    if (this.minExperience > 0) {
      data = data.filter((mentor) => mentor.experience >= this.minExperience);
    }

    if (this.maxRate > 0) {
      data = data.filter((mentor) => mentor.hourlyRate <= this.maxRate);
    }

    if (this.sortBy === 'rateLowHigh') {
      data.sort((a, b) => a.hourlyRate - b.hourlyRate);
    }
    if (this.sortBy === 'experienceHighLow') {
      data.sort((a, b) => b.experience - a.experience);
    }

    return data;
  }

  paginatedMentors() {
    const data = this.getFilteredMentors();
    const start = (this.currentPage - 1) * this.pageSize;
    return data.slice(start, start + this.pageSize);
  }

  totalPages(): number {
    const total = this.getFilteredMentors().length;
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  pageNumbers(): number[] {
    const pages = this.totalPages();
    return Array.from({ length: pages }, (_, idx) => idx + 1);
  }

  goToPage(page: number) {
    const bounded = Math.max(1, Math.min(page, this.totalPages()));
    this.currentPage = bounded;
  }

  previousPage() {
    this.goToPage(this.currentPage - 1);
  }

  nextPage() {
    this.goToPage(this.currentPage + 1);
  }

  private resetPagination() {
    this.currentPage = 1;
  }

  constructor(
    private router: Router,
    private mentorService: Mentor,
    private permissionService: Permission,
    private userService: User,
  ) {}

  ngOnInit(){
    this.permissionService.refreshAccessContext().subscribe();
    this.mentorService.getAllMentors().subscribe({
      next : (res : any) =>{
        const mentors: any[] = res?.data ?? res ?? [];
        const list = (Array.isArray(mentors) ? mentors : []).filter(
          (m) => String(m?.status || '').toUpperCase() === 'APPROVED',
        ) as MentorInterface[];

        if (list.length === 0) {
          this.mentors.set([]);
          this.loading.set(false);
          return;
        }

        // Enrich mentors with user names for display.
        const requests = list.map((m) =>
          this.userService.getUserById(m.userId).pipe(
            map((uRes: any) => {
              const u = uRes?.data;
              return {
                ...m,
                userName: u?.name ?? undefined,
                userEmail: u?.email ?? undefined,
              } as MentorWithUser;
            }),
            catchError(() => of({ ...m } as MentorWithUser)),
          ),
        );

        forkJoin(requests).subscribe({
          next: (enriched) => {
            this.mentors.set(enriched);
            this.loading.set(false);
          },
          error: () => {
            // Fallback to raw mentors.
            this.mentors.set(list as MentorWithUser[]);
            this.loading.set(false);
          },
        });
      },
      error : (err) =>{
        console.error("Error fetching mentors ", err);
        this.errorMessage.set('Failed to load mentors. Please try again later.');
        this.loading.set(false);
      }
    });
  }

  // viewMentor(mentor : MentorInterface){
  //   this.router.navigate(['/mentors',mentor.userId]);
  // }
  viewMentor(mentor : MentorInterface){
    this.router.navigate(['/mentors',mentor.id]);
  }

  bookMentor(mentor: MentorInterface) {
    if (!this.canBookMentor()) {
      return;
    }
    this.router.navigate(['/sessions'], { queryParams: { mentorId: mentor.id } });
  }

  canBookMentor(): boolean {
    return this.permissionService.can('BOOK_SESSION');
  }

  clearFilters() {
    this.searchTerm = '';
    this.minExperience = 0;
    this.maxRate = 0;
    this.sortBy = 'relevance';
    this.resetPagination();
  }

  onSearchTermChange(value: string) {
    this.searchTerm = value;
    this.resetPagination();
  }

  onMinExperienceChange(value: number) {
    this.minExperience = Number(value) || 0;
    this.resetPagination();
  }

  onMaxRateChange(value: number) {
    this.maxRate = Number(value) || 0;
    this.resetPagination();
  }

  onSortChange(value: string) {
    this.sortBy = value;
    this.resetPagination();
  }
    
}
