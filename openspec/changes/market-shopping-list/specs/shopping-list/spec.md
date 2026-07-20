# Shopping List Specification

## Purpose

Lista compartida de compras con sincronización en tiempo real vía Firestore y soporte offline.

## Requirements

### Requirement: Item CRUD

El sistema DEBE permitir crear, leer, actualizar y eliminar items en la lista compartida.

#### Scenario: Add item

- GIVEN el usuario está en la pantalla de lista del hogar
- WHEN ingresa un nombre de item y presiona "Agregar"
- THEN se crea `households/{hid}/items/{itemId}` con `name`, `addedBy`, `createdAt`, `isChecked: false`, `storeId: null`, `price: null`
- AND el item aparece inmediatamente en la lista de todos los miembros

#### Scenario: Edit item name

- GIVEN un item existe en la lista
- WHEN el usuario modifica el nombre y confirma
- THEN se actualiza `name` y `updatedAt` en el documento
- AND el cambio se refleja en dispositivos de otros miembros

#### Scenario: Delete item

- GIVEN un item existe en la lista
- WHEN el usuario lo desliza hacia la izquierda y confirma eliminación
- THEN se elimina `households/{hid}/items/{itemId}`
- AND el item desaparece de la lista de todos los miembros

#### Scenario: Delete by non-owner (admin only)

- GIVEN un item fue agregado por otro usuario
- WHEN un `member` intenta eliminarlo
- THEN la acción es denegada — solo el creador o un `admin` puede eliminar

### Requirement: Real-time Sync

El sistema DEBE sincronizar cambios de la lista en tiempo real entre todos los dispositivos del hogar.

#### Scenario: Multi-device sync

- GIVEN dos dispositivos conectados al mismo hogar
- WHEN un usuario agrega un item en el dispositivo A
- THEN el item aparece en el dispositivo B dentro de 2 segundos
- AND sin necesidad de recarga manual

#### Scenario: Concurrent edits

- GIVEN dos usuarios editan el mismo item simultáneamente
- WHEN ambos guardan cambios
- THEN prevalece la última escritura (last-write-wins) con `serverTimestamp`
- AND se muestra `updatedAt` como referencia de cuál fue el último cambio

### Requirement: Offline Support

El sistema DEBE permitir operaciones CRUD sin conexión a internet.

#### Scenario: Add item offline

- GIVEN el dispositivo sin conexión de red
- WHEN el usuario agrega un item
- THEN el item se muestra inmediatamente en la lista local
- AND cuando se restaura la conexión, el item se sincroniza con Firestore
- AND se resuelve conflicto con last-write-wins si hay duplicados

#### Scenario: Offline indicator

- GIVEN el dispositivo está sin conexión
- WHEN el usuario navega por la app
- THEN se muestra un indicador visual "Sin conexión — los cambios se sincronizarán automáticamente"

### Requirement: Check-off

El sistema DEBE permitir marcar items como completados durante la compra.

#### Scenario: Check off item

- GIVEN un item no está marcado
- WHEN el usuario toca el checkbox
- THEN `isChecked` se establece en `true`
- AND `checkedBy` se registra con el uid del usuario
- AND `checkedAt` se establece con timestamp

#### Scenario: Admin check-off with reason

- GIVEN un usuario con rol `admin` marca un item
- WHEN se le solicita un motivo (opcional)
- THEN `checkReason` se guarda si se proporciona
- AND el item se muestra tachado con el motivo visible

#### Scenario: Uncheck item

- GIVEN un item está marcado como completado
- WHEN el usuario toca el checkbox nuevamente
- THEN `isChecked` se establece en `false`
- AND los campos `checkedBy`, `checkedAt`, `checkReason` se limpian

## Data Model

```
households/{hid}/items/{itemId}
  ├── name: string
  ├── addedBy: string (uid)
  ├── createdAt: timestamp
  ├── updatedAt: timestamp
  ├── isChecked: boolean
  ├── checkedBy: string (uid, nullable)
  ├── checkedAt: timestamp (nullable)
  ├── checkReason: string (nullable)
  ├── storeId: string (nullable, ref → stores/{sid})
  ├── price: number (nullable)
  └── quantity: number (default 1)
```

## Dependencies

- `auth`: Identifica al usuario que agrega/modifica items
- `household`: Items pertenecen a un hogar específico
- `store-management`: Items pueden asignarse a tiendas

## Edge Cases

- Item con nombre duplicado → permitido (mismo producto, distintas cantidades)
- Eliminar hogar → cascade elimina todos los items
- Límite de Firestore: 50K reads/día → paginate listas mayores a 200 items
- Conflicto offline: dos usuarios eliminan el mismo item → se resuelve con la escritura más reciente
