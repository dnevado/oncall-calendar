# Frontend de guardias: compilación con Vite y nginx sirviendo la SPA bajo /app/.

FROM node:20.18.0-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist /usr/share/nginx/html/app
EXPOSE 80
