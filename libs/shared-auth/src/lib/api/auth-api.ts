const BFF_BASE_URL = import.meta.env.VITE_BFF_URL || 'http://localhost:8080';

export interface SessionResponse {
  authenticated: boolean;
  user: UserInfo | null;
}

export interface UserInfo {
  id: string;
  email: string;
  name: string;
}

export async function getSession(): Promise<SessionResponse> {
  const response = await fetch(`${BFF_BASE_URL}/api/auth/session`, {
    credentials: 'include',
  });

  if (!response.ok) {
    return { authenticated: false, user: null };
  }

  return response.json();
}

export function initiateLogin(): void {
  window.location.href = `${BFF_BASE_URL}/api/auth/login`;
}

export async function logout(): Promise<void> {
  await fetch(`${BFF_BASE_URL}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
}
