# ViNote 2 — Production Product Requirements Document

**Status:** Implementation-ready  
**Target:** ViNote-2 Android + ViNote backend  
**Current state:** Functional prototype / partially wired production architecture  
**Primary goal:** Turn ViNote-2 into a reliable daily-use personal finance companion.

---

## 1. Product Vision

ViNote is an AI-powered personal finance companion for students and young users. The core promise is:

> **The user spends money normally; ViNote detects it, records it once, updates the correct financial state, and helps the user understand their money.**

The existing repository already contains Jetpack Compose UI, ViewModel/domain/data layers, Room/local persistence, Firestore synchronization, OpenRouter integration, offline NLP, goals, transactions, DANA/GoPay adapters, and Android `NotificationListenerService`. Do not rebuild from scratch; harden and connect the existing foundations.

## 2. Production Gaps

- Replace UI-only startup/auth state with persistent authenticated state.
- Replace mutable global wallet processor state with lifecycle-safe dependency injection/application scope.
- Harden notification parsing, confidence, fingerprints, and deduplication.
- Prevent balance double-counting.
- Make Room the immediate local source of truth and cloud synchronization eventually consistent.
- Make every cloud API strictly user-scoped.
- Use **Auth.js / NextAuth as the official authentication authority**.
- Keep OAuth, database, and AI provider secrets on the backend only.
- Make AI optional and never authoritative for financial arithmetic.
- Complete offline, loading, empty, error, permission, recovery, and sync states.
- Add financial, authentication, synchronization, and real-device tests.

## 3. Product Goals

### P0
- Automatic DANA/GoPay transaction detection while Activity is closed.
- Exact, idempotent ledger and wallet accounting.
- Real Room-backed transaction history.
- Auth.js/NextAuth-backed identity and authenticated backend API.
- User-scoped cloud synchronization.
- Reliable Home, Activity, E-Wallet, and authentication flows.
- Offline-first core finance functionality.
- Review/edit/delete for detected transactions.

### P1
- Grounded NoTa AI.
- Receipt and voice transaction drafts.
- Goals and budgets.
- Cross-device recovery.
- Production diagnostics.

### P2
- More providers.
- Merchant/category learning.
- Advanced personalization and offline AI.

## 4. Non-Goals

ViNote is not a bank, payment processor, money-moving wallet, credential scraper, or AI-only ledger. It only observes permitted notification data and records user-authorized financial information.

---

## 5. Core UX

### First launch

1. Onboarding/value proposition.
2. Sign in/create account.
3. Establish authenticated backend session.
4. Quick setup: currency, starting balance, wallets, budget, goals.
5. Explain notification-based automatic detection.
6. Request notification-listener permission contextually.
7. Enable supported apps individually.
8. Verify setup and enter Home.

### Daily loop

`Spend → provider notification → ViNote detects → parses → validates → fingerprints → deduplicates → records → updates ledger → queues sync → optional ViNote notification`

The user should not need to open ViNote for every transaction.

### Detection confidence

- **High:** auto-record.
- **Medium:** pending transaction + confirmation.
- **Low:** no financial-state mutation; optionally keep a reviewable detection event.

---

# 6. Authentication Architecture — Auth.js / NextAuth

**Auth.js (NextAuth) is the official authentication architecture for ViNote 2.**

The Android application is an authenticated client of a ViNote Next.js backend. Android must not implement a competing authentication system.

### Target architecture

```text
ViNote Android
      │ HTTPS / authenticated API
      ▼
ViNote Next.js Backend
      │
      ├── Auth.js / NextAuth
      │     ├── Google OAuth initially
      │     ├── Session management
      │     ├── Account linking
      │     └── Canonical user identity
      │
      ├── API routes / domain services
      │
      └── Database access
             │
             ▼
       PostgreSQL / Supabase
```

### Auth.js responsibilities

Auth.js/NextAuth owns:

- authentication;
- supported OAuth providers;
- Google OAuth initially;
- session lifecycle;
- account/provider linking;
- authentication callbacks;
- identity resolution.

The design must allow additional providers later.

### Android responsibilities

Android must:

- launch the supported authentication flow;
- securely maintain only the credential/session information required to call the backend;
- restore a valid session on startup;
- handle expiry/revocation;
- sign out and clear authenticated in-memory state;
- never contain OAuth client secrets, provider secrets, database credentials, or server-side API keys.

