import { Outlet, Link } from '@tanstack/react-router';
import { useAuth, AuthMfeConfigProvider } from '@mono-repo-v2/shared-auth';

export function AuthenticatedLayout() {
  const { user, logout } = useAuth();

  return (
    <AuthMfeConfigProvider>
      <div className="authenticated-layout">
        <header className="app-header">
          <nav>
            <ul className="nav-links">
              <li>
                <Link to="/dashboard">Dashboard</Link>
              </li>
              <li>
                <Link to="/profile">Profile</Link>
              </li>
              <li>
                <Link to="/summary">Summary</Link>
              </li>
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
          <Outlet />
        </main>
      </div>
    </AuthMfeConfigProvider>
  );
}
