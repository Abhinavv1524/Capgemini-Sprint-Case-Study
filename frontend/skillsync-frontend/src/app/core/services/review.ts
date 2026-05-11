import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AddReviewRequest {
  mentorId: number;
  sessionId: number;
  rating: number;
  comment: string;
}

export interface ReviewResponse {
  id: number;
  mentorId: number;
  userId: number;
  sessionId: number;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface RatingResponse {
  averageRating: number;
  totalReviews: number;
}

@Injectable({
  providedIn: 'root',
})
export class Review {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  addReview(data: AddReviewRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/reviews`, data);
  }

  getReviewsByMentor(mentorId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/reviews/mentor/${mentorId}`);
  }

  getRatingByMentor(mentorId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/reviews/mentor/${mentorId}/rating`);
  }
}