### Canonical identity

The authenticated server-side user ID is canonical. Never trust a client-supplied `userId` for authorization.

All user-owned resources must resolve through the authenticated identity:

```text
User
 ├── WalletAccount
 │     └── Transaction
 ├── DetectionEvent
 ├── Goal
 ├── Budget
 ├── AppIntegration
 └── AiConversation
```

### Logout/account switching

On logout:

- end/invalidate the backend session according to the Auth.js session strategy;
- clear authenticated in-memory state;
- stop user-specific background processing where required;
- prevent cached data from the previous account being displayed;
- never mix local data between accounts.

### Authentication DoD

- [ ] Auth.js/NextAuth is the only official authentication authority.
- [ ] Google authentication works.
- [ ] Session restoration works.
- [ ] Expired/revoked sessions are handled gracefully.
- [ ] Every private API request is authorized against the authenticated user.
- [ ] User A cannot access User B's data.
- [ ] Logout/account switching is isolated and safe.
- [ ] No server secret is packaged in the APK.

---

# 7. Backend Architecture

Use a dedicated Next.js backend/service layer for authentication, authorization, cloud persistence, synchronization, and controlled AI access.

### Request flow

```text
Android
  ↓
Authenticated HTTPS request
  ↓
Next.js API
  ↓
Auth.js session/token validation
  ↓
Resolve canonical user ID
  ↓
Authorization / ownership check
  ↓
Domain service
  ↓
PostgreSQL / Supabase
```

### Backend owns

- authentication/session validation;
- authorization;
- user/profile operations;
- transaction/wallet/goal/budget sync;
- cloud persistence;
- NoTa AI gateway/proxy;
- server-side secrets;
- synchronization and idempotency.

### API principles

- HTTPS only.
- Private endpoints require authentication.
- Authorization is server-side and user-scoped.
- Validate all request bodies with schemas.
- Never accept arbitrary client `userId` as proof of ownership.
- Use stable error codes.
- Use idempotency keys for financial sync operations.
- Do not expose database internals.
- Rate-limit expensive/sensitive endpoints.

### Conceptual API

```text
POST   /api/auth/*
GET    /api/me
PATCH  /api/me

GET    /api/wallets
POST   /api/wallets
PATCH  /api/wallets/:id
DELETE /api/wallets/:id

GET    /api/transactions
POST   /api/transactions
PATCH  /api/transactions/:id
DELETE /api/transactions/:id
POST   /api/transactions/sync

GET    /api/goals
POST   /api/goals
PATCH  /api/goals/:id

GET    /api/budgets
POST   /api/budgets
PATCH  /api/budgets/:id

POST   /api/ai/chat
POST   /api/ai/transaction-draft

GET    /api/sync/status
POST   /api/sync/push
POST   /api/sync/pull
```

The exact routing convention can change, but authentication, authorization, domain logic, and persistence must remain separated.

---

# 8. Database & Synchronization Architecture

### Android local database

Room is the immediate source of truth for UI and offline finance operations.

Required concepts:

```text
UserSession
WalletAccount
Transaction
DetectionEvent
Goal
Budget
Category
AppIntegration
AiConversation
AiMessage
SyncMetadata
```

### Cloud database

Use PostgreSQL/Supabase as backend persistence where practical. Auth.js/NextAuth identity must map to the canonical application user record.

Rules:

- Every user-owned resource must be owned/scoped by the authenticated server identity.
- Use exact money representation, never floating-point money.
- Use migrations, never destructive production resets.
- Use stable IDs and timestamps/versions for synchronization.
- Use tombstones/soft deletes where required.
- Minimize raw notification retention.

### Local-first sync

```text
Room change
   ↓
Sync queue
   ↓
Authenticated API
   ↓
Auth.js identity + authorization
   ↓
PostgreSQL/Supabase
   ↓
Pull changes
   ↓
Room
```

States:

`LOCAL_ONLY → SYNCING → SYNCED`

Failure:

`SYNC_ERROR`

Cloud failure must never block local finance actions.

### Conflict policy

Financial conflicts must not be silently overwritten. Use stable entity IDs, versions/timestamps, deterministic conflict rules, and explicit reconciliation for exceptional financial conflicts.

---

# 9. Automatic E-Wallet / Banking Detection — P0

This is ViNote's defining production feature.

### Adapter architecture

