Documento Tecnico de Respuesta de la Api de TMDB
Fase 1: Petición de Búsqueda (Search)
Objetivo: Obtener una lista de coincidencias según lo que el Administrador escribió en el buscador.

Endpoint (Ruta): GET https://api.themoviedb.org/3/search/movie

Parámetros (Query Params): api_key={TU_CLAVE}&query=obsesion&language=es-MX

1. ¿Cómo te responde TMDB? (Ejemplo JSON simplificado)
Te devuelve un objeto con un arreglo llamado results.

{
  "page": 1,
  "results": [
    {
      "id": 1339713,
      "title": "Obsesión",
      "release_date": "2026-04-16",
      "poster_path": "/rmCkNtzYR2xTOO3ZXmIqB5zgYdE.jpg"
    },
    {
      "id": 4780,
      "title": "Obsession",
      "release_date": "1976-08-01",
      "poster_path": "/9160yxML0m3XiBdwdfrJwBlS356.jpg"
    }
  ]
}
2. ¿Qué dato NECESITAS de aquí para la segunda petición?
Lo único que tu sistema necesita capturar cuando el Administrador haga clic en una película de esta lista es el **id** (ejemplo: 1339713). Ese número es la llave para traer todo lo demás.

Fase 2: Petición de Detalle Completo (Details + Append)
Objetivo: Traer toda la información de la película seleccionada en un solo viaje.

Endpoint (Ruta): GET https://api.themoviedb.org/3/movie/1339713

Parámetros (Query Params): api_key={TU_CLAVE}&language=es-MX&append_to_response=credits,videos,images

1. ¿Cómo te responde TMDB? (Ejemplo JSON Estructural)
{
  "id": 1339713,
  "title": "Obsesión",
  "overview": "Tras romper el misterioso Sauce de un Deseo...",
  "release_date": "2026-04-16",
  "runtime": 120,
  "credits": {
    "cast": [
      {
        "name": "Tom Holland",
        "character": "Peter Parker"
      }
    ],
    "crew": [
      {
        "name": "Jon Watts",
        "job": "Director"
      }
    ]
  },
  "videos": {
    "results": [
      {
        "name": "Trailer Oficial Subtitulado",
        "key": "1mTjfMFyPi8",
        "site": "YouTube",
        "type": "Trailer"
      }
    ]
  },
  "images": {
    "posters": [
      {
        "file_path": "/rmCkNtzYR2xTOO3ZXmIqB5zgYdE.jpg"
      }
    ]
  }
}
2. Lista Exacta de Datos a Extraer (Mapeo)
Esta es la lista de campos que tu Backend debe leer de ese JSON:

**id** (Número): El identificador único de TMDB.
**title** (String): El Título oficial de la película en español.
**overview** (String): La Sinopsis completa.
**runtime** (Número): La Duración en minutos (vital para el Showtime Service).
**release_date** (String): La Fecha de estreno (formato YYYY-MM-DD).
**credits.cast[].name** (String): Nombres de los actores principales (puedes tomar los primeros 3 o 5 elementos del arreglo cast).
**credits.crew[].name** (String): Nombre del director. Tienes que iterar el arreglo crew y buscar el objeto donde el campo job sea exactamente igual a "Director".
Fase 3: Reconstrucción de Recursos Multimedia (¡Lo más importante!)
TMDB nunca te dará la URL lista para usar. Tu Backend tiene que armarla concatenando textos. Aquí tienes las fórmulas exactas que programarás:

A. Construcción de la URL de Portadas (Imágenes)
De la respuesta anterior, vas al bloque "images" -> "posters". Extraes el valor de **file_path** (ej. "/rmCkNtzYR2xTOO3ZXmIqB5zgYdE.jpg").

Fórmula: [BASE_URL] + [TAMAÑO] + [FILE_PATH]
Componentes:
BASE_URL: Siempre será https://image.tmdb.org/t/p/
TAMAÑO: Para web, el estándar recomendado es w500 (500 píxeles de ancho). Si la quieres original pon original.
Código/String Final Generado:https://image.tmdb.org/t/p/w500/rmCkNtzYR2xTOO3ZXmIqB5zgYdE.jpg
B. Construcción de la URL de Trailers (Videos)
De la respuesta anterior, vas al bloque "videos" -> "results". Aquí es obligatorio aplicar un filtro por código antes de armar la URL:

Filtro: Iteras el arreglo y solo te quedas con los objetos donde site == "YouTube" Y type == "Trailer".
Una vez filtrado, extraes el valor de **key** (ej. "1mTjfMFyPi8").
Fórmula: [URL_YOUTUBE] + [KEY]
Componentes:
URL_YOUTUBE: Siempre será https://www.youtube.com/watch?v=
Código/String Final Generado:https://www.youtube.com/watch?v=1mTjfMFyPi8
