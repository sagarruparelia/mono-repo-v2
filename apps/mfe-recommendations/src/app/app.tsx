import { QueryProvider } from '@mono-repo-v2/shared-query';
import { RecommendationsApp } from './RecommendationsApp';

export function App() {
  return (
    <QueryProvider>
      <RecommendationsApp />
    </QueryProvider>
  );
}

export default App;
