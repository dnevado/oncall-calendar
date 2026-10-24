# Entorno local de desarrollo

Para ver la aplicación nueva en `http://localhost:5173/app/` con datos ficticios. No es el despliegue (`deploy/docker-compose.yml`, T061) y no usa datos reales.

> No apuntes el backend a otra base (por ejemplo, la MySQL local del legacy): Liquibase se ejecuta al arrancar y le aplicaría los changesets 001–005.

## Arranque

1. Base de datos (MySQL 5.7 en el puerto 3307, esquema legacy + `datos-ficticios.sql`):

   ```sh
   docker compose -f deploy/local/docker-compose.yml up -d
   ```

2. Backend en el puerto 8081 (el 8080 suele estar ocupado por el Tomcat del legacy). Desde `backend/`:

   ```sh
   SERVER_PORT=8081 \
   guardias_db_url='jdbc:mysql://localhost:3307/guardias?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8' \
   guardias_db_user=guardias guardias_db_password=local \
   mvn spring-boot:run
   ```

   En PowerShell, define antes las variables con `$env:SERVER_PORT='8081'`, etc.

3. Frontend. Desde `frontend/`:

   ```sh
   GUARDIAS_API_URL=http://localhost:8081 npm run dev
   ```

4. Abre `http://localhost:5173/app/`.

## Usuarios (contraseña `prueba` para todos)

| Email | Papel |
|---|---|
| `admin@prueba.local` | Administrador del servicio «Cardiología (ficticio)» |
| `ana@prueba.local` | Médica sin rol de administrador: la API responde 403 |

## Qué probar

Las fechas son relativas al día en que se creó el contenedor (D).

- **D+5, guardia de presencia de Ana**: «Ceder». Solo aparece como candidato Bruno:
  - Carla está de vacaciones ese día (CG-012).
  - David tiene presencia el D+6 (CG-013).
  - Elena está inactiva (CG-015).
  - Rita y Raúl son residentes (CG-014).
  - Admin ya tiene guardia ese día (CG-011).
- **Intercambio**: guardia de Ana del D+5 → «Intercambiar» → fecha D+8 → Bruno. Cada uno queda en el día del otro y el tipo se queda con el día.
- **Ajuste R3–R5**:
  - Si Rita (R2) cede su guardia del D+5 a Raúl (R4), el refuerzo de Admin ese día pasa a localizada.
  - Si Raúl cede su guardia del D+10 a Rita, la localizada de Ana pasa a refuerzo.
- **D−2**: guardia pasada, no se puede seleccionar.
- **Rechazos**: llamando a la API sin pasar por la pantalla, por ejemplo cediendo a Carla, se ven las infracciones (422).

## Volver a empezar

`datos-ficticios.sql` solo se carga cuando el contenedor se crea por primera vez. Para recrear la base con fechas nuevas:

```sh
docker compose -f deploy/local/docker-compose.yml down -v
```
