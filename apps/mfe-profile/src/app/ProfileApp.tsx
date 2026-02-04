import { useQuery } from '@tanstack/react-query';

interface ProfileAppProps {
  userId?: string;
  embedded?: boolean;
  apiBaseUrl?: string;
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

export function ProfileApp({
  userId,
  embedded = false,
  apiBaseUrl = 'http://localhost:8080',
}: ProfileAppProps) {
  const { data: profile, isLoading, error } = useQuery<UserProfile>({
    queryKey: ['profile', userId],
    queryFn: () => fetchProfile(userId, apiBaseUrl),
    retry: 1,
  });

  if (isLoading) {
    return (
      <div className={`profile-app ${embedded ? 'embedded' : 'standalone'}`}>
        <div className="profile-loading">Loading profile...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`profile-app ${embedded ? 'embedded' : 'standalone'}`}>
        <div className="profile-error">Failed to load profile</div>
      </div>
    );
  }

  return (
    <div className={`profile-app ${embedded ? 'embedded' : 'standalone'}`}>
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
