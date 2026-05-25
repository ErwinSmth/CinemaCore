import { Router } from 'express';
// se importa el controlador de autenticacion
import { login, register } from '../controllers/auth.controller';

// se instancia el router de express
const router = Router();

// definicion de rutas del modulo auth
// ruta publica para iniciar sesion
router.post('/login', login);

// ruta publica para registrar un nuevo usuario
router.post('/register', register);

export default router;
