import axios, { AxiosInstance, AxiosError } from 'axios';
import { env } from '../config/env'; // se importa las variables de entorno

// cliente HTTP centralizado para el servicio de autenticacion
const authHttpClient: AxiosInstance = axios.create({
  baseURL: env.AUTH_SERVICE_URL, // se apunta a la URL del servicio de autenticacion
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' },
});

// clase custom para preservar el status code del microservicio
export class HttpError extends Error {
  constructor(public statusCode: number, message: string) {
    super(message);
    this.name = 'HttpError';
  }
}

// se define la estructura de datos que esperamos recibir de java
export interface AuthResponse {
  token: string;
  email: string;
  roles: string[];
}

// funcion que autentica al usuario con el servicio de java
// @param email y contrasena recibidos del usuario
// @return respuesta con el token JWT
export const loginUser = async (email: string, contrasena: string): Promise<AuthResponse> => {
  try {
    // realizamos un POST hacia el endpoint de validacion
    const { data } = await authHttpClient.post<AuthResponse>(
      '/api/auth/login',
      { email, contrasena }
    );
    return data; // retornamos los datos si todo sale bien
  } catch (error) {
    // manero de errores de comunicacion o respuestas de error en Java
    const axiosError = error as AxiosError<{ message?: string }>;
    if (axiosError.response) {
      // extrae el mensaje de la clase ErrorResponse de Java y su status real
      const serverMessage = axiosError.response.data?.message || 'Error de autenticación';
      throw new HttpError(axiosError.response.status, serverMessage);
    }
    throw new HttpError(503, 'Servicio de Autenticacion no disponible');
  }
};

// estructura del cuerpo del registro
export interface RegisterRequest {
  email: string;
  contrasena: string;
  nombres: string;
  apellidos: string;
}

// funcion que registra un nuevo usuario en el servicio de java
// @param datos del formulario de registro
// @return respuesta con el token JWT y datos del usuario
export const registerUser = async (payload: RegisterRequest): Promise<AuthResponse> => {
  try {
    const { data } = await authHttpClient.post<AuthResponse>(
      '/api/auth/register',
      payload
    );
    return data;
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    if (axiosError.response) {
      const serverMessage = axiosError.response.data?.message || 'Error en el registro';
      throw new HttpError(axiosError.response.status, serverMessage);
    }
    throw new HttpError(503, 'Servicio de Autenticacion no disponible');
  }
};
