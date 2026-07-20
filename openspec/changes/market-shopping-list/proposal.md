# Proposal: Market — Shared Shopping List App

## Intent

Build a shared shopping list Android app for 2-3 users (one household) with real-time sync, offline support, price comparison across stores, and purchase history. The app must work on the Firebase free tier with no local Android Studio — APK built via GitHub Actions.

## Scope

### In Scope
- Google Sign-In auth + household invite system (code/link)
- Role-based access (admin controls list/store/item management; member edits own items, views all)
- Shared shopping list with Firestore real-time sync
- Items organized by store with admin drag-and-drop ordering
- Check-off with optional reason (admin only)
- Price comparison: same item, different stores
- Purchase history (past shopping trips)
- Offline Firestore persistence
- Polished Material 3 UI with dynamic color + teal fallback
- CI/CD: GitHub Actions APK build

### Out of Scope
- Photo attachments on items (deferred to v1.1)
- Push notifications (FCM deferred — manual refresh acceptable for v1)
- Multiple households per user
- Expense splitting or payment integration
- Localization (Spanish-only v1)
- Widget or wear OS support

## Capabilities

### New Capabilities
- `auth`: Google Sign-In, user profiles, role assignment (admin/member)
- `household`: Household creation, invite code/link, member management
- `shopping-list`: Shared item list with real-time Firestore sync, offline support
- `store-management`: Store CRUD, admin drag-and-drop ordering, item-to-store assignment
- `price-comparison`: Track item prices per store, surface cheapest option
- `purchase-history`: Log completed shopping trips, view past purchases
- `design-system`: Color palette, typography, reusable Compose components

### Modified Capabilities
None — greenfield project.

## Approach

**Tech stack**: Kotlin + Jetpack Compose + Material 3, Firebase Auth + Firestore + FCM, Clean Architecture (data/domain/presentation layers), MVVM with StateFlow.

**Phased implementation**:

| Phase | Deliverable | Scope |
|-------|------------|-------|
| 1 — Foundation | Project scaffold, auth, household | Firebase setup, Google Sign-In, household CRUD, invite flow |
| 2 — Core List | Shared list + stores | Item CRUD, store CRUD, real-time sync, offline persistence |
| 3 — Purchase Flow | Check-off + price comparison | Admin check-off, price tracking per store, cheapest-surface logic |
| 4 — History + Polish | Purchase history, UI refinement | History screen, animations, error states, edge cases |
| 5 — CI + Release | GitHub Actions APK | Build pipeline, signing, release APK |

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `app/src/main/java/com/market/` | New | All source code — clean architecture layers |
| `app/src/main/res/` | New | Themes, strings, icons |
| `app/build.gradle.kts` | New | Dependencies, SDK config |
| `.github/workflows/` | New | APK build pipeline |
| `openspec/specs/` | New | 7 new capability specs |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Firestore free tier limits (1GB storage, 50K reads/day) | Med | Paginate history, prune old data, monitor usage |
| GitHub Actions build time/reliability | Low | Cache Gradle deps, retry on failure |
| Offline conflict resolution | Med | Last-write-wins with server timestamps; admin overrides |
| Small team (1 admin) scope creep | High | Strict phased approach — defer photos, notifications |

## Rollback Plan

- Each phase produces a buildable APK — rollback = revert to last phase tag
- Firestore data lives in cloud — no local data loss risk
- Git tags per phase (`v0.1-foundation`, `v0.2-list`, etc.)

## Dependencies

- Firebase project (Auth + Firestore + FCM enabled)
- Google Cloud Console access for OAuth client config
- GitHub repo with Actions enabled

## Success Criteria

- [ ] APK installs and runs on Android 7+ (API 24)
- [ ] Google Sign-In works for 2+ users
- [ ] Household invite flow completes (admin → member)
- [ ] Items sync in real-time across 2 devices within 2s
- [ ] Offline: list loads and items can be added without network
- [ ] Price comparison shows cheapest store per item
- [ ] Purchase history records and displays past trips
- [ ] UI matches Material 3 specs with no visual bugs
- [ ] App stays within Firebase free tier (1GB / 50K reads/day)