```text
WalletAdapter
- packageNames
- displayName
- isFinancialNotification(notification)
- parseNotification(notification)
- supportedTransactionTypes
- parserVersion
```

Reuse and harden the existing DANA and GoPay adapters.

### Notification listener

`WalletNotificationListenerService` must:

- capture only relevant notifications;
- process off the main thread;
- avoid sensitive production logs;
- survive Activity closure;
- recover after process death/restart;
- handle listener reconnect/disconnect;
- expose listener health;
- delegate to an application-scoped injected coordinator;
- not depend on mutable process-global processor state.

### Pipeline

```text
Notification
 ↓
Normalize
 ↓
Identify provider
 ↓
Adapter parser
 ↓
Validate amount/type/currency/time
 ↓
Generate stable fingerprint
 ↓
Deduplicate
 ↓
Calculate confidence
 ↓
High → auto-record
Medium → pending confirmation
Low → ignore/review
 ↓
Atomic local persistence
 ↓
Update ledger/account state
 ↓
Queue cloud sync
 ↓
Optional ViNote notification
```

### Fingerprinting

Do not rely only on Android notification ID. Prefer source package, normalized type, amount, timestamp bucket, merchant/reference, and provider transaction ID when available. Enforce uniqueness in the database where practical.

### Balance model

Support two explicit modes:

**Ledger-derived:** `openingBalance + Σ income - Σ expense ± adjustments`

**Provider-reported:** current balance explicitly reported by the financial provider.

Never subtract an expense twice when a provider-reported balance already reflects it. When both are available, store both and show reconciliation status.

### Wallet reconciliation

Show:

- ViNote calculated balance;
- provider-reported balance when available;
- difference;
- last detection/sync;
- reconcile/set actual balance action.

---

# 10. Transaction Ledger

Required fields:

- stable internal ID;
- authenticated user ownership;
- exact amount;
- currency;
- type: income / expense / transfer / adjustment;
- category;
- title;
- merchant;
- wallet/account ID;
- source;
- source event ID/fingerprint;
- timestamp;
- created/updated timestamps;
- sync state;
- confidence where applicable;
- confirmation state.

Rules:

- No floating-point money arithmetic.
- Edits/deletes update aggregates consistently.
- Transfers are not spending.
- Imported events are idempotent.
- Historical transactions remain stable when parser versions change.

---

# 11. Home & Activity

Home must answer quickly:

- current money;
- recent spending;
- budget status;
- recent changes;
- pending detections.

Use real repository data only. No fake/mock financial values in production.

Activity must provide chronological history, search/filter, wallet/source indicators, detail, edit/delete, pending detections, and loading/empty/error states.

---

# 12. NoTa AI

NoTa is the conversational layer, not the ledger.

Capabilities:

- explain spending;
- summarize activity;
- answer budget questions;
- identify patterns;
- suggest practical actions;
- create transaction/goal drafts;
- explain financial concepts.

### Controlled tools

```text
get_current_balance()
get_wallets()
get_transactions(dateRange, filters)
get_budget()
get_goals()
create_transaction_draft(...)
create_goal_draft(...)
```

Rules:

- never invent balances/transactions;
- never own authoritative financial arithmetic;
- never directly mutate consequential financial records without confirmation;
- validate structured outputs;
- continue core finance functionality if AI is unavailable.

### OpenRouter

Keep OpenRouter behind `AiGateway`/`ViNoteAiService`. Production requests should use server-side credentials where appropriate. Add cancellation, timeouts, retry/backoff, structured validation, configurable model, and privacy-safe diagnostics.

The Android APK must never contain a shared production OpenRouter API key.

---

# 13. Offline / Online Hybrid

### Offline

- view/add/edit/delete transactions;
- calculate balances;
- manage goals/budgets;
- deterministic wallet parsing;
- queue synchronization;
- supported local NoTa functionality.

### Online

- Auth.js-backed session operations;
- cloud synchronization;
- OpenRouter AI;
- advanced insights;
- optional model updates.

Offline finance operations must continue without network access.

---

# 14. Goals, Budgets, Receipt & Voice

Goals support target, deadline, wallet, contributions, progress, and archive/edit.

Budgets support monthly/category limits, progress, remaining amount, warnings, and deterministic reset periods.

Receipt flow:

`Camera → OCR → candidates → validation → editable draft → confirmation → transaction`

Voice flow:

