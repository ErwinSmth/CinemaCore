import 'dotenv/config';

const getEnvVar = (key: string, fallback?: string): string => {
  const value = process.env[key] ?? fallback;
  if (value === undefined) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
};

export const env = {
  PORT: parseInt(getEnvVar('PORT', '3000'), 10),
  AUTH_SERVICE_URL: getEnvVar('AUTH_SERVICE_URL'),
  MOVIE_SERVICE_URL: getEnvVar('MOVIE_SERVICE_URL'),
  JWT_SECRET: getEnvVar('JWT_SECRET'),
} as const;
