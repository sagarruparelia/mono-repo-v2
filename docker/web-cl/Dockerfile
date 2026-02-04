# Stage 1: Build
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./
COPY nx.json ./
COPY tsconfig.base.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY apps/web-cl ./apps/web-cl
COPY apps/mfe-profile ./apps/mfe-profile
COPY apps/mfe-summary ./apps/mfe-summary
COPY libs ./libs

# Build the application
RUN npx nx build web-cl --configuration=production

# Stage 2: Serve
FROM nginx:1.25-alpine

# Copy nginx config
COPY docker/web-cl/nginx.conf /etc/nginx/nginx.conf

# Copy built files
COPY --from=builder /app/dist/apps/web-cl /usr/share/nginx/html

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
