import { Component, EventEmitter, Output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AppRole, Auth } from '../../core/services/auth';
import { Permission } from '../../core/services/permission';
import { User } from '../../core/services/user';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  @Output() menuToggle = new EventEmitter<void>();
  userName = signal<string>('');
 
  constructor(
    private authService: Auth,
    private permissionService: Permission,
    private userService: User,
    private router: Router,
  ) {}

  ngOnInit() {
    this.permissionService.refreshAccessContext().subscribe();

    const authUserId = this.authService.getUserId();
    if (authUserId) {
      this.userService.getUserByAuthUserId(authUserId).subscribe({
        next: (res: any) => {
          const name = String(res?.data?.name || '').trim();
          this.userName.set(name);
        },
        error: () => this.userName.set(''),
      });
    }
  }

  get role(): AppRole | null {
    return this.permissionService.getCurrentRole();
  }

  get roleLabel(): string {
    if (this.role === 'ROLE_ADMIN') {
      return 'Admin';
    }
    if (this.role === 'ROLE_MENTOR') {
      return 'Mentor';
    }
    if (this.role === 'ROLE_LEARNER') {
      return 'Learner';
    }
    return 'User';
  }

  get displayLabel(): string {
    const name = this.userName();
    if (name) {
      return name;
    }
    return this.roleLabel;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  toggleMenu() {
    this.menuToggle.emit();
  }
}
