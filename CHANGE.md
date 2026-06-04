Changelog - Proyecto HABITAX
Todos los cambios notables en el proyecto Habitax serán documentados en este archivo.

[1.5.0] - 2026-06-04
### Remediación de Vulnerabilidades y Sincronización de Capas

## Añadido
- Control de Fuerza Bruta: Integración del componente LoginRateLimiterService en el flujo de autenticación, limitando el acceso en memoria por dirección de correo electrónico a un máximo de 5 intentos fallidos antes de aplicar un bloqueo temporal de 15 minutos (Cumplimiento Auditoría Tarea 2.6).

- Endpoint Nativo de Login: Desarrollo del método de procesamiento @PostMapping("/login") manual tras la remoción del framework automático de seguridad corporativa, permitiendo la validación directa contra el repositorio de persistencia.

## Cambiado
- Intercambio Seguro de Datos: Migración del almacenamiento de estado de sesión desde la entidad pesada Usuario hacia la estructura optimizada UsuarioSesionDTO, previniendo la exposición interna de hashes de contraseñas hacia el motor de renderizado de la interfaz (Cumplimiento Auditoría Tarea 2.7).

- Extracción Dinámica Multiprovincia: Rediseño completo del flujo de asignación de variables en el método de actualización del panel del perfil. Se sustituyeron los valores de fallback estáticos por la lectura dinámica de los campos guardados en la tabla de favoritos, ampliando el soporte de recálculo en vivo a cualquier región de España de forma automática.

## Corregido
- Excepción de Conversión de Clases: Solventado error crítico 500 (java.lang.ClassCastException) en las rutas del perfil del usuario, alineando los tipos de objetos recuperados mediante el atributo "usuarioLogueado" en la HttpSession.

- Malformación de Peticiones HTTP: Subsanado el fallo de red 400 Bad Request (Missing required parameters) producido al contactar los servidores externos de RapidAPI. Se corrigió la inversión de parámetros en el mapeo cruzado de las columnas zona y barrio del formulario Thymeleaf, asegurando que las dimensiones físicas (m²) y geográficas viajen alineadas al servicio web.

- Consistencia del Renderizado: Corrección del fallo de parseo de plantillas (Template Parsing Error) mediante la reinyección forzada del árbol de atributos del modelo (nombreUsuario, favoritos e historial) tras la ejecución de peticiones POST de recálculo, evitando la interrupción visual de la UI.

[1.4.0] - 2026-05-15
### Ecosistema de Usuario y Persistencia de Favoritos

## Añadido
- Módulo de Favoritos: Implementación de la entidad Favorito con persistencia en H2 y creación del endpoint /favoritos/guardar para almacenar métricas clave (zona, metros y precio estimado).
- Algoritmo de Segmentación: Añadida lógica de cálculo en el servidor para generar tres niveles de precio dinámicos: Oportunidad (-15%), Precio Medio (Base) y Premium (+25%).
- Recálculo en Caliente: Implementado endpoint /recalculate que permite actualizar el valor de mercado de un favorito guardado, integrando la lógica de Caché Temporal (1 hora) desarrollada en la versión anterior.
- Historial Visual: Integración de la lista de Últimas 3 Consultas en el panel principal mediante PageRequest para optimizar el rendimiento de la base de datos.

## Cambiado
- Arquitectura de UI: Refactorización de index.html hacia una estructura simétrica de tres columnas (col-lg-3 | 6 | 3) para mejorar la experiencia de usuario y el equilibrio visual de los edificios laterales.
- Robustez de Thymeleaf: Implementadas directivas th:if de seguridad en los fragmentos de resultados para evitar excepciones de renderizado (Whitelabel Error Page) ante variables nulas.

## Corregido
- Conflictos de Mapeo: Resuelto error de Ambiguous Mapping mediante la unificación de métodos @PostMapping duplicados en el controlador.
- Integridad de Datos: Eliminación de registros duplicados en la tabla de usuarios mediante la aplicación de restricciones de unicidad en el campo email

[1.2.0] - 2024-05-21
### Integración de API Real y Desplegables Dinámicos
## Añadido
- Conexión Dinámica: Implementado endpoint /api/zonas en el controlador para realizar peticiones en tiempo real a la API de Idealista (v7) mediante RestTemplate.
- Normalización de Datos: Añadida lógica de procesamiento de texto con Normalizer para eliminar tildes y caracteres especiales en las búsquedas, asegurando la compatibilidad con los servidores de RapidAPI.
- Mapeo de JSON: Configurada la extracción de datos mediante la clave específica "locations" identificada tras el análisis de la respuesta bruta del servidor.

[1.1.0] - 2024-05-20 
### Optimización de Seguridad y UX
## Añadido
- Validación de Cliente: Implementada lógica en JavaScript para validar la robustez de la contraseña en el formulario de registro (8 caracteres, 1 número, 1 carácter especial).
- Feedback Visual: Añadido contenedor dinámico #passwordError en la vista de registro para notificar errores sin recargar la página.
- Modularización JS: Creado archivo externo validaciones.js en la carpeta static/js para seguir el principio de separación de responsabilidades.


[1.0.0] - 2024-05-10 (Lanzamiento Inicial)
## Añadido
- Estructura base del proyecto con Spring Boot.
- Integración con la API de Idealista a través de RapidAPI.
- Sistema de persistencia en memoria con H2.
- Motor de vistas con Thymeleaf y estilos con Bootstrap.