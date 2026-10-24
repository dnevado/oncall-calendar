# Secretos de despliegue (R14)

Esta carpeta contiene **solo este README** en git. Los ficheros de secretos se crean en el servidor y están ignorados por git.

| Fichero | Contenido |
|---|---|
| `guardias_db_url` | URL JDBC de la MySQL del legacy, p. ej. `jdbc:mysql://<host>:3306/<base>?useUnicode=true&characterEncoding=utf8` |
| `guardias_db_user` | Usuario de la base de datos del backend nuevo |
| `guardias_db_password` | Contraseña de ese usuario |

Requisitos:
- Un valor por fichero, sin salto de línea final.
- Permisos restringidos (`chmod 600`), propietario el usuario que ejecuta Docker.
- Nunca credenciales de producción en equipos de desarrollo (constitución, principio VI).
- El usuario de base de datos del backend debe tener solo los privilegios necesarios. Las migraciones (Liquibase) se lanzan con un usuario aparte desde el runbook.
