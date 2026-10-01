# ──────────────────────────────────────────────────────────────
# Production image — multi-stage build.
#
# Layer cache strategy (top = slowest-changing, bottom = fastest-changing):
#   1. Base image           — changes only on Node LTS security releases
#   2. package*.json        — changes only when deps added/removed
#   3. npm install          — re-runs only when packages change
#   4. src/ + public/       — changes on every commit  ← cache busts here
#   5. ARG/ENV (URLs)       — changes on env reconfiguration (AFTER src copy
#                             so a URL change doesn't bust the npm install layer)
#   6. npm run build        — re-runs when src or URLs change
# ──────────────────────────────────────────────────────────────

# ── Stage 1: build ──────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# [SLOW] Package manifest — only changes when deps are added/removed
COPY package*.json ./
RUN npm install --legacy-peer-deps

# [FAST] Source — changes on every commit; intentionally after npm install
COPY public/ public/
COPY src/ src/

# [VARIABLE] Build-time env vars — placed AFTER source copy so a URL change
# does NOT bust the npm install cache layer above.
ARG REACT_APP_BASE_API_URL
ENV REACT_APP_BASE_API_URL=$REACT_APP_BASE_API_URL

ARG REACT_APP_N8N_WEBHOOK_URL
ENV REACT_APP_N8N_WEBHOOK_URL=$REACT_APP_N8N_WEBHOOK_URL

RUN npm run build

# ── Stage 2: runtime ────────────────────────────────────────
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# [SLOW] Non-root user + nginx config — never changes
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

RUN rm -rf /etc/nginx/conf.d/* && rm -f /etc/nginx/nginx.conf

RUN echo 'pid /tmp/nginx.pid;' > /etc/nginx/nginx.conf && \
    echo 'worker_processes auto;' >> /etc/nginx/nginx.conf && \
    echo 'error_log /tmp/nginx_error.log warn;' >> /etc/nginx/nginx.conf && \
    echo 'events { worker_connections 1024; }' >> /etc/nginx/nginx.conf && \
    echo 'http {' >> /etc/nginx/nginx.conf && \
    echo '  include /etc/nginx/mime.types;' >> /etc/nginx/nginx.conf && \
    echo '  default_type application/octet-stream;' >> /etc/nginx/nginx.conf && \
    echo '  include /etc/nginx/conf.d/*.conf;' >> /etc/nginx/nginx.conf && \
    echo '}' >> /etc/nginx/nginx.conf

# [SLOW] nginx site config — rarely changes
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN mkdir -p /var/run/nginx /var/cache/nginx /var/log/nginx /tmp && \
    chown -R appuser:appgroup /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx/conf.d /var/run/nginx /tmp && \
    chmod -R 755 /var/run/nginx /var/cache/nginx /var/log/nginx /tmp

# [FAST] Built React bundle — changes on every commit
COPY --from=build /app/build .

USER appuser
EXPOSE 80
CMD ["/bin/sh", "-c", "export PORT=${PORT:-80} && envsubst '$PORT' < /etc/nginx/conf.d/default.conf > /tmp/default.conf && mv /tmp/default.conf /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
