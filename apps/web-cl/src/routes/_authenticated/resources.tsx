import { useAuth } from '@mono-repo-v2/shared-auth';
import { ResourcesApp } from '@mono-repo-v2/mfe-resources';

export function ResourcesPage() {
  const { user } = useAuth();

  return (
    <div className="resources-page">
      <ResourcesApp userId={user?.id} />
    </div>
  );
}
