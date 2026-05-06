import { NavItem } from '../../../core/models/nav-item.model';
import { NAV_ICONS } from '../../../core/ui/nav-icons';

export const STUDENT_NAV: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', route: '/student/dashboard', svgIcon: NAV_ICONS['dashboard'] },
  { key: 'courses', label: 'Courses', route: '/student/courses', svgIcon: NAV_ICONS['courses'] },
  { key: 'forum', label: 'Forum', route: '/student/forum', svgIcon: NAV_ICONS['forum'] },
  { key: 'insights', label: 'My Progress', route: '/student/ai-insights', svgIcon: NAV_ICONS['insights'] },
  { key: 'roadmap', label: 'Study Plan', route: '/student/learning-path', svgIcon: NAV_ICONS['roadmap'] },
  { key: 'recommendation', label: 'Recommendations', route: '/student/recommendations', svgIcon: NAV_ICONS['recommendation'] },
  { key: 'collaboration', label: 'Practice Rooms', route: '/student/collaboration', svgIcon: NAV_ICONS['collaboration'] },
  { key: 'badges', label: 'Badges', route: '/student/badges', svgIcon: NAV_ICONS['badges'] }
];
