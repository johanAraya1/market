# Exploration: Market — Shared Shopping List App

## 1. Product Domain Summary

**Market** is a collaborative shopping list Android app for small groups (3 users max). One admin manages the shopping experience by organizing items into stores, tracking prices, and checking off purchases. All members see a shared, real-time list.

### Core User Journeys
1. **Member adds item** → item appears in "Unsorted" section → admin drags to store
2. **Admin organizes** → drags items into store sections → sets prices
3. **Admin shops** → checks off items → marks "not purchased" with reason → members see results
4. **Price comparison** → admin views same product across stores → sees price differences

### Constraints
- **Firebase Spark (free tier)**: 1 GiB storage, 50K reads/day, 20K writes/day, 20K deletes/day
- **No Cloud Functions** on Spark → no server-side push notifications
- **3 users max** → very low Firestore usage, well within free tier
- **APK via GitHub Actions** → no local Android Studio required
- **Material Design 3** → beautiful, professional UI with dynamic color support

---

## 2. User Model

### Roles
| Role | Capabilities |
|------|-------------|
| **Admin** | Full control: create/delete stores, assign items to stores, set prices, check off purchases, reorder items, manage members, delete any item |
| **Member** | Add items, edit own items (name, quantity, notes), delete own items, view all items and store organization, see purchase results |

### Authentication
- **Google Sign-In** via Firebase Auth — simplest integration, no phone verification, instant UX
- After sign-in, check if user's UID exists in any list's `memberIds`
- If not in any list → show join screen (enter invite code)
- If in a list → show the shared list

### Invitation Flow
- Admin creates a list → system generates a **6-character invite code** (e.g., `SHOP99`)
- Admin shares code verbally, via text, or QR code
- New user opens app → signs in with Google → enters code → joins the list
- Code is stored on the list document, valid until list is deleted
- Admin can regenerate code if compromised (rare for 3 users)

### User Document
```
users/{uid}
  dn: string       // display name
  em: string       // email
  role: "admin" | "member"
  avatarUrl?: string
  createdAt: timestamp
```

**Design Decision**: The `role` is per-list, not global. But since we cap at 3 users and one list, storing role on the user doc is simpler. If user joins a second list (not supported), role would need to move to a membership subcollection. For now, one user = one list = one role.

**Alternative considered**: Store role in a `lists/{listId}/members/{uid}` subcollection. Pros: supports multiple lists. Cons: extra reads, overkill for 3-user single-list app. **Decision: role on user doc** for simplicity.

---

## 3. List Structure

### Active List vs History
- Each list has an `active` boolean field
- When shopping trip is done, admin can "archive" the list → sets `active: false`
- Archived lists are hidden from main view but accessible via history
- Items in archived lists keep their purchase status for reference (e.g., "last time milk was $2.50")
- Admin can create a new list (starts fresh, new items)

### List Document
```
lists/{listId}
  name: string           // e.g., "Weekly Shopping"
  adminId: string        // UID of admin
  memberIds: string[]    // [uid1, uid2, uid3] — max 3
  inviteCode: string     // 6-char code, unique
  active: boolean        // true = current list
  createdAt: timestamp
  updatedAt: timestamp
```

### Why Not Separate "History" Collection
- 3 users, few lists → no performance concern reading all lists
- Single `lists` collection with `active` filter is simpler
- Query: `where("active", "==", true)` for main view, `where("active", "==", false)` for history

---

## 4. Store / Location Model

Admin creates stores to organize items. Each store represents a physical shopping location.

### Store Document
```
lists/{listId}/stores/{storeId}
  name: string     // "Supermercado", "Feria", "Ferretería"
  icon: string     // emoji: "🛒", "🏪", "🔧"
  sort: number     // ordering (10, 20, 30... for easy insert)
  createdAt: timestamp
```

### CRUD Operations
| Operation | Who | Behavior |
|-----------|-----|----------|
| Create | Admin only | Add store with name + icon |
| Read | All members | See all stores in order |
| Update | Admin only | Change name, icon, reorder |
| Delete | Admin only | Items in that store become unassigned (status reset to unsorted) |

