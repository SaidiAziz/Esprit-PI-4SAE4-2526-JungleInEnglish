import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import {PagedResponse, TutorProfile, UpdateProfileRequest, UserResponse, UserSummary} from '@core/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserServiceService {

  private apiUrl = 'http://localhost:8081/users'
  private tutorProfileApiUrl = 'http://localhost:8081/tutorProfile'

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/getAllUsers`);
  }

  getAllTutorProfiles(): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorProfileApiUrl}/getAllTutorProfiles`);
  }

  searchUsers(
    search: string = '',
    role: string = 'ALL',
    sortBy: string = 'createdAt',
    sortDir: string = 'desc',
    page: number = 0,
    size: number = 10
  ): Observable<PagedResponse<UserSummary>> {
    let params = new HttpParams()
      .set('search', search)
      .set('role', role)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir)
      .set('page', page)
      .set('size', size);

    return this.http.get<PagedResponse<UserSummary>>(`${this.apiUrl}/search`, { params });
  }

  getUserById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/getUserById/${id}`);
  }

  updateUser(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/updateUser/${id}`, data);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/deleteUser/${id}`);
  }

  /** Get own profile (uses JWT from interceptor) */
  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  /** Update own profile */
  updateProfile(data: UpdateProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/me`, data);
  }

  /** Upload profile picture for a given user id */
  updateProfilePicture(userId: number, base64Image: string): Observable<UserResponse> {
    return this.http.put<UserResponse>(
      `${this.apiUrl}/${userId}/profile-picture`,
      { profilePicture: base64Image }
    );
  }
}
