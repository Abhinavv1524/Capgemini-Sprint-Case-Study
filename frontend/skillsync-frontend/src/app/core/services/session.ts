import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SessionRequest {
  mentorId: number;
  sessionTime: string;
}

export interface SessionResponse {
  id: number;
  mentorId: number;
  learnerId: number;
  sessionTime: string;
  status: 'REQUESTED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class Session {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createSession(data: SessionRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/sessions`, data);
  }

  getMySessions(): Observable<any> {
    return this.http.get(`${this.baseUrl}/sessions/user`);
  }

  cancelSession(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/sessions/${id}/cancel`, {});
  }

  acceptSession(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/sessions/${id}/accept`, {});
  }

  rejectSession(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/sessions/${id}/reject`, {});
  }
}
