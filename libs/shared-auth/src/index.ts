export { getSession, initiateLogin, logout } from './lib/api/auth-api';
export type { SessionResponse, UserInfo } from './lib/api/auth-api';

export { useSession, useLogin, useLogout, SESSION_QUERY_KEY } from './lib/use-session';

export { AuthProvider, useAuth } from './lib/auth-context';

export {
  MfeConfigProvider,
  useMfeConfig,
  useMfeConfigOptional,
} from './lib/mfe-config-context';
export type { MfeConfig, Persona } from './lib/mfe-config-context';

export { AuthMfeConfigProvider } from './lib/auth-mfe-config-provider';
