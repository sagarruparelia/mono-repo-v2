import { useAuth } from '@mono-repo-v2/shared-auth';
import { ProfileApp } from '@mono-repo-v2/mfe-profile';

export function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="profile-page">
      <ProfileApp userId={user?.id} />
    </div>
  );
}
