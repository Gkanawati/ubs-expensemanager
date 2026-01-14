import { cn } from '@/lib/utils';
import * as Icons from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';

interface MenuItemProps {
  label: string;
  path: string;
  icon: string;
  onClick?: () => void;
}

export const MenuItem = ({ label, path, icon, onClick }: MenuItemProps) => {
  const location = useLocation();
  const isActive =
    location.pathname === path || location.pathname.startsWith(`${path}/`);

  const IconComponent = Icons[
    icon as keyof typeof Icons
  ] as React.ComponentType<{ className?: string }>;

  return (
    <Link
      to={path}
      onClick={onClick}
      className={cn(
        'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all',
        'hover:bg-muted',
        isActive
          ? 'bg-muted text-foreground'
          : 'text-muted-foreground'
      )}
    >
      {IconComponent && <IconComponent className='h-5 w-5' />}
      <span>{label}</span>
    </Link>
  );
};
