import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { DocumentsApp } from './app/DocumentsApp';

createWebComponent({
  tagName: 'mfe-documents',
  Component: DocumentsApp,
  observedAttributes: ['user-id', 'styles-url'],
  shadow: true,
});

export { DocumentsApp };
