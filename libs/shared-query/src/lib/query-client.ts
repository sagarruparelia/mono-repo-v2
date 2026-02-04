import { QueryClient, QueryCache, MutationCache } from '@tanstack/react-query';

// Custom error handler - can be extended to report to error tracking service
function handleQueryError(error: unknown): void {
  console.error('[Query Error]', error);
}

function handleMutationError(error: unknown): void {
  console.error('[Mutation Error]', error);
}

export function createQueryClient(): QueryClient {
  return new QueryClient({
    queryCache: new QueryCache({
      onError: handleQueryError,
    }),
    mutationCache: new MutationCache({
      onError: handleMutationError,
    }),
    defaultOptions: {
      queries: {
        staleTime: 1000 * 60 * 5, // 5 minutes - data is fresh for 5 min
        gcTime: 1000 * 60 * 30, // 30 minutes - cache retained for 30 min
        retry: (failureCount, error) => {
          // Don't retry on 4xx errors (client errors)
          if (error instanceof Error && 'status' in error) {
            const status = (error as { status: number }).status;
            if (status >= 400 && status < 500) return false;
          }
          return failureCount < 2;
        },
        refetchOnWindowFocus: true,
        refetchOnReconnect: true,
        networkMode: 'online', // Only fetch when online
      },
      mutations: {
        retry: 0,
        networkMode: 'online',
      },
    },
  });
}
