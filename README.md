# Habitax
Habitax - Predictor Inmobiliario Pro
Habitax es una aplicación web desarrollada en Java Spring Boot que permite estimar el valor de mercado de una vivienda utilizando datos reales. El proyecto integra la API de Idealista para obtener precios actualizados por metro cuadrado en distintas zonas geográficas.

Funcionalidades
Consulta en Tiempo Real: Conexión con la API de Idealista (vía RapidAPI) para obtener datos de mercado vigentes.

Cálculo Inteligente: Algoritmo que promedia el precio/m² de los testigos de la zona para ofrecer una valoración ajustada.

Interfaz Moderna: Diseño responsivo creado con Bootstrap 5 y Font Awesome.

Sistema Anti-Fallos: Incluye lógica de respaldo (fallback) para ofrecer estimaciones base si la API no devuelve resultados o hay problemas de conexión.

🛠️ Tecnologías Utilizadas
Backend: Java 21 & Spring Boot 3.4.2

Frontend: Thymeleaf, HTML5, CSS3 (Bootstrap 5)

Gestión de Dependencias: Maven

API Externa: Idealista7 (RapidAPI)

Requisitos Previos
Java SDK 21 o superior.

Maven instalado.

Una API Key de RapidAPI (Suscripción activa a la API de Idealista7).

Configuración e Instalación
Clonar el repositorio:

Bash

git clone https://github.com/tu-usuario/habitax-predictor.git
Configurar el puerto: Por defecto, la aplicación corre en el puerto 8081 para evitar conflictos de sistema. Puedes cambiarlo en src/main/resources/application.properties:

Properties

server.port=8081
Configurar la API Key: En el archivo HabitaxController.java, localiza la línea de los headers y pega tu clave:

Java

headers.set("x-rapidapi-key", "TU_CLAVE_AQUI"); 

Cómo ejecutarlo
Abre el proyecto en VS Code.

Ejecuta la clase PredictorApplication.java.

Abre tu navegador en: http://localhost:8081
