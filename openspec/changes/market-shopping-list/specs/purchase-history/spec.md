# Purchase History Specification

## Purpose

Registro de compras completadas (trips) y consulta del historial de compras pasadas.

## Requirements

### Requirement: Complete Shopping Trip

El sistema DEBE permitir finalizar una compra y registrar los items comprados.

#### Scenario: Finish shopping trip

- GIVEN hay items marcados como completados en la lista
- WHEN el usuario presiona "Finalizar compra"
- THEN se crea `households/{hid}/trips/{tripId}` con `completedBy`, `completedAt`, `items[]`
- AND cada item registrado tiene `name`, `storeId`, `storeName`, `price`, `quantity`
- AND los items completados se desmarcan de la lista activa

#### Scenario: Complete with no checked items

- GIVEN no hay items marcados como completados
- WHEN el usuario presiona "Finalizar compra"
- THEN se muestra "No hay items marcados para registrar"

#### Scenario: Complete trip preserves checked items

- GIVEN hay 5 items marcados
- WHEN se finaliza la compra
- THEN los 5 items se guardan en el trip con sus precios y tiendas actuales
- AND los items se desmarcan (isChecked = false) en la lista activa

### Requirement: Purchase History List

El sistema DEBE mostrar un historial cronológico de compras realizadas.

#### Scenario: View purchase history

- GIVEN el hogar tiene al menos un trip registrado
- WHEN el usuario accede a "Historial de compras"
- THEN se muestran los trips ordenados por `completedAt` descendente
- AND cada trip muestra: fecha, quien completó, cantidad de items, total estimado

#### Scenario: Empty history

- GIVEN no hay trips registrados
- WHEN el usuario accede a "Historial de compras"
- THEN se muestra "Aún no hay compras registradas" con ilustración vacía

#### Scenario: Paginate history

- GIVEN el hogar tiene más de 20 trips
- WHEN el usuario hace scroll hacia abajo
- THEN se cargan más trips progresivamente (lazy loading)
- AND NO se cargan todos los trips de una vez (límite Firestore reads)

### Requirement: Trip Detail View

El sistema DEBE permitir ver el detalle de un trip específico.

#### Scenario: View trip detail

- GIVEN un trip existe en el historial
- WHEN el usuario selecciona un trip
- THEN se muestra: fecha completa, quien completó, lista de items con nombre, tienda, precio, cantidad
- AND se muestra el total de la compra

#### Scenario: Trip with items from multiple stores

- GIVEN un trip tiene items de 3 tiendas distintas
- WHEN se visualiza el detalle
- THEN los items se agrupan por tienda
- AND se muestra subtotal por tienda

### Requirement: Trip Deletion

El sistema DEBE permitir al admin eliminar trips del historial.

#### Scenario: Delete trip

- GIVEN un admin está en el historial
- WHEN selecciona un trip y confirma eliminación
- THEN se elimina `households/{hid}/trips/{tripId}`
- AND el trip desaparece del historial

#### Scenario: Member cannot delete trips

- GIVEN un usuario con rol `member`
- WHEN intenta eliminar un trip
- THEN se deniega con "Solo los administradores pueden eliminar historial"

## Data Model

```
households/{hid}/trips/{tripId}
  ├── completedBy: string (uid)
  ├── completedBy: string (displayName)
  ├── completedAt: timestamp
  ├── totalEstimated: number
  └── items[]
        ├── name: string
        ├── storeId: string (nullable)
        ├── storeName: string (nullable)
        ├── price: number (nullable)
        └── quantity: number
```

## Dependencies

- `shopping-list`: Items completados se extraen de la lista activa
- `store-management`: Nombres de tiendas se copian al trip
- `price-comparison`: Precios se copian al trip al momento de completar
- `auth`: Identifica quién completó la compra

## Edge Cases

- Trip con items sin precio → total se calcula solo con items que tienen precio
- Eliminar tienda después de un trip → el trip conserva el nombre de la tienda en `storeName`
- Trip con 0 items (creado por error) → permitido pero se muestra advertencia
- Firestore free tier: paginate a 20 trips por carga
