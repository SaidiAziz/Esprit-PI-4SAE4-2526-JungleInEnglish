# Stage 1 - Build Angular App
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Build production app
RUN npm run build -- --configuration production

# Stage 2 - Serve with nginx
FROM nginx:alpine

# Copy built browser assets to nginx web root
COPY --from=build /app/dist/pi-front/browser /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

