FROM nginx:1.25-alpine@sha256:a27232e9fbc41d92a73eeeabb4ca38d46a8d0f620a8ef3d182e5ab8b3f2be3e8

# Copy nginx config
COPY docker/web-cl/nginx.conf /etc/nginx/nginx.conf

# Copy pre-built web-cl
COPY dist/apps/web-cl /usr/share/nginx/html

# Copy MFE bundles for external consumers
COPY dist/apps/mfe-profile/web-component /usr/share/nginx/html/mfe/profile
COPY dist/apps/mfe-summary/web-component /usr/share/nginx/html/mfe/summary

# Create non-root user and set permissions
RUN addgroup -S nginx-app && adduser -S nginx-app -G nginx-app \
    && chown -R nginx-app:nginx-app /usr/share/nginx/html \
    && chown -R nginx-app:nginx-app /var/cache/nginx \
    && chown -R nginx-app:nginx-app /var/log/nginx \
    && touch /var/run/nginx.pid \
    && chown -R nginx-app:nginx-app /var/run/nginx.pid

USER nginx-app:nginx-app

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
