-- =======================================================================
-- MIGRATION: V2__seed_users.sql
-- DESCRIPCIÓN: Inserta usuarios iniciales para pruebas del sistema.
-- La contraseña por defecto para todos es: 123456
-- (Hash BCrypt: $2a$10$XURPShQNCsLjp1ESc2laoO49BPZfHiITj7rLzJq/3MqwI/p2U4Xz6)
-- =======================================================================

-- 1. Insertar Usuarios
INSERT INTO usuarios (email, contrasena, nombres, apellidos, estado) VALUES 
('admin@cinemacore.com', '$2a$10$XURPShQNCsLjp1ESc2laoO49BPZfHiITj7rLzJq/3MqwI/p2U4Xz6', 'Admin', 'Principal', TRUE),
('cliente@cinemacore.com', '$2a$10$XURPShQNCsLjp1ESc2laoO49BPZfHiITj7rLzJq/3MqwI/p2U4Xz6', 'Cliente', 'Frecuente', TRUE),
('taquilla@cinemacore.com', '$2a$10$XURPShQNCsLjp1ESc2laoO49BPZfHiITj7rLzJq/3MqwI/p2U4Xz6', 'Carlos', 'Taquillero', TRUE);

-- 2. Asignar Roles a los Usuarios
-- Los roles ya fueron insertados en V1: 1=ROLE_CLIENTE, 2=ROLE_ADMINISTRADOR, 3=ROLE_TAQUILLERO

-- admin@cinemacore.com -> ROLE_ADMINISTRADOR (2)
INSERT INTO user_role (user_id, rol_id) 
SELECT user_id, 2 FROM usuarios WHERE email = 'admin@cinemacore.com';

-- cliente@cinemacore.com -> ROLE_CLIENTE (1)
INSERT INTO user_role (user_id, rol_id) 
SELECT user_id, 1 FROM usuarios WHERE email = 'cliente@cinemacore.com';

-- taquilla@cinemacore.com -> ROLE_TAQUILLERO (3)
INSERT INTO user_role (user_id, rol_id) 
SELECT user_id, 3 FROM usuarios WHERE email = 'taquilla@cinemacore.com';
