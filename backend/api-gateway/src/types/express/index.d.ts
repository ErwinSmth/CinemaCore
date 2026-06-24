import 'express';

declare module 'express-serve-static-core' {
  interface Request {
    // el '?' significa que la propiedad es opcional
    user?: {
      sub: string;
      roles: string[];
    };
  }
}
