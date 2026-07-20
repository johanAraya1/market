# Design: Market — Shared Shopping List App

## Technical Approach

Greenfield Android app using Kotlin + Jetpack Compose + Material 3 with Clean Architecture (data/domain/presentation layers). Firebase backend (Auth Google Sign-In + Firestore real-time sync + offline persistence). Dependency injection via Hilt. Navigation via Jetpack Navigation Compose. MVVM with StateFlow for state management. All prices in Costa Rican colones (CRC). App name "Market", logo minimal "M" in teal #0D9488.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|--------------|-----------|
| Architecture | Clean Architecture (data/domain/presentation) | MVI, TCA, simple MVVM | Layer isolation enables testability; domain layer has zero Android deps |
| DI | Hilt | Koin, Manual | Official Google recommendation, compile-time safety, Compose integration |
| Navigation | Jetpack Navigation Compose | Decompose, Custom | Official, type-safe routes, deep link support for invite links |
| State | ViewModel + StateFlow | Molecule, Orbit | Standard pattern, Hilt integration, lifecycle-aware |
| Backend | Firebase (Auth + Firestore) | Supabase, custom backend | Real-time sync built-in, free tier fits scope, offline persistence native |
| UI | Material 3 Compose | XML views, Compose Multiplatform | Modern, declarative, dynamic color support, single-platform target |

## Module Structure

```
app/
├── data/
│   ├── remote/          # Firestore data sources
│   │   ├── AuthDataSource.kt
│   │   ├── HouseholdDataSource.kt
│   │   ├── ItemDataSource.kt
│   │   ├── StoreDataSource.kt
│   │   ├── PriceDataSource.kt
│   │   └── TripDataSource.kt
│   ├── repository/      # Repository implementations
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── HouseholdRepositoryImpl.kt
│   │   ├── ItemRepositoryImpl.kt
│   │   ├── StoreRepositoryImpl.kt
│   │   ├── PriceRepositoryImpl.kt
│   │   └── TripRepositoryImpl.kt
│   └── model/           # Firestore DTOs
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic
│       ├── auth/        # SignInUseCase, GetCurrentUserUseCase
│       ├── household/   # CreateHouseholdUseCase, JoinHouseholdUseCase, InviteCodeUseCase
│       ├── item/        # AddItemUseCase, ToggleCheckUseCase, DeleteItemUseCase
│       ├── store/       # AddStoreUseCase, ReorderStoresUseCase
│       ├── price/       # RecordPriceUseCase, GetCheapestStoreUseCase
│       └── trip/        # CompleteTripUseCase, GetHistoryUseCase
├── presentation/
│   ├── navigation/      # NavHost, Route sealed class
│   ├── screen/          # Screen composables
│   │   ├── auth/        # LoginScreen
│   │   ├── household/   # CreateHouseholdScreen, JoinHouseholdScreen
│   │   ├── list/        # ShoppingListScreen (main)
│   │   ├── store/       # StoreManagementScreen
│   │   ├── price/       # PriceComparisonScreen, PriceHistoryScreen
│   │   ├── history/     # PurchaseHistoryScreen, TripDetailScreen
│   │   └── settings/    # SettingsScreen (household admin)
│   ├── component/       # Reusable composables
│   │   ├── ItemCard.kt
│   │   ├── StoreHeader.kt
│   │   ├── PriceBadge.kt
│   │   ├── EmptyState.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── OfflineBanner.kt
│   │   └── CheckOffDialog.kt
│   └── theme/           # Theme.kt, Color.kt, Type.kt
└── di/                  # Hilt modules
    ├── AppModule.kt
    ├── RepositoryModule.kt
    └── DataSourceModule.kt
```

## Navigation Flow

```
LoginScreen
    └─→ CreateHouseholdScreen / JoinHouseholdScreen
            └─→ ShoppingListScreen (main, bottom nav)
                    ├── StoreManagementScreen (admin)
                    ├── PriceComparisonScreen
                    ├── PurchaseHistoryScreen → TripDetailScreen
                    └── SettingsScreen (household admin)
```

