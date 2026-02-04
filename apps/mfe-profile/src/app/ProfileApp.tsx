import { useQuery } from '@tanstack/react-query';
import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface ProfileAppProps {
  userId?: string;
}

interface UserProfile {
  id: string;
  name: string;
  email: string;
  bio?: string;
  avatarUrl?: string;
  createdAt?: string;
}

async function fetchProfile(
  userId: string | undefined,
  apiBaseUrl: string
): Promise<UserProfile> {
  const response = await fetch(`${apiBaseUrl}/api/profile/${userId || 'me'}`, {
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch profile');
  }
  return response.json();
}

export function ProfileApp({ userId }: ProfileAppProps) {
  const { serviceBaseUrl, isEmbedded } = useMfeConfig();

  const { data: profile, isLoading, error } = useQuery<UserProfile>({
    queryKey: ['profile', userId],
    queryFn: () => fetchProfile(userId, serviceBaseUrl),
    retry: 1,
  });

  if (isLoading) {
    return (
      <div className={`profile-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
        <div className="profile-loading">Loading profile...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`profile-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
        <div className="profile-error">Failed to load profile</div>
      </div>
    );
  }

  return (
    <div className={`profile-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>User Profile</h2>
      {profile && (
        <div className="profile-content">
          {profile.avatarUrl && (
            <img
              src={profile.avatarUrl}
              alt={profile.name}
              className="profile-avatar"
            />
          )}
          <div className="profile-details">
            <p>
              <strong>Name:</strong> {profile.name}
            </p>
            <p>
              <strong>Email:</strong> {profile.email}
            </p>
            {profile.bio && (
              <p>
                <strong>Bio:</strong> {profile.bio}
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default ProfileApp;
