import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { SummaryApp } from './app/SummaryApp';

createWebComponent({
  tagName: 'mfe-summary',
  Component: SummaryApp,
  observedAttributes: ['user-id', 'styles-url'],
  shadow: true,
});

export { SummaryApp };
