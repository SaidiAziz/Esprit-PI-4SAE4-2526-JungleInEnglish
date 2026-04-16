export interface ForumComment {
  id: number;
  content: string;
  authorName: string;
  createdAt: string;
}

export interface ForumPostListItem {
  id: number;
  title: string;
  authorName: string;
  category?: string | null;
  courseId?: number | null;
  tags?: string[];
  pinned: boolean;
  createdAt: string;
  updatedAt: string;
  commentsCount: number;
  likesCount: number;
  dislikesCount: number;
}

export interface ForumPostDetails {
  id: number;
  title: string;
  content: string;
  authorName: string;
  category?: string | null;
  courseId?: number | null;
  tags?: string[];
  pinned: boolean;
  acceptedCommentId?: number | null;
  createdAt: string;
  updatedAt: string;
  comments: ForumComment[];
  likesCount: number;
  dislikesCount: number;
}

export interface CreateForumPostRequest {
  title: string;
  content: string;
  authorName: string;
  category?: string;
  courseId?: number | null;
  tags?: string[];
}

export interface UpdateForumPostRequest {
  title: string;
  content: string;
  authorName: string;
  category?: string;
  courseId?: number | null;
  tags?: string[];
}

export interface CreateForumCommentRequest {
  content: string;
  authorName: string;
}

export type ForumReactionType = 'LIKE' | 'DISLIKE';

export interface ReactToPostRequest {
  voterName: string;
  type: ForumReactionType;
}

export interface ReportRequest {
  reporterName: string;
  reason: string;
}

export interface ReportResponse {
  id: number;
  targetType: 'POST' | 'COMMENT';
  targetId: number;
  reporterName: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  reviewedAt: string | null;
  reviewedBy: string | null;
}

export interface CourseSuggestion {
  id: number;
  title: string;
  level: string;
  durationHours: number;
  price: number | null;
}

export interface RecommendationsResponse {
  similarPosts: ForumPostListItem[];
  suggestedCourses: CourseSuggestion[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
