import { NavItem } from '../../../core/models/nav-item.model';

export const STUDENT_NAV: NavItem[] = [
  { label: 'Dashboard',  route: '/student/dashboard', icon: '🏠' },
  { label: 'AI Insights', route: '/student/ai-insights', icon: '🧠' },
  { label: 'Recommendations', route: '/student/recommendations', icon: '✨' },
  { label: 'Learning Path', route: '/student/learning-path', icon: '🗺️' },
  { label: 'Courses', route: '/student/courses', icon: '📚' },
  { label: 'Forum', route: '/student/forum', icon: '💬' },
  { label: 'Collaboration Hub', route: '/student/collaboration', icon: '🤝' },
  { label: 'Badges', route: '/student/badges', icon: '🏅' },
  { label: 'Events', route: '/events', icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty', route: '/loyalty', icon: '🏆' },
  { label: 'Find Tutors', route: '/student/tutors' },
  { label: 'My Sessions', route: '/student/sessions' },
  { label: 'Booking Dashboard', route: '/student/booking-dashboard', icon: '📊' },
  { label: 'Session Bookings', route: '/student/bookings' },
  { label: 'Timeslots Calendar', route: '/student/timeslots' },
  { label: 'History', route: '/student/booking-history' },
  { label: 'My Profile', route: '/student/profile', icon: '👤' },
  { label: 'Settings', route: '/student/settings', icon: '⚙️' },
  { label: 'Quizzes', route: '/student/quiz' }
];
