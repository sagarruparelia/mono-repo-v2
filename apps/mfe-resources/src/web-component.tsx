import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { ResourcesApp } from './app/ResourcesApp';

createWebComponent({
  tagName: 'mfe-resources',
  Component: ResourcesApp,
  observedAttributes: ['user-id', 'styles-url'],
  shadow: true,
});

export { ResourcesApp };
