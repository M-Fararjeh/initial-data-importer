# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install &&  npm ci --only=production

# Copy source code
COPY . .

# Build the application
RUN npm install -g @angular/cli
RUN npm install @angular-devkit/build-angular --save-dev
RUN npm run build

# Production stage
FROM nginx:alpine

# Install envsubst for environment variable substitution
RUN apk add --no-cache gettext

# Copy built application
COPY --from=build /app/dist/incoming-correspondence-migration-ui /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy environment script
COPY env.sh /docker-entrypoint.d/env.sh
RUN chmod +x /docker-entrypoint.d/env.sh

# Create a startup script that runs env.sh before nginx
RUN echo '#!/bin/sh' > /docker-entrypoint.d/00-env.sh && \
    echo 'echo "Running environment variable substitution..."' >> /docker-entrypoint.d/00-env.sh && \
    echo '/docker-entrypoint.d/env.sh' >> /docker-entrypoint.d/00-env.sh && \
    chmod +x /docker-entrypoint.d/00-env.sh

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]