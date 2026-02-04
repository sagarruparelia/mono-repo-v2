import {
  createRouter,
  createRootRoute,
  createRoute,
  redirect,
} from '@tanstack/react-router';
import { getSession, type UserInfo } from '@mono-repo-v2/shared-auth';

// Route context types
interface AuthenticatedRouteContext {
  user: UserInfo;
}
import { RootLayout } from './routes/__root';
import { IndexPage } from './routes/index';
import { AuthenticatedLayout } from './routes/_authenticated';
import { DashboardPage } from './routes/_authenticated/dashboard';
import { ProfilePage } from './routes/_authenticated/profile';
import { SummaryPage } from './routes/_authenticated/summary';
import { CareTeamPage } from './routes/_authenticated/care-team';
import { DocumentsPage } from './routes/_authenticated/documents';
import { RecommendationsPage } from './routes/_authenticated/recommendations';
import { ResourcesPage } from './routes/_authenticated/resources';

const rootRoute = createRootRoute({
  component: RootLayout,
  notFoundComponent: () => (
    <div className="not-found">
      <h1>404 - Page Not Found</h1>
      <p>The page you're looking for doesn't exist.</p>
    </div>
  ),
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: IndexPage,
});

const authenticatedRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: 'authenticated',
  beforeLoad: async (): Promise<AuthenticatedRouteContext> => {
    const session = await getSession();
    if (!session.authenticated || !session.user) {
      throw redirect({ to: '/' });
    }
    return { user: session.user };
  },
  pendingComponent: () => (
    <div className="auth-loading">Verifying authentication...</div>
  ),
  errorComponent: ({ error }) => (
    <div className="auth-error">
      <h2>Authentication Error</h2>
      <p>{error instanceof Error ? error.message : 'An error occurred'}</p>
    </div>
  ),
  component: AuthenticatedLayout,
});

const dashboardRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/dashboard',
  component: DashboardPage,
});

const profileRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/profile',
  component: ProfilePage,
});

const summaryRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/summary',
  component: SummaryPage,
});

const careTeamRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/care-team',
  component: CareTeamPage,
});

const documentsRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/documents',
  component: DocumentsPage,
});

const recommendationsRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/recommendations',
  component: RecommendationsPage,
});

const resourcesRoute = createRoute({
  getParentRoute: () => authenticatedRoute,
  path: '/resources',
  component: ResourcesPage,
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  authenticatedRoute.addChildren([
    dashboardRoute,
    profileRoute,
    summaryRoute,
    careTeamRoute,
    documentsRoute,
    recommendationsRoute,
    resourcesRoute,
  ]),
]);

export const router = createRouter({
  routeTree,
  defaultPreload: 'intent',
  defaultPreloadStaleTime: 0, // Always check for fresh data on preload
  defaultPendingMinMs: 200, // Avoid flashing loading states for fast loads
  defaultPendingMs: 1000, // Show pending state after 1s if still loading
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
