import { Component, computed, signal, OnInit } from '@angular/core';
import { Mentor } from '../../../core/services/mentor';
import { Api, SkillResponse } from '../../../core/services/api';
import { User } from '../../../core/services/user';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Activity } from '../../../core/services/activity';
import { forkJoin } from 'rxjs';
import { Group } from '../../../core/services/group';

interface KpiCard {
  label: string;
  value: number;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-admin-page',
  imports: [FormsModule, DatePipe],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.css',
})
export class AdminPage implements OnInit {
  mentors = signal<any[]>([]);
  users = signal<any[]>([]);
  groups = signal<any[]>([]);
  loading = signal(true);
  actionLoadingIds = signal<Set<number>>(new Set<number>());
  successMessage = signal('');
  errorMessage = signal('');
  skills = signal<SkillResponse[]>([]);
  skillName = signal('');
  creatingSkill = signal(false);

  kpis = signal<KpiCard[]>([]);
  kpiLoading = signal(true);
  activePanel = signal<'users' | 'mentors' | 'groups' | 'approvals'>('approvals');
  adminSection = signal<'operations' | 'skills'>('operations');

  recentActivity = computed(() => this.activityService.recent(8));

  pendingMentors = computed(() =>
    this.mentors().filter((mentor) => String(mentor.status || '').toUpperCase() === 'PENDING'),
  );

  constructor(
    private mentorService: Mentor,
    private apiService: Api,
    private userService: User,
    private groupService: Group,
    private activityService: Activity,
  ) {}

  ngOnInit() {
    this.activePanel.set('approvals');
    this.loadMentors();
    this.loadUsers();
    this.loadGroups();
    this.loadSkills();
    this.loadKpis();
  }

  loadKpis() {
    this.kpiLoading.set(true);
    forkJoin({
      users: this.userService.getAllUsers(),
      mentors: this.mentorService.getAllMentors(),
      groups: this.groupService.getAllGroups(),
      skills: this.apiService.getAllSkills(),
    }).subscribe({
      next: (res) => {
        const users = this.unwrapArray((res.users as any)?.data ?? res.users);
        const mentors = this.unwrapArray((res.mentors as any)?.data ?? res.mentors);
        const groups = this.unwrapArray((res.groups as any)?.data ?? res.groups);
        const skills = this.unwrapArray((res.skills as any)?.data ?? res.skills);
        const pendingMentorRequests = mentors.filter(
          (item: any) => String(item?.status || '').toUpperCase() === 'PENDING',
        ).length;

        this.kpis.set([
          { label: 'Total Users', value: users.length, icon: 'U', color: 'primary' },
          { label: 'Total Mentors', value: mentors.length, icon: 'M', color: 'success' },
          { label: 'Pending Approvals', value: pendingMentorRequests, icon: 'P', color: 'warning' },
          { label: 'Total Groups', value: groups.length, icon: 'G', color: 'info' },
        ]);
        this.kpiLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading KPIs', err);
        this.kpis.set([
          { label: 'Total Users', value: 0, icon: 'U', color: 'primary' },
          { label: 'Total Mentors', value: 0, icon: 'M', color: 'success' },
          { label: 'Pending Approvals', value: 0, icon: 'P', color: 'warning' },
          { label: 'Total Groups', value: 0, icon: 'G', color: 'info' },
        ]);
        this.kpiLoading.set(false);
      },
    });
  }

  loadMentors() {
    this.loading.set(true);
    this.mentorService.getAllMentors().subscribe({
      next: (res: any) => {
        this.mentors.set(res?.data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading mentors', err);
        this.errorMessage.set('Failed to fetch mentor applications.');
        this.loading.set(false);
      },
    });
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (res: any) => this.users.set(this.unwrapArray(res?.data ?? res)),
      error: () => this.users.set([]),
    });
  }

  loadGroups() {
    this.groupService.getAllGroups().subscribe({
      next: (res: any) => this.groups.set(this.unwrapArray(res?.data ?? res)),
      error: () => this.groups.set([]),
    });
  }

  approveMentor(mentorId: number) {
    this.setActionLoading(mentorId, true);
    this.clearMessages();
    this.mentorService.approveMentor(mentorId).subscribe({
      next: () => {
        this.successMessage.set('Mentor approved successfully.');
        this.activityService.log('MENTOR', 'Mentor Approved', `Mentor #${mentorId} approved`);
        this.loadMentors();
        this.loadKpis();
        this.setActionLoading(mentorId, false);
      },
      error: (err) => {
        console.error('Error approving mentor', err);
        this.errorMessage.set('Unable to approve mentor.');
        this.setActionLoading(mentorId, false);
      },
    });
  }

  rejectMentor(mentorId: number) {
    this.setActionLoading(mentorId, true);
    this.clearMessages();
    this.mentorService.rejectMentor(mentorId).subscribe({
      next: () => {
        this.successMessage.set('Mentor rejected successfully.');
        this.activityService.log('MENTOR', 'Mentor Rejected', `Mentor #${mentorId} rejected`);
        this.loadMentors();
        this.setActionLoading(mentorId, false);
      },
      error: (err) => {
        console.error('Error rejecting mentor', err);
        this.errorMessage.set('Unable to reject mentor.');
        this.setActionLoading(mentorId, false);
      },
    });
  }

  loadSkills() {
    this.apiService.getAllSkills().subscribe({
      next: (res: any) => this.skills.set(res?.data ?? []),
      error: () => this.skills.set([]),
    });
  }

  createSkill() {
    this.clearMessages();
    const name = this.skillName().trim();
    if (!name) {
      this.errorMessage.set('Skill name is required.');
      return;
    }

    this.creatingSkill.set(true);
    this.apiService.createSkill({ name }).subscribe({
      next: () => {
        this.successMessage.set('Skill created successfully.');
        this.activityService.log('SKILL', 'Skill Created', `Skill "${name}" added`);
        this.skillName.set('');
        this.creatingSkill.set(false);
        this.loadSkills();
      },
      error: (err) => {
        console.error('Error creating skill', err);
        this.errorMessage.set('Unable to create skill.');
        this.creatingSkill.set(false);
      },
    });
  }

  isActionLoading(mentorId: number): boolean {
    return this.actionLoadingIds().has(mentorId);
  }

  onKpiClick(label: string): void {
    switch (label) {
      case 'Total Users':
        this.activePanel.set('users');
        break;
      case 'Total Mentors':
        this.activePanel.set('mentors');
        break;
      case 'Pending Approvals':
        this.activePanel.set('approvals');
        break;
      case 'Total Groups':
        this.activePanel.set('groups');
        break;
    }
  }

  getKpiClass(label: string): string {
    switch (label) {
      case 'Total Users': return 'stat-groups';
      case 'Total Mentors': return 'stat-mentors';
      case 'Pending Approvals': return 'stat-approvals';
      case 'Total Groups': return 'stat-sessions';
      default: return '';
    }
  }

  private setActionLoading(mentorId: number, isLoading: boolean) {
    this.actionLoadingIds.update((ids) => {
      const next = new Set(ids);
      if (isLoading) {
        next.add(mentorId);
      } else {
        next.delete(mentorId);
      }
      return next;
    });
  }

  private clearMessages() {
    this.successMessage.set('');
    this.errorMessage.set('');
  }

  private unwrapArray<T = any>(value: unknown): T[] {
    return Array.isArray(value) ? (value as T[]) : [];
  }
}
