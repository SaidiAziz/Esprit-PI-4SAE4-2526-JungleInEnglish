import { NavItem } from '../../../core/models/nav-item.model';

export const TUTOR_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/tutor/dashboard', icon: '🏠' },
  { label: 'Rooms', route: '/tutor/rooms', icon: '🧩' },
  { label: 'Challenges', route: '/tutor/challenges', icon: '📝' },
  { label: 'Analytics', route: '/tutor/analytics', icon: '📈' },
  { label: 'Events', route: '/events', icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty', route: '/loyalty', icon: '🏆' },
  { label: 'My Sessions', route: '/tutor/sessions' },
  { label: 'Session Bookings', route: '/tutor/bookings' },
  { label: 'Timeslots Calendar', route: '/tutor/timeslots' },
  { label: 'Availability', route: '/tutor/availability' },
  { label: 'My Students', route: '/tutor/students' },
  { label: 'My Profile', route: '/tutor/profile', icon: '👤' },
  { label: 'Settings', route: '/tutor/settings', icon: '⚙️' },
  { label: 'Quizzes', route: '/tutor/quiz' }
];
