import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { CareTeamApp } from './app/CareTeamApp';

createWebComponent({
  tagName: 'mfe-care-team',
  Component: CareTeamApp,
  observedAttributes: ['user-id', 'styles-url'],
  shadow: true,
});

export { CareTeamApp };