Deep link: `market://invite?code={code}` → JoinHouseholdScreen with pre-filled code.

## Firestore Schema

```
users/{uid}
  displayName: string
  email: string
  photoUrl: string
  createdAt: timestamp
  lastLoginAt: timestamp

households/{hid}
  name: string
  createdAt: timestamp
  createdBy: string (uid)
  inviteCode: string (6 chars, nullable)
  inviteCodeExpiry: timestamp (nullable)

households/{hid}/members/{uid}
  role: "admin" | "member"
  displayName: string
  joinedAt: timestamp

households/{hid}/items/{itemId}
  name: string
  addedBy: string (uid)
  createdAt: timestamp
  updatedAt: timestamp
  isChecked: boolean
  checkedBy: string (uid, nullable)
  checkedAt: timestamp (nullable)
  checkReason: string (nullable)
  storeId: string (nullable → stores/{sid})
  price: number (nullable)
  quantity: number (default 1)

households/{hid}/stores/{storeId}
  name: string
  order: number
  createdAt: timestamp
  updatedAt: timestamp (nullable)

households/{hid}/items/{itemId}/prices/{storeId}
  amount: number
  currency: "CRC"
  recordedBy: string (uid)
  recordedAt: timestamp

households/{hid}/trips/{tripId}
  completedBy: string (uid)
  completedByName: string
  completedAt: timestamp
  totalEstimated: number
  items[]: { name, storeId, storeName, price, quantity }
```

**Indexes**:
- `households/{hid}/stores` → `order` ASC
- `households/{hid}/trips` → `completedAt` DESC
- `households/{hid}/items` → `storeId` ASC, `isChecked` ASC

**Security Rules**: Users can only access collections under `households/{hid}` where they have a member document. Admin-only writes enforced at app layer (Firestore rules don't check role — client-side gate + server timestamps for audit).

## Color Palette

| Token | Light | Dark |
|-------|-------|------|
| Primary | #0D9488 | #2DD4BF |
| On Primary | #FFFFFF | #003D38 |
| Primary Container | #CCFBF1 | #0D3D38 |
| Secondary | #0F766E | #5EEAD4 |
| Error | #DC2626 | #FCA5A5 |
| Background | #FAFAFA | #121212 |
| Surface | #FFFFFF | #1E1E1E |

Dynamic color on API 31+, teal fallback below.

## Typography

Material 3 default scale. Display Large for screen titles, Title Medium for section headings, Body Large for primary content, Body Medium for list items, Label Small for badges/captions. System font scaling respected.

## Offline Strategy

Firestore `enablePersistence()` with `synchronizeTabs(true)`. Offline banner component observes `FirebaseFirestore.isNetworkConnected`. Last-write-wins with `serverTimestamp()` for conflict resolution. All writes queue automatically and sync when connectivity returns.

## Error Handling

| Error | User Message | Action |
|-------|-------------|--------|
| Network unavailable | "Sin conexión — los cambios se sincronizarán automáticamente" | Banner, queue writes |
| Auth failure | "Error al iniciar sesión" | Retry button |
| Permission denied | "Solo los administradores pueden realizar esta acción" | Snackbar |
| Validation (empty name) | "El nombre debe tener entre 1 y 50 caracteres" | Inline error |
| Validation (negative price) | "El precio no puede ser negativo" | Inline error |
| Invalid invite code | "Código inválido o expirado" | Inline error |
| Firestore error | "Error al guardar — intenta de nuevo" | Snackbar + retry |

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Unit | Use cases, repository logic, formatters | JUnit + MockK, fake Firestore |
| Integration | Repository ↔ Firestore interaction | Firestore emulator + Hilt tests |
| UI | Screen rendering, navigation, interactions | Compose Testing + Roborazzi for screenshots |

## Migration / Rollout

No migration — greenfield. Phased delivery per proposal: Foundation → Core List → Purchase Flow → History + Polish → CI/CD.

## Open Questions

None — all specs are complete and unambiguous.
