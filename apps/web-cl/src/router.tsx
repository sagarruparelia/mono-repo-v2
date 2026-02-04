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

const routeTree = rootRoute.addChildren([
  indexRoute,
  authenticatedRoute.addChildren([dashboardRoute, profileRoute, summaryRoute]),
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
