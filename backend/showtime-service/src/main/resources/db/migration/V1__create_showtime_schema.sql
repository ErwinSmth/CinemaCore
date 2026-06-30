-- V1__create_showtime_schema.sql
-- Creación de las tablas iniciales para el microservicio de funciones (Showtime Service)

CREATE TABLE salas (
    sala_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    capacidad INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
);

CREATE TABLE proyeccion (
    proyeccion_id SERIAL PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE funcion (
    funcion_id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL, -- Referencia externa al Movie Service
    sala_id BIGINT NOT NULL,
    proyeccion_id INTEGER NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    precio_ticket DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id) REFERENCES salas(sala_id),
    CONSTRAINT fk_funcion_proyeccion FOREIGN KEY (proyeccion_id) REFERENCES proyeccion(proyeccion_id)
);

-- Índices recomendados para búsquedas frecuentes
CREATE INDEX idx_funcion_movie_id ON funcion(movie_id);
CREATE INDEX idx_funcion_sala_id ON funcion(sala_id);
CREATE INDEX idx_funcion_fecha_inicio ON funcion(fecha_inicio);

-- Inserts iniciales para el catálogo de proyección estático
INSERT INTO proyeccion (codigo, descripcion) VALUES
('2D', 'Proyección Digital 2D Standard'),
('3D', 'Proyección Digital 3D Estereoscópica'),
('IMAX', 'Proyección IMAX 2D'),
('IMAX-3D', 'Proyección IMAX 3D'),
('4DX', 'Proyección 4DX con movimiento y efectos');
