import { useAuth } from '@mono-repo-v2/shared-auth';
import { SummaryApp } from '@mono-repo-v2/mfe-summary';

export function SummaryPage() {
  const { user } = useAuth();

  return (
    <div className="summary-page">
      <SummaryApp userId={user?.id} />
    </div>
  );
}
