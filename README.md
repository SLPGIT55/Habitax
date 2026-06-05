# HABITAX — Predictor Inmobiliario

Sistema web de estimación de precios inmobiliarios para el mercado español.
El usuario introduce provincia, zona, metros cuadrados, habitaciones y baños y recibe tres estimaciones de precio: **Oportunidad (-15%)**, **Precio Medio** y **Premium (+25%)**, calculadas con datos en tiempo real de la API de Idealista.

---

## 🌐 Acceso a producción (AWS)

**URL:** http://habitax-env.eba-yrbph7mf.eu-west-1.elasticbeanstalk.com

Desplegado en AWS Elastic Beanstalk (eu-west-1, Irlanda).
Cada push a `main` despliega automáticamente vía CodePipeline → CodeBuild (~5-7 min).

---

## ⚡ Arrancar en local (versión corta)

```bash
# 1. Clonar
git clone https://github.com/SLPGIT55/Habitax.git

# 2. Abrir la carpeta predictor/ en IntelliJ / VS Code / Eclipse

# 3. Ejecutar PredictorApplication.java con el botón Play

# 4. Aparecerá una ventana de configuración — elegir modo (ver abajo)

# 5. Abrir en el navegador
http://localhost:8081
```

---

## 🖥️ Ventana de configuración al arrancar

Al ejecutar la app **sin variables de entorno definidas**, aparece automáticamente una ventana nativa antes de que Spring Boot inicialice. Hay que elegir entre dos modos:

### Opción A — Modo desarrollo (H2 en memoria)
- La base de datos se crea vacía en cada arranque y se borra al cerrar.
- Puedes introducir opcionalmente tu API Key de RapidAPI para que el desplegable de zonas funcione.
- Sin API Key, el desplegable de zonas dará error (limitación conocida, el resto de la app funciona con normalidad).
- Ideal para: login, registro, historial, favoritos, perfil.

### Opción B — Conectar a producción (MySQL RDS)
- Haz clic en `...` para seleccionar tu fichero de variables de entorno desde el explorador.
- En Mac/Linux: `~/habitax-env.sh` — En Windows: `C:\Users\TuUsuario\habitax-env.bat`
- La app conectará a la BD real de AWS. **Ojo: trabajas sobre los mismos datos que producción.**
- Pide el fichero de variables al responsable de infraestructura si no lo tienes.

> Si la variable `SPRING_DATASOURCE_URL` ya está definida en tu entorno (por haber ejecutado `source ~/habitax-env.sh` antes), la ventana no aparece y la app arranca directamente en modo producción.

---

## 📋 Comportamiento según entorno

| Entorno | Variables | Base de datos | API Idealista |
|---------|-----------|---------------|---------------|
| Local — modo desarrollo | No definidas | H2 en memoria | Solo con API Key en el dialog |
| Local — modo producción | Cargadas desde fichero .env | MySQL RDS real | Funciona |
| AWS Elastic Beanstalk | Definidas en EB | MySQL RDS real | Funciona |

---

## ✅ Requisitos previos

- Java 17 o superior (recomendado Java 21 Corretto)
- Maven — incluido en el proyecto como `mvnw`, no hace falta instalarlo
- Git
- IDE: IntelliJ IDEA, VS Code (con Extension Pack for Java) o Eclipse

```bash
# Comprobar Java
java -version
```

---

## 🚀 Arrancar desde cada IDE

### IntelliJ IDEA
1. `File → Open` → selecciona la carpeta `predictor/`
2. Espera a que Maven descargue dependencias
3. Abre `src/main/java/com/habitax/predictor/PredictorApplication.java`
4. Click derecho → `Run 'PredictorApplication'`
5. Aparece la ventana de configuración — elige modo
6. Abrir `http://localhost:8081`

### VS Code
1. Instala "Extension Pack for Java" si no la tienes
2. `File → Open Folder` → selecciona la carpeta `predictor/`
3. Abre `PredictorApplication.java`
4. Pulsa el icono `Run` sobre el método `main`
5. Aparece la ventana de configuración — elige modo
6. Abrir `http://localhost:8081`

