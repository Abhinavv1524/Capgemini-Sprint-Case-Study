import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../../core/services/auth';
import { User } from '../../../core/services/user';
import { Api, SkillResponse } from '../../../core/services/api';

@Component({
  selector: 'app-profile-page',
  imports: [FormsModule],
  templateUrl: './profile-page.html',
  styleUrl: './profile-page.css',
})
export class ProfilePage {
  private readonly authUserId = signal<number | null>(null);
  private readonly profileId = signal<number | null>(null);

  name = signal('');
  email = signal('');
  bio = signal('');
  skills = signal('');
  role = signal('');

  skillCatalog = signal<SkillResponse[]>([]);
  catalogLoaded = signal(false);
  skillSearch = signal('');
  selectedSkillNames = signal<Set<string>>(new Set());

  loading = signal(false);
  saving = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  canRefresh = computed(() => !this.loading() && !this.saving());

  constructor(private authService: Auth, private userService: User, private apiService: Api) {}

  ngOnInit() {
    this.authUserId.set(this.authService.getUserId());
    this.loadSkillCatalog();
    this.loadProfile(true);
  }

  private loadSkillCatalog() {
    this.apiService.getAllSkills().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res?.data) ? res.data : [];
        this.skillCatalog.set(list);
        this.catalogLoaded.set(true);
        // After catalog loads, normalize selected skills against it.
        this.syncSelectedSkillsFromString(this.skills());
      },
      error: () => {
        this.skillCatalog.set([]);
        this.catalogLoaded.set(false);
      },
    });
  }

  filteredSkillCatalog(): SkillResponse[] {
    const list = this.skillCatalog();
    const q = this.skillSearch().trim().toLowerCase();
    if (!q) return list;
    return list.filter((s) => String(s?.name || '').toLowerCase().includes(q));
  }

  selectedSkillList(): string[] {
    return Array.from(this.selectedSkillNames()).sort();
  }

  toggleSkill(name: string, checked: boolean) {
    const n = String(name || '').trim();
    if (!n) return;
    this.selectedSkillNames.update((set) => {
      const next = new Set(set);
      if (checked) next.add(n);
      else next.delete(n);
      return next;
    });
    this.skills.set(Array.from(this.selectedSkillNames()).sort().join(', '));
  }

  private selectedSkillsString(): string {
    return Array.from(this.selectedSkillNames()).sort().join(', ');
  }

  clearSelectedSkills() {
    this.selectedSkillNames.set(new Set());
    this.skills.set('');
  }

  private syncSelectedSkillsFromString(value: string) {
    const raw = String(value || '')
      .split(',')
      .map((x) => x.trim())
      .filter(Boolean);

    const catalog = this.skillCatalog();
    if (!catalog || catalog.length === 0) {
      // Catalog not loaded yet (or unavailable). Preserve what we have.
      this.selectedSkillNames.set(new Set(raw));
      this.skills.set(raw.join(', '));
      return;
    }

    // Normalize against catalog (case-insensitive) while keeping catalog's canonical casing.
    const allowedByLower = new Map<string, string>();
    for (const s of catalog) {
      const name = String(s?.name || '').trim();
      if (name) {
        allowedByLower.set(name.toLowerCase(), name);
      }
    }

    const normalized: string[] = [];
    for (const name of raw) {
      const canon = allowedByLower.get(name.toLowerCase());
      if (canon) {
        normalized.push(canon);
      }
    }

    const unique = Array.from(new Set(normalized));
    this.selectedSkillNames.set(new Set(unique));
    this.skills.set(unique.join(', '));
  }

  loadProfile(showLoader = true) {
    this.successMessage.set('');
    this.errorMessage.set('');
    if (showLoader) {
      this.loading.set(true);
    }

    const authUserId = this.authUserId();
    if (!authUserId) {
      this.errorMessage.set('User context not found.');
      this.loading.set(false);
      return;
    }

    this.userService.getUserByAuthUserId(authUserId).subscribe({
      next: (res: any) => {
        const data = res?.data;
        this.profileId.set(data?.id ?? null);
        this.name.set(data?.name ?? '');
        this.email.set(data?.email ?? '');
        this.bio.set(data?.bio ?? '');
        this.skills.set(data?.skills ?? '');
        this.syncSelectedSkillsFromString(this.skills());
        this.role.set(data?.role ?? '');
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading profile', err);
        this.errorMessage.set('Unable to load profile.');
        this.loading.set(false);
      },
    });
  }

  saveProfile() {
    this.successMessage.set('');
    this.errorMessage.set('');

    const profileId = this.profileId();
    if (!profileId) {
      this.errorMessage.set('Profile ID not available.');
      return;
    }

    this.saving.set(true);
    this.userService
      .updateUser(profileId, {
        name: this.name().trim(),
        email: this.email().trim(),
        bio: this.bio().trim(),
        // Save selected skills (catalog-based) as comma-separated text.
        skills: this.selectedSkillsString(),
      })
      .subscribe({
        next: () => {
          this.successMessage.set('Profile updated successfully.');
          this.saving.set(false);
          // Re-fetch to reflect backend formatting/normalization if any.
          this.loadProfile(false);
        },
        error: (err) => {
          console.error('Error updating profile', err);
          this.errorMessage.set('Unable to update profile.');
          this.saving.set(false);
        },
      });
  }
}
