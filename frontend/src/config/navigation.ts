export type UserRole = 'ROLE_MANAGER' | 'ROLE_EMPLOYEE' | 'ROLE_FINANCE';

export interface MenuItem {
  id: string;
  label: string;
  path: string;
  icon: string;
  allowedRoles: UserRole[];
  children?: MenuItem[];
}

export const navigationConfig: MenuItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    path: '/dashboard',
    icon: 'LayoutDashboard',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'],
  },
  {
    id: 'expenses',
    label: 'Expenses',
    path: '/expenses',
    icon: 'Receipt',
    allowedRoles: ['ROLE_EMPLOYEE'],
  },
  {
    id: 'manage-expenses',
    label: 'Manage Expenses',
    path: '/manage-expenses',
    icon: 'FileCheck',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_FINANCE'],
  },
    {
    id: 'expenses-report',
    label: 'Expenses Report',
    path: '/expenses-report',
    icon: 'ChartLine',
    allowedRoles: ['ROLE_FINANCE'],
  },
  {
    id: 'department',
    label: 'Department',
    path: '/department',
    icon: 'Building2',
    allowedRoles: ['ROLE_FINANCE'],
  },
  {
    id: 'users',
    label: 'User Management',
    path: '/users',
    icon: 'Users',
   allowedRoles: ['ROLE_FINANCE'],
  },
   {
    id: 'category',
    label: 'Category Management',
    path: '/category',
    icon: 'ClipboardPenLine',
   allowedRoles: ['ROLE_FINANCE'],
  },
  {
    id: 'alert',
    label: 'Alert Management',
    path: '/alert',
    icon: 'TriangleAlert',
   allowedRoles: ['ROLE_FINANCE'],
  },
];

export const getMenuItemsForRole = (userRole: UserRole): MenuItem[] => {
  return navigationConfig.filter((item) =>
    item.allowedRoles.includes(userRole)
  );
};

export const hasAccessToMenuItem = (
  menuItemId: string,
  userRole: UserRole
): boolean => {
  const menuItem = navigationConfig.find((item) => item.id === menuItemId);
  return menuItem ? menuItem.allowedRoles.includes(userRole) : false;
};
