import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, RegisterRequest } from '../../../core/services/auth';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  name: string = '';
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  submitting: boolean = false;

  constructor(private authService: Auth, private router: Router) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate([this.authService.getDefaultRouteForRole()]);
    }
  }

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';
    this.submitting = true;

    const data: RegisterRequest = {
      name: this.name,
      email: this.email,
      password: this.password,
      role: 'ROLE_LEARNER',
    };

    this.authService.register(data).subscribe({
      next: () => {
        this.successMessage = 'Registration successful. You can now log in.';
        this.submitting = false;
        setTimeout(() => this.router.navigate(['/login']), 900);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Registration failed. Please check your details and try again.';
        this.submitting = false;
      },
    });
  }
}
