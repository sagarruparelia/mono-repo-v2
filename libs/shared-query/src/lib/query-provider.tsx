import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode, useState } from 'react';
import { createQueryClient } from './query-client';

interface QueryProviderProps {
  readonly children: ReactNode;
  readonly client?: QueryClient;
}

export function QueryProvider({ children, client }: Readonly<QueryProviderProps>) {
  const [queryClient] = useState(() => client ?? createQueryClient());

  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
}
