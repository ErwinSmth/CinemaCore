import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { env } from '../config/env';

// middleware de seguridad
// intercepta las peticiones y verifique que el token sea valido antes de llegar al controlador
export const authMiddleware = (req: Request, res: Response, next: NextFunction): void => {

  // se extrae el encabezado de autorizacion
  const authHeader = req.headers['authorization'];

  const sendError = (status: number, error: string, message: string) => {
    res.status(status).json({
      timestamp: new Date().toISOString(),
      status,
      error,
      message,
      path: req.originalUrl || req.url
    });
  };

  // validamos que el header exista y tenga el formato correcto
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    sendError(401, 'Unauthorized', 'No Autorizado: No se ha proporcionado ningun token');
    return;
  }

  // extraemos el token de la cabecera
  const token = authHeader.split(' ')[1];

  if (!token) {
    sendError(401, 'Unauthorized', 'No Autorizado: Cabecera de autorizacion mal formada');
    return;
  }

  try {
    // verificamos el token con la llave secreta compartida
    // as le dice a TypeScript que estructura esperamos dentro del token
    const decoded = jwt.verify(token, env.JWT_SECRET) as { sub: string; roles: string[] };
    
    // inyectamos los datos del usuario en la peticion para que el controlador los use (legacy)
    req.user = decoded;

    // Zero Trust Identity Propagation: Inyectamos en las cabeceras HTTP salientes
    req.headers['x-user-id'] = decoded.sub; // el email o ID viaja en el subject (sub)
    req.headers['x-user-roles'] = decoded.roles ? decoded.roles.join(',') : '';

    // pasamos al siguiente middleware o controlador (Proxy)
    next();
  } catch {
    // si la firma no coincido o el token expiro, bloqueamos la peticion
    sendError(401, 'Unauthorized', 'Token invalido o expirado');
  }
};
