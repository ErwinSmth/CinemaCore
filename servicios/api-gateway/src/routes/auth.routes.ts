import { Router } from 'express';
// se importa el middleware y el controlador de autenticacion
import { authMiddleware } from '../middleware/auth.middleware';
import { getMe } from '../controllers/auth.controller';

// se instancia el router de express
const router = Router();

// definicio de rutas del modulo auth
// primero se ejecuta el middlewara para validar el token
// si el middleware aprueba la peticion, se ejecuta el controlador
router.get('/me', authMiddleware, getMe);

export default router;
