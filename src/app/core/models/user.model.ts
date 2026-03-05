export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'STUDENT' | 'TUTOR' | 'ADMIN';
  accountStatus: string;
  createdAt: string;
  updatedAt: string;
  profilePicture?: string;
  // Student fields
  level?: string;
  learningGoals?: string;
  // Tutor fields
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
}

export interface LoginResponse {
  token: string;
  user: UserResponse;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: 'STUDENT' | 'TUTOR';
  accountStatus?: string;
  // Student fields
  level?: string;
  learningGoals?: string;
  // Tutor fields
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  profilePicture?: string;
  level?: string;
  learningGoals?: string;
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface UserSummary {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'STUDENT' | 'TUTOR' | 'ADMIN';
  status: string;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
