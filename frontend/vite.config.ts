import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Lets `npm run dev`/`preview` point the API proxy at a backend other than the default local
  // instance without editing this file.
  const env = loadEnv(mode, process.cwd(), '')
  const apiProxyTarget = env.VITE_API_PROXY_TARGET ?? 'http://localhost:8081'

  // The app is served at https://<domain>/wordwang/ by default (see frontend/Caddyfile), not the
  // site root, so every built asset reference needs this prefix or the browser requests them
  // from the wrong path once index.html is served from a subpath. Overridable via
  // frontend/Dockerfile's APP_BASE_PATH build arg, which must agree with frontend/Caddyfile's
  // matching $APP_PATH.
  const basePath = env.APP_BASE_PATH || '/wordwang/'
  const apiPathPrefix = basePath.replace(/\/$/, '')

  return {
    base: basePath,
    plugins: [react()],
    define: {
      global: 'globalThis',
    },
    server: {
      // Binds the dev server to all network interfaces, not just localhost, so it's reachable at
      // the machine's LAN IP (e.g. from a phone on the same Wi-Fi) - Vite defaults to localhost-only.
      host: true,
      port: 5174,
      proxy: {
        // The frontend calls <basePath>/api/... and connects to <basePath>/ws (see
        // src/api/client.ts and src/hooks/useGameSocket.ts) so its requests carry the same path
        // prefix in dev as they will once deployed behind the shared reverse proxy (see
        // frontend/Caddyfile) - the backend itself is still mapped at bare /api/... and /ws, so
        // the prefix is stripped here before forwarding.
        [`${apiPathPrefix}/api`]: {
          target: apiProxyTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiPathPrefix}`), ''),
        },
        [`${apiPathPrefix}/ws`]: {
          target: apiProxyTarget,
          ws: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiPathPrefix}`), ''),
        },
      },
    },
  }
})
