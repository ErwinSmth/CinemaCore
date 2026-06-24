import { Request, Response } from 'express';
import { loginUser, registerUser, HttpError } from '../clients/auth.client';

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

    // manejo de errores centralizado (usando el custom HttpError)
    const status = error instanceof HttpError ? error.statusCode : 500;
    const message = error instanceof Error ? error.message : 'Error desconocido';
    
    res.status(status).json({
      timestamp: new Date().toISOString(),
      status,
      error: status === 500 ? 'Internal Server Error' : 'Bad Request',
      message,
      path: req.originalUrl || req.url
    });
  }
};

// controlador de registro
// recibe los datos del formulario y delega al cliente HTTP
export const register = async (req: Request, res: Response): Promise<void> => {
  try {
    // extrae los campos del cuerpo de la peticion
    const { email, contrasena, nombres, apellidos } = req.body;

    // delega el registro al auth.client.ts
    const newUser = await registerUser({ email, contrasena, nombres, apellidos });

    // respuesta 201 Created con el token y datos del nuevo usuario
    res.status(201).json(newUser);
  } catch (error) {

    // manejo de errores centralizado
    const status = error instanceof HttpError ? error.statusCode : 500;
    const message = error instanceof Error ? error.message : 'Error desconocido';
    
    res.status(status).json({
      timestamp: new Date().toISOString(),
      status,
      error: status === 500 ? 'Internal Server Error' : 'Bad Request',
      message,
      path: req.originalUrl || req.url
    });
  }
};