`Speech → text → extraction → editable draft → confirmation → transaction`

Uncertain values must never be silently finalized.

---

# 15. UX, Mascot, Notifications & Permissions

NoTa remains friendly, concise, supportive, and minimally animated.

Brand palette:

- Primary Blue `#4F8CFF`
- Secondary Blue `#7AB6FF`
- Mint `#5CE1C6`
- White `#FFFFFF`
- Soft Pink `#FFD6E5`

Use meaningful mascot states for transaction detection, budget warnings, goals, setup, and sync health.

ViNote output notifications should cover recorded transactions, pending reviews, budget warnings, and goal progress. They must be configurable and non-spammy.

Request notification-listener, POST_NOTIFICATIONS, camera, and microphone permissions contextually. Each permission flow needs a recovery path through Settings.

---

# 16. Security & Privacy

Mandatory:

- no production API keys committed;
- no OAuth/backend secrets in Android;
- no raw financial notification logging in production;
- secure session/credential storage;
- server-side user ownership enforcement;
- schema validation;
- privacy-safe diagnostics;
- minimized notification retention;
- minimized raw financial data sent to AI;
- HTTPS only.

Auth.js/NextAuth secrets exist only in the trusted backend environment.

Prefer server-side AI proxying for shared provider credentials so Android cannot extract them.

---

# 17. Architecture Rules

### Android

```text
Compose UI
   ↓
ViewModel
   ↓
Domain / Use Cases
   ↓
Repository Interfaces
   ↓
Room / Network / Android Services
```

### Backend

```text
Android API Client
   ↓
Next.js API
   ↓
Auth.js / NextAuth
   ↓
Authorization
   ↓
Domain Services
   ↓
PostgreSQL / Supabase
```

Rules:

- UI never directly manipulates persistence/network.
- Android services use injected application/domain coordinators.
- Financial calculations are deterministic.
- AI is behind interfaces.
- Avoid user-specific global mutable state.
- Background work uses lifecycle-safe mechanisms.
- Network synchronization is idempotent.

---

# 18. Testing Strategy

### Unit

DANA/GoPay parsing, provider identification, amount extraction, classification, fingerprinting, deduplication, balance/budget/goal calculations, AI structured parsing, and authentication/session state.

### Integration

Notification → adapter → ledger; duplicate notification → one transaction; process restart persistence; offline → authenticated sync; Auth.js API authorization; cross-user isolation; logout/login isolation; sync retry/conflicts.

### UI

Onboarding/auth, session restoration, Home, Activity CRUD, E-Wallet setup, pending confirmation, Goals, and NoTa.

### Real-device

Test Activity closed, process killed, reboot, notification access toggled, aggressive OEM battery restrictions, offline/recovery, and expired/recovered authentication sessions.

---

# 19. Definition of Done

### Authentication

- [ ] Auth.js/NextAuth is the official and only authentication authority.
- [ ] Android uses the backend authentication flow.
- [ ] Google authentication works.
- [ ] Session restoration works.
- [ ] Expiry/revocation is handled.
- [ ] API authorization uses authenticated server identity.
- [ ] Cross-user access is impossible.
- [ ] Logout/account switching is safe.

### Wallet automation

- [ ] DANA works for supported formats.
- [ ] GoPay works for supported formats.
- [ ] Detection works while Activity is closed.
- [ ] Duplicate events create one transaction.
- [ ] Invalid events cannot change financial state.
- [ ] High-confidence events auto-record.
- [ ] Ambiguous events can be reviewed.
- [ ] Balance cannot be double-counted.

### Backend/database

- [ ] All private APIs are authenticated and user-scoped.
- [ ] Canonical user ownership is enforced server-side.
- [ ] Room works offline.
- [ ] Sync retries after recovery.
- [ ] Sync is idempotent.
- [ ] Conflicts follow deterministic rules.

### AI/security

- [ ] NoTa is grounded in actual ledger data.
- [ ] AI cannot invent ledger state.
- [ ] AI failure does not break finance features.
- [ ] No production secrets are committed or packaged in APK.
- [ ] Sensitive notification data is not logged.

---

# 20. Implementation Phases

## Phase 0 — Foundation

- Audit prototype-only paths.
- Establish dependency injection/application scope.
- Establish canonical models/migrations.
- Establish persistent auth state.
- Establish backend/API contract.
- Add baseline tests.

