# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first for better layer caching
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY public/ public/
COPY src/ src/

# Build argument for API URL (must be set at build time for React)
ARG REACT_APP_BASE_API_URL
ENV REACT_APP_BASE_API_URL=$REACT_APP_BASE_API_URL

ARG REACT_APP_N8N_WEBHOOK_URL
ENV REACT_APP_N8N_WEBHOOK_URL=$REACT_APP_N8N_WEBHOOK_URL

# Build the production bundle
RUN npm run build

# Production stage
FROM nginx:alpine
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /usr/share/nginx/html

# Remove default nginx static files and config
RUN rm -rf /etc/nginx/conf.d/* && rm -f /etc/nginx/nginx.conf

# Create custom nginx.conf for non-root user
RUN echo 'pid /tmp/nginx.pid;' > /etc/nginx/nginx.conf && \
    echo 'worker_processes auto;' >> /etc/nginx/nginx.conf && \
    echo 'error_log /tmp/nginx_error.log warn;' >> /etc/nginx/nginx.conf && \
    echo 'events { worker_connections 1024; }' >> /etc/nginx/nginx.conf && \
    echo 'http {' >> /etc/nginx/nginx.conf && \
    echo '  include /etc/nginx/mime.types;' >> /etc/nginx/nginx.conf && \
    echo '  default_type application/octet-stream;' >> /etc/nginx/nginx.conf && \
    echo '  include /etc/nginx/conf.d/*.conf;' >> /etc/nginx/nginx.conf && \
    echo '}' >> /etc/nginx/nginx.conf

# Copy custom nginx config for this application
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built app from build stage
COPY --from=build /app/build .

# Create necessary directories and set permissions for non-root user
RUN mkdir -p /var/run/nginx /var/cache/nginx /var/log/nginx /tmp && \
    chown -R appuser:appgroup /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx/conf.d /var/run/nginx /tmp && \
    chmod -R 755 /var/run/nginx /var/cache/nginx /var/log/nginx /tmp

USER appuser
EXPOSE 80
CMD ["/bin/sh", "-c", "export PORT=${PORT:-80} && envsubst '$PORT' < /etc/nginx/conf.d/default.conf > /tmp/default.conf && mv /tmp/default.conf /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
