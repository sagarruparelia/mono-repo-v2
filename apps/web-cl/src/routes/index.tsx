import { useAuth } from '@mono-repo-v2/shared-auth';
import { Navigate } from '@tanstack/react-router';

export function IndexPage() {
  const { isAuthenticated, isLoading, login } = useAuth();

  if (isLoading) {
    return (
      <div className="loading-container">
        <p>Loading...</p>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" />;
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Welcome to Web CL</h1>
        <p>Please log in to continue</p>
        <button onClick={login} className="login-button">
          Login with HSID
        </button>
      </div>
    </div>
  );
}
