import { Request, Response, NextFunction } from 'express';
import { authMiddleware } from './auth.middleware';
import jwt from 'jsonwebtoken';
import { env } from '../config/env';

// Mock jsonwebtoken
jest.mock('jsonwebtoken');

describe('Auth Middleware', () => {
  let mockRequest: Partial<Request>;
  let mockResponse: Partial<Response>;
  let nextFunction: NextFunction;

  beforeEach(() => {
    mockRequest = {
      headers: {},
      url: '/api/v1/test'
    };
    mockResponse = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn()
    };
    nextFunction = jest.fn();
    jest.clearAllMocks();
  });

  it('deberia retornar 401 si no hay cabecera de autorizacion', () => {
    authMiddleware(mockRequest as Request, mockResponse as Response, nextFunction);

    expect(mockResponse.status).toHaveBeenCalledWith(401);
    expect(mockResponse.json).toHaveBeenCalledWith(expect.objectContaining({
      error: 'Unauthorized',
      message: 'No Autorizado: No se ha proporcionado ningun token'
    }));
    expect(nextFunction).not.toHaveBeenCalled();
  });

  it('deberia retornar 401 si la cabecera esta mal formada (sin Bearer)', () => {
    mockRequest.headers = { authorization: 'Basic token123' };
    
    authMiddleware(mockRequest as Request, mockResponse as Response, nextFunction);

    expect(mockResponse.status).toHaveBeenCalledWith(401);
    expect(nextFunction).not.toHaveBeenCalled();
  });

  it('deberia inyectar X-User-Id y X-User-Roles y llamar a next() si el token es valido', () => {
    mockRequest.headers = { authorization: 'Bearer valid-token' };
    
    const mockDecoded = { sub: 'test@cinestar.com', roles: ['ROLE_CLIENTE'] };
    (jwt.verify as jest.Mock).mockReturnValue(mockDecoded);

    authMiddleware(mockRequest as Request, mockResponse as Response, nextFunction);

    expect(jwt.verify).toHaveBeenCalledWith('valid-token', env.JWT_SECRET);
    expect(mockRequest.headers['x-user-id']).toBe('test@cinestar.com');
    expect(mockRequest.headers['x-user-roles']).toBe('ROLE_CLIENTE');
    expect(nextFunction).toHaveBeenCalled();
  });

  it('deberia retornar 401 si falla la verificacion del token (expirado/invalido)', () => {
    mockRequest.headers = { authorization: 'Bearer invalid-token' };
    (jwt.verify as jest.Mock).mockImplementation(() => { throw new Error('Invalid token'); });

    authMiddleware(mockRequest as Request, mockResponse as Response, nextFunction);

    expect(mockResponse.status).toHaveBeenCalledWith(401);
    expect(mockResponse.json).toHaveBeenCalledWith(expect.objectContaining({
      error: 'Unauthorized',
      message: 'Token invalido o expirado'
    }));
    expect(nextFunction).not.toHaveBeenCalled();
  });
});
