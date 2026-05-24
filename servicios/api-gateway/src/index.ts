import express, { type Request, type Response } from 'express';
import cors from 'cors';
import { env } from './config/env'; // se importa la configuracion validada

// creamos la instancia de la aplicacion express
const app = express();

// --- Middlewares Globales ---
// cada peticion HTTP que llega debe pasar por estos middlewares antes de llegar al endpoint
app.use(cors());
// permite peticiones desde dominios externos o el frontend
app.use(express.json());

// --- Salud API Gateway ---
// endpoint para monitoreo
// docker llamara a esta URL para saber si el contenedor esta vivo
app.get('/health', (_req: Request, res: Response) => {
  res.json({ status: 'API Gateway esta activa' });
});

// --- Inicio del Servidor ---
// Vincula la apliacion al puerto definido en el archivo .env
app.listen(env.PORT, () => {
  console.log(`[API Gateway] Corriendo en el puerto ${env.PORT}`);
});

// exportamos la app para que pueda ser usadas en otros modulos
export default app;
