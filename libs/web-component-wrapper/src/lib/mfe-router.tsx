import { HashRouter, Routes, Route, useNavigate } from 'react-router-dom';
import { useMfeConfig } from '@mono-repo-v2/shared-auth';
import { ReactNode } from 'react';

interface MfeRoute {
  path: string;
  element: ReactNode;
}

interface MfeRouterProviderProps {
  children: ReactNode;
  routes?: MfeRoute[];
}

export function MfeRouterProvider({ children, routes }: MfeRouterProviderProps) {
  const { isEmbedded } = useMfeConfig();

  if (isEmbedded && routes) {
    // Web component mode - use HashRouter for deep linking
    return (
      <HashRouter>
        <Routes>
          {routes.map((route) => (
            <Route key={route.path} path={route.path} element={route.element} />
          ))}
        </Routes>
      </HashRouter>
    );
  }

  // Direct import mode - render children (parent controls routing)
  return <>{children}</>;
}

export function useMfeNavigate() {
  const { isEmbedded } = useMfeConfig();

  if (isEmbedded) {
    // Use react-router-dom navigate in web component mode
    return useNavigate();
  }

  // In direct import mode, return a no-op or parent navigation
  return () => console.warn('Navigation not available in direct import mode');
}

export { useNavigate, useParams, Link } from 'react-router-dom';
