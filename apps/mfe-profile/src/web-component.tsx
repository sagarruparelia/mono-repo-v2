import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { ProfileApp } from './app/ProfileApp';

createWebComponent({
  tagName: 'mfe-profile',
  Component: ProfileApp,
  observedAttributes: ['user-id', 'embedded', 'api-base-url', 'styles-url'],
  shadow: true,
});

export { ProfileApp };
