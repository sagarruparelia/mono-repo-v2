import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface DocumentsAppProps {
  userId?: string;
}

export function DocumentsApp({ userId }: DocumentsAppProps) {
  const { isEmbedded } = useMfeConfig();

  return (
    <div className={`documents-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Documents</h2>
      <div className="documents-content">
        <p>Documents information coming soon.</p>
        {userId && <p className="user-context">User: {userId}</p>}
      </div>
    </div>
  );
}

export default DocumentsApp;
