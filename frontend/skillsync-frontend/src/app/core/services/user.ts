import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserRequest {
  name: string;
  email: string;
  bio: string;
  skills: string;
}

export interface UserResponse {
  id: number;
  authUserId: number;
  name: string;
  email: string;
  bio: string;
  skills: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class User {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<any> {
    return this.http.get(`${this.baseUrl}/users`);
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/users/${id}`);
  }

  getUserByAuthUserId(authUserId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/users/auth/${authUserId}`);
  }

  updateUser(id: number, data: UserRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/${id}`, data);
  }
}
