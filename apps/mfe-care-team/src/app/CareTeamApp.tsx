import { useMfeConfig } from '@mono-repo-v2/shared-auth';
import { MfeRouterProvider } from '@mono-repo-v2/web-component-wrapper';
import { CareTeamList } from './routes/CareTeamList';
import { CareTeamMemberDetail } from './routes/CareTeamMemberDetail';

interface CareTeamAppProps {
  userId?: string;
}

const routes = [
  { path: '/', element: <CareTeamList /> },
  { path: '/member/:id', element: <CareTeamMemberDetail /> },
];

export function CareTeamApp({ userId }: CareTeamAppProps) {
  const { isEmbedded } = useMfeConfig();

  return (
    <div className={`care-team-app ${isEmbedded ? 'embedded' : 'standalone'}`}>
      <h2>Care Team</h2>
      {userId && <p className="user-context">User: {userId}</p>}
      <MfeRouterProvider routes={routes}>
        <CareTeamList />
      </MfeRouterProvider>
    </div>
  );
}

export default CareTeamApp;
