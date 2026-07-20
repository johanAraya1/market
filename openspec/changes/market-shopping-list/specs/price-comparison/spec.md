# Price Comparison Specification

## Purpose

Registro de precios por tienda por item y visualización de la opción más barata.

## Requirements

### Requirement: Price Tracking per Store

El sistema DEBE permitir registrar el precio de un item en una tienda específica.

#### Scenario: Record price for item at store

- GIVEN un item está asignado a una tienda
- WHEN el usuario ingresa un precio y confirma
- THEN se crea/actualiza `households/{hid}/items/{itemId}/prices/{storeId}` con `amount`, `recordedBy`, `recordedAt`
- AND el precio se muestra junto al item en la lista

#### Scenario: Update price at same store

- GIVEN ya existe un precio registrado para un item en una tienda
- WHEN el usuario ingresa un nuevo precio
- THEN se actualiza `amount` y `recordedAt`
- AND se mantiene el historial de cambios en el documento

#### Scenario: Record price without store assignment

- GIVEN un item no tiene `storeId` asignado
- WHEN el usuario intenta registrar un precio
- THEN se muestra "Primero asigna el item a una tienda para registrar su precio"

### Requirement: Cheapest Option Surfacing

El sistema DEBE mostrar la tienda más barata para cada item que tenga precios registrados.

#### Scenario: Show cheapest store

- GIVEN un item tiene precios registrados en 2+ tiendas
- WHEN el usuario visualiza la lista
- THEN el item muestra un badge "Más barato en {storeName}" con el menor precio
- AND el precio más barato se resalta visualmente (color teal)

#### Scenario: Single store price

- GIVEN un item tiene precio en solo 1 tienda
- WHEN se visualiza el item
- THEN se muestra el precio sin badge de comparación
- AND se muestra "Solo registrado en {storeName}"

#### Scenario: No prices recorded

- GIVEN un item no tiene precios en ninguna tienda
- WHEN se visualiza el item
- THEN se muestra "Sin precio registrado"
- AND se ofrece opción de registrar precio

### Requirement: Price Summary

El sistema DEBE calcular el total estimado de la compra por tienda.

#### Scenario: Total per store

- GIVEN hay items con precios registrados en diferentes tiendas
- WHEN el usuario accede al resumen de precios
- THEN se muestra el total estimado por tienda
- AND se indica cuál tienda ofrece el menor gasto total
- AND items sin precio se excluyen del total con indicador

### Requirement: Price History

El sistema DEBE mantener un registro de precios anteriores para detectar variaciones.

#### Scenario: View price history

- GIVEN un item tiene múltiples registros de precio en una tienda
- WHEN el usuario selecciona "Ver historial de precios"
- THEN se muestra una lista cronológica con fechas y montos
- AND se indica si el precio actual subió o bajó respecto al anterior

## Data Model

```
households/{hid}/items/{itemId}/prices/{storeId}
  ├── amount: number
  ├── currency: "CRC" (default — Costa Rican colones)
  ├── recordedBy: string (uid)
  └── recordedAt: timestamp
```

## Dependencies

- `shopping-list`: Precios se asocian a items
- `store-management`: Precios se asocian a tiendas
- `auth`: Identifica quién registró el precio

## Edge Cases

- Precio de $0 → permitido (producto en promoción gratis)
- Precio negativo → rechazado con "El precio no puede ser negativo"
- Muchos precios por item (>10 tiendas) → paginate la lista de precios
- Currency hardcoded a CRC (colones costarricenses) en v1 — soporte multi-moneda diferido a v1.1
