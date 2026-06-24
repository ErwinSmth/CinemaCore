import request from 'supertest';
import app from '../index';
import { loginUser, registerUser, HttpError } from '../clients/auth.client';

// Mockeamos modulos externos para evitar errores de ESM y aislar las pruebas
jest.mock('http-proxy-middleware', () => ({
  createProxyMiddleware: jest.fn(() => (req: any, res: any, next: any) => next())
}));
jest.mock('../clients/auth.client', () => {
  const originalModule = jest.requireActual('../clients/auth.client');
  return {
    ...originalModule,
    loginUser: jest.fn(),
    registerUser: jest.fn()
  };
});

describe('Auth Routes (API Gateway E2E)', () => {
  
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /api/v1/auth/login', () => {

    it('deberia retornar 400 si falta el campo contrasena (OpenAPI Validation)', async () => {
      const payload = {
        email: "test@correo.com"
        // Falta la contraseña
      };

      const res = await request(app)
        .post('/api/v1/auth/login')
        .send(payload)
        .set('Accept', 'application/json');

      expect(res.status).toBe(400);
      expect(res.body).toHaveProperty('message');
      expect(res.body.message).toContain("request/body must have required property 'contrasena'");
      // Aseguramos que NUNCA llego a llamar al backend
      expect(loginUser).not.toHaveBeenCalled();
    });

    it('deberia retornar 200 y el token si el login es exitoso', async () => {
      const mockSession = {
        token: 'fake-jwt-token',
        email: 'test@correo.com',
        roles: ['ROLE_CLIENTE']
      };

      // Simulamos la respuesta exitosa del backend
      (loginUser as jest.Mock).mockResolvedValue(mockSession);

      const payload = {
        email: "test@correo.com",
        contrasena: "Secure123*"
      };

      const res = await request(app)
        .post('/api/v1/auth/login')
        .send(payload)
        .set('Accept', 'application/json');

      expect(res.status).toBe(200);
      expect(res.body).toEqual(mockSession);
      expect(loginUser).toHaveBeenCalledWith('test@correo.com', 'Secure123*');
    });

    it('deberia retornar 401 si el backend dice que las credenciales son invalidas', async () => {
      const mockError = new HttpError(401, 'Credenciales incorrectas');
      (loginUser as jest.Mock).mockRejectedValue(mockError);

      const payload = {
        email: "wrong@correo.com",
        contrasena: "BadPass123*"
      };

      const res = await request(app)
        .post('/api/v1/auth/login')
        .send(payload)
        .set('Accept', 'application/json');

      expect(res.status).toBe(401);
      expect(res.body).toHaveProperty('error', 'Bad Request'); // De acuerdo al auth.controller.ts linea 26/54
      expect(res.body).toHaveProperty('message', 'Credenciales incorrectas');
    });
  });

  describe('POST /api/v1/auth/register', () => {

    it('deberia retornar 400 si la validacion de esquema falla', async () => {
      const payload = {
        email: "test@correo.com",
        contrasena: "Secure123*"
        // Faltan nombres y apellidos
      };

      const res = await request(app)
        .post('/api/v1/auth/register')
        .send(payload);

      expect(res.status).toBe(400);
      expect(res.body.message).toContain("request/body must have required property 'nombres'");
      expect(registerUser).not.toHaveBeenCalled();
    });

    it('deberia retornar 201 y enrutar correctamente si el payload es valido', async () => {
      const payload = {
        email: "nuevo@correo.com",
        contrasena: "Secure123*",
        nombres: "Juan",
        apellidos: "Perez"
      };

      const mockResponse = {
        token: 'new-jwt-token',
        email: 'nuevo@correo.com',
        roles: ['ROLE_CLIENTE']
      };

      (registerUser as jest.Mock).mockResolvedValue(mockResponse);

      const res = await request(app)
        .post('/api/v1/auth/register')
        .send(payload);

      expect(res.status).toBe(201);
      expect(res.body).toEqual(mockResponse);
      expect(registerUser).toHaveBeenCalledWith(payload);
    });

  });
});
