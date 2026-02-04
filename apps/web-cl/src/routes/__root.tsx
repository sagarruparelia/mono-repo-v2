import { Outlet } from '@tanstack/react-router';
import { QueryProvider } from '@mono-repo-v2/shared-query';
import { AuthProvider } from '@mono-repo-v2/shared-auth';

export function RootLayout() {
  return (
    <QueryProvider>
      <AuthProvider>
        <div className="app-container">
          <Outlet />
        </div>
      </AuthProvider>
    </QueryProvider>
  );
}
