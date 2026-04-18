Changelog - Proyecto HABITAX
Todos los cambios notables en el proyecto Habitax serán documentados en este archivo.

[1.1.0] - 2024-05-20 
Optimización de Seguridad y UX
- Validación de Cliente: Implementada lógica en JavaScript para validar la robustez de la contraseña en el formulario de registro (8 caracteres, 1 número, 1 carácter especial).
- Feedback Visual: Añadido contenedor dinámico #passwordError en la vista de registro para notificar errores sin recargar la página.
- Modularización JS: Creado archivo externo validaciones.js en la carpeta static/js para seguir el principio de separación de responsabilidades.


[1.0.0] - 2024-05-10 (Lanzamiento Inicial)
Añadido
- Estructura base del proyecto con Spring Boot.
- Integración con la API de Idealista a través de RapidAPI.
- Sistema de persistencia en memoria con H2.
- Motor de vistas con Thymeleaf y estilos con Bootstrap.