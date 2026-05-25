import { Request, Response } from 'express';
import { validateToken } from '../clients/auth.client';

// controlador de autenticacion
// su unica funcion es recibir la peticion del front
// orquesta la llamada y responde al cliente
export const getMe = async (req: Request, res: Response): Promise<void> => {
  try {

    // extrae el token del encabezado de la peticion
    const authHeader = req.headers['authorization'] as string;
    const token = authHeader.split(' ')[1] as string;

    // delega la validacion al auth.client.ts
    const userSession = await validateToken(token);

    // respuesta exitosa al frontend
    res.status(200).json({
      userId: userSession.userId,
      role: userSession.role,
      valid: userSession.valid,
    });
  } catch (error) {

    // manejo de errores centralizado
    const message = error instanceof Error ? error.message : 'Error desconocido';
    const status = message.includes('no disponible') ? 503 : 401;
    res.status(status).json({ error: message });
  }
};
