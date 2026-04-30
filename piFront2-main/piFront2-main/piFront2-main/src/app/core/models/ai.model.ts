export interface PerformanceAnalysis {
  id: number;
  userId: number;
  courseId: number;
  grammarScore: number;
  listeningScore: number;
  speakingScore: number;
  averageScore: number;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  createdAt: string;
  lastUpdated: string;
}

export interface Recommendation {
  id: number;
  userId: number;
  type: 'COURSE' | 'LESSON' | 'EXERCISE';
  contentId: number;
  contentTitle?: string | null;
  contentDescription?: string | null;
  contentLevel?: string | null;
  contentPrice?: number | null;
  reason: string;
  focusSkill: 'GRAMMAR' | 'LISTENING' | 'SPEAKING';
  createdAt: string;
}

export interface LearningPath {
  id: number;
  userId: number;
  courseId: number;
  lessonOrder: string;
  progress: number;
  focusSkill: 'GRAMMAR' | 'LISTENING' | 'SPEAKING';
  targetLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  createdAt: string;
  updatedAt: string;
}

export interface AiSummary {
  userId: number;
  currentLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  overallAverage: number;
  weakestSkill: 'GRAMMAR' | 'LISTENING' | 'SPEAKING';
  totalAnalyses: number;
  performanceAnalyses: PerformanceAnalysis[];
  recommendations: Recommendation[];
  learningPaths: LearningPath[];
}

export interface CreatePerformanceAnalysisRequest {
  userId: number;
  courseId: number;
  grammarScore: number;
  listeningScore: number;
  speakingScore: number;
}

export interface UpdatePerformanceAnalysisRequest {
  courseId: number;
  grammarScore: number;
  listeningScore: number;
  speakingScore: number;
}

export interface GenerateLearningPathRequest {
  userId: number;
  courseId: number;
}

export interface UpdateLearningPathProgressRequest {
  progress: number;
}

export interface AiAdminMetric {
  label: string;
  value: number;
}

export interface AiAdminDashboard {
  totalStudents: number;
  totalAnalyses: number;
  totalRecommendations: number;
  totalLearningPaths: number;
  levelDistribution: AiAdminMetric[];
  weakestSkillDistribution: AiAdminMetric[];
  recentAnalyses: PerformanceAnalysis[];
}

export interface AiAdminStudentOverview {
  userId: number;
  currentLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  overallAverage: number;
  weakestSkill: 'GRAMMAR' | 'LISTENING' | 'SPEAKING';
  analysisCount: number;
  recommendationCount: number;
  learningPathCount: number;
  lastAnalysisAt: string | null;
}
