import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserServiceService {

  private apiUrl = 'http://localhost:8081/users'

  constructor(private http: HttpClient) { }
    getAllUsers(): Observable<any> {
      return this.http.get<any>(`${this.apiUrl}/getAllUsers`);
    }
}
