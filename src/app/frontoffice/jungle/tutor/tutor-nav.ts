import { NavItem } from '../../../core/models/nav-item.model';

export const TUTOR_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/tutor/dashboard', icon: '🏠' },
  { label: 'Events', route: '/events', icon: '📅' },
  { label: 'My Event Stats', route: '/stats-events', icon: '📊' },
  { label: 'Loyalty', route: '/loyalty', icon: '🏆' },
  { label: 'My Profile', route: '/tutor/profile', icon: '👤' },
  { label: 'Settings', route: '/tutor/settings', icon: '⚙️' },
  { label: 'Sessions Booking', route: '/tutor/sessions' },
  { label: 'Availability', route: '/tutor/availability' },
  { label: 'My Students', route: '/tutor/students' },
  { label: 'Bookings', route: '/tutor/bookings' },
  { label: 'Quizzes', route: '/tutor/quiz' }
];
