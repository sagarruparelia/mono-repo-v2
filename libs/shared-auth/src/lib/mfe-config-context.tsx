import { createContext, useContext, useMemo, ReactNode } from 'react';

export type Persona = 'self' | 'parent' | 'agent' | 'state-worker';

export interface MfeConfig {
  enterpriseId: string;
  persona: Persona;
  serviceBaseUrl: string;
  isEmbedded: boolean;
}

interface MfeConfigProviderProps {
  children: ReactNode;
  enterpriseId: string;
  persona: Persona;
  serviceBaseUrl: string;
  isEmbedded: boolean;
}

const MfeConfigContext = createContext<MfeConfig | null>(null);

export function MfeConfigProvider({
  children,
  enterpriseId,
  persona,
  serviceBaseUrl,
  isEmbedded,
}: MfeConfigProviderProps) {
  const value = useMemo(
    () => ({
      enterpriseId,
      persona,
      serviceBaseUrl,
      isEmbedded,
    }),
    [enterpriseId, persona, serviceBaseUrl, isEmbedded]
  );

  return (
    <MfeConfigContext.Provider value={value}>
      {children}
    </MfeConfigContext.Provider>
  );
}

export function useMfeConfig(): MfeConfig {
  const context = useContext(MfeConfigContext);
  if (!context) {
    throw new Error('useMfeConfig must be used within MfeConfigProvider');
  }
  return context;
}

export function useMfeConfigOptional(): MfeConfig | null {
  return useContext(MfeConfigContext);
}
