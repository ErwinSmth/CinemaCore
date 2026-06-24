DESCRIPCION DEL PROYECTO 

Este proyecto propone el desarrollo de un sistema web basado en microservicios para la gestión de venta de entradas de cine, integrando funcionalidades como cartelera, programación de funciones, selección de asientos y autenticación de usuarios. La solución busca optimizar la experiencia del cliente y mejorar la eficiencia operativa mediante el uso de tecnologías modernas como APIs REST, tokens JWT y procesamiento en tiempo real. Además, se enfoca en garantizar la seguridad, escalabilidad y disponibilidad del sistema. 

Tecnologías Utilizadas 

Componente 

Tecnología 

Descripción 

Backend 

Spring Boot 

Framework para el desarrollo de APIs REST, permite crear servicios escalables, seguros y de alto rendimiento. 

Frontend 

Angular + CSS 

Framework para construir interfaces dinámicas y responsivas, junto con CSS para el diseño visual. 

Base de Datos 

PostgreSQL 

Sistema de gestión de bases de datos relacional, robusto y confiable para el almacenamiento de información. 

Contenedores 

Docker 

Plataforma que permite empaquetar y ejecutar los servicios en contenedores, facilitando su despliegue y escalabilidad. 

 

FASE I: ANÁLISIS DE REQUERIMIENTOS: SISTEMA DE CINE 

I. ACTORES DEL SISTEMA Y NECESIDADES 

ACTOR 

NECESIDAD PRINCIPAL 

IMPACTO EN EL NEGOCIO 

Cliente / Usuario Final 

Consultar la cartelera actualizada, horarios disponibles y seleccionar asientos en tiempo real sin errores de duplicidad. 

Incremento en las ventas digitales y mejora drástica en la experiencia de usuario (UX). 

Administrador de Cine 

Gestionar el catálogo de películas (Movie Service) y configurar la asignación de salas y horarios (Showtime Service). 

Optimización de la ocupación de las salas y control eficiente de la oferta comercial. 

Taquillero (Personal de Local) 

Acceso inmediato al inventario de asientos para ventas presenciales y validación de estados (Disponible/Vendido). 

Reducción de colas en el cine físico y sincronización total con la venta online para evitar colisiones. 

Sistema de Pagos Externo 

Comunicación mediante API para confirmar transacciones y liberar o confirmar el bloqueo de asientos. 

Seguridad financiera y reducción de "asientos perdidos" por procesos de pago inconclusos. 

 

I.II ARQUITECTURA DE SERVICIOS AUTÓNOMOS 

Para el correcto funcionamiento del sistema de venta de entradas, se ha planteado una arquitectura basada en servicios que permite dividir las responsabilidades y mejorar la eficiencia operativa. Cada servicio cumple una función específica dentro del sistema, facilitando la gestión de la información, la escalabilidad y la disponibilidad en tiempo real. 

Esta estructura permite que los diferentes componentes trabajen de manera independiente pero integrada, asegurando una mejor experiencia para el usuario, así como una mayor capacidad de respuesta ante cambios en la demanda. A continuación, se describen los principales servicios que conforman el sistema. 

I.II.I Authentication Service (Servicio de Autenticación / Login) 

Función: Gestiona el proceso de autenticación de usuarios (Cliente, Administrador y Taquillero), validando credenciales y generando tokens de acceso seguros para el uso del sistema. 

Dinamismo: Utiliza el estándar OAuth 2.0 y tokens JWT para permitir autenticación sin estado (stateless), garantizando seguridad, escalabilidad y control de acceso en tiempo real entre los diferentes servicios del sistema. 

I.II.II Movie Service (servicio de películas) 

Función: Gestiona metadatos, sinopsis y recursos multimedia de la cartelera de forma centralizada. 

Dinamismo: Provee consultas de alta velocidad mediante bases documentales y capas de caché para respuestas instantáneas. 

I.II.III Showtime Service (Servicio de Funciones) 

Función: Vincula películas con salas, horarios y tipos de tecnología (2D, 3D, IMAX) disponibles. 

Dinamismo: Cruza la disponibilidad de salas con el catálogo para generar y ajustar la oferta comercial diaria. 

        I.II.IV Seat Inventory Service (Servicio de asientos) 

Función: Administra la distribución física y el estado de cada asiento (Disponible/Ocupado) en tiempo real. 

 

 

I.III REQUERIMIENTOS DEL SISTEMA 

I.III.I Requerimientos Funcionales (RF) 

1. Authentication Service (Servicio de Autenticación / Login) 

ID 

Requerimiento Específico 

Descripción 

RF-01 

Registro de Usuarios 

El sistema debe permitir el registro de nuevos usuarios con datos básicos y credenciales. 

RF-02 

Inicio de Sesión 

