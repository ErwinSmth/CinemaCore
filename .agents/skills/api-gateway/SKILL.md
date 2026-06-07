---
name: api-gateway
description: Guía de desarrollo del API Gateway con Express 5, TypeScript, axios clients
---

# API Gateway Skill - Cinestar Backend

## Stack

- Node.js 22
- TypeScript 6
- Express 5
- Axios (cliente HTTP interno)
- jsonwebtoken (JWT)
- dotenv

## Estructura

```
servicios/api-gateway/
├── src/
│   ├── index.ts              # Entry point
│   ├── config/
│   │   └── env.ts            # Variables de entorno
│   ├── clients/
│   │   ├── auth.client.ts    # Cliente HTTP → Auth Service
│   │   ├── movie.client.ts   # Cliente HTTP → Movie Service
│   │   ├── showtime.client.ts# Cliente HTTP → Showtime Service
│   │   └── seat.client.ts    # Cliente HTTP → Seat Service
│   ├── controllers/
│   │   ├── auth.controller.ts
│   │   ├── movie.controller.ts
│   │   ├── showtime.controller.ts
│   │   └── seat.controller.ts
│   ├── middleware/
│   │   └── auth.middleware.ts # JWT verification
│   ├── routes/
│   │   ├── auth.routes.ts
│   │   ├── movie.routes.ts
│   │   ├── showtime.routes.ts
│   │   └── seat.routes.ts
│   └── types/
│       └── express/
│           └── index.d.ts    # TypeScript augmentation
├── package.json
├── tsconfig.json
├── .env
└── Dockerfile
```

## Convenciones

- **Clients**: Un archivo por servicio backend, usa axios con base URL de env
- **Controllers**: Delegan al client, manejan errores HttpError
- **Middleware**: JWT middleware antes de rutas protegidas
- **Types**: Augmentar Express Request con `user?: { id: string; role: string }`

## Variables de Entorno

```env
PORT=3000
AUTH_SERVICE_URL=http://localhost:8080
MOVIE_SERVICE_URL=http://localhost:8081
SHOWTIME_SERVICE_URL=http://localhost:8082
SEAT_SERVICE_URL=http://localhost:8083
JWT_SECRET=4qhq8L6CGncLSv60o1C1v0wKk8K7gXyZ1oG7rT2bU4v=
```

## Client Pattern

```typescript
import axios, { AxiosError } from 'axios';
import { MOVIE_SERVICE_URL } from '../config/env';

export class HttpError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

export async function getPeliculas() {
  try {
    const response = await axios.get(`${MOVIE_SERVICE_URL}/api/movies`);
    return response.data;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new HttpError(
      axiosError.response?.status || 500,
      axiosError.message
    );
  }
}
```

## JWT Middleware

```typescript
import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { JWT_SECRET } from '../config/env';

export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) return res.status(401).json({ message: 'Token required' });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded as { id: string; role: string };
    next();
  } catch {
    res.status(401).json({ message: 'Invalid token' });
  }
}
```

## Rutas

```typescript
import { Router } from 'express';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

// Públicas
router.post('/login', loginHandler);
router.post('/register', registerHandler);

// Protegidas
router.get('/peliculas', authMiddleware, getPeliculasHandler);

export default router;
```

## Docker

```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
RUN addgroup -S gateway && adduser -S gateway -G gateway
USER gateway
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package*.json ./
RUN npm ci --omit=dev
EXPOSE 3000
HEALTHCHECK --interval=30s CMD wget -q --spider http://localhost:3000/health || exit 1
CMD ["node", "dist/index.js"]
```
