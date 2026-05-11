import { Component, EventEmitter, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AppRole } from '../../core/services/auth';
import { Permission } from '../../core/services/permission';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  @Output() navSelect = new EventEmitter<void>();

  constructor(private permissionService: Permission) {}

  ngOnInit() {
    this.permissionService.refreshAccessContext().subscribe();
  }

  get role(): AppRole | null {
    return this.permissionService.getCurrentRole();
  }

  isLearner() {
    return this.role === 'ROLE_LEARNER';
  }

  isMentor() {
    return this.role === 'ROLE_MENTOR';
  }

  isAdmin() {
    return this.role === 'ROLE_ADMIN';
  }

  onNavClick() {
    this.navSelect.emit();
  }
}