Permitir a los usuarios autenticarse mediante usuario y contraseña. 

RF-03 

Validación de Credenciales 

Verificar que las credenciales ingresadas sean correctas antes de otorgar acceso. 

RF-04 

Generación de token JWT 

Generar un token de acceso seguro (JWT) al iniciar sesión correctamente. 

RF-05 

Gestión de Roles 

Asignar y validar roles de usuario (Administrador, Cliente, Taquillero). 

RF-06 

Cierre de Sesión 

Permitir al usuario cerrar sesión invalidando su token de acceso. 

 

2. Movie Service (Servicio de Películas) 

ID 

Requerimiento Específico 

Descripción 

RF-07 

Registro de Películas 

El sistema debe permitir registrar títulos, sinopsis, duración y clasificación. 

RF-08 

Actualización de Contenido 

Permitir editar información de películas y recursos multimedia. 

RF-09 

Eliminación de Películas 

Permitir eliminar películas que ya no estén en cartelera. 

RF-10 

Gestión de Multimedia 

Permitir cargar imágenes, trailers y contenido relacionado. 

RF-11 

Consulta de Cartelera 

Permitir listar películas disponibles en tiempo real. 

RF-12 

Búsqueda y Filtro 

Permitir filtrar películas por género, duración o clasificación. 

RF-13 

Cacheo de Consultas 

Proveer consultas rápidas mediante almacenamiento en caché. 

 

 

3. Showtime Service(Servicio de funciones) 

ID 

Requerimiento Específico 

Descripción 

RF-14 

Creación de Funciones 

Permitir programar funciones asignando película, sala y horario. 

RF-15 

Modificación de Funciones 

Permitir actualizar horarios o salas de funciones existentes. 

RF-16 

Eliminación de Funciones 

Permitir eliminar funciones programadas. 

RF-17 

Validación de Horarios 

Evitar traslapes de horarios en una misma sala. 

RF-18 

Asignación de Tecnología 

Permitir definir tipo de proyección (2D, 3D, IMAX). 

RF-19 

Consulta de Funciones 

Permitir visualizar funciones disponibles por fecha y película. 

 

4.Seat Inventory Service (Servicio de Reserva de asientos) 

ID 

Requerimiento Específico 

Descripción 

RF-20 

Visualización de Asientos 

Mostrar el estado de asientos en tiempo real (Disponible/Ocupado). 

RF-21 

Selección de Asientos 

Permitir al usuario seleccionar asientos disponibles. 

RF-22 

Bloqueo Temporal de asientos 

Reservar asientos temporalmente durante el proceso de pago. 

RF-23 

Liberación de Asientos 

Liberar asientos si el pago no se completa en un tiempo determinado. 

RF-24 

Confirmación de Compra 

Marcar asientos como ocupados tras pago exitoso. 

RF-25 

Sincronización en Tiempo Real 

Actualizar el estado de asientos en la plataforma. 

 

I.III.II Requerimientos No Funcionales (RNF) 

AREA FUNCIONAL 

ID 

REQUERIMIENTO ESPECIFICO 

DESCRIPCION 

USABILIDAD 

RNF-01 

Interfaz Adaptable (Responsive) 

El sistema web debe renderizarse correctamente y mantener el 100% de operatividad en resoluciones móviles (desde 360px), tablets y pantallas de escritorio o terminales táctiles de taquilla. 

RNF-02 

Experiencia de Selección Visual 

El mapa interactivo de butacas debe cargar su estado de disponibilidad en un lapso aceptable, garantizando una renderización fluida en el cliente web. 

SEGURIDAD 

RNF-03 

Autenticación y Roles de Acceso 

Uso del estándar OAuth 2.0 y tokens JWT (JSON Web Tokens) para una validación sin estado (stateless) entre los microservicios, asegurando los accesos para Administrador, Cliente y Taquillero. 

RNF-04 

Integridad y Cifrado de contraseñas 

Todo el tráfico de red externo debe estar blindado bajo el protocolo TLS 1.2 o superior (reemplazo moderno de SSL). Se ejecutarán backups incrementales diarios de las bases de datos. 

RENDIMIENTO 

RNF-05 

Alta Concurrencia y Escalabilidad 

Durante estrenos, el API Gateway debe soportar un pico de almenos 1000 peticiones concurrentes sin degradar el tiempo de respuesta promedio por encima de los 500 milisegundos, garantizando la estabilidad del sistema. 

INFRAESTRUCTURA 

RNF-06 

Arquitectura de Contenedores 

Todos los servicios backend y frontend se aislarán en imágenes Docker ligeras, facilitando la orquestación local con Docker Compose. 

DISPONIBILIDAD 

RNF-07 

Tolerancia a Fallos parciales 

