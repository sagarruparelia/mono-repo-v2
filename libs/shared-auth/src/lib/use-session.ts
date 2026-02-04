import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getSession,
  logout,
  initiateLogin,
  SessionResponse,
} from './api/auth-api';

export const SESSION_QUERY_KEY = ['session'] as const;

export function useSession() {
  return useQuery<SessionResponse>({
    queryKey: SESSION_QUERY_KEY,
    queryFn: getSession,
    staleTime: 1000 * 60, // 1 minute
    refetchOnWindowFocus: true,
    retry: false,
  });
}

export function useLogin() {
  return {
    login: initiateLogin,
  };
}

export function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.setQueryData(SESSION_QUERY_KEY, {
        authenticated: false,
        user: null,
      });
      queryClient.invalidateQueries({ queryKey: SESSION_QUERY_KEY });
    },
  });
}
