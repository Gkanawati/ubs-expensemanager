import { describe, it, expect } from 'vitest';
import { getMenuItemsForRole, hasAccessToMenuItem } from '../navigation';
import type { UserRole } from '../navigation';

describe('Navigation Config', () => {
  describe('getMenuItemsForRole', () => {
    it('should return all menu items available to all roles', () => {
      const items = getMenuItemsForRole('ROLE_EMPLOYEE');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'expenses' }),
        ])
      );
    });

    it('should return limited menu items for ROLE_EMPLOYEE', () => {
      const items = getMenuItemsForRole('ROLE_EMPLOYEE');

      // Employee has access to these
      expect(items.find(item => item.id === 'dashboard')).toBeDefined();
      expect(items.find(item => item.id === 'expenses')).toBeDefined();

      // Should NOT include manager/finance items
      expect(items.find(item => item.id === 'manage-expenses')).toBeUndefined();
      expect(items.find(item => item.id === 'users')).toBeUndefined();
    });

    it('should include manage-expenses for ROLE_MANAGER', () => {
      const items = getMenuItemsForRole('ROLE_MANAGER');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'manage-expenses' }),
        ])
      );

      // Manager should NOT have access to 'expenses' (employee-only) or finance-only items
      expect(items.find(item => item.id === 'expenses')).toBeUndefined();
      expect(items.find(item => item.id === 'users')).toBeUndefined();
    });

    it('should include all management items for ROLE_FINANCE', () => {
      const items = getMenuItemsForRole('ROLE_FINANCE');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'manage-expenses' }),
          expect.objectContaining({ id: 'expenses-report' }),
          expect.objectContaining({ id: 'department' }),
          expect.objectContaining({ id: 'users' }),
          expect.objectContaining({ id: 'category' }),
          expect.objectContaining({ id: 'alert' }),
        ])
      );

      // Finance should NOT have access to 'expenses' (employee-only)
      expect(items.find(item => item.id === 'expenses')).toBeUndefined();
    });
  });

  describe('hasAccessToMenuItem', () => {
    it('should return true when user has required role', () => {
      const result = hasAccessToMenuItem('users', 'ROLE_FINANCE');

      expect(result).toBe(true);
    });

    it('should return false when user does not have required role', () => {
      const result = hasAccessToMenuItem('users', 'ROLE_EMPLOYEE');

      expect(result).toBe(false);
    });

    it('should return true when user has one of multiple allowed roles', () => {
      const result = hasAccessToMenuItem('manage-expenses', 'ROLE_MANAGER');

      expect(result).toBe(true);
    });

    it('should return false for non-existent menu item', () => {
      const result = hasAccessToMenuItem('non-existent', 'ROLE_EMPLOYEE');

      expect(result).toBe(false);
    });
  });

  describe('Menu item structure', () => {
    it('should have all required fields for each menu item', () => {
      const roles: UserRole[] = ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'];

      roles.forEach(role => {
        const items = getMenuItemsForRole(role);

        items.forEach(item => {
          expect(item).toHaveProperty('id');
          expect(item).toHaveProperty('label');
          expect(item).toHaveProperty('path');
          expect(item).toHaveProperty('icon');
          expect(typeof item.id).toBe('string');
          expect(typeof item.label).toBe('string');
          expect(typeof item.path).toBe('string');
          expect(typeof item.icon).toBe('string');
        });
      });
    });
  });
});
