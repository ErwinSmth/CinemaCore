import { Request, Response } from 'express';
import { loginUser } from '../clients/auth.client';

// controlador de autenticacion
// su unica funcion es recibir la peticion del front
// orquesta la llamada y responde al cliente
export const login = async (req: Request, res: Response): Promise<void> => {
  try {
    // extrae email y contrasena del cuerpo de la peticion
    const { email, contrasena } = req.body;

    // delega el login al auth.client.ts
    const userSession = await loginUser(email, contrasena);

    // respuesta exitosa al frontend con el token y datos
    res.status(200).json(userSession);
  } catch (error) {

    // manejo de errores centralizado
    const message = error instanceof Error ? error.message : 'Error desconocido';
    const status = message.includes('no disponible') ? 503 : 401;
    res.status(status).json({ error: message });
  }
};
