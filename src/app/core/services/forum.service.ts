import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateForumCommentRequest,
  CreateForumPostRequest,
  ForumComment,
  ForumPostDetails,
  ForumPostListItem,
  PageResponse,
  RecommendationsResponse,
  ReportRequest,
  ReportResponse,
  ReactToPostRequest,
  UpdateForumPostRequest
} from '../models/forum.model';

@Injectable({
  providedIn: 'root'
})
export class ForumService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/forum';

  listPosts(params?: { q?: string; page?: number; size?: number }): Observable<PageResponse<ForumPostListItem>> {
    return this.http.get<PageResponse<ForumPostListItem>>(`${this.baseUrl}/posts`, {
      params: {
        ...(params?.q ? { q: params.q } : {}),
        ...(params?.page != null ? { page: params.page } : {}),
        ...(params?.size != null ? { size: params.size } : {})
      } as never
    });
  }

  getPost(id: number): Observable<ForumPostDetails> {
    return this.http.get<ForumPostDetails>(`${this.baseUrl}/posts/${id}`);
  }

  createPost(payload: CreateForumPostRequest): Observable<ForumPostDetails> {
    return this.http.post<ForumPostDetails>(`${this.baseUrl}/posts`, payload);
  }

  updatePost(id: number, payload: UpdateForumPostRequest): Observable<ForumPostDetails> {
    return this.http.put<ForumPostDetails>(`${this.baseUrl}/posts/${id}`, payload);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/posts/${id}`);
  }

  setPinned(id: number, pinned: boolean): Observable<ForumPostDetails> {
    return this.http.patch<ForumPostDetails>(`${this.baseUrl}/posts/${id}/pin`, null, {
      params: { pinned }
    });
  }

  addComment(postId: number, payload: CreateForumCommentRequest): Observable<ForumComment> {
    return this.http.post<ForumComment>(`${this.baseUrl}/posts/${postId}/comments`, payload);
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/comments/${commentId}`);
  }

  reactToPost(postId: number, payload: ReactToPostRequest): Observable<ForumPostDetails> {
    return this.http.post<ForumPostDetails>(`${this.baseUrl}/posts/${postId}/reactions`, payload);
  }

  acceptAnswer(postId: number, commentId: number): Observable<ForumPostDetails> {
    return this.http.post<ForumPostDetails>(`${this.baseUrl}/posts/${postId}/accept`, { commentId });
  }

  reportPost(postId: number, payload: ReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.baseUrl}/posts/${postId}/report`, payload);
  }

  reportComment(commentId: number, payload: ReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.baseUrl}/comments/${commentId}/report`, payload);
  }

  listReports(params?: { status?: string; page?: number; size?: number }): Observable<PageResponse<ReportResponse>> {
    return this.http.get<PageResponse<ReportResponse>>(`${this.baseUrl}/admin/reports`, {
      params: {
        ...(params?.status ? { status: params.status } : {}),
        ...(params?.page != null ? { page: params.page } : {}),
        ...(params?.size != null ? { size: params.size } : {})
      } as never
    });
  }

  reviewReport(id: number, status: 'APPROVED' | 'REJECTED', reviewedBy = 'admin'): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.baseUrl}/admin/reports/${id}/review`, null, {
      params: { status, reviewedBy }
    });
  }

  getRecommendations(postId: number): Observable<RecommendationsResponse> {
    return this.http.get<RecommendationsResponse>(`${this.baseUrl}/posts/${postId}/recommendations`);
  }
}
