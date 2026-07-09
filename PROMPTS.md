

## Prompt 1 - Análisis inicial del enunciado

**Herramienta usada:** ChatGPT

**Prompt usado:**

El proyecto consiste en una API para un sistema de pedidos de restaurante con Spring Boot, seguridad, JWT, roles, Docker y GitHub Actions. El sistema debe manejar restaurantes o sucursales, mesas, pedidos y usuarios con roles. También debo implementar una regla de negocio no trivial.

**Qué generó la IA:**

La IA propuso una estructura general del proyecto, separando paquetes por responsabilidad: configuración, seguridad, autenticación.

**Qué se corrigió o completó manualmente:**

Se decidió mantener y crear otros paquetes que la IA estaba utilizando de manera erronea, así mismo 
unas clases que debían ir en otro package .



## Prompt 2 - Configuración de Spring Security

**Herramienta usada:** ChatGPT

**Prompt usado:**

Dame una configuración de Spring Security para proteger endpoints con JWT, permitir endpoints de autenticación y usar roles con @PreAuthorize.

**Qué generó la IA:**

La IA generó la clase `SecurityConfig`, configurando:

- CSRF desactivado.
- Sesiones stateless.
- Rutas `/api/auth/**` públicas.
- Resto de endpoints autenticados.
- Filtro JWT antes de `UsernamePasswordAuthenticationFilter`.
- PasswordEncoder con BCrypt.

**Qué se corrigió o completó manualmente:**

Se validó que los endpoints de autenticación quedaran públicos para poder iniciar sesión. También se revisó que el filtro JWT solo aceptara access tokens y no refresh tokens para acceder a recursos protegidos.

