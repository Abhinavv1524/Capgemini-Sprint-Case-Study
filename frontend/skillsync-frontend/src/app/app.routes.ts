import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Dashboard } from './features/dashboard/dashboard/dashboard';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { guestGuard } from './core/guards/guest-guard';
import { Shell } from './layout/shell/shell';
import { MentorList } from './features/mentors/mentor-list/mentor-list';
import { MentorDetail } from './features/mentors/mentor-detail/mentor-detail';
import { SessionPage } from './features/sessions/session-page/session-page';
import { GroupPage } from './features/groups/group-page/group-page';
import { AdminPage } from './features/admin/admin-page/admin-page';
import { ReviewPage } from './features/reviews/review-page/review-page';
import { ProfilePage } from './features/profile/profile-page/profile-page';
import { NotificationPage } from './features/notifications/notification-page/notification-page';

export const routes: Routes = [
    {path : '', redirectTo : 'dashboard', pathMatch : 'full'},
    {path : 'login', component : Login, canActivate : [guestGuard]},
    {path : 'register', component : Register, canActivate : [guestGuard]},
    {
        path : '',
        component : Shell,
        canActivate : [authGuard],
        children : [
            {path : 'dashboard', component : Dashboard, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'mentors', component : MentorList, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'mentors/:id', component : MentorDetail, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'sessions', component : SessionPage, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'groups', component : GroupPage, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'reviews', component : ReviewPage, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR'] }},
            {path : 'notifications', component : NotificationPage, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR', 'ROLE_ADMIN'] }},
            {path : 'profile', component : ProfilePage, canActivate : [roleGuard], data : { roles : ['ROLE_LEARNER', 'ROLE_MENTOR', 'ROLE_ADMIN'] }},
            {path : 'admin', component : AdminPage, canActivate : [roleGuard], data : { roles : ['ROLE_ADMIN'] }},
        ]
    },

    // {path : 'mentors', component : MentorList},
    // {path : 'mentors/:userId', component : MentorDetail},
    // {path : 'mentors/:id', component : MentorDetail},
    // {path : 'dashboard', component :Dashboard, canActivate : [authGuard]},

    // FALLBACK ROUTE
    {path : '**', redirectTo : 'dashboard'}
];
