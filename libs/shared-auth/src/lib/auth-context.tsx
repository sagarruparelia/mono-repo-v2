import { createContext, useContext, ReactNode } from 'react';
import { useSession, useLogin, useLogout } from './use-session';
import type { UserInfo } from './api/auth-api';

interface AuthContextValue {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: UserInfo | null;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const { data: session, isLoading } = useSession();
  const { login } = useLogin();
  const { mutate: logoutMutate } = useLogout();

  const value: AuthContextValue = {
    isAuthenticated: session?.authenticated ?? false,
    isLoading,
    user: session?.user ?? null,
    login,
    logout: logoutMutate,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
