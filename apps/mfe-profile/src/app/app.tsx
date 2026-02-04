import { QueryProvider } from '@mono-repo-v2/shared-query';
import { ProfileApp } from './ProfileApp';

export function App() {
  return (
    <QueryProvider>
      <ProfileApp />
    </QueryProvider>
  );
}

export default App;
