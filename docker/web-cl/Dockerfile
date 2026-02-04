FROM nginx:1.25-alpine

# Copy nginx config
COPY docker/web-cl/nginx.conf /etc/nginx/nginx.conf

# Copy pre-built web-cl
COPY dist/apps/web-cl /usr/share/nginx/html

# Copy MFE bundles for external consumers
COPY dist/apps/mfe-profile/web-component /usr/share/nginx/html/mfe/profile
COPY dist/apps/mfe-summary/web-component /usr/share/nginx/html/mfe/summary

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
