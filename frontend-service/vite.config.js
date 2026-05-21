import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tsconfigPaths from 'vite-tsconfig-paths';
import { fileURLToPath, URL } from 'node:url';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

const jsonlintSafeFix = () => ({
  name: 'jsonlint-safe-fix',
  enforce: 'pre',
  transform(code, id) {
    if (id.includes('node_modules/jsonlint-lines/lib/jsonlint.js')) {
      // Replace require.main === module with false so the CLI guard code never executes in browser
      const updated = code.replace(/require\.main\s*===\s*module/g, 'false');
      return updated === code ? null : updated;
    }
    return null;
  }
});

export default defineConfig({
  plugins: [
    jsonlintSafeFix(),
    react(),
    tsconfigPaths(),
    nodePolyfills({
      protocolImports: true
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0'
  },
  build: {
    outDir: 'build',
    commonjsOptions: {
      include: [/node_modules/],
      transformMixedEsModules: true
    }
  },

  css: {
    preprocessorOptions: {
      scss: {
        loadPaths: [fileURLToPath(new URL('./src', import.meta.url))]
      }
    }
  }
});
