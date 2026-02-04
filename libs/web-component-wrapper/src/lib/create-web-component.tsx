import { ComponentType, StrictMode } from 'react';
import { createRoot, Root } from 'react-dom/client';
import { QueryProvider, createQueryClient } from '@mono-repo-v2/shared-query';
import { MfeConfigProvider, Persona } from '@mono-repo-v2/shared-auth';

interface WebComponentOptions {
  tagName: string;
  Component: ComponentType<Record<string, unknown>>;
  observedAttributes?: string[];
  shadow?: boolean;
}

export function createWebComponent(options: WebComponentOptions): void {
  const {
    tagName,
    Component,
    observedAttributes = [],
    shadow = true,
  } = options;

  class ReactWebComponent extends HTMLElement {
    private root: Root | null = null;
    private mountPoint: HTMLElement | null = null;
    private queryClient = createQueryClient();

    static get observedAttributes() {
      return observedAttributes;
    }

    connectedCallback() {
      if (shadow) {
        const shadowRoot = this.attachShadow({ mode: 'open' });
        this.mountPoint = document.createElement('div');
        shadowRoot.appendChild(this.mountPoint);
        this.injectStyles(shadowRoot);
      } else {
        this.mountPoint = document.createElement('div');
        this.appendChild(this.mountPoint);
      }

      this.root = createRoot(this.mountPoint);
      this.render();
    }

    disconnectedCallback() {
      if (this.root) {
        this.root.unmount();
        this.root = null;
      }
    }

    attributeChangedCallback() {
      this.render();
    }

    private render() {
      if (!this.root) return;

      const props = this.getPropsFromAttributes();
      const mfeConfig = this.getMfeConfig();

      this.root.render(
        <StrictMode>
          <QueryProvider client={this.queryClient}>
            <MfeConfigProvider {...mfeConfig}>
              <Component {...props} />
            </MfeConfigProvider>
          </QueryProvider>
        </StrictMode>
      );
    }

    private getMfeConfig() {
      return {
        enterpriseId: this.getAttribute('enterprise-id') || '',
        persona: (this.getAttribute('persona') || 'agent') as Persona,
        serviceBaseUrl: this.getAttribute('service-base-url') || 'http://localhost:8080',
        isEmbedded: true,
      };
    }

    private getPropsFromAttributes(): Record<string, unknown> {
      const props: Record<string, unknown> = {};
      for (const attr of observedAttributes) {
        const value = this.getAttribute(attr);
        if (value !== null) {
          try {
            props[this.toCamelCase(attr)] = JSON.parse(value);
          } catch {
            props[this.toCamelCase(attr)] = value;
          }
        }
      }
      return props;
    }

    private toCamelCase(str: string): string {
      return str.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
    }

    private injectStyles(shadowRoot: ShadowRoot) {
      const stylesUrl = this.getAttribute('styles-url');
      if (stylesUrl) {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = stylesUrl;
        shadowRoot.appendChild(link);
      }
    }
  }

  if (!customElements.get(tagName)) {
    customElements.define(tagName, ReactWebComponent);
  }
}
