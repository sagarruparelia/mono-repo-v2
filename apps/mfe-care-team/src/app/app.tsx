import { QueryProvider } from '@mono-repo-v2/shared-query';
import { CareTeamApp } from './CareTeamApp';

export function App() {
  return (
    <QueryProvider>
      <CareTeamApp />
    </QueryProvider>
  );
}

export default App;
