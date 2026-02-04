import { QueryProvider } from '@mono-repo-v2/shared-query';
import { DocumentsApp } from './DocumentsApp';

export function App() {
  return (
    <QueryProvider>
      <DocumentsApp />
    </QueryProvider>
  );
}

export default App;
