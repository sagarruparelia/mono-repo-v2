import { QueryProvider } from '@mono-repo-v2/shared-query';
import { ResourcesApp } from './ResourcesApp';

export function App() {
  return (
    <QueryProvider>
      <ResourcesApp />
    </QueryProvider>
  );
}

export default App;
