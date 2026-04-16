import { NavItem } from '../../../core/models/nav-item.model';

export const TUTOR_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/tutor/dashboard', icon: '🏠' },
  { label: 'Events', route: '/events', icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty', route: '/loyalty', icon: '🏆' },
  { label: 'My Profile', route: '/tutor/profile', icon: '👤' },
  { label: 'Settings', route: '/tutor/settings', icon: '⚙️' },
  { label: 'My Sessions', route: '/tutor/sessions' },
  { label: 'Session Bookings', route: '/tutor/bookings' },
  { label: 'Timeslots Calendar', route: '/tutor/timeslots' },
  { label: 'Availability', route: '/tutor/availability' },
  { label: 'My Students', route: '/tutor/students' },
  { label: 'Quizzes', route: '/tutor/quiz' }
];
