import express, { type Request, type Response } from 'express';
import cors from 'cors';
import { env } from './config/env';

const app = express();

// --- Middlewares Globales ---
app.use(cors());
app.use(express.json());

// --- Salud API Gateway ---
app.get('/health', (_req: Request, res: Response) => {
  res.json({ status: 'API Gateway is running' });
});

// --- Inicio del Servidor ---
app.listen(env.PORT, () => {
  console.log(`[API Gateway] Running on port ${env.PORT}`);
});

export default app;