### Icon Selection
- Predefined set of ~20 emojis (not freeform) for consistency
- Examples: 🛒 Supermercado, 🥬 Feria, 🔧 Ferretería, 💊 Farmacia, 🧴 Farmacia, 🏪 Tienda, 🥩 Carnicería, 🧀 Lácteos
- Admin picks from a grid during store creation

### Sort Order
- Uses `sort` field (integer, step 10)
- Drag-and-drop reordering updates `sort` values
- Admin-only operation

---

## 5. Item Model

Items are the core entities. Each item lives in ONE list and optionally in ONE store.

### Item Document
```
lists/{listId}/items/{itemId}
  name: string          // "milk", "cheese", "tomatoes"
  storeId: string|null  // null = unsorted
  price: number|null    // price at assigned store
  addedBy: string       // UID of member who added it
  addedByName: string   // display name (denormalized)
  status: "pending"|"purchased"|"skipped"
  skipReason: string|null  // "out of stock", "too expensive", etc.
  qty: number           // quantity (default 1)
  unit: string|null     // "kg", "lb", "units", "packs"
  note: string|null     // "organic preferred", "any brand"
  sort: number          // ordering within store section
  createdAt: timestamp
  updatedAt: timestamp
```

### Permissions Matrix
| Action | Admin | Member (own items) | Member (others' items) |
|--------|-------|-------------------|----------------------|
| Add item | ✅ | ✅ | ❌ |
| Edit name, qty, unit, note | ✅ | ✅ | ❌ |
| Delete item | ✅ | ✅ | ❌ |
| Assign to store | ✅ | ❌ | ❌ |
| Set price | ✅ | ❌ | ❌ |
| Change status | ✅ | ❌ | ❌ |
| Reorder items | ✅ | ❌ | ❌ |
| View all items | ✅ | ✅ | ✅ |

### Unsorted Items
- Items without a `storeId` appear at the top of the list in an "Unsorted" section
- Admin drags them into store sections
- This is the primary admin workflow: sort → organize → shop

### Item Lifecycle
```
Added (pending, unsorted) 
  → Admin assigns to store + sets price 
  → Admin shops and marks purchased/skipped
  → Item moves to "completed" section
```

---

## 6. Purchase Flow

### Admin Shopping Mode
1. Admin opens the list → sees items organized by store
2. Admin goes to a store section → checks off items as purchased
3. For items that are unavailable, admin marks as "skipped" with optional reason

### Purchase Actions
| Action | Effect |
|--------|--------|
| **Mark Purchased** | `status: "purchased"` — item gets ✅ checkmark, price recorded |
| **Mark Skipped** | `status: "skipped"` — item gets ⏭️ icon, admin enters reason (optional) |
| **Undo** | Revert to `status: "pending"` — item returns to active state |

### Skip Reasons (preset list)
- Out of stock
- Too expensive
- Not the right brand
- Expired/bad quality
- Other (free text)

### Member View
- Members see real-time updates as admin checks items off
- Purchased items shown with green checkmark and price
- Skipped items shown with gray icon and reason tooltip
- Members cannot change purchase status (admin-only)

### Visual Design
- Purchased items: ✅ green, slight opacity reduction, price badge
- Skipped items: ⏭️ gray, strikethrough name, reason as subtitle
- Pending items: normal appearance, full opacity

---

## 7. Price Comparison Logic

### How It Works
- Admin assigns item to a store and sets the price
- If the same product name appears at multiple stores, admin can see all prices
- Price comparison is a **view/screen**, not a data model change

### Price Comparison View
- Query: all items with the same `name` across different `storeId` values
- Display: table/list showing item name, store, price
- Example:
  ```
  Milk
    🛒 Supermercado: $2.50
    🥬 Feria: $2.00  ← cheapest
  ```
- Highlight cheapest option

### Data Model Decision
**Option A (chosen):** Store price on the item document itself (`price` field). Each item is unique, lives in one store, has one price.
- Pros: Simple queries, no joins, minimal reads
- Cons: Same product at two stores = two item documents
- Works because: admin can add the same product name twice for different stores

**Option B (rejected):** Separate `prices` subcollection per item.
- Pros: Track price history
- Cons: Extra reads, complexity, overkill for 3 users

**Option C (rejected):** Price map on item (`{storeId: price}`).
- Pros: Single document
- Cons: Item can only be in one store visually, so map is confusing

### Price History (stretch feature)
- When admin changes a price, the old price is overwritten
- No automatic history tracking on free tier (would need Cloud Functions or client-side writes)
- Admin can manually note price changes in item notes
- **Deferred**: Price history can be added later with a `priceHistory` array field

---

## 8. Notification Strategy

### Constraint
- Firebase Cloud Functions require **Blaze plan** (pay-as-you-go)
- FCM server-side sending requires a server component
- Free tier = no server-side push notifications

### Solution: Real-Time Listeners + Background Sync
1. **Firestore Snapshot Listeners** — instant updates when app is open
2. **Firestore Offline Persistence** — writes queued, synced when online
3. **Background Listener** — Firestore SDK maintains connection even when app is backgrounded (limited by OS, ~1-2 minutes on Android)

### What Users Experience
- **App open**: Instant updates (real-time listener)
- **App backgrounded (< 2 min)**: Updates arrive when user returns
- **App killed**: User opens app → syncs immediately → sees all changes

### When App Is Open
- Firestore snapshot listener on `lists/{listId}/items` collection
- Changes appear instantly in UI
- Subtle animation for new/updated items

### Optional Enhancement: OneSignal Free Tier
- OneSignal offers free push notifications (up to 10K subscribers)
- No Cloud Functions needed — client SDK handles token registration
- Can send pushes via OneSignal dashboard or REST API
- **Deferred to Phase 2** — not needed for MVP

---

## 9. Offline Capability

### Firestore SDK Offline Persistence
- **Enabled by default** on Android Firestore SDK
- Local cache stores recently accessed documents
- Writes are queued and synced when connection is restored
- Conflict resolution: last-write-wins (Firestore default)

### UI Indicators
- **Online**: Green dot or subtle indicator
- **Offline**: Orange dot with "Offline — changes sync when connected" message
- **Syncing**: Brief spinner or pulse animation

### Offline Behavior
| Operation | Offline Behavior |
|-----------|-----------------|
| Add item | Queued locally, synced when online |
| Edit item | Queued locally, synced when online |
| Mark purchased | Queued locally, synced when online |
| Reorder stores | Queued locally, synced when online |
| View items | Shows cached data (may be stale) |

### Data Consistency
- For 3 users on a shared list, conflicts are rare
- If two users edit the same item offline, last-write-wins on sync
- Acceptable for a shopping list app (not financial data)

---

## 10. Color Palette & Design System

### Strategy
- **Dynamic Color** (Android 12+): Derive palette from user's wallpaper
- **Fallback** (Android < 12): Custom seed color — **Teal/Green** (`#1EB980`) for fresh, organic, shopping vibe
- **Dark Mode**: Full support via `isSystemInDarkTheme()`

### Custom Color Palette (Fallback)

| Role | Light | Dark | Usage |
|------|-------|------|-------|
| Primary | `#1EB980` | `#66FFC7` | Main actions, FAB, active states |
| Primary Container | `#C4F0DC` | `#004D32` | Chips, badges, selected items |
| Secondary | `#4A635D` | `#A0CDB8` | Subtle actions, secondary buttons |
| Tertiary | `#E8985A` | `#FFB68C` | Price highlights, warnings |
| Error | `#BA1A1A` | `#FFB4AB` | Delete actions, errors |
| Surface | `#FBFDF9` | `#191C1A` | Backgrounds |
| Surface Container | `#EFF1ED` | `#1E211F` | Cards, list items |
| On Surface | `#191C1A` | `#E1E3DF` | Primary text |

### Design Principles
1. **Clean & Professional** — lots of whitespace, clear hierarchy
2. **Color-Coded Stores** — each store gets a distinct accent color for visual grouping
3. **Smooth Animations** — item transitions, drag-and-drop, purchase checkmarks
4. **Accessible** — proper contrast ratios, scalable text, touch targets ≥ 48dp

### Key UI Components
| Component | Style |
|-----------|-------|
| App Bar | Top app bar with list name, member avatars, settings gear |
| Item Card | Elevated card with name, price badge, store icon, status indicator |
| Store Section | Collapsible section with store icon + name header |
| FAB | "+" to add item, with item name input |
| Bottom Sheet | Item detail/edit bottom sheet |
| Drag Handle | Visual drag handle for reordering items into stores |
| Purchase Checkbox | Animated circular checkbox (not standard checkbox) |
| Invite Code Display | Large, centered code with copy button + QR code |

---

## 11. Firestore Data Model — Complete

### Collections Hierarchy
```
firestore/
├── users/{uid}
│   ├── dn: string
│   ├── em: string
│   ├── role: "admin" | "member"
│   ├── avatarUrl?: string
│   └── createdAt: timestamp
│
└── lists/{listId}
    ├── name: string
    ├── adminId: string
    ├── memberIds: string[]
    ├── inviteCode: string
    ├── active: boolean
    ├── createdAt: timestamp
    ├── updatedAt: timestamp
    │
    ├── stores/{storeId}          // subcollection
    │   ├── name: string
    │   ├── icon: string
    │   ├── sort: number
    │   └── createdAt: timestamp
    │
    └── items/{itemId}            // subcollection
        ├── name: string
        ├── storeId: string|null
        ├── price: number|null
        ├── addedBy: string
        ├── addedByName: string
        ├── status: "pending"|"purchased"|"skipped"
        ├── skipReason: string|null
        ├── qty: number
        ├── unit: string|null
        ├── note: string|null
        ├── sort: number
        ├── createdAt: timestamp
        └── updatedAt: timestamp
```

### Firestore Security Rules (Conceptual)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own document
    match /users/{uid} {
      allow read: if request.auth.uid == uid;
      allow write: if request.auth.uid == uid;
    }
    
    // List members can read list and subcollections
    match /lists/{listId} {
      allow read: if request.auth.uid in resource.data.memberIds;
      allow create: if request.auth.uid != null;
      allow update, delete: if request.auth.uid == resource.data.adminId;
      
      // Stores: only admin can write
      match /stores/{storeId} {
        allow read: if true;  // member check is on parent
        allow write: if request.auth.uid == get(/databases/$(database)/documents/lists/$(listId)).data.adminId;
      }
      
      // Items: members can add/edit their own, admin can do anything
      match /items/{itemId} {
        allow read: if true;  // member check is on parent
        allow create: if request.auth.uid != null;
        allow update: if request.auth.uid == resource.data.addedBy 
                    || request.auth.uid == get(/databases/$(database)/documents/lists/$(listId)).data.adminId;
        allow delete: if request.auth.uid == resource.data.addedBy
                   || request.auth.uid == get(/databases/$(database)/documents/lists/$(listId)).data.adminId;
      }
    }
  }
}
```

### Indexes Required
| Collection | Fields | Purpose |
|-----------|--------|---------|
| `items` | `status` ASC, `sort` ASC | Main list view |
| `items` | `storeId` ASC, `sort` ASC | Items within a store |
| `items` | `name` ASC, `storeId` ASC | Price comparison |
| `stores` | `sort` ASC | Store ordering |
| `lists` | `inviteCode` ASC | Join by code lookup |
| `lists` | `active` DESC, `createdAt` DESC | List history |

### Free Tier Budget Analysis (3 Users)
| Metric | Daily Budget | Estimated Usage | Margin |
|--------|-------------|-----------------|--------|
| Reads | 50,000 | ~600 (20 per user × 10 sessions × 3 users) | 98.8% |
| Writes | 20,000 | ~90 (3 items per user × 3 sessions × 3 users) | 99.6% |
| Deletes | 20,000 | ~30 | 99.9% |
| Storage | 1 GiB | ~5 MB (100 lists × 50 items × 1 KB avg) | 99.5% |

**Conclusion**: Free tier is extremely comfortable for 3 users. Even with 10x usage, we're well within limits.

---

## 12. Navigation Structure

### Screen Flow
```
Splash → Auth (Google Sign-In)
  ├── No list → Join Screen (enter invite code)
  │     → Main List Screen
  └── Has list → Main List Screen

