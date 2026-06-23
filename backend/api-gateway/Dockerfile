# ============================================================
# Fase 1: Construccion
# Instala todas las dependencias (dev + prod) y compila el TS
# ============================================================
FROM node:22-alpine AS builder

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos primero solo los manifiestos para aprovechar la cache de capas
# Si package.json no cambia, Docker no reinstala las dependencias
COPY package.json package-lock.json ./

# Instalamos TODAS las dependencias (incluyendo devDependencies para el compilador)
RUN npm ci

# Copiamos el código fuente y la configuracion de TypeScript
COPY tsconfig.json ./
COPY src/ ./src/

# Compilamos TypeScript -> JavaScript en la carpeta /app/dist
RUN npx tsc --project tsconfig.json

# ============================================================
# Fase 2: Produccion
# Imagen final ligera: solo el JS compilado + dependencias de prod
# ============================================================
FROM node:22-alpine AS production

# Metadatos de la imagen
LABEL maintainer="Cinestar Backend Team"
LABEL service="api-gateway"

# Creamos un usuario no-root por seguridad (buena practica en produccion)
RUN addgroup -S cinestar && adduser -S gateway -G cinestar

WORKDIR /app

# Copiamos solo los manifiestos para instalar UNICAMENTE dependencias de produccion
COPY package.json package-lock.json ./
RUN npm ci --omit=dev && npm cache clean --force

# Copiamos el JS compilado desde la etapa builder
COPY --from=builder /app/dist ./dist

# Cambiamos al usuario no-root
USER gateway

# Puerto expuesto (debe coincidir con PORT en tu .env)
EXPOSE 3000

# Healthcheck: Docker comprobara este endpoint cada 30s
# Si /health responde OK, el contenedor se marca como "healthy"
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:3000/health || exit 1

# Punto de entrada: ejecuta el JS compilado directamente con Node (sin ts-node-dev)
CMD ["node", "dist/index.js"]
