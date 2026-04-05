import React, { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Building2, Users, BarChart3, Settings,
  Car, ShoppingCart, MessageSquare, UserCircle, LogOut,
  ChevronRight, Bell
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

interface SidebarItem {
  label: string;
  icon: React.ElementType;
  to: string;
}

const adminNav: SidebarItem[] = [
  { label: 'Dashboard',  icon: LayoutDashboard, to: '/admin/dashboard' },
  { label: 'Dealers',    icon: Building2,        to: '/admin/dealers'   },
  { label: 'Users',      icon: Users,            to: '/admin/users'     },
  { label: 'Reports',    icon: BarChart3,        to: '/admin/reports'   },
  { label: 'Settings',   icon: Settings,         to: '/admin/settings'  },
];

const dealerNav: SidebarItem[] = [
  { label: 'Dashboard',  icon: LayoutDashboard, to: '/dealer/dashboard'  },
  { label: 'Customers',  icon: Users,            to: '/dealer/customers'  },
  { label: 'Vehicles',   icon: Car,              to: '/dealer/vehicles'   },
  { label: 'Orders',     icon: ShoppingCart,     to: '/dealer/orders'     },
  { label: 'Inquiries',  icon: MessageSquare,    to: '/dealer/inquiries'  },
  { label: 'Profile',    icon: UserCircle,       to: '/dealer/profile'    },
];

interface AppLayoutProps {
  children: ReactNode;
  title: string;
}

export default function AppLayout({ children, title }: AppLayoutProps) {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const navItems = isAdmin ? adminNav : dealerNav;

  const handleLogout = () => {
    logout();
    toast.success('Logged out successfully');
    navigate('/login');
  };

  const initials = user
    ? (user.firstName[0] + user.lastName[0]).toUpperCase()
    : 'U';

  return (
    <div className="app-layout">
      {/* ---- Sidebar ---- */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">S</div>
          <div>
            <div className="sidebar-brand-text">Serene</div>
            <div className="sidebar-brand-sub">DMS Platform</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="sidebar-section-title">
            {isAdmin ? 'Administration' : 'Dealer Portal'}
          </div>

          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `sidebar-item${isActive ? ' active' : ''}`
                }
              >
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-item" style={{ marginBottom: 8, pointerEvents: 'none', opacity: 0.7 }}>
            <div className="avatar" style={{ width: 28, height: 28, fontSize: '0.7rem' }}>{initials}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 600 }} className="truncate">
                {user?.firstName} {user?.lastName}
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }} className="truncate">
                {user?.roles[0]}
              </div>
            </div>
          </div>
          <button className="sidebar-item btn-ghost w-full" onClick={handleLogout}
            style={{ cursor: 'pointer', background: 'none', border: 'none', color: 'var(--danger)', width: '100%' }}>
            <LogOut size={17} />
            Sign Out
          </button>
        </div>
      </aside>

      {/* ---- Topbar ---- */}
      <header className="topbar">
        <div className="topbar-left">
          <h1 className="topbar-title">{title}</h1>
        </div>
        <div className="topbar-right">
          <button className="btn btn-ghost btn-icon">
            <Bell size={18} />
          </button>
          <div className="avatar">{initials}</div>
        </div>
      </header>

      {/* ---- Page content ---- */}
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
