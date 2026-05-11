import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface GroupResponse {
  id: number;
  name: string;
  description: string;
  createdBy: number;
  createdAt: string;
}

export interface CreateGroupRequest {
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class Group {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllGroups(): Observable<any> {
    return this.http.get(`${this.baseUrl}/groups`);
  }

  createGroup(data: CreateGroupRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/groups`, data);
  }

  joinGroup(groupId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/groups/${groupId}/join`, {});
  }

  leaveGroup(groupId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/groups/${groupId}/leave`, {});
  }
}
