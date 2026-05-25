import axios, { AxiosInstance, AxiosError } from 'axios';
import { env } from '../config/env'; // se importa las variables de entorno

// cliente HTTP centralizado para el servicio de autenticacion
const authHttpClient: AxiosInstance = axios.create({
  baseURL: env.AUTH_SERVICE_URL, // se apunta a la URL del servicio de autenticacion
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' },
});

// se define la estructura de datos que esperamos recibir de java
export interface TokenValidationResponse {
  valid: boolean;
  userId: string;
  role: string;
}

// funcion que valida el token con el servicio de autenticacion
// @param JWT recibido del usuario
// @return respuesta validada del auth service
export const validateToken = async (token: string): Promise<TokenValidationResponse> => {
  try {
    // realizamos un POST hacia el endpoint de validacion
    const { data } = await authHttpClient.post<TokenValidationResponse>(
      '/api/auth/validate',
      { token }
    );
    return data; // retornamos los datos si todo sale bien
  } catch (error) {
    // manero de errores de comunicacion o respuestas de error en Java
    const axiosError = error as AxiosError;
    const status = axiosError.response?.status;
    if (status === 401 || status === 403) {
      throw new Error('Token inválido o sin permisos');
    }
    throw new Error('Servicio de Autenticacion no disponible');
  }
};
