import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface RecommendationsAppProps {
  userId?: string;
}

export function RecommendationsApp({ userId }: RecommendationsAppProps) {
  const { isEmbedded } = useMfeConfig();

  return (
    <div className={`recommendations-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Recommendations</h2>
      <div className="recommendations-content">
        <p>Recommendations information coming soon.</p>
        {userId && <p className="user-context">User: {userId}</p>}
      </div>
    </div>
  );
}

export default RecommendationsApp;
