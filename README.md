# Guia de desarrollo local - Habitax

## Arrancar la aplicacion (version corta)

1. Clonar el repo.
2. Abrir la carpeta `predictor/` en tu IDE (IntelliJ / VS Code / Eclipse).
3. Esperar a que Maven descargue dependencias
4. Ejecutar la clase `PredictorApplication.java` con el boton play.
5. Abrir en el navegador: http://localhost:8081

Eso es todo. No hay que configurar variables, perfiles ni nada.

## Que esta pasando por debajo

Al arrancar **sin variables de entorno definidas**, Spring Boot:

- Detecta que no hay configuracion de MySQL.
- Usa por defecto una base de datos **H2 en memoria** (se crea vacia cada vez).
- Arranca la app en el puerto 8081.

Cuando paras la app, la BD se borra. La proxima vez que arranques, vuelve a crearse limpia. Perfecto para desarrollo rapido.

## Comportamiento segun entorno

| Entorno | Variables | Base de datos | API Idealista |
|---------|-----------|---------------|---------------|
| Desarrollo local (IDE) | No definidas | H2 en memoria | No funciona (limitacion conocida) |
| Acceso de admin a RDS | Se cargan manualmente | MySQL RDS real | Funciona si hay RAPIDAPI_KEY |
| Produccion (AWS) | Definidas en EB | MySQL RDS real | Funciona |

## Limitacion conocida en local: la API de Idealista

Cuando arrancas en local **sin variables de entorno**, el desplegable de zonas/barrios **no funciona** y te saldra un error al cargar zonas. Esto es esperado.

### Por que

La consulta de barrios llama a la API de Idealista (RapidAPI), que requiere una clave. Por seguridad, la clave **no esta en el repositorio** ni se distribuye al equipo.

### Soluciones

**Opcion A: trabajar sin el desplegable de zonas** 

Puedes desarrollar cualquier funcionalidad que no dependa del autocomplete de zonas: login, registro, historial, favoritos, perfil, etc. El resto de la app funciona con normalidad usando la BD H2.

**Opcion B: pedir las variables de entorno**

Si tu tarea requiere probar con datos reales de Idealista, pide al responsable de infra el script `habitax-env.sh` con las variables necesarias. Cargalo antes de arrancar:

```bash
source ~/habitax-env.sh
cd predictor
./mvnw spring-boot:run
```

Con esas variables cargadas, la app conectara a **MySQL RDS real** (ojo: trabajas sobre la misma BD que produccion) y la API de Idealista funcionara.

## Requisitos previos

- **Java 17 o superior**. Recomendado **Java 21** (Corretto).
- **Maven** (incluido en el proyecto como `mvnw`, no hace falta instalar).
- **Git** para clonar el repo.
- Un IDE: IntelliJ IDEA, VS Code (con extension Java), o Eclipse.

Comprueba tu Java:
```bash
java -version
```

## Arrancar desde IntelliJ IDEA

1. File -> Open -> selecciona la carpeta `predictor/`.
2. Espera a que indexe el proyecto.
3. Abre `src/main/java/com/example/predictor/PredictorApplication.java`.
4. Click derecho -> Run 'PredictorApplication'.
5. Al arrancar, abrir http://localhost:8081.

## Arrancar desde VS Code

1. Instala la extension "Extension Pack for Java" si no la tienes.
2. File -> Open Folder -> selecciona la carpeta `predictor/`.
3. Espera a que VS Code indexe el proyecto.
4. Abre `PredictorApplication.java`.
5. Pulsa el icono "Run" sobre el metodo `main`.
6. Al arrancar, abrir http://localhost:8081.

## Arrancar desde terminal (alternativa)

```bash
cd predictor
./mvnw spring-boot:run
```

(En Windows: `mvnw.cmd spring-boot:run`)

## Como probar que funciona

1. Arranca la app.
2. Ve a http://localhost:8081 -> debe aparecer el login.
3. Registra un usuario: test@test.com / Test1234.
4. Haz login con ese mismo usuario.
5. Navega por la app (perfil, historial vacio, etc.).

Si intentas hacer una busqueda de predicciones, te dara error al cargar zonas. Es normal en local sin variables (ver limitacion conocida arriba).

## Consola H2 (inspeccionar la BD en vivo)

Mientras la app esta corriendo:

- URL: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:habitax`
- User Name: `sa`
- Password: (dejar vacio)

Pulsar **Connect** y ejecutar queries:

```sql
SELECT * FROM USUARIO;
SELECT * FROM PREDICCION;
```

## Flujo de trabajo recomendado

1. Crea rama de trabajo:
   ```
   git checkout -b feature/mi-funcionalidad
   ```
2. Desarrolla y prueba en tu IDE.
3. Commit y push:
   ```
   git add .
   git commit -m "feat: descripcion corta"
   git push origin feature/mi-funcionalidad
   ```
4. Abre Pull Request en GitHub hacia `main`.
5. Al mergear, CodePipeline despliega automaticamente en AWS (~5-7 min).
6. Verifica en la URL de AWS que tu cambio funciona con datos reales.