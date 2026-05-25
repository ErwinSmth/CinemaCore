import { Router } from 'express';
// se importa el controlador de autenticacion
import { login } from '../controllers/auth.controller';

// se instancia el router de express
const router = Router();

// definicion de rutas del modulo auth
// ruta publica para iniciar sesion
router.post('/login', login);

export default router;
