import { useAuth } from '@mono-repo-v2/shared-auth';
import { DocumentsApp } from '@mono-repo-v2/mfe-documents';

export function DocumentsPage() {
  const { user } = useAuth();

  return (
    <div className="documents-page">
      <DocumentsApp userId={user?.id} />
    </div>
  );
}
