import { useAuth } from '@mono-repo-v2/shared-auth';
import { CareTeamApp } from '@mono-repo-v2/mfe-care-team';

export function CareTeamPage() {
  const { user } = useAuth();

  return (
    <div className="care-team-page">
      <CareTeamApp userId={user?.id} />
    </div>
  );
}
