import { createWebComponent } from '@mono-repo-v2/web-component-wrapper';
import { RecommendationsApp } from './app/RecommendationsApp';

createWebComponent({
  tagName: 'mfe-recommendations',
  Component: RecommendationsApp,
  observedAttributes: ['user-id', 'styles-url'],
  shadow: true,
});

export { RecommendationsApp };
