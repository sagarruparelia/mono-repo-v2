import { useAuth } from '@mono-repo-v2/shared-auth';
import { RecommendationsApp } from '@mono-repo-v2/mfe-recommendations';

export function RecommendationsPage() {
  const { user } = useAuth();

  return (
    <div className="recommendations-page">
      <RecommendationsApp userId={user?.id} />
    </div>
  );
}
