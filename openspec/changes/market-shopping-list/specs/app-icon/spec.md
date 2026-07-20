# App Icon Specification

## Purpose

Icono y logo de la app Market — estilo moderno, minimalista, vectorial.

## Requirements

### Requirement: App Icon Design

El sistema DEBE tener un icono de app que represente la marca "Market" de forma moderna y minimalista.

#### Scenario: Adaptive icon (Android)

- GIVEN la app se instala en un dispositivo Android
- WHEN el icono se renderiza en el launcher
- THEN se muestra como adaptive icon con foreground vectorial sobre fondo teal #0D9488
- AND el icono se adapta a las formas de icono del dispositivo (círculo, cuadrado, squircle)

#### Scenario: Icon elements

- GIVEN el diseño del icono
- WHEN se descompone en capas
- THEN el foreground contiene una letra "M" en estilo sans-serif bold, color blanco (#FFFFFF)
- AND la "M" está estilizada con esquinas redondeadas y proporciones equilibradas
- AND el fondo es un rectángulo redondeado color teal #0D9488
- AND NO hay elementos decorativos adicionales (no carrito, no lista, no bolsa)

### Requirement: Vector Specification

El sistema DEBE proporcionar el icono en formato vectorial para máxima escalabilidad.

#### Scenario: Vector assets

- GIVEN el diseñador genera los assets del icono
- WHEN se exportan para Android
- THEN se genera:
  - `ic_launcher_foreground.xml` (vector drawable, 108x108dp)
  - `ic_launcher_background.xml` (color sólido #0D9488)
  - `ic_launcher.xml` (adaptive icon manifest)
  - `ic_launcher_round.xml` (para dispositivos que lo requieran)
- AND los assets se colocan en `app/src/main/res/`

#### Scenario: Legacy icon fallback

- GIVEN un dispositivo con Android < 8 (API < 26)
- WHEN se necesita un icono no-adaptive
- THEN se genera `ic_launcher.png` en 48x48, 72x72, 96x96, 144x144 px
- AND cada PNG se exporta desde el vectorial a la resolución exacta

### Requirement: Splash Screen Icon

El sistema DEBE mostrar el icono en la splash screen al iniciar la app.

#### Scenario: Splash screen display

- GIVEN el usuario abre la app
- WHEN se muestra la splash screen
- THEN el icono "M" se centra en pantalla sobre fondo blanco (#FFFFFF)
- AND la splash dura el tiempo mínimo del cold start
- AND se transiciona suavemente a la pantalla de login o dashboard

### Requirement: Play Store Listing

El sistema DEBE tener el icono en formato Play Store.

#### Scenario: Store listing assets

- GIVEN se publica la app en Google Play Store
- WHEN se preparan los assets
- THEN se genera `play_store_icon.png` de 512x512 px
- AND el icono es idéntico al adaptive icon pero en formato cuadrado
- AND se guarda en la raíz del repositorio para fácil acceso

## Color Specification

| Elemento | Color | Valor |
|----------|-------|-------|
| Fondo del icono | Teal 600 | #0D9488 |
| Letra "M" | Blanco | #FFFFFF |
| Sombra sutil (si aplica) | Teal 800 | #115E59 |

## Typography Specification (Icon)

| Propiedad | Valor |
|-----------|-------|
| Font | Google Sans / Product Sans (o Roboto Bold como fallback) |
| Weight | Bold (700) |
| Style | Sin serif, esquinas ligeramente redondeadas |
| Tamaño relativo | Ocupa ~60% del espacio del foreground |

## Dependencies

- `design-system`: Colores del icono consisten con la paleta de la app
- Ninguna capability funcional depende del icono

## Edge Cases

- Dispositivo con icon shape masking agresivo → el "M" debe estar centrado para no cortarse
- Fondo dinámico del adaptive icon → solo el foreground es vectorial, el background es color sólido
- Icono en modo monochrome (Android 13+) → versión monocromática del "M" en outline
