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
  level?: string;
  learningGoals?: string;
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
  profilePicture?: string;
  twoFactorEnabled?: boolean;
  twoFactorMethod?: 'EMAIL' | 'TOTP';
}

export interface LoginResponse {
  token: string;
  user: UserResponse;
  requires2FA?: boolean;
  email?: string;
  twoFactorMethod?: 'EMAIL' | 'TOTP';
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
  level?: string;
  learningGoals?: string;
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
}

export interface Tutor extends UserResponse {

  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
  rating?: number;
  reviewCount?: number;
  profileImage?: string;
  languages?: string[];
}

export interface TutorProfile {
  id: number;
  bio?: string;
  specialization?: string;
  experienceYears?: number;
  hourlyRate?: number;
  user: UserResponse;
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
  accountStatus: string;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
