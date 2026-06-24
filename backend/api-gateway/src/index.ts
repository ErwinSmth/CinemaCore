import express, { type Request, type Response, type NextFunction } from 'express';
import cors from 'cors';
import path from 'path';
import * as OpenApiValidator from 'express-openapi-validator';
import rateLimit from 'express-rate-limit';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { env } from './config/env';
import authRoutes from './routes/auth.routes';
import { authMiddleware } from './middleware/auth.middleware';

const app = express();

// --- Middlewares Globales de Seguridad ---

// 1. CORS Estricto: Solo permitir dominios del frontend oficial (cambiar en produccion)
app.use(cors({
  origin: ['http://localhost:4200', 'http://localhost:3000'], // angular o react dev
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

// 2. Limite de Payload: Previene ataques de agotamiento de memoria (Max 100kb)
app.use(express.json({ limit: '100kb' }));

// 2.5 Validación Dinámica OpenAPI (SDD)
app.use(
  OpenApiValidator.middleware({
    apiSpec: path.resolve(process.cwd(), '../../docs/api-spec.yml'),
    validateRequests: true,
    validateResponses: false,
    ignorePaths: /.*\/health$/
  })
);

// 3. Rate Limiting: Proteccion contra ataques de fuerza bruta
const authLimiter = rateLimit({
  windowMs: 5 * 60 * 1000, // 5 minutos
  max: 10, // limite de 10 peticiones por IP cada 5 min
  message: {
    timestamp: new Date().toISOString(),
    status: 429,
    error: 'Too Many Requests',
    message: 'Demasiados intentos, por favor intente de nuevo más tarde.',
    path: '/api/v1/auth'
  }
});

// --- Salud API Gateway ---
app.get('/health', (_req: Request, res: Response) => {
  res.json({ status: 'API Gateway esta activa' });
});

// --- Rutas Locales (BFF) ---
// Aplicamos el rate limiter solo a las rutas de autenticacion
app.use('/api/v1/auth', authLimiter, authRoutes);

// --- Rutas Reverse Proxy (Microservicios) ---

// Movie Service: Rutas publicas de solo lectura (No requieren JWT)
app.use('/api/v1/movies', createProxyMiddleware({
  target: env.MOVIE_SERVICE_URL,
  changeOrigin: true,
  pathFilter: (path, req) => req.method === 'GET', // Solo GET es publico
}));

// Movie Service: Rutas privadas de escritura (Requieren JWT y Propagacion de Identidad)
app.use('/api/v1/movies', authMiddleware, createProxyMiddleware({
  target: env.MOVIE_SERVICE_URL,
  changeOrigin: true,
  pathFilter: (path, req) => req.method !== 'GET', // POST, PUT, DELETE protegidos
}));

// Seat Service: Las reservas siempre requieren estar logueado
app.use('/api/v1/seats', authMiddleware, createProxyMiddleware({
  target: env.SEAT_SERVICE_URL,
  changeOrigin: true
}));

// Showtime Service: Consultas de asientos publicas, programar funciones privado
app.use('/api/v1/showtimes', createProxyMiddleware({
  target: env.SHOWTIME_SERVICE_URL,
  changeOrigin: true,
  pathFilter: (path, req) => req.method === 'GET'
}));

app.use('/api/v1/showtimes', authMiddleware, createProxyMiddleware({
  target: env.SHOWTIME_SERVICE_URL,
  changeOrigin: true,
  pathFilter: (path, req) => req.method !== 'GET'
}));

// --- Global Error Handler (OpenAPI Validator & Otros) ---
app.use((err: any, req: Request, res: Response, next: NextFunction) => {
  res.status(err.status || 500).json({
    timestamp: new Date().toISOString(),
    status: err.status || 500,
    error: err.name || 'Internal Server Error',
    message: err.message,
    path: req.path
  });
});

// --- Inicio del Servidor ---
app.listen(env.PORT, () => {
  console.log(`[API Gateway] Corriendo en el puerto ${env.PORT} - Protegido con RateLimit, Proxy y JWT Zero Trust`);
});

export default app;