### Terminal
```bash
cd predictor
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

---

## 🔍 Cómo verificar que funciona

1. Arranca la app y elige modo en la ventana de configuración
2. Ve a `http://localhost:8081` → debe aparecer el login
3. Regístrate: `test@test.com` / `Test1234`
4. Haz login con ese usuario
5. Navega por la app: perfil, historial, favoritos
6. Si estás en modo desarrollo sin API Key, el desplegable de zonas dará error al cargar — es normal

---

## 🗄️ Consola H2 (inspeccionar la BD en vivo)

Solo disponible en modo desarrollo. Mientras la app está corriendo:

- **URL:** `http://localhost:8081/habitax-db-panel`
- **JDBC URL:** `jdbc:h2:mem:habitax`
- **User Name:** `sa`
- **Password:** *(dejar vacío)*

```sql
SELECT * FROM USUARIO;
SELECT * FROM PREDICCION;
SELECT * FROM FAVORITO;
```

---

## 🌿 Flujo de trabajo en equipo

```bash
# 1. Crear rama de trabajo
git checkout -b feature/mi-funcionalidad

# 2. Desarrollar y probar en local

# 3. Commit y push
git add .
git commit -m "feat: descripción corta"
git push origin feature/mi-funcionalidad

# 4. Abrir Pull Request en GitHub hacia main
# Al mergear → CodePipeline despliega automáticamente en AWS (~5-7 min)

# 5. Verificar en la URL de AWS que el cambio funciona con datos reales
```

> ⚠️ **Antes de mergear a main:** si has añadido columnas nuevas a una entidad JPA, ejecuta primero el `ALTER TABLE` correspondiente en RDS (con MySQL Workbench conectado a la BD de producción). Con `ddl-auto=validate`, Hibernate no arranca si el esquema no coincide.

---

## 🏗️ Estructura del proyecto

```
predictor/
├── src/main/java/com/habitax/predictor/
│   ├── PredictorApplication.java       ← Arranque + dialog Swing
│   ├── config/
│   │   └── AppConfig.java              ← RestTemplate con timeouts
│   ├── controller/
│   │   ├── HabitaxController.java      ← Rutas HTTP
│   │   └── StartupController.java      ← Dialog de arranque (Swing)
│   ├── service/
│   │   ├── PrecioService.java          ← Caché + API + cálculos
│   │   ├── LoginRateLimiterService.java ← Protección 
│   │   └── UsuarioDetailsService.java
│   ├── model/
│   │   ├── Usuario.java
│   │   ├── Prediccion.java
│   │   └── Favorito.java
│   ├── repository/
│   │   ├── UsuarioRepository.java
│   │   ├── PrediccionRepository.java
│   │   └── FavoritoRepository.java
│   └── dto/
│       ├── UsuarioSesionDTO.java        ← Sin password en sesión
│       └── PrediccionDTO.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/                      ← Vistas Thymeleaf
├── buildspec.yml                       ← Configuración CodeBuild
└── pom.xml
```

---

## 🔒 Seguridad y credenciales

- **Ningún secreto en el repositorio.** Ni API keys, ni contraseñas de BD, ni en `application.properties`.
- Las credenciales de producción viven en Elastic Beanstalk Environment Properties.
- Las credenciales de desarrollo local viven en `~/habitax-env.sh` (no trackeado por Git).
- Contraseñas de usuario cifradas con BCrypt. Nunca se almacenan en texto plano.
- Rate limiting: 5 intentos de login fallidos → bloqueo de 15 minutos por email.

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Backend | Java 21 / Spring Boot 3.5.14 / Spring Data JPA |
| Frontend | Thymeleaf + Bootstrap 5 |
| BD local | H2 en memoria |
| BD producción | MySQL 8.4 en AWS RDS (eu-west-1) |
| Infraestructura | AWS Elastic Beanstalk + CodePipeline + CodeBuild |
| Pool conexiones | HikariCP (máx. 5 conexiones) |
| API externa | RapidAPI — Idealista7 |

---

*Habitax — Proyecto de Informática II — Universidad Europea de Madrid — 2025/2026*