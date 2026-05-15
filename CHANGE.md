Changelog - Proyecto HABITAX
Todos los cambios notables en el proyecto Habitax serán documentados en este archivo.

[1.4.0] - 2026-05-15
Ecosistema de Usuario y Persistencia de Favoritos

##Añadido
-Módulo de Favoritos: Implementación de la entidad Favorito con persistencia en H2 y creación del endpoint /favoritos/guardar para almacenar métricas clave (zona, metros y precio estimado).
-Algoritmo de Segmentación: Añadida lógica de cálculo en el servidor para generar tres niveles de precio dinámicos: Oportunidad (-15%), Precio Medio (Base) y Premium (+25%).
-Recálculo en Caliente: Implementado endpoint /recalculate que permite actualizar el valor de mercado de un favorito guardado, integrando la lógica de Caché Temporal (1 hora) desarrollada en la versión anterior.
-Historial Visual: Integración de la lista de Últimas 3 Consultas en el panel principal mediante PageRequest para optimizar el rendimiento de la base de datos.

##Cambiado
-Arquitectura de UI: Refactorización de index.html hacia una estructura simétrica de tres columnas (col-lg-3 | 6 | 3) para mejorar la experiencia de usuario y el equilibrio visual de los edificios laterales.
-Robustez de Thymeleaf: Implementadas directivas th:if de seguridad en los fragmentos de resultados para evitar excepciones de renderizado (Whitelabel Error Page) ante variables nulas.

##Corregido
-Conflictos de Mapeo: Resuelto error de Ambiguous Mapping mediante la unificación de métodos @PostMapping duplicados en el controlador.
-Integridad de Datos: Eliminación de registros duplicados en la tabla de usuarios mediante la aplicación de restricciones de unicidad en el campo email

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