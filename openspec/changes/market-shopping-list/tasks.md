# Tasks: Market — Shared Shopping List App

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 4,500–6,000 (additions only, greenfield) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | 5 PRs (one per phase), each under 800 lines |
| Delivery strategy | ask-on-risk |
| Chain strategy | feature-branch-chain |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation: scaffold, auth, household, nav | PR 1 → feature/market | Base = feature/market |
| 2 | Core List: items, stores, sync, offline | PR 2 → PR 1 | Base = PR 1 branch |
| 3 | Purchase Flow: check-off, prices, comparison | PR 3 → PR 2 | Base = PR 2 branch |
| 4 | History + Polish: history, UI, errors | PR 4 → PR 3 | Base = PR 3 branch |
| 5 | CI/CD: GitHub Actions, signing, release | PR 5 → PR 4 | Base = PR 4 branch |

---

## Phase 1: Foundation

- [x] 1.1 Create Android project scaffold: `build.gradle.kts` (project + app), `settings.gradle.kts`, `gradle.properties`, `AndroidManifest.xml`, SDK 24–34, Kotlin + Compose plugins, dependencies (Hilt, Navigation Compose, Firebase BOM, Material 3).
- [x] 1.2 Create `app/src/main/java/com/market/MarketApp.kt` — `@HiltAndroidApp` Application class. Create `app/src/main/java/com/market/MainActivity.kt` — single-activity `@AndroidEntryPoint` with `setContent { MarketTheme { ... } }`.
- [x] 1.3 Create design system: `presentation/theme/Color.kt` (teal #0D9488 palette), `Type.kt`, `Theme.kt` (dynamic color + fallback, dark mode). Verify: app compiles and renders a teal-themed empty screen.
- [x] 1.4 Create app icon: `res/drawable/ic_launcher_foreground.xml` (vector "M"), `ic_launcher_background.xml` (#0D9488), `ic_launcher.xml` adaptive, `ic_launcher_round.xml`. Legacy PNG fallbacks in mipmap folders.
- [x] 1.5 Create Hilt modules: `di/AppModule.kt` (Firebase Auth + Firestore providers), `di/RepositoryModule.kt`, `di/DataSourceModule.kt`.
- [x] 1.6 Create domain layer: `domain/model/User.kt`, `domain/repository/AuthRepository.kt` interface (getCurrentUser, signIn, signOut), `domain/usecase/auth/SignInUseCase.kt`, `domain/usecase/auth/GetCurrentUserUseCase.kt`.
- [x] 1.7 Create data layer: `data/remote/AuthDataSource.kt` (Google Sign-In with `signInWithCredential`, create/update `users/{uid}` doc, session persistence via `FirebaseAuth.currentUser`), `data/repository/AuthRepositoryImpl.kt`.
- [x] 1.8 Create `presentation/screen/auth/LoginScreen.kt` — Google Sign-In button, error state with retry, loading indicator. Verify: first-time sign-in creates `users/{uid}`, returning user updates `lastLoginAt`.
- [x] 1.9 Create domain: `domain/model/Household.kt`, `domain/model/Member.kt`, `domain/repository/HouseholdRepository.kt` (create, join, generateInviteCode, getMembers, removeMember, leave), use cases for each operation.
- [x] 1.10 Create data: `data/remote/HouseholdDataSource.kt` (Firestore CRUD for `households/{hid}`, `members/{uid}`, invite code generation with 7-day expiry), `data/repository/HouseholdRepositoryImpl.kt`.
- [x] 1.11 Create `presentation/screen/household/CreateHouseholdScreen.kt` — name input (1–50 chars validation), creates household + assigns admin role. Create `JoinHouseholdScreen.kt` — 6-char code input, validates expiry, adds as member.
- [x] 1.12 Create navigation: `presentation/navigation/Route.kt` (sealed class: Login, CreateHousehold, JoinHousehold, ShoppingList, Stores, Prices, History, TripDetail, Settings), `presentation/navigation/NavHost.kt` with deep link `market://invite?code={code}`.
- [x] 1.13 Create `presentation/component/LoadingIndicator.kt` and `presentation/component/EmptyState.kt` — reusable composables used across screens. Wire Login → CreateHousehold/JoinHousehold → ShoppingList nav flow.

**Acceptance criteria**: App compiles, Google Sign-In works, household creation assigns admin, join adds member with member role, invite code generates with 7-day expiry, deep link pre-fills code, nav flow works end-to-end.

---

## Phase 2: Core List

- [x] 2.1 Create domain: `domain/model/ShoppingItem.kt`, `domain/repository/ItemRepository.kt` (add, update, delete, observeByHousehold), use cases (AddItemUseCase, EditItemUseCase, DeleteItemUseCase, ObserveItemsUseCase).
- [x] 2.2 Create `data/remote/ItemDataSource.kt` — Firestore CRUD for `households/{hid}/items/{itemId}`, all writes use `serverTimestamp()`. `data/repository/ItemRepositoryImpl.kt` with real-time snapshot listeners.
- [x] 2.3 Create domain: `domain/model/Store.kt`, `domain/repository/StoreRepository.kt` (add, update, delete, reorder, observeByHousehold), use cases (AddStoreUseCase, EditStoreUseCase, DeleteStoreUseCase, ReorderStoresUseCase).
- [x] 2.4 Create `data/remote/StoreDataSource.kt` — Firestore CRUD for `households/{hid}/stores/{storeId}`, order field as `max(order)+1` on create, batch update on reorder. `data/repository/StoreRepositoryImpl.kt`.
- [x] 2.5 Create `presentation/component/ItemCard.kt` — checkbox, name (2-line ellipsis), store badge, price badge. Supports swipe-to-delete (confirm dialog), long-press edit dialog, tap toggle.
- [x] 2.6 Create `presentation/component/StoreHeader.kt` — store name + icon, admin drag handle (long-press reorder), overflow menu (edit/delete). Hidden if store has no items.
- [x] 2.7 Create `presentation/component/CheckOffDialog.kt` — admin-only optional reason text field on check-off.
- [x] 2.8 Create `presentation/screen/list/ShoppingListScreen.kt` — items grouped by store (order field), "Sin asignar" section at bottom, FAB to add item, bottom nav (List, Prices, History, Settings). Real-time updates via snapshot listeners.
- [x] 2.9 Enable offline persistence: `FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setSynchronizeTabs(true)` in `AppModule.kt`. Verify: items add/load without network, sync on reconnect.
- [x] 2.10 Create `presentation/component/OfflineBanner.kt` — observes `FirebaseFirestore.isNetworkConnected`, shows teal banner "Sin conexión — los cambios se sincronizarán automáticamente". Conditionally visible across all screens.

**Acceptance criteria**: Items CRUD works with real-time sync across 2 devices (< 2s), stores reorder with drag-and-drop, items group by store, offline add/load works, offline banner shows/hides correctly.

---

## Phase 3: Purchase Flow

- [x] 3.1 Add check-off logic to `ItemRepository`: toggle `isChecked`, set `checkedBy`/`checkedAt`/`checkReason` (admin only reason). Create `domain/usecase/item/ToggleCheckUseCase.kt` with admin-reason parameter.
- [x] 3.2 Create domain: `domain/model/Price.kt`, `domain/repository/PriceRepository.kt` (record, update, getForItem, observeHistory), use cases (RecordPriceUseCase, GetCheapestStoreUseCase, GetPriceHistoryUseCase).
- [x] 3.3 Create `data/remote/PriceDataSource.kt` — Firestore CRUD for `households/{hid}/items/{itemId}/prices/{storeId}`, validates non-negative amount. `data/repository/PriceRepositoryImpl.kt`.
- [x] 3.4 Create `presentation/component/PriceBadge.kt` — formats CRC (₡ symbol), teal highlight + "Más barato" badge when cheapest, "Sin precio registrado" + register prompt when no prices.
- [x] 3.5 Create `presentation/screen/price/PriceComparisonScreen.kt` — list all items, show cheapest store per item, total estimated per store, highlight cheapest overall store. Items without prices excluded from totals.
- [x] 3.6 Create `presentation/screen/price/PriceHistoryScreen.kt` — chronological price records per item per store, up/down arrow indicator vs previous price.
- [x] 3.7 Add store assignment to item: bottom sheet on item long-press to select store, "Sin tienda" option to unassign. Update `ShoppingListScreen` to show items grouped by store with `StoreHeader`.

**Acceptance criteria**: Admin check-off records reason, prices record per store, cheapest badge shows on items with 2+ store prices, price comparison screen shows totals, price history shows chronology with up/down indicators.

---

## Phase 4: History + Polish

- [x] 4.1 Create domain: `domain/model/Trip.kt`, `domain/repository/TripRepository.kt` (completeTrip, getHistory, getTripDetail, deleteTrip), use cases (CompleteTripUseCase, GetHistoryUseCase, DeleteTripUseCase).
- [x] 4.2 Create `data/remote/TripDataSource.kt` — Firestore CRUD for `households/{hid}/trips/{tripId}`, snapshot of checked items with storeName/price/quantity, `totalEstimated` sum. Paginate at 20 trips. `data/repository/TripRepositoryImpl.kt`.
- [x] 4.3 Create "Finalizar compra" button on `ShoppingListScreen` — enabled only when items checked, creates trip, unchecks items. Error: "No hay items marcados para registrar".
- [x] 4.4 Create `presentation/screen/history/PurchaseHistoryScreen.kt` — chronological list (completedAt DESC), each shows date, who, item count, total. Empty state: "Aún no hay compras registradas". Lazy loading pagination.
- [x] 4.5 Create `presentation/screen/history/TripDetailScreen.kt` — full trip view: date, person, items grouped by store with subtotals, total at bottom. Admin swipe-to-delete trip (confirm dialog).
- [x] 4.6 Add swipe-to-delete to `ItemCard` — confirm dialog "¿Eliminar {name}?", only creator or admin can delete. Member deletion attempt: snackbar "Solo los administradores pueden realizar esta acción".
- [x] 4.7 Add inline validation errors: empty item name (1–50 chars), negative price ("El precio no puede ser negativo"), invalid invite code ("Código inválido o expirado"). Use `OutlinedTextField` with `supportingText`.
- [x] 4.8 Add error states for all Firestore operations: network errors, permission denied, generic failures. Each maps to user message from design spec. Snackbar for transient errors, inline for form errors.
- [x] 4.9 Create `presentation/screen/settings/SettingsScreen.kt` — household name display, invite code + share button (deep link), member list with admin controls (remove, transfer role), leave household button (members).
- [x] 4.10 Add animations: screen transitions (fade + slide), list item add/remove (AnimatedVisibility), check-off toggle (scale), offline banner slide-in. Keep under 200ms duration.

**Acceptance criteria**: Trip creation snapshots checked items, history shows paginated list, trip detail groups by store with subtotals, swipe-to-delete works with role checks, all validation errors show inline, offline banner animates, all error states from design spec handled.

---

## Phase 5: CI/CD

- [x] 5.1 Create `.github/workflows/android.yml` — triggers on push to main and PRs, steps: checkout, setup JDK 17, Gradle cache (`actions/cache`), `./gradlew assembleDebug`, upload APK artifact.
- [x] 5.2 Create release workflow: `.github/workflows/release.yml` — triggers on tag push (`v*`), builds release APK with signing, creates GitHub Release with APK attached.
- [x] 5.3 Create `keystore.properties` template (gitignored) and signing config in `app/build.gradle.kts` — reads from `keystore.properties` for release builds, skips for debug.
- [x] 5.4 Add `local.properties` and `keystore.properties` to `.gitignore`. Document Firebase config placement (`google-services.json` in `app/`).
- [x] 5.5 Create `play_store_icon.png` (512×512) and commit to repo root for Play Store listing.

**Acceptance criteria**: Push to main triggers debug APK build, tag push builds release APK with signing, APK installs on Android 7+, all secrets gitignored, workflow completes in < 10 minutes.