Main List Screen
  ├── App Bar: list name, members, settings
  ├── Store Sections (collapsible)
  │   ├── Unsorted (top, items without store)
  │   ├── Store 1 (e.g., Supermercado)
  │   ├── Store 2 (e.g., Feria)
  │   └── Store 3 (e.g., Ferretería)
  └── FAB: Add item

Admin-Only Screens
  ├── Store Management (CRUD stores)
  ├── Price Comparison (by item name)
  ├── Member Management (view members)
  └── List History (archived lists)

Settings
  ├── Profile
  ├── Notifications (future)
  ├── About
  └── Leave List
```

### Bottom Navigation (Admin)
1. **List** — Main shopping list
2. **Prices** — Price comparison view
3. **Stores** — Store management
4. **Settings** — Profile, history, about

### Bottom Navigation (Member)
1. **List** — Main shopping list
2. **History** — Past purchase results
3. **Settings** — Profile, about

---

## 13. Key Architecture Decisions

### Decision Log

| # | Decision | Rationale | Trade-off |
|---|----------|-----------|-----------|
| 1 | Google Sign-In for auth | Simplest integration, no phone verification, instant UX | Requires Google account (universally available) |
| 2 | Role on user doc, not per-list | 3 users, one list — simpler queries, fewer reads | Can't support multiple lists per user (acceptable) |
| 3 | Subcollections for stores/items | Firestore best practice, security rules per subcollection | Slightly more complex queries than root collections |
| 4 | Short field names (dn, em) | Saves storage on Firestore free tier (field names count) | Less readable in console (acceptable for small project) |
| 5 | No Cloud Functions | Free tier constraint | No server-side push notifications, no automated triggers |
| 6 | Real-time listeners only | Free, works with Firestore SDK, instant updates | No push notifications when app is killed |
| 7 | Dynamic color with fallback | Best UX on Android 12+, beautiful fallback for older | Custom palette needs maintenance |
| 8 | Emoji icons for stores | Zero cost, immediate visual recognition, fun | Limited customization, platform-dependent rendering |
| 9 | Item name as price comparison key | Simple, no extra collections, natural user flow | Requires exact name match (no fuzzy search) |
| 10 | `sort` field with step-10 | Easy reorder insertion between items | Requires updating multiple docs on reorder |

---

## 14. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Firestore free tier exceeded | App stops working | Budget analysis shows 99%+ margin; monitor via Firebase console |
| Item name mismatch in price comparison | Incorrect price data | Admin sees exact names; consider autocomplete from past items |
| Offline conflict (2 users edit same item) | Last-write-wins data loss | Acceptable for shopping list; add "last edited by" indicator |
| Google Sign-In dependency | Users without Google account can't join | Universally available; consider email/password as Phase 2 fallback |
| Android OS kills background listener | Stale data when app reopened | Standard Firestore behavior; sync on app resume |
| Invite code sharing friction | Users struggle to join | Provide QR code option, deep link as alternative |

---

## 15. Ready for Proposal

**Status**: ✅ Ready

All domain concepts are defined: user model, list structure, stores, items, purchase flow, price comparison, notifications, offline support, and design system. The Firestore data model is complete with security rules, indexes, and free tier budget analysis.

**Recommendation**: Proceed to `sdd-propose` to define the initial implementation scope (which features to build first, phase ordering, and MVP definition).

**Key question for user**: Do you want photos on items (requires Firebase Cloud Storage, adds ~5MB to free tier) or text-only items for MVP?
