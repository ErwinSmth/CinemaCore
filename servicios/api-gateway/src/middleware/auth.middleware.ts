import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { env } from '../config/env';

// middleware de seguridad
// intercepta las peticiones y verifique que el token sea valido antes de llegar al controlador
export const authMiddleware = (req: Request, res: Response, next: NextFunction): void => {

  // se extrae el encabezado de autorizacion
  const authHeader = req.headers['authorization'];

  // validamos que el header exista y tenga el formato correcto
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    res.status(401).json({ error: 'No Autorizado: No se ha proporcionado ningun token' });
    return;
  }

  // extraemos el token de la cabecera
  const token = authHeader.split(' ')[1];

  if (!token) {
    res.status(401).json({ error: 'No Autorizado: Cabecera de autorizacion mal formada' });
    return;
  }

  try {
    // verificamos el token con la llave secreta compartida
    // as le dice a TypeScript que estructura esperamos dentro del token
    const decoded = jwt.verify(token, env.JWT_SECRET) as { id: string; role: string };
    
    // inyectamos los datos del usuario en la peticion para que el controlador los use
    req.user = decoded;

    // pasamos al siguiente middleware o controlador
    next();
  } catch {
    // si la firma no coincido o el token expiro, bloqueamos la peticion
    res.status(403).json({ error: 'Forbidden: Token invalido o expirado' });
  }
};
