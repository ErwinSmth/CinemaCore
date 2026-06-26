-- =======================================================================
-- MIGRATION: V1__init_movie_schema.sql
-- =======================================================================

-- 1. Habilitar extensión para el índice de Trigramas (Búsqueda aproximada)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. Tabla de Catálogo: Restricciones de Edad
CREATE TABLE restriccion_edad (
    restriccion_id SERIAL PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    descripcion VARCHAR(100) NOT NULL
);

-- Insertar datos semilla de restricciones
INSERT INTO restriccion_edad (codigo, descripcion) VALUES
('APT', 'Apto para todo público'),
('7+', 'Mayores de 7 años'),
('12+', 'Mayores de 12 años'),
('14+', 'Mayores de 14 años'),
('16+', 'Mayores de 16 años'),
('18+', 'Mayores de 18 años');

-- 3. Tabla Principal: Películas
CREATE TABLE peliculas (
    peliculas_id BIGSERIAL PRIMARY KEY,
    tmdb_id INTEGER UNIQUE NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    sinopsis TEXT,
    duracion_min INTEGER,
    fecha_estreno DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'INACTIVO', -- INACTIVO, PRE-ESTRENO, CARTELERA, ELIMINADA, RETIRADA
    restriccion_id INTEGER REFERENCES restriccion_edad(restriccion_id),
    metadata JSONB -- Almacenará actores, directores, imágenes, trailers anidados
);

-- =======================================================================
-- ÍNDICES ESTRATÉGICOS DE RENDIMIENTO
-- =======================================================================

-- Índice B-Tree clásico para cruces de FK
CREATE INDEX idx_peliculas_restriccion ON peliculas(restriccion_id);

-- A. Índice de Trigramas (pg_trgm) para el Título
-- Permite búsquedas borrosas súper rápidas como "spidermn" -> "Spider-Man" usando el operador ILIKE o %
CREATE INDEX idx_movies_titulo_trgm ON peliculas USING GIN (titulo gin_trgm_ops);

-- B. Índice Parcial (Solo Cartelera)
-- Ocupa muy poca RAM porque solo indexa películas cuyo estado es 'CARTELERA'. Acelera el endpoint público.
CREATE INDEX idx_movies_cartelera_partial ON peliculas (estado) WHERE estado = 'CARTELERA';

-- C. Índice GIN para la Metadata JSONB
-- Permite hacer queries directamente dentro del documento JSON a la velocidad de la luz (ej. buscar un actor específico)
CREATE INDEX idx_movies_metadata_gin ON peliculas USING GIN (metadata);
