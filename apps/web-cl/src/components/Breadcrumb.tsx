import { Link, useRouterState } from '@tanstack/react-router';

interface BreadcrumbItem {
  label: string;
  to?: string;
}

const routeLabels: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/profile': 'Profile',
  '/summary': 'Summary',
  '/care-team': 'Care Team',
  '/documents': 'Documents',
  '/recommendations': 'Recommendations',
  '/resources': 'Resources',
};

export function Breadcrumb() {
  const routerState = useRouterState();
  const pathname = routerState.location.pathname;

  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Home', to: '/dashboard' },
  ];

  // Add current page if not dashboard
  if (pathname !== '/dashboard') {
    const label = routeLabels[pathname] || pathname.slice(1);
    breadcrumbs.push({ label });
  }

  return (
    <nav aria-label="Breadcrumb" className="breadcrumb">
      <ol className="breadcrumb-list">
        {breadcrumbs.map((crumb, index) => {
          const isLast = index === breadcrumbs.length - 1;

          return (
            <li key={crumb.label} className="breadcrumb-item">
              {isLast || !crumb.to ? (
                <span className="breadcrumb-current" aria-current="page">
                  {crumb.label}
                </span>
              ) : (
                <>
                  <Link to={crumb.to} className="breadcrumb-link">
                    {crumb.label}
                  </Link>
                  <span className="breadcrumb-separator" aria-hidden="true">
                    /
                  </span>
                </>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
