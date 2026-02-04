import { useQuery } from '@tanstack/react-query';
import { useMfeConfig } from '@mono-repo-v2/shared-auth';

interface SummaryAppProps {
  userId?: string;
}

interface SummaryData {
  totalItems: number;
  completedItems: number;
  pendingItems: number;
  recentActivity: Array<{
    id: string;
    action: string;
    timestamp: string;
  }>;
}

async function fetchSummary(
  userId: string | undefined,
  apiBaseUrl: string
): Promise<SummaryData> {
  const response = await fetch(`${apiBaseUrl}/api/summary/${userId || 'me'}`, {
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch summary');
  }
  return response.json();
}

export function SummaryApp({ userId }: SummaryAppProps) {
  const { serviceBaseUrl, isEmbedded } = useMfeConfig();

  const { data: summary, isLoading, error } = useQuery<SummaryData>({
    queryKey: ['summary', userId],
    queryFn: () => fetchSummary(userId, serviceBaseUrl),
    retry: 1,
  });

  if (isLoading) {
    return (
      <div className={`summary-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
        <div className="summary-loading">Loading summary...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`summary-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
        <div className="summary-error">Failed to load summary</div>
      </div>
    );
  }

  return (
    <div className={`summary-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Summary</h2>
      {summary && (
        <div className="summary-content">
          <div className="summary-stats">
            <div className="stat-card">
              <span className="stat-value">{summary.totalItems}</span>
              <span className="stat-label">Total</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{summary.completedItems}</span>
              <span className="stat-label">Completed</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{summary.pendingItems}</span>
              <span className="stat-label">Pending</span>
            </div>
          </div>
          {summary.recentActivity && summary.recentActivity.length > 0 && (
            <div className="recent-activity">
              <h3>Recent Activity</h3>
              <ul>
                {summary.recentActivity.map((activity) => (
                  <li key={activity.id}>
                    <span className="activity-action">{activity.action}</span>
                    <span className="activity-time">{activity.timestamp}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default SummaryApp;