Si un servicio no crítico falla, el core transaccional de venta de boletos en Spring debe seguir operando con normalidad. 

 

 

 

   I.IV CASOS DE USO DEL SISTEMA 

ID 

Caso de Uso 

Actores 

Descripción 

CU01 

Registrar nueva película 

Administrador 

Permite registrar una nueva película con sus datos básicos como título, sinopsis, duración y clasificación. 

CU02 

Actualizar metadatos y multimedia 

Administrador 

Permite editar la información de la película y actualizar imágenes, trailers u otros recursos. 

CU03 

Eliminar película 

Administrador 

Permite eliminar películas que ya no estarán disponibles en cartelera. 

CU04 

Gestionar clasificaciones 

Administrador 

Permite asignar o modificar la clasificación de las películas (edad, género, etc.). 

CU05 

Asignar sala y horario 

Administrador 

Permite programar funciones vinculando película, sala y horario. 

CU06 

Definir formato de proyección 

Administrador 

Permite configurar el tipo de proyección (2D, 3D, IMAX) para cada función. 

CU07 

Ver cartelera actualizada 

Cliente / Usuario 

Permite visualizar la lista de películas disponibles en tiempo real. 

CU08 

Consultar detalles de película 

Cliente / Usuario 

Permite ver información detallada de una película (sinopsis, duración, horarios). 

CU09 

Buscar y filtrar películas 

Cliente / Usuario 

Permite buscar películas por género, duración o clasificación. 

CU10 

Consultar funciones disponibles 

Cliente / Usuario 

Permite visualizar funciones por fecha, sala y película. 

CU11 

Seleccionar horario y sala 

Cliente / Usuario 

Permite elegir una función específica según horario y sala disponible. 

CU12 

Visualizar mapa de asientos 

Cliente / Usuario / Taquillero 

Muestra el estado de los asientos en tiempo real (disponible u ocupado). 

CU13 

Seleccionar asientos 

Cliente / Usuario 

Permite seleccionar asientos disponibles para la compra. 

CU14 

Realizar pago 

Cliente / Usuario 

Permite procesar el pago mediante integración con pasarela de pago. 

CU15 

Iniciar sesión 

Cliente / Administrador / Taquillero 

Permite autenticarse en el sistema mediante credenciales válidas. 

CU16 

Registrar usuario 

Cliente 

Permite crear una nueva cuenta en el sistema. 

CU17 

Cerrar sesión 

Cliente / Administrador / Taquillero 

Permite finalizar la sesión del usuario en el sistema. 

CU18 

Venta presencial de entradas 

Taquillero 

Permite registrar la venta de entradas directamente en el punto físico. 

CU19 

Imprimir comprobante 

Taquillero 

Permite generar e imprimir el ticket de compra para el cliente. 

CU20 

Bloquear asientos por incidencia 

Taquillero 

Permite bloquear asientos por mantenimiento o fallas técnicas. 

 

 
 I.VI INTERACCIÓN ENTRE SERVICIOS 

Escenario Operativo 

Servicio Iniciador 

Acción Técnica 

Servicio Destino 

Consulta de Cartelera 

Cliente -- Movie Service 

Solicita la lista de películas activas y sus metadatos para mostrar la cartelera. 

Movie Service 

Consulta de Funciones 

Movie Service 

Solicita los horarios disponibles de cada película seleccionada. 

Showtime Service 

Programación de Función 

Administrador -- Showtime Service 

Vincula película, sala, horario y tipo de proyección validando disponibilidad. 

Movie Service 

Validación de Horarios 

Showtime Service 

Verifica que no existan traslapes en la programación de salas. 

(Interno) 

Visualización de Asientos 

Cliente -- Showtime Service 

Solicita la estructura de la sala y función seleccionada. 

Seat Service 

Selección de Asientos 

Cliente -- Seat Service 

Consulta y muestra el estado actual de los asientos en tiempo real. 

Seat Service 

Bloqueo de Asientos 

Seat Service 

Bloquea temporalmente los asientos seleccionados durante el proceso de compra. 

(Interno - Redis) 

Confirmación de Compra 

Seat Service 

Cambia el estado de los asientos de “Bloqueado” a “Vendido” tras pago exitoso. 

Seat Service 

Proceso de Pago 

Cliente -- Authentication Service 

Valida identidad del usuario antes de procesar la compra. 

Authentication Service 

Integración de Pago 

Sistema 

Envía solicitud de pago y recibe confirmación de la pasarela de pago. 

Pasarela de Pagote 

Registro de Venta 

Seat Service 

Notifica la venta confirmada para fines de control y análisis. 

(Opcional: Reportes) 

Inicio de Sesión 

Cliente -- Authentication Service 

Envía credenciales y recibe token JWT para acceso al sistema. 

Authentication Service 

 

 

 

 