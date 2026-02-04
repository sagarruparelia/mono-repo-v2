import { useAuth } from '@mono-repo-v2/shared-auth';

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="dashboard-page">
      <h1>Dashboard</h1>
      <p>Welcome, {user?.name}!</p>
      <div className="dashboard-content">
        <p>This is the main dashboard page.</p>
      </div>
    </div>
  );
}
