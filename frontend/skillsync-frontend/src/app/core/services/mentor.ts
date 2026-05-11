import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MentorRequest {
  bio: string;
  experience: number;
  hourlyRate: number;
}

@Injectable({
  providedIn: 'root',
})
export class Mentor {
  private baseUrl : string = environment.apiUrl;

  constructor(private http: HttpClient){}

  getAllMentors() : Observable<any>{
    return this.http.get(`${this.baseUrl}/mentors`);
  }

  getMentorByUserId(userId : number) : Observable<any>{
    return this.http.get(`${this.baseUrl}/mentors/user/${userId}`);
  }

  getByMentorId(id : number) : Observable<any>{
    return this.http.get(`${this.baseUrl}/mentors/${id}`);
  }

  applyAsMentor(data: MentorRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/mentors/apply`, data);
  }

  approveMentor(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/mentors/${id}/approve`, {});
  }

  rejectMentor(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/mentors/${id}/reject`, {});
  }

  updateMentor(id: number, data: MentorRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/mentors/${id}`, data);
  }
}
