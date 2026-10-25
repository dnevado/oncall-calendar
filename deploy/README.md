# Despliegue

El sistema nuevo se despliega con Docker junto al legacy, detrás de un proxy nginx en el mismo dominio (research R1):

| Ruta | Destino |
|---|---|
| `/app/` | `frontend`: la SPA (nginx con los ficheros de `vite build`) |
| `/api/` | `backend`: Spring Boot, Java 21, sin usuario root |
| resto | el Tomcat del legacy (`LEGACY_UPSTREAM`) |

- Para probar en un equipo de desarrollo sin Docker, usa `deploy/local/README.md`.
- Para el paso a producción, sigue `deploy/RUNBOOK.md`.

## Requisitos

- **TLS delante del proxy.**
  - Las cookies de sesión y CSRF van marcadas `Secure` y `SameSite=Strict`, así que el navegador solo las envía por HTTPS (salvo en `localhost`).
  - El proxy escucha en HTTP: la terminación TLS va delante (pendiente operativo de R1).
- **Red del legacy.** Una red Docker externa (`LEGACY_NETWORK`, por defecto `legacy`) desde la que se alcanzan la MySQL 5.7 del legacy y su Tomcat.
- **Secretos.**
  - Van en `deploy/secrets/`, nunca en git; ver `deploy/secrets/README.md`.
  - Solo el backend los monta, en `/run/secrets/`.
- **Pruebas antes de construir.** `mvn -f backend/pom.xml verify` y `npm --prefix frontend test`. Las imágenes no las ejecutan, porque las del backend necesitan Docker (Testcontainers).

## Variables

| Variable | Por defecto | Uso |
|---|---|---|
| `GUARDIAS_PERFIL` | `prod` | Perfil de Spring. En `prod` Liquibase **no** se ejecuta al arrancar (R6). |
| `GUARDIAS_PUERTO` | `80` | Puerto publicado del proxy |
| `GUARDIAS_VERSION` | `local` | Etiqueta de las imágenes |
| `LEGACY_UPSTREAM` | `legacy:8080` | Host y puerto del Tomcat del legacy dentro de su red |
| `LEGACY_NETWORK` | `legacy` | Nombre de la red Docker externa |

## Arranque

```sh
docker compose -f deploy/docker-compose.yml build
docker compose -f deploy/docker-compose.yml up -d
docker compose -f deploy/docker-compose.yml logs -f backend
```

Comprobación rápida:
- `GET /app/` responde 200.
- `GET /api/v1/auth/me` responde 401 con la cookie `XSRF-TOKEN`.
- `/` muestra el legacy.

## Probarlo con la base de desarrollo

Con la MySQL de `deploy/local` en marcha:

1. Crea los secretos con valores de desarrollo en una carpeta fuera del repositorio. La URL es `jdbc:mysql://mysql:3306/guardias?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8`, el usuario `guardias` y la contraseña `local`.
2. Escribe un fichero de override que apunte `secrets:` a esa carpeta y la red `legacy` a `guardias-local_default`.
3. Arranca con:

   ```sh
   GUARDIAS_PUERTO=8088 LEGACY_UPSTREAM=host.docker.internal:8080 \
     docker compose -f deploy/docker-compose.yml -f <override.yml> up -d
   ```

4. Abre `http://localhost:8088/app/`.
