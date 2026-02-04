import { Outlet, Link, useRouterState } from '@tanstack/react-router';
import { useAuth, AuthMfeConfigProvider } from '@mono-repo-v2/shared-auth';
import { Breadcrumb } from '../components/Breadcrumb';

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/profile', label: 'Profile' },
  { to: '/summary', label: 'Summary' },
  { to: '/care-team', label: 'Care Team' },
  { to: '/documents', label: 'Documents' },
  { to: '/recommendations', label: 'Recommendations' },
  { to: '/resources', label: 'Resources' },
] as const;

export function AuthenticatedLayout() {
  const { user, logout } = useAuth();
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  return (
    <AuthMfeConfigProvider>
      <div className="authenticated-layout">
        <header className="app-header">
          <nav>
            <ul className="nav-links">
              {navItems.map(({ to, label }) => (
                <li key={to}>
                  <Link
                    to={to}
                    className={currentPath === to ? 'active' : ''}
                    preload="intent"
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
          <div className="user-info">
            <span>{user?.name}</span>
            <button onClick={() => logout()} className="logout-button">
              Logout
            </button>
          </div>
        </header>
        <main className="app-main">
          <Breadcrumb />
          <Outlet />
        </main>
      </div>
    </AuthMfeConfigProvider>
  );
}
