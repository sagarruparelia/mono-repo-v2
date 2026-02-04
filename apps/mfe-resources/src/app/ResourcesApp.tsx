import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface ResourcesAppProps {
  userId?: string;
}

export function ResourcesApp({ userId }: ResourcesAppProps) {
  const { isEmbedded } = useMfeConfig();

  return (
    <div className={`resources-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Resources</h2>
      <div className="resources-content">
        <p>Resources information coming soon.</p>
        {userId && <p className="user-context">User: {userId}</p>}
      </div>
    </div>
  );
}

export default ResourcesApp;
