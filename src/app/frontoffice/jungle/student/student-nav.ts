import { NavItem } from '../../../core/models/nav-item.model';

export const STUDENT_NAV: NavItem[] = [
  { label: 'Dashboard',  route: '/student/dashboard', icon: '🏠' },
  { label: 'Events',     route: '/events',            icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty',   route: '/loyalty',          icon: '🏆' },
  { label: 'My Profile', route: '/student/profile',   icon: '👤' },
  { label: 'Settings',   route: '/student/settings',  icon: '⚙️' },
  //{ label: 'My Courses', route: '/student/courses',   icon: '📚' },
  //{ label: 'Progress',   route: '/student/progress',  icon: '📈' },
  //{ label: 'Schedule',   route: '/student/schedule',  icon: '🗓️' },
  //{ label: 'My Tutor',   route: '/student/tutor',     icon: '💬' },
  { label: 'Find Tutors', route: '/student/tutors' },
  { label: 'My Booking Sessions ', route: '/student/sessions' },
  { label: 'My Bookings', route: '/student/bookings' },
  { label: 'History', route: '/student/booking-history' },
  { label: 'Quizzes', route: '/student/quiz' },
  { label: 'Profile', route: '/student/profile' },
];