**Exit:** Android and backend foundations are production-ready.

## Phase 1 — Auth.js/NextAuth + Backend

- Create Next.js backend/service layer.
- Configure Auth.js/NextAuth.
- Implement Google OAuth.
- Define Android authentication/session flow.
- Implement authenticated API middleware/authorization.
- Establish PostgreSQL/Supabase schema.
- Map Auth.js identity to application User.

**Exit:** User can securely authenticate and access only their own cloud data.

## Phase 2 — Production Ledger

- Harden Room schema.
- Complete transaction use cases.
- Implement deterministic accounting.
- Complete Home/Activity with real data.
- Implement wallet/account model.

**Exit:** Manual finance tracking is reliable offline.

## Phase 3 — Automatic E-Wallet Engine

- Refactor NotificationListenerService lifecycle.
- Remove fragile global processor state.
- Implement injected coordinator.
- Harden DANA/GoPay adapters.
- Add event fingerprints and DB deduplication.
- Add confidence/pending review.
- Add reconciliation.

**Exit:** Detection runs unattended without double-counting.

## Phase 4 — Cloud Sync

- Implement Room sync queue.
- Implement authenticated push/pull APIs.
- Add idempotency, retry/backoff, conflict handling.
- Add logout/session cleanup.

**Exit:** Local-first data synchronizes securely to the authenticated account.

## Phase 5 — NoTa Production AI

- Define AI gateway.
- Harden OpenRouter integration.
- Add controlled finance tools.
- Add structured extraction.
- Add conversation persistence.
- Add offline fallback.

**Exit:** NoTa is useful and grounded without owning financial truth.

## Phase 6 — Input Expansion & Production Polish

- Receipt OCR drafts.
- Voice drafts.
- Goals/budgets integration.
- Permission UX.
- Notifications.
- Accessibility/performance.
- Diagnostics.
- OEM/device testing.
- Release signing and production configuration.

**Exit:** ViNote can be used daily without developer intervention.

---

# 21. Priority Matrix

| Feature | Priority | Release Gate |
|---|---:|---:|
| Auth.js / NextAuth authentication | P0 | Yes |
| Authenticated backend API | P0 | Yes |
| User-scoped PostgreSQL/Supabase | P0 | Yes |
| Real transaction ledger | P0 | Yes |
| Local persistence | P0 | Yes |
| Automatic wallet detection | P0 | Yes |
| DANA adapter | P0 | Yes |
| GoPay adapter | P0 | Yes |
| Deduplication | P0 | Yes |
| Correct balance accounting | P0 | Yes |
| Pending transaction review | P0 | Yes |
| Cloud sync | P0 | Yes |
| Home dashboard | P0 | Yes |
| Activity CRUD | P0 | Yes |
| E-Wallet settings | P0 | Yes |
| Grounded NoTa | P1 | No |
| Receipt scanning | P1 | No |
| Voice input | P1 | No |
| Goals | P1 | No |
| Budgets | P1 | No |
| Additional adapters | P2 | No |
| Advanced personalization | P2 | No |

---

# 22. Developer Instructions

1. Do not rebuild ViNote from scratch.
2. Reuse sound existing screens, models, adapters, repositories, and services.
3. Never use fake/mock financial data in production screens.
4. Prioritize accounting correctness over visual polish.
5. AI interprets; deterministic application code owns financial truth.
6. Every background event must be idempotent.
7. Every network feature must degrade gracefully offline.
8. Every permission-dependent feature needs recovery UX.
9. Use migrations rather than destructive database resets.
10. Write tests alongside financial and authentication logic.
11. Keep provider-specific parsing behind adapters.
12. Never expose server secrets or shared API keys in Android.
13. Authorize all backend financial resources using Auth.js/NextAuth authenticated identity.
14. Never trust a client-supplied user ID for authorization.
15. Build and test every phase on a real Android device.

---

# 23. Final Product Definition

ViNote 2 is complete when a user can install the app, authenticate through the Auth.js/NextAuth-backed account system, enable DANA/GoPay detection, close the app, spend money normally, have ViNote detect and record the transaction exactly once, see the correct wallet/ledger state later, continue working offline, synchronize securely when online, and ask NoTa questions grounded in the real ledger.

A second user must be completely isolated from the first user's financial data.

That is the transition from **ViNote-2 prototype** to a genuinely usable production product.
