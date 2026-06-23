📄 DOCUMENTO DE ARQUITECTURA: Flujo de Gestión de Cartelera (Movie Service)
Fase 1: La Búsqueda Externa (El Administrador explora)
Intención: El Administrador del cine ingresa a su panel privado (Web Admin) y navega a la sección "Agregar* Nueva Pel*ícula".
Acción: Ingresa el nombre de la película (ej. "Obsesión") en la barra de búsqueda.
Por debajo (Back/Front): El Frontend envía esta palabra al Backend. El Backend actúa como puente y le pregunta al catálogo mundial (TMDB). TMDB le devuelve una lista general de coincidencias, que el Backend reenvía al Frontend.
Resultado Visual: El Administrador ve en su pantalla una cuadrícula con varias opciones de películas que coinciden con ese nombre, mostrando el año y la portada básica para ayudarle a diferenciar (ej. "Obsesión de 1976" vs "Obsesión de 2026").
Fase 2: La Selección y Curación Local (El Administrador personaliza)
Acción: El Administrador identifica la película correcta y hace clic en ella (botón "Importar al Cine").
Por debajo (Back/Front): El Backend va rápidamente a TMDB y descarga absolutamente todo sobre esa película específica (elenco completo, docenas de portadas, todos los trailers disponibles).
Resultado Visual (Formulario de Curación): La pantalla del Administrador cambia. Ahora ve un formulario pre-llenado donde:
El título y la sinopsis ya están escritos automáticamente (pero puede corregirlos si lo desea).
Selecciona de una galería cuál será el Póster Oficial para el cine.
Marca qué Trailers se mostrarán.
Aporte Local (Crucial): El Administrador elige en un menú desplegable la Restricción de Edad según la ley peruana (APT, +14, +18) y define el Estado de la película (Ej. "PRE-ESTRENO" o "CARTELERA").
Fase 3: La Persistencia (Guardado en la Base de Datos)
Acción: El Administrador hace clic en "Guardar en Base de Datos".
Por debajo (Back/Front): El Frontend empaqueta solo las decisiones finales del Administrador y las envía al Backend.
Impacto en BD (**db_movies**): El Backend guarda esta información de forma definitiva en tu base de datos PostgreSQL local. Los datos de búsqueda rápida (título, duración, estado, llave foránea de edad) van a columnas tradicionales, mientras que el resto de información variable (urls de la imagen elegida, urls de los trailers, actores) se comprime y guarda dentro de la columna inteligente JSONB.
A partir de este momento, el cine es dueño de su información y ya no depende de TMDB para saber qué proyectar.
Fase 4: La Visualización del Cliente (Consumo Optimizado)
Intención: Un Cliente final (usuario normal) ingresa a la página pública de Cinestar (www.cinestar.com) desde su celular para ver qué hay hoy en el cine.
Por debajo (Back/Front): El Frontend del cliente le pide al Backend únicamente las películas cuyo estado local sea "CARTELERA".
Respuesta Rápida: El Backend (Movie Service) hace una consulta rapidísima a la base de datos, toma el JSONB de las películas y se lo envía al celular del cliente. El Backend no envía imágenes pesadas, solo envía texto y enlaces.
Resultado Visual: El celular del cliente recibe los enlaces y, usando el internet del propio cliente, descarga las imágenes directamente desde los servidores de TMDB. El cliente ve una cartelera fluida, hermosa, con el elenco y trailers, mientras que tu servidor Backend se mantiene totalmente relajado y sin gastar ancho de banda excesivo.
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
📄 Documentación: Estrategia de Concurrencia y Reserva de Asientos
![[Pasted image 20260602132326.png]]

1. El Ciclo de Vida del Ticket (Pre-generación)
Regla de Arquitectura: Los registros en la tabla tickets NO se crean en el momento en que el usuario hace clic en un asiento.

¿Cuándo se crean? Se pre-generan en bloque en el momento en que el Administrador crea la función (Showtime).
Ejemplo: Si el Admin programa Spider-Man en la Sala 1 (100 butacas), el sistema hace un INSERT de 100 filas en la tabla tickets automáticamente.
Estado Inicial: Todas las filas nacen con user_id = null, status = 'AVAILABLE' y version = 0.
Beneficio: Al pre-existir la fila, cuando múltiples usuarios intentan comprar al mismo tiempo, el sistema solo compite por hacer un UPDATE, permitiendo que las herramientas de bloqueo de la base de datos funcionen a la perfección.
2. Anatomía de las Columnas Especiales
La tabla tickets posee 3 columnas encargadas de orquestar la transacción:

A. status (El Semáforo del Negocio)
Qué es: Es una etiqueta de texto (AVAILABLE, LOCKED, SOLD).
Para qué sirve: Le indica al Frontend de qué color pintar la butaca (Verde = Libre, Amarillo = En proceso de pago, Gris = Vendido). A la base de datos no le importa el valor de este texto.
B. version (El Escudo de la Base de Datos - Optimistic Locking)
Qué es: Es un contador numérico interno controlado nativamente por JPA/Hibernate (@Version).
Para qué sirve: Impide matemáticamente que dos transacciones modifiquen la misma fila al mismo tiempo. Es invisible para el Frontend y para el usuario.
¿Es lo mismo que el **status**?: NO. * Analogía del Baño Público: El status es el letrero de plástico por fuera que dice "Libre / Ocupado" (para que la gente lo vea). La version es la cerradura de metal por dentro que traba la puerta e impide físicamente que alguien más entre (para evitar choques).
C. locked_until (El Temporizador de Abandono)
Qué es: Un campo de fecha y hora (TIMESTAMP).
Para qué sirve: Define el tiempo límite que un asiento puede permanecer en estado LOCKED. Si el usuario no completa el pago (ej. cierra el navegador), el Backend utilizará esta hora para saber si ya pasaron los 5 minutos de gracia y puede devolver la butaca a estado AVAILABLE.
3. Caso Práctico de Colisión: El Escenario "Juan vs. María"
¿Qué sucede cuando dos personas intentan comprar la misma butaca en el mismo milisegundo?

Minuto 0: El Escenario

Juan y María están en sus casas viendo la cartelera.
Ambos ven el asiento "G-14" en color verde.
Su Frontend tiene el dato en memoria: Asiento G-14 | status: AVAILABLE | version: 1.
Minuto 1: El Choque (Race Condition)

Ambos hacen clic en "Comprar G-14" casi al unísono. Hay solo 2 milisegundos de diferencia entre la petición de Juan y la de María.
Resolución en el Backend (PostgreSQL + Spring Boot):

Petición de Juan (Llega primero):
El Backend envía a la BD: "Actualiza el asiento G-14 a LOCKED, suma 5 min a locked_until, y cambia la versión a 2, SOLO SI la versión actual sigue siendo 1".
PostgreSQL dice: "La versión es 1. Perfecto. Filas actualizadas: 1".
Resultado: Juan avanza a la pasarela de pagos.
Petición de María (Llega 2 milisegundos tarde):
El Backend envía la misma orden exacta: "Actualiza el asiento G-14 a LOCKED, y cambia la versión a 2, SOLO SI la versión actual sigue siendo 1".
PostgreSQL busca el asiento G-14 con versión 1. Pero ya no lo encuentra, porque Juan lo acaba de cambiar a versión 2.
PostgreSQL responde: "Filas actualizadas: 0".
La Excepción:
Spring Boot detecta que se afectaron 0 filas y lanza una OptimisticLockException.
El Backend intercepta este error y le devuelve una respuesta limpia al Frontend de María: "Lo sentimos, este asiento acaba de ser tomado por otro usuario".

