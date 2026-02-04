import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface CareTeamAppProps {
  userId?: string;
}

export function CareTeamApp({ userId }: CareTeamAppProps) {
  const { isEmbedded } = useMfeConfig();

  return (
    <div className={`care-team-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Care Team</h2>
      <div className="care-team-content">
        <p>Care team information coming soon.</p>
        {userId && <p className="user-context">User: {userId}</p>}
      </div>
    </div>
  );
}

export default CareTeamApp;
