# Frontend Dockerfile
FROM node:18-alpine as build

WORKDIR /app

# Copier les fichiers package.json
COPY package*.json ./

# Installer les dépendances
RUN npm install

# Copier tout le code source
COPY . .

# Build de l'application
RUN npm run build

# Stage de production avec nginx
FROM nginx:alpine

# Copier les fichiers buildés
COPY --from=build /app/build /usr/share/nginx/html

# Copier la configuration nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
