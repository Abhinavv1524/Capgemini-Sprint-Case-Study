import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../../core/services/auth';
import { Router, RouterLink } from '@angular/router';

interface ApiResponse<T> {
  success?: boolean;
  message?: string;
  data?: T;
}

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  email : string = '';
  password : string ='';
  errorMessage : string = '';
  submitting : boolean = false;

  constructor(private authService : Auth, private router : Router){}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate([this.authService.getDefaultRouteForRole()]);
    }
  }

  onSubmit() {
    this.errorMessage = '';
    this.submitting = true;

    const data = {
      email : this.email,
      password : this.password
    };

    this.authService.login(data).subscribe({
      next : (res: ApiResponse<string> | string) =>{
        const token = typeof res === 'string' ? res : res?.data;
        if (!token || typeof token !== 'string') {
          this.errorMessage = 'Login response did not contain a valid token.';
          this.submitting = false;
          return;
        }

        this.authService.setToken(token);
        this.router.navigate([this.authService.getDefaultRouteForRole()]);
        this.submitting = false;
      },
      error : (err) => {
        console.error(err);
        this.errorMessage = 'Login failed. Please check your credentials and try again.';
        this.submitting = false;
      }
    });
  }
}
