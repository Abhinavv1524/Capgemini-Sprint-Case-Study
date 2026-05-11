import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AppRole, Auth } from './auth';
import { Mentor } from './mentor';

export type PermissionAction =
  | 'VIEW_ADMIN'
  | 'BOOK_SESSION'
  | 'RESPOND_SESSION'
  | 'CREATE_GROUP'
  | 'JOIN_GROUP'
  | 'SUBMIT_REVIEW'
  | 'MANAGE_SKILL_CATALOG';

@Injectable({
  providedIn: 'root',
})
export class Permission {
  private readonly effectiveRoleSubject = new BehaviorSubject<AppRole | null>(null);
  private readonly mentorRoleDerivedSubject = new BehaviorSubject<boolean>(false);
  private checkedMentorLookupForUserId: number | null = null;

  readonly effectiveRole$ = this.effectiveRoleSubject.asObservable();

  constructor(private authService: Auth, private mentorService: Mentor) {
    this.effectiveRoleSubject.next(this.authService.getUserRole());
  }

  refreshAccessContext(): Observable<AppRole | null> {
    if (!this.authService.isLoggedIn()) {
      this.effectiveRoleSubject.next(null);
      this.mentorRoleDerivedSubject.next(false);
      return of(null);
    }

    const tokenRole = this.authService.getUserRole();
    if (tokenRole === 'ROLE_ADMIN' || tokenRole === 'ROLE_MENTOR') {
      this.effectiveRoleSubject.next(tokenRole);
      this.mentorRoleDerivedSubject.next(false);
      this.checkedMentorLookupForUserId = this.authService.getUserId();
      return of(tokenRole);
    }

    const userId = this.authService.getUserId();
    if (!userId) {
      this.effectiveRoleSubject.next(tokenRole);
      this.mentorRoleDerivedSubject.next(false);
      return of(tokenRole);
    }

    // Avoid repeated /mentors/user/{id} probes from multiple components in the same login session.
    if (this.checkedMentorLookupForUserId === userId) {
      return of(this.effectiveRoleSubject.value ?? tokenRole);
    }

    return this.mentorService.getAllMentors().pipe(
      map((res: any) => {
        this.checkedMentorLookupForUserId = userId;
        const list = Array.isArray(res?.data) ? res.data : [];
        const mine = list.find((m: any) => Number(m?.userId) === Number(userId));
        const status = String(mine?.status ?? '').toUpperCase();
        if (status === 'APPROVED') {
          this.effectiveRoleSubject.next('ROLE_MENTOR');
          this.mentorRoleDerivedSubject.next(true);
          return 'ROLE_MENTOR' as AppRole;
        }

        this.effectiveRoleSubject.next(tokenRole);
        this.mentorRoleDerivedSubject.next(false);
        return tokenRole;
      }),
      catchError(() => {
        this.checkedMentorLookupForUserId = userId;
        this.effectiveRoleSubject.next(tokenRole);
        this.mentorRoleDerivedSubject.next(false);
        return of(tokenRole);
      }),
    );
  }

  getCurrentRole(): AppRole | null {
    return this.effectiveRoleSubject.value;
  }

  getCurrentUserId(): number | null {
    return this.authService.getUserId();
  }

  isMentorRoleDerived(): boolean {
    return this.mentorRoleDerivedSubject.value;
  }

  can(action: PermissionAction): boolean {
    const role = this.getCurrentRole();
    if (!role) {
      return false;
    }

    if (action === 'VIEW_ADMIN' || action === 'MANAGE_SKILL_CATALOG') {
      return role === 'ROLE_ADMIN';
    }

    if (action === 'RESPOND_SESSION') {
      return role === 'ROLE_MENTOR';
    }

    if (action === 'BOOK_SESSION' || action === 'CREATE_GROUP' || action === 'JOIN_GROUP' || action === 'SUBMIT_REVIEW') {
      return role === 'ROLE_LEARNER';
    }

    return false;
  }
}
