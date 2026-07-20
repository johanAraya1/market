# Household Specification

## Purpose

Gestión de hogares: creación, invitación por código/enlace y administración de miembros.

## Requirements

### Requirement: Household Creation

El sistema DEBE permitir a un usuario autenticado crear un nuevo hogar.

#### Scenario: Create household

- GIVEN el usuario no pertenece a ningún hogar
- WHEN ingresa un nombre y confirma la creación
- THEN se crea `households/{hid}` con `name`, `createdAt`, `createdBy`
- AND se crea `households/{hid}/members/{uid}` con `role: "admin"`
- AND se genera un `inviteCode` de 6 caracteres alfanuméricos
- AND se navega a la pantalla principal del hogar

#### Scenario: Household name validation

- GIVEN el usuario ingresa un nombre de hogar
- WHEN el nombre está vacío o superiza 50 caracteres
- THEN se muestra error "El nombre debe tener entre 1 y 50 caracteres"
- AND NO se crea el hogar

### Requirement: Invite Code

El sistema DEBE generar códigos de invitación únicos para unirse a un hogar.

#### Scenario: Generate invite code

- GIVEN un admin está en la pantalla de administración del hogar
- WHEN solicita generar un nuevo código de invitación
- THEN se genera un código de 6 caracteres (A-Z, 0-9) único
- AND el código se guarda en `households/{hid}.inviteCode`
- AND el código tiene una vida útil de 7 días

#### Scenario: Join household via code

- GIVEN un usuario autenticado sin hogar
- WHEN ingresa un código de invitación válido
- THEN se verifica que el código existe y no expiró
- AND se agrega al usuario como `member` en `households/{hid}/members/{uid}`
- AND se navega a la pantalla principal del hogar

#### Scenario: Invalid or expired code

- GIVEN un usuario ingresa un código de invitación
- WHEN el código no existe o expiró
- THEN se muestra "Código inválido o expirado"
- AND NO se modifica ningún documento

### Requirement: Invite Link

El sistema DEBE generar enlaces de invitación que abren la app directamente con el código pre-cargado.

#### Scenario: Share invite link

- GIVEN un admin tiene un código de invitación activo
- WHEN presiona "Compartir enlace"
- THEN se genera un deep link `market://invite?code={code}`
- AND se abre el selector de compartir del sistema

#### Scenario: Open invite link

- GIVEN un usuario recibe un enlace de invitación
- WHEN abre el enlace y la app está instalada
- THEN la app se abre con el código pre-cargado en el campo de ingreso
- AND si la app no está instalada, se muestra instrucciones para descargarla

### Requirement: Member Management

El sistema DEBE permitir al admin gestionar los miembros del hogar.

#### Scenario: Remove member

- GIVEN un admin está en la lista de miembros
- WHEN selecciona un miembro (no a sí mismo) y confirma la eliminación
- THEN se elimina `households/{hid}/members/{uid}` del miembro
- AND el miembro pierde acceso a todos los datos del hogar

#### Scenario: Admin cannot remove self

- GIVEN un admin es el único administrador del hogar
- WHEN intenta eliminarse a sí mismo
- THEN se muestra "No puedes eliminarte si eres el único administrador"

#### Scenario: Transfer admin role

- GIVEN un admin quiere transferir el rol a otro miembro
- WHEN selecciona "Hacer administrador" en un miembro
- THEN el miembro seleccionado recibe `role: "admin"`
- AND el admin original se convierte en `member`

### Requirement: Leave Household

El sistema DEBE permitir a un miembro (no-admin) abandonar el hogar.

#### Scenario: Member leaves household

- GIVEN un miembro con rol `member`
- WHEN confirma "Salir del hogar"
- THEN se elimina su documento `households/{hid}/members/{uid}`
- AND se redirige a la pantalla de selección/creación de hogar

## Data Model

```
households/{hid}
  ├── name: string
  ├── createdAt: timestamp
  ├── createdBy: string (uid)
  ├── inviteCode: string (6 chars, nullable)
  ├── inviteCodeExpiry: timestamp (nullable)
  └── members/{uid}
        ├── role: "admin" | "member"
        ├── displayName: string
        └── joinedAt: timestamp
```

## Dependencies

- `auth`: Requiere usuario autenticado con uid

## Edge Cases

- Hogar con 0 miembros después de eliminar todos → se elimina el hogar automáticamente
- Código de invitación ya en uso al re-generar → se reemplaza el anterior
- Usuario con múltiples hogares (futuro) → v1 soporta solo 1 hogar por usuario
