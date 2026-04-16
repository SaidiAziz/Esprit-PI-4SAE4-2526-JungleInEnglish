import { NavItem } from '../../../core/models/nav-item.model';

export const STUDENT_NAV: NavItem[] = [
  { label: 'Dashboard',  route: '/student/dashboard', icon: '🏠' },
  { label: 'Events',     route: '/events',            icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty',   route: '/loyalty',          icon: '🏆' },
  { label: 'My Profile', route: '/student/profile',   icon: '👤' },
  { label: 'Settings',   route: '/student/settings',  icon: '⚙️' },
  { label: 'Find Tutors', route: '/student/tutors' },
  { label: 'My Sessions', route: '/student/sessions' },
  { label: 'Session Bookings', route: '/student/bookings' },
  { label: 'Timeslots Calendar', route: '/student/timeslots' },
  { label: 'History', route: '/student/booking-history' },
  { label: 'Quizzes', route: '/student/quiz' }
];
