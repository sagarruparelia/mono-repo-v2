import {
  createRouter,
  createRootRoute,
  createRoute,
  redirect,
} from '@tanstack/react-router';
import { getSession } from '@mono-repo-v2/shared-auth';
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
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: IndexPage,
});

const authenticatedRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: 'authenticated',
  beforeLoad: async () => {
    const session = await getSession();
    if (!session.authenticated) {
      throw redirect({ to: '/' });
    }
    return { user: session.user };
  },
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
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
