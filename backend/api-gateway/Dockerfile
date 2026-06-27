# syntax=docker/dockerfile:1

# ================================
# Fase 1: Construccion (Node.js)
# ================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar configuración de dependencias
COPY backend/api-gateway/package.json backend/api-gateway/package-lock.json ./

# Instalar TODAS las dependencias (incluyendo devDependencies para compilar TS)
RUN npm ci --legacy-peer-deps

# Copiar el código fuente y compilar TypeScript
COPY backend/api-gateway/tsconfig.json ./
COPY backend/api-gateway/src ./src
RUN npm run build

# ================================
# Fase 2: Ejecucion (Produccion)
# ================================
FROM node:20-alpine AS runner

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S nodejs && adduser -S nodejs -G nodejs

# Copiar solo package.json y package-lock.json
COPY backend/api-gateway/package.json backend/api-gateway/package-lock.json ./

# Instalar SOLO las dependencias de producción (ahorra muchísimo espacio)
RUN npm ci --omit=dev --legacy-peer-deps && npm cache clean --force

# Copiar el código compilado desde la fase anterior
COPY --from=builder --chown=nodejs:nodejs /app/dist ./dist

# Copiar el yaml de OpenAPI
COPY docs/api-spec.yml /docs/api-spec.yml

# Cambiar de dueño
RUN chown -R nodejs:nodejs /app

USER nodejs

EXPOSE 8080

# Comando de inicio usando Node directamente sobre el JS compilado (mayor rendimiento)
CMD ["node", "dist/index.js"]
