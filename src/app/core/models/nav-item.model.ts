export interface NavItem {
  label: string;
  route?: string;
  icon?: string;
  children?: NavItem[];
}
