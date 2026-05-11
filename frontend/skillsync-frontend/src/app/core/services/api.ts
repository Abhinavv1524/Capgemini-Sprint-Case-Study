import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SkillRequest {
  name: string;
}

export interface SkillResponse {
  id: number;
  name: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class Api {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createSkill(data: SkillRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/skills`, data);
  }

  getAllSkills(): Observable<any> {
    return this.http.get(`${this.baseUrl}/skills`);
  }

  getSkillById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/skills/${id}`);
  }

  testNotification(event: {
    eventType: 'SESSION_BOOKED' | 'SESSION_ACCEPTED';
    sessionId: number;
    mentorId: number;
    learnerId: number;
    sessionTime: string;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/notifications/test`, event);
  }
}
