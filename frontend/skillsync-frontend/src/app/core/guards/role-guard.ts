import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth, AppRole } from '../services/auth';
import { Permission } from '../services/permission';
import { map } from 'rxjs/operators';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(Auth);
  const router = inject(Router);
  const permissionService = inject(Permission);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const allowedRoles = (route.data?.['roles'] as AppRole[] | undefined) ?? [];
  return permissionService.refreshAccessContext().pipe(
    map((role) => {
      if (!role) {
        authService.logout();
        router.navigate(['/login']);
        return false;
      }

      if (allowedRoles.length === 0 || allowedRoles.includes(role)) {
        return true;
      }

      router.navigate([authService.getDefaultRouteForRole(role)]);
      return false;
    }),
  );
};
