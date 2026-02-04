import { ReactNode } from 'react';
import { useAuth } from './auth-context';
import { MfeConfigProvider, Persona } from './mfe-config-context';

const SERVICE_BASE_URL = import.meta.env.VITE_BFF_URL || 'http://localhost:8080';

interface AuthMfeConfigProviderProps {
  children: ReactNode;
}

export function AuthMfeConfigProvider({ children }: AuthMfeConfigProviderProps) {
  const { user } = useAuth();

  if (!user) {
    return <>{children}</>;
  }

  return (
    <MfeConfigProvider
      enterpriseId={user.enterpriseId}
      persona={user.persona as Persona}
      serviceBaseUrl={SERVICE_BASE_URL}
      isEmbedded={false}
    >
      {children}
    </MfeConfigProvider>
  );
}
