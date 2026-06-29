-- =======================================================================
-- MIGRATION: V3__fix_seed_passwords.sql
-- DESCRIPCIÓN: Corrige el hash BCrypt de los usuarios de prueba.
-- La contraseña por defecto para todos sigue siendo: 123456
-- =======================================================================

UPDATE usuarios 
SET contrasena = '$2a$10$ppfOWfexy1Af5WM48eiRs.npOE2HyKYSE2gLE9mUU7SuzJjXOIZWq' 
WHERE email IN ('admin@cinemacore.com', 'cliente@cinemacore.com', 'taquilla@cinemacore.com');
