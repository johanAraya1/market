# Design System Specification

## Purpose

Sistema de diseño: paleta de colores, tipografía, tema Material 3 y componentes Compose reutilizables.

## Requirements

### Requirement: Color Palette

El sistema DEBE usar una paleta de colores basada en teal #0D9488 como color primario.

#### Scenario: Dynamic color with fallback

- GIVEN el dispositivo soporta Android 12+ (API 31+)
- WHEN el usuario abre la app
- THEN se aplica Material 3 dynamic color basado en el wallpaper del usuario
- AND teal #0D9488 se usa como fallback si dynamic color no está disponible

#### Scenario: Color token mapping

- GIVEN la paleta del sistema
- WHEN se renderiza cualquier componente
- THEN los tokens de color se mapean así:
  - Primary: #0D9488 (teal-600)
  - On Primary: #FFFFFF
  - Primary Container: #CCFBF1 (teal-100)
  - Secondary: #0F766E (teal-700)
  - Error: #DC2626 (red-600)
  - Background: #FAFAFA (gray-50) / #121212 (dark mode)
  - Surface: #FFFFFF / #1E1E1E (dark mode)

### Requirement: Typography

El sistema DEBE usar la tipografía predeterminada de Material 3 con escala legible.

#### Scenario: Text hierarchy

- GIVEN cualquier pantalla de la app
- WHEN se renderiza texto
- THEN se aplican los estilos Material 3:
  - Display Large: títulos de pantalla
  - Title Medium: headings de sección
  - Body Large: contenido principal
  - Body Medium: items de lista
  - Label Small: badges, captions
- AND la escala de texto respeta la configuración de accesibilidad del sistema

### Requirement: Reusable Compose Components

El sistema DEBE proporcionar componentes Compose reutilizables para elementos comunes de la UI.

#### Scenario: ItemCard component

- GIVEN un item de la lista de compras
- WHEN se renderiza en la lista
- THEN se muestra como un Card con: checkbox, nombre del item, badge de tienda (si asignado), precio (si registrado)
- AND el Card soporta: swipe-to-delete, long-press para editar, tap para toggle check

#### Scenario: EmptyState component

- GIVEN una pantalla sin datos
- WHEN se renderiza
- THEN se muestra ilustración vectorial centrada + texto descriptivo + botón de acción primaria (si aplica)

#### Scenario: StoreHeader component

- GIVEN items agrupados por tienda
- WHEN se renderiza el heading de una tienda
- THEN se muestra nombre de la tienda con icono de tienda, drag handle (admin), y menú de opciones

#### Scenario: PriceBadge component

- GIVEN un item tiene precio registrado
- WHEN se renderiza el badge de precio
- THEN se muestra el monto formateado como moneda local
- AND si es el más barato, se muestra en teal con badge "Más barato"

#### Scenario: LoadingIndicator component

- GIVEN la app está cargando datos
- WHEN se renderiza el indicador
- THEN se muestra un CircularProgressIndicator con color primary
- AND se acompaña de texto "Cargando..." si la carga supera 1 segundo

### Requirement: Dark Mode Support

El sistema DEBE soportar tema oscuro completo.

#### Scenario: Follow system theme

- GIVEN el dispositivo tiene tema oscuro activado
- WHEN el usuario abre la app
- THEN la app renderiza con colores dark mode
- AND todos los componentes adaptan sus colores automáticamente

### Requirement: Accessibility

El sistema DEBE cumplir con estándares básicos de accesibilidad.

#### Scenario: Content descriptions

- GIVEN cualquier elemento interactivo o imagen
- WHEN un servicio de acceso lo describe
- THEN tiene un `contentDescription` en español
- AND los colores de texto cumplen ratio de contraste 4.5:1 mínimo

## Dependencies

- `auth`: Pantallas de login usan el design system
- `household`: Pantallas de hogar usan el design system
- Todas las demás capabilities consumen estos componentes

## Edge Cases

- Dispositivo sin dynamic color (API < 31) → fallback a teal manual
- Texto largo en items → truncar con ellipsis después de 2 líneas
- Pantallas muy pequeñas (< 320dp) → layout responsive con scroll vertical
