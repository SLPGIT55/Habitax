Changelog - Proyecto HABITAX
Todos los cambios notables en el proyecto Habitax serán documentados en este archivo.

[1.2.0] - 2024-05-21
Integración de API Real y Desplegables Dinámicos
Añadido
- Conexión Dinámica: Implementado endpoint /api/zonas en el controlador para realizar peticiones en tiempo real a la API de Idealista (v7) mediante RestTemplate.
- Normalización de Datos: Añadida lógica de procesamiento de texto con Normalizer para eliminar tildes y caracteres especiales en las búsquedas, asegurando la compatibilidad con los servidores de RapidAPI.
- Mapeo de JSON: Configurada la extracción de datos mediante la clave específica "locations" identificada tras el análisis de la respuesta bruta del servidor.

[1.1.0] - 2024-05-20 
Optimización de Seguridad y UX
Añadido
- Validación de Cliente: Implementada lógica en JavaScript para validar la robustez de la contraseña en el formulario de registro (8 caracteres, 1 número, 1 carácter especial).
- Feedback Visual: Añadido contenedor dinámico #passwordError en la vista de registro para notificar errores sin recargar la página.
- Modularización JS: Creado archivo externo validaciones.js en la carpeta static/js para seguir el principio de separación de responsabilidades.


[1.0.0] - 2024-05-10 (Lanzamiento Inicial)
Añadido
- Estructura base del proyecto con Spring Boot.
- Integración con la API de Idealista a través de RapidAPI.
- Sistema de persistencia en memoria con H2.
- Motor de vistas con Thymeleaf y estilos con Bootstrap.