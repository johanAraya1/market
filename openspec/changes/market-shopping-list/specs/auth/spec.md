# Auth Specification

## Purpose

Autenticación con Google Sign-In, perfiles de usuario y asignación de roles (admin/member) dentro de un hogar.

## Requirements

### Requirement: Google Sign-In

El sistema DEBE permitir autenticación exclusiva mediante Google Sign-In. NO se permite email/password ni otros proveedores.

#### Scenario: First-time sign-in

- GIVEN el usuario no tiene sesión activa
- WHEN presiona "Iniciar sesión con Google" y completa el flujo OAuth
- THEN se crea un documento en `users/{uid}` con `displayName`, `email`, `photoUrl`, `createdAt`
- AND se redirige a la pantalla de selección/creación de hogar

#### Scenario: Returning user sign-in

- GIVEN el usuario ya tiene documento en `users/{uid}`
- WHEN completa Google Sign-In
- THEN se actualiza `lastLoginAt` y se navega directamente a su hogar

#### Scenario: Sign-in failure

- GIVEN el usuario aborta el flujo de Google o pierde conexión
- WHEN el intento de sign-in falla
- THEN se muestra un error amigable con opción de reintentar
- AND NO se crea ningún documento en Firestore

### Requirement: User Profile

El sistema DEBE mantener un perfil de usuario con datos básicos del proveedor Google.

#### Scenario: Profile creation

- GIVEN un usuario completa Google Sign-In por primera vez
- WHEN se crea el documento `users/{uid}`
- THEN los campos `displayName`, `email`, `photoUrl` se copian del token de Google
- AND `createdAt` y `lastLoginAt` se establecen al timestamp actual

#### Scenario: Profile photo update

- GIVEN el usuario tiene un perfil existente
- WHEN cambia su foto de perfil en Google
- THEN `photoUrl` se actualiza en el próximo sign-in

### Requirement: Role Assignment

El sistema DEBE asignar roles de `admin` o `member` a cada usuario dentro de un hogar.

#### Scenario: First member becomes admin

- GIVEN un usuario crea un nuevo hogar
- WHEN se completa la creación del hogar
- THEN el usuario recibe rol `admin` en el documento `households/{hid}/members/{uid}`
- AND `role` = `"admin"`

#### Scenario: Invited user becomes member

- GIVEN un usuario se une a un hogar existente mediante código de invitación
- WHEN se completa el join
- THEN el usuario recibe rol `member` en `households/{hid}/members/{uid}`
- AND `role` = `"member"`

#### Scenario: Role permissions enforcement

- GIVEN un usuario con rol `member`
- WHEN intenta realizar una acción reservada a `admin` (eliminar hogar, expulsar miembro, eliminar item de otro)
- THEN la acción es denegada con mensaje "Solo los administradores pueden realizar esta acción"

### Requirement: Session Persistence

El sistema DEBE mantener la sesión del usuario entre aperturas de la app.

#### Scenario: App restart with valid session

- GIVEN el usuario cerró la app con sesión activa
- WHEN reabre la app
- THEN se restaura la sesión automáticamente sin mostrar pantalla de login
- AND se navega al último hogar conocido

## Data Model

```
users/{uid}
  ├── displayName: string
  ├── email: string
  ├── photoUrl: string
  ├── createdAt: timestamp
  └── lastLoginAt: timestamp
```

## Dependencies

- `household`: Rol asignado al unirse/crear un hogar
- Firebase Auth (Google provider) + Firestore

## Edge Cases

- Usuario de Google con email privado → usar uid como fallback identifier
- Cuenta de Google desvinculada → sesión expira en próximo intento de escritura Firestore
- Múltiples cuentas de Google en un dispositivo → solo una sesión activa a la vez
