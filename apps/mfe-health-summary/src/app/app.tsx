import { QueryProvider } from '@mono-repo-v2/shared-query';
import { SummaryApp } from './SummaryApp';

export function App() {
  return (
    <QueryProvider>
      <SummaryApp />
    </QueryProvider>
  );
}

export default App;
