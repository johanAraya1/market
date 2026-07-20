# Store Management Specification

## Purpose

CRUD de tiendas, ordenamiento por drag-and-drop (admin) y asignación de items a tiendas.

## Requirements

### Requirement: Store CRUD

El sistema DEBE permitir crear, editar y eliminar tiendas dentro del hogar.

#### Scenario: Add store

- GIVEN un admin está en la pantalla de tiendas
- WHEN ingresa un nombre y presiona "Agregar tienda"
- THEN se crea `households/{hid}/stores/{storeId}` con `name`, `order`, `createdAt`
- AND la tienda aparece en la lista ordenada

#### Scenario: Edit store name

- GIVEN una tienda existe
- WHEN un admin modifica su nombre
- THEN se actualiza `name` y `updatedAt`
- AND el cambio se refleja en todos los dispositivos

#### Scenario: Delete store

- GIVEN una tienda existe
- WHEN un admin la elimina
- THEN se elimina `households/{hid}/stores/{storeId}`
- AND todos los items asignados a esa tienda quedan con `storeId: null`
- AND se muestra confirmación "Los items asignados quedarán sin tienda"

#### Scenario: Non-admin cannot manage stores

- GIVEN un usuario con rol `member`
- WHEN intenta crear, editar o eliminar una tienda
- THEN se deniega la acción con "Solo los administradores pueden gestionar tiendas"

### Requirement: Admin Drag-and-Drop Ordering

El sistema DEBE permitir al admin reordenar tiendas mediante drag-and-drop.

#### Scenario: Reorder stores

- GIVEN un admin ve la lista de tiendas
- WHEN mantienepresionada una tienda y la arrastra a nueva posición
- THEN se actualiza el campo `order` de todas las tiendas afectadas
- AND el nuevo orden se refleja en todos los dispositivos
- AND el orden se mantiene entre sesiones

#### Scenario: Order initialization

- GIVEN se crea una nueva tienda
- WHEN se guarda por primera vez
- THEN `order` se establece como el siguiente número secuencial (max(order) + 1)

### Requirement: Item-to-Store Assignment

El sistema DEBE permitir asignar items a una tienda específica.

#### Scenario: Assign item to store

- GIVEN un item existe en la lista y al menos una tienda existe
- WHEN el usuario selecciona una tienda para el item
- THEN `storeId` del item se actualiza con el uid de la tienda
- AND el item se muestra bajo el heading de esa tienda en la lista

#### Scenario: Remove item from store

- GIVEN un item está asignado a una tienda
- WHEN el usuario selecciona "Sin tienda" o desasigna
- THEN `storeId` se establece en `null`
- AND el item aparece en la sección "Sin asignar" al final de la lista

#### Scenario: List items grouped by store

- GIVEN existen items asignados a diferentes tiendas
- WHEN el usuario visualiza la lista principal
- THEN los items se muestran agrupados por tienda en el orden definido por `order`
- AND items sin tienda aparecen en la sección "Sin asignar"
- AND tiendas sin items no muestran heading (ocultas)

## Data Model

```
households/{hid}/stores/{storeId}
  ├── name: string
  ├── order: number
  ├── createdAt: timestamp
  └── updatedAt: timestamp (nullable)
```

## Dependencies

- `household`: Tiendas pertenecen a un hogar
- `auth`: Solo admins pueden gestionar tiendas
- `shopping-list`: Items se asignan a tiendas

## Edge Cases

- Eliminar última tienda → items quedan todos en "Sin asignar"
- Drag-and-drop con una sola tienda → operación no-op
- Nombre de tienda duplicado → permitido (mismo producto en distintas cadenas)
