# Configuración segura

La API no inicia si `JWT_SECRET` está ausente, tiene menos de 32 bytes o si
`JWT_EXPIRATION` no es positivo. Generá el secreto fuera del repositorio:

```sh
openssl rand -base64 64
```

## Primer administrador

Para crear el administrador inicial, usá una sola vez:

```env
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_EMAIL=admin@tu-dominio.com
BOOTSTRAP_ADMIN_PASSWORD=una-contraseña-única-y-segura
```

Iniciá la API, verificá que el usuario se haya creado y volvé a establecer
`BOOTSTRAP_ADMIN_ENABLED=false`. La aplicación rechaza el arranque si se
habilita el bootstrap sin correo o contraseña.

## Producción

- Configurá secretos con el gestor del proveedor de hosting, nunca en `.env` ni
  en `application.properties`.
- Definí únicamente dominios HTTPS reales en `CORS_ALLOWED_ORIGINS`, separados
  por comas. No uses comodines.
- Mantené Swagger, Actuator y los uploads detrás de HTTPS y aplicá backups
  verificados de PostgreSQL antes de cada migración.
