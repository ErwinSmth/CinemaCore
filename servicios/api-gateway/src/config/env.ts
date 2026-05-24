import 'dotenv/config'; // carga las variables del archivo .env


// Funcion utilizada para leer variables de entorno de forma segura
const getEnvVar = (key: string, fallback?: string): string => {
  // process.env es un objeto global que contiene las variables de entorno del sistema
  const value = process.env[key] ?? fallback;

  // Condicional por si falta alguna variable, el sistema no arranca evitanto errores en ejecucion
  if (value === undefined) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
};

// Objeto donde se guardan las variables de entorno
export const env = {
  // Convertimos a entero las variables del puerto
  PORT: parseInt(getEnvVar('PORT', '3000'), 10),

  // URLs de los microservicios, para el gateway sepa a donde redirigir
  AUTH_SERVICE_URL: getEnvVar('AUTH_SERVICE_URL'),

  // Llave secreta para firmar el JWT
  JWT_SECRET: getEnvVar('JWT_SECRET'),
} as const;
