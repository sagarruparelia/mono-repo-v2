/// <reference types='vitest' />
import { defineConfig, type LibraryFormats } from 'vite';
import react from '@vitejs/plugin-react-swc';
import { nxViteTsPaths } from '@nx/vite/plugins/nx-tsconfig-paths.plugin';
import { nxCopyAssetsPlugin } from '@nx/vite/plugins/nx-copy-assets.plugin';
import { resolve } from 'path';

const isWebComponentBuild = process.env.BUILD_MODE === 'web-component';

export default defineConfig(() => ({
  root: import.meta.dirname,
  cacheDir: '../../node_modules/.vite/apps/mfe-health-summary',
  server: {
    port: 4204,
    host: 'localhost',
  },
  preview: {
    port: 4204,
    host: 'localhost',
  },
  plugins: [react(), nxViteTsPaths(), nxCopyAssetsPlugin(['*.md'])],
  build: isWebComponentBuild
    ? {
        outDir: '../../dist/apps/mfe-health-summary/web-component',
        emptyOutDir: true,
        lib: {
          entry: resolve(import.meta.dirname, 'src/web-component.tsx'),
          name: 'MfeHealthSummary',
          fileName: 'mfe-health-summary',
          formats: ['es', 'umd'] as LibraryFormats[],
        },
        rollupOptions: {
          external: ['react', 'react-dom'],
          output: {
            globals: {
              react: 'React',
              'react-dom': 'ReactDOM',
            },
          },
        },
      }
    : {
        outDir: '../../dist/apps/mfe-health-summary',
        emptyOutDir: true,
        reportCompressedSize: true,
        commonjsOptions: {
          transformMixedEsModules: true,
        },
      },
  test: {
    name: 'mfe-health-summary',
    watch: false,
    globals: true,
    environment: 'jsdom',
    include: ['{src,tests}/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
    reporters: ['default'],
    coverage: {
      reportsDirectory: '../../coverage/apps/mfe-health-summary',
      provider: 'v8' as const,
    },
  },
}));
