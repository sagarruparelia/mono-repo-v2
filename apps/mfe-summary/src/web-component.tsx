import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { SummaryApp } from './app/SummaryApp';

createWebComponent({
  tagName: 'mfe-summary',
  Component: SummaryApp,
  observedAttributes: ['user-id', 'embedded', 'api-base-url', 'styles-url'],
  shadow: true,
});

export { SummaryApp };
