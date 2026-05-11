import { Component, signal } from '@angular/core';
import { Mentor } from '../../../core/services/mentor';
import { ActivatedRoute, Router } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { User } from '../../../core/services/user';

@Component({
  selector: 'app-mentor-detail',
  imports: [],
  templateUrl: './mentor-detail.html',
  styleUrl: './mentor-detail.css',
})
export class MentorDetail {

  mentor = signal<any>(null);
  user = signal<any>(null);
  loading = signal(true);

  constructor(
    private route : ActivatedRoute,
    private mentorService : Mentor,
    private router: Router,
    private authService: Auth,
    private userService: User,
  ){}

  ngOnInit(){
    const id = this.route.snapshot.paramMap.get('id');
    if(id){
      this.mentorService.getByMentorId(+id).subscribe({
        next : (res : any) =>{
          const m = res?.data ?? res;
          this.mentor.set(m);
          const userId = Number(m?.userId);
          if (Number.isFinite(userId) && userId > 0) {
            this.userService.getUserById(userId).subscribe({
              next: (uRes: any) => this.user.set(uRes?.data ?? null),
              error: () => this.user.set(null),
            });
          } else {
            this.user.set(null);
          }
          this.loading.set(false);
        },
        error : (err) =>{
          console.error("Error fetching mentor detail ", err);
          alert('Failed to load mentor details. Please try again later.');
          this.loading.set(false);
        }
      });
    } else {
      // console.log(id);
      alert('Invalid mentor ID.');
      this.loading.set(false);
    }
  }

  bookSession() {
    const mentorId = this.mentor()?.id;
    if (!mentorId) {
      return;
    }
    this.router.navigate(['/sessions'], { queryParams: { mentorId } });
  }

  canBookSession() {
    return this.authService.getUserRole() === 'ROLE_LEARNER';
  }

}
