import { NavItem } from '../../../core/models/nav-item.model';
import { NAV_ICONS } from '../../../core/ui/nav-icons';

export const TUTOR_NAV: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', route: '/tutor/dashboard', svgIcon: NAV_ICONS['dashboard'] },
  { key: 'room', label: 'Rooms', route: '/tutor/rooms', svgIcon: NAV_ICONS['room'] },
  { key: 'challenge', label: 'Challenges', route: '/tutor/challenges', svgIcon: NAV_ICONS['challenge'] },
  { key: 'analytics', label: 'Analytics', route: '/tutor/analytics', svgIcon: NAV_ICONS['analytics'] }
];
