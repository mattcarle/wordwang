# Frontend/edge image: builds the React app, then serves the static build and reverse-proxies
# /api/* and /ws (the STOMP/SockJS endpoint) to the backend from the same Caddy instance - see
# ../docker-compose.yml. Caddy also handles TLS (automatic Let's Encrypt for a real domain, a
# locally-trusted cert for "localhost").

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Defaults to this app's path behind the shared carle7-edge reverse proxy (see vite.config.ts).
ARG APP_BASE_PATH=/wordwang/
ENV APP_BASE_PATH=$APP_BASE_PATH
RUN npm run build

FROM caddy:2-alpine
COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist /srv
