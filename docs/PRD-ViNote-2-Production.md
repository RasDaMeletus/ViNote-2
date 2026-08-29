# ViNote 2 — Production Product Requirements Document

**Document status:** Implementation-ready PRD  
**Target:** ViNote-2 Android application  
**Current state:** Functional prototype / partially wired production architecture  
**Primary goal:** Turn the current prototype into a reliable, daily-use personal finance companion.

---

## 1. Executive Summary

ViNote is an AI-powered personal finance companion for students and young users. It should reduce the friction of tracking money by automatically detecting financial activity, maintaining an accurate balance, explaining spending behavior, and proactively helping the user make better decisions.

The current ViNote-2 repository already contains the foundations of this product: Jetpack Compose screens, a ViewModel, Room/local data, Firestore synchronization, OpenRouter integration, offline NLP infrastructure, goals, transaction management, and Android `NotificationListenerService` wallet detection. The repository is therefore **not a greenfield rebuild**. The implementation should evolve the existing architecture into a production system instead of replacing working modules without reason.

The most important product transformation is:

> **From: a finance UI that can record transactions.  
> To: a finance companion that continuously understands the user's money with minimal manual input.**

---

## 2. Product Vision

ViNote should feel like a small financial companion living on the user's phone.

It should:

1. Know the user's current tracked money.
2. Automatically recognize incoming and outgoing transactions from supported wallet/banking notifications.
3. Deduplicate and validate events before recording them.
4. Keep balances and transaction history consistent.
5. Let the user correct mistakes easily.
6. Use AI for understanding, categorization, explanations, and conversational assistance — not as the sole source of truth for financial arithmetic.
7. Work gracefully when offline.
8. Protect financial data and minimize unnecessary data transmission.
9. Make financial literacy actionable rather than presenting only charts.

---

## 3. Current Repository Assessment

The repository is already structured around separate UI, ViewModel, domain, data, AI, and service layers. The main navigation currently exposes Home, Activity, NoTa, Goals, and Me, with additional flows for authentication, setup, transactions, receipt scanning, voice input, e-wallets, settings, profile, and bank integrations.

The Android manifest already registers a native `NotificationListenerService`, and the wallet pipeline currently follows the intended high-level flow: notification capture → wallet adapter detection → deterministic parsing → deduplication → transaction insertion, with an AI fallback when deterministic parsing cannot extract a valid amount.

The repository also contains DANA and GoPay adapters, local Room DAOs/database models, Firestore synchronization, an OpenRouter client, and an offline NLP engine.

### Production gaps to resolve

- App startup/navigation state is currently UI-driven and should become persistent/auth-aware.
- Notification processing must be lifecycle-safe and recoverable rather than depending on mutable global state.
- Wallet detection needs robust package identification, parsing confidence, transaction identity, and user correction flows.
- Balance handling must distinguish **calculated balance** from **provider-reported balance** and prevent double deduction.
- AI calls must be resilient, structured, observable, and never allowed to corrupt financial state.
- Offline/online behavior needs explicit policy and UI.
- Data synchronization needs deterministic conflict rules and idempotency.
- Production error states, loading states, empty states, permissions, and recovery flows need to be complete.
- Security/secrets handling must be production-ready; API credentials must never be hard-coded or exposed in the APK.
- Automated tests are required for financial parsing, transaction accounting, persistence, and critical UI flows.

---

## 4. Goals

### P0 goals

- Automatic transaction detection works reliably while the app is not open.
- Supported e-wallet notifications can create transactions without manual entry.
- Incoming top-ups/income and outgoing spending update the correct wallet balance exactly once.
- Transactions survive app restarts.
- Duplicate notifications do not create duplicate transactions.
- User can review, edit, confirm, or delete detected transactions.
- Home dashboard reflects the same source of truth as Activity and wallet balances.
- Authentication and user-specific cloud data work correctly.
- Offline mode remains usable for core finance tracking.
- AI assistant can answer questions using the user's actual stored financial data.

### P1 goals

- Receipt scanning creates an editable transaction draft.
- Voice input creates an editable transaction draft.
- Goals and budgets are integrated with transaction activity.
- NoTa provides proactive but non-spammy financial insights.
- Cloud sync works across reinstall/device changes.
- Production observability and diagnostics are available.

### P2 goals

- Expand wallet/bank adapters.
- Smarter merchant/category learning.
- Advanced financial insights and personalized recommendations.
- Optional richer offline AI capabilities when technically practical.

---

## 5. Non-Goals

Do not turn ViNote into:

- A bank or payment processor.
- A wallet that actually moves user money.
- A replacement for official banking/e-wallet applications.
- A system that requires accessibility abuse or credential scraping.
- An AI-only financial ledger where the model determines balances.
- A social network or gamified finance platform before core reliability is complete.

ViNote only **observes permitted notification data and records user-authorized financial information**.

---

## 6. Core User Experience

### 6.1 First launch

1. Launch ViNote.
2. Show concise value proposition.
3. Create account / sign in.
4. Complete financial setup:
   - preferred currency (default IDR)
   - starting cash balance, if applicable
   - optional wallets/accounts
   - monthly budget
   - financial goals
5. Explain automatic detection clearly.
6. Ask for notification-listener permission only after explaining why it is needed.
7. Let the user enable supported apps individually.
8. Run a short setup verification.
9. Enter Home.

Never imply that ViNote can read private banking data directly. Explain that detection uses notifications from selected apps.

### 6.2 Daily experience

The ideal daily loop is:

`User spends money → wallet/bank posts notification → ViNote detects → parses → validates → records → updates balance → optionally notifies user → Home/Activity updates.`

The user should not need to open ViNote for every transaction.

### 6.3 Manual transaction

Manual entry remains available as a fallback:

- amount
- income/expense
- category
- merchant/title
- wallet/account
- date/time
- note
- optional receipt

Save should be immediate locally, then synchronize in the background.

### 6.4 Detected transaction review

If confidence is high and the notification is unambiguous, auto-record it.

If confidence is medium/ambiguous:

- create a pending transaction
- show a compact confirmation notification/in-app card
- allow Confirm / Edit / Ignore

If confidence is low:

- do not modify financial state
- show a reviewable detection event when appropriate

---

## 7. Information Architecture

### Bottom navigation

1. **Home** — current financial state and important actions.
2. **Activity** — transaction history, filtering, search, details.
3. **NoTa** — AI financial companion.
4. **Goals** — savings/financial goals and progress.
5. **Me** — profile, wallets, integrations, preferences, privacy, settings.

### Home priorities

The Home screen should answer within seconds:

- How much money do I have?
- How much did I spend recently?
- Am I within budget?
- What changed today?
- Is there anything I need to review?

Avoid filling the first screen with decorative charts. Information hierarchy must favor actionable financial state.

---

## 8. Wallet & Banking Automation — P0

This is the defining production feature.

### 8.1 Supported source architecture

Use an adapter interface so each financial app has isolated parsing rules.

Conceptual interface:

```text
WalletAdapter
- packageNames
- displayName
- isFinancialNotification(notification)
- parseNotification(notification)
- supportedTransactionTypes
- parserVersion
```

Existing DANA and GoPay adapters should be hardened rather than discarded. New adapters should be added without modifying the notification service itself.

### 8.2 Notification listener

The Android `NotificationListenerService` must:

- capture only relevant notifications
- ignore notifications from unrelated apps
- avoid logging sensitive notification content in production
- process work off the main thread
- remain resilient after process death/restart
- expose service status to the UI
- handle listener disconnect/reconnect
- avoid relying on process-global mutable processor state as the long-term architecture

### 8.3 App selection

E-wallet settings should show detected/supported apps with:

- app name/icon
- supported status
- enabled/disabled toggle
- listener permission status
- last detected event
- parser health/version

The user should **not** paste notification text manually.

### 8.4 Parsing pipeline

```text
Notification
    ↓
Normalize payload
    ↓
Identify financial app
    ↓
Adapter parser
    ↓
Validate amount/type/currency/time
    ↓
Create stable event fingerprint
    ↓
Deduplicate
    ↓
Determine confidence
    ↓
[High confidence] Auto-record
[Medium confidence] Pending confirmation
[Low confidence] Ignore / review
    ↓
Persist transaction atomically
    ↓
Update wallet/account ledger
    ↓
Sync cloud
    ↓
Optional user notification
```

### 8.5 Transaction fingerprint

Do not use only Android notification ID.

Fingerprint should be derived from stable normalized fields such as:

- source package
- normalized transaction type
- amount
- timestamp bucket
- normalized merchant/reference text
- provider transaction/reference ID when available

Store the fingerprint with a unique database constraint where practical.

### 8.6 Balance rules

ViNote must never blindly subtract an expense from a balance if the balance was already updated from a provider-reported balance.

Use one of these explicit accounting modes per wallet:

**Mode A — Ledger-derived balance**

`balance = openingBalance + Σ income - Σ expense`

**Mode B — Provider-reported balance**

Provider notification supplies a trustworthy current balance. Store that value as `providerBalance` and track transactions separately.

If both are available, display reconciliation status rather than silently applying both.

Every balance-changing event must be idempotent.

### 8.7 Reconciliation

Provide a wallet detail screen with:

- ViNote calculated balance
- provider-reported balance when available
- difference
- last sync/detection time
- Reconcile / Set as actual balance action

This prevents silent accounting drift.

---

## 9. Transaction System

Transactions are the canonical financial events in ViNote.

### Required fields

- immutable internal ID
- user ID
- amount in smallest currency unit / safe numeric representation
- currency
- type: income / expense / transfer / adjustment
- category
- title
- merchant
- wallet/account ID
- source: manual / e-wallet / bank / receipt / voice / AI
- source event ID/fingerprint
- timestamp
- created timestamp
- updated timestamp
- sync state
- confidence, if automatically detected
- confirmation state
- optional raw-source metadata, minimized and privacy-safe

### Important accounting rules

- Money values must not use floating-point arithmetic.
- Deleting/editing a transaction must update all dependent aggregates consistently.
- Transfers between tracked wallets must not be counted as spending.
- An imported/detected transaction must be safe to process more than once.
- Historical transactions must remain stable if parser rules change.

---

## 10. Activity Screen

Activity must be a real ledger, not a visual mock.

Features:

- chronological transaction list
- date grouping
- income/expense indicators
- wallet/source indicator
- category
- search
- filters by date, wallet, category, type, source
- transaction detail sheet
- edit
- delete with confirmation
- undo after deletion
- pending detection section
- empty state
- loading state
- error/retry state

A transaction detail view must clearly show whether it was manually entered or automatically detected.

---

## 11. Home Dashboard

### Required components

1. Current tracked balance.
2. Wallet/account breakdown.
3. Today/this-week spending.
4. Monthly budget progress.
5. Recent transactions.
6. Pending detections.
7. Goal progress snapshot.
8. One useful NoTa insight.
9. Quick actions:
   - Add transaction
   - Scan receipt
   - Voice entry
   - Ask NoTa

### Dashboard principles

- Never show stale values without indicating sync status.
- Use the same repository/source of truth as Activity.
- Loading should be skeleton/placeholder, not fake data.
- Errors should be recoverable.

---

## 12. NoTa AI Assistant

NoTa is the conversational layer of ViNote, not the ledger itself.

### Capabilities

- explain spending
- summarize recent activity
- answer budget questions
- identify unusual spending patterns
- suggest practical savings actions
- create transaction drafts from natural language
- create goal drafts
- answer questions about recorded transactions
- explain financial concepts in simple language

### Tool-based architecture

The AI should access controlled application tools rather than receiving an unrestricted database dump.

Example tools:

```text
get_current_balance()
get_wallets()
get_transactions(dateRange, filters)
get_budget()
get_goals()
create_transaction_draft(...)
create_goal_draft(...)
```

AI responses should be grounded in tool results.

### AI safety/product rules

- AI must never invent a transaction or balance.
- AI must not directly mutate financial records without an explicit user-confirmation flow for consequential changes.
- Arithmetic must be performed deterministically by application code.
- Structured extraction should use validated JSON/schema where supported.
- If AI is unavailable, the app must continue core finance functionality.

### OpenRouter

Keep OpenRouter behind an interface such as `AiGateway`/`ViNoteAiService` so the provider can be replaced later.

The existing OpenRouter client should be hardened with:

- secure credential injection
- request cancellation
- retry/backoff for transient errors
- timeout handling
- structured response parsing
- model configuration
- usage/error telemetry without financial-content leakage

---

## 13. Offline / Online Hybrid

Core finance tracking must work offline.

### Offline-capable

- view transactions
- add/edit/delete local transactions
- calculate balances
- manage goals/budgets
- local deterministic wallet parsing
- queue cloud sync
- basic NoTa intent/extraction where the offline engine supports it

### Online-enhanced

- OpenRouter AI
- cloud synchronization
- advanced AI insights
- optional model downloads/updates

### Sync states

Every syncable entity should have a clear state:

`LOCAL_ONLY → SYNCING → SYNCED`

and failure state:

`SYNC_ERROR`

Retry automatically with backoff, but never block local finance actions because cloud sync failed.

---

## 14. Authentication & User Data

Authentication must be a real application flow, not a prototype screen.

Requirements:

- sign up
- sign in
- sign out
- session restoration
- password/account recovery where supported by backend
- user-specific local/cloud data
- first-time setup state
- secure session handling

On logout:

- stop user-specific background processing where required
- clear sensitive in-memory state
- do not accidentally expose the previous user's transactions after account switch

---

## 15. Cloud Synchronization

Use cloud storage for backup/synchronization, not as the only local data source.

The repository currently contains Firestore synchronization infrastructure. Production implementation must define:

- canonical user ID
- document/entity IDs
- created/updated timestamps
- conflict policy
- deletion/tombstone policy
- retry behavior
- offline queue
- migration/versioning

Recommended rule:

> Local Room is the immediate source of truth for the UI; cloud sync is eventually consistent backup/synchronization.

For conflicting edits, use deterministic version/timestamp rules plus explicit handling for financial records rather than silently overwriting newer data.

---

## 16. Goals & Budgets

### Goals

Users can:

- create a goal
- set target amount
- set deadline
- assign optional wallet/account
- add contributions
- view progress
- edit/archive goal

### Budgets

Support:

- monthly overall budget
- category budgets
- spending progress
- exceeded warning
- remaining amount
- budget reset period

Budget calculations must use the transaction ledger and deterministic date boundaries.

NoTa can explain budget status, but budget arithmetic is application-owned.

---

## 17. Receipt Scanning

Receipt flow should create a **draft**, never immediately finalize uncertain financial data.

Pipeline:

`Camera → OCR/extraction → amount/merchant/date/category candidates → validation → editable draft → user confirmation → transaction`

Handle:

- no camera permission
- blurry receipt
- unsupported receipt
- multiple totals
- tax/service charge
- extraction failure

Never fabricate missing values.

---

## 18. Voice Input

Voice input should follow the same transaction-draft architecture as receipt scanning.

Example intent:

> “I spent twenty thousand for lunch.”

Pipeline:

`Speech → text → deterministic/AI extraction → draft → user confirmation → transaction`

The final ledger must only receive validated values.

---

## 19. NoTa Personality & Mascot

NoTa should feel friendly, supportive, and concise.

Visual identity should remain consistent with the ViNote mascot system:

- primary blue: `#4F8CFF`
- secondary blue: `#7AB6FF`
- accent mint: `#5CE1C6`
- white belly: `#FFFFFF`
- soft pink blush: `#FFD6E5`

NoTa should react contextually:

- successful transaction detection
- spending milestone
- budget warning
- goal progress
- setup completion
- sync state

Do not over-animate. Mascot interactions should improve feedback rather than distract from financial information.

---

## 20. Notifications

ViNote has two distinct notification categories:

### A. Input notifications

Financial notifications from other apps are consumed by the notification listener and parsed locally.

### B. ViNote output notifications

Examples:

- “Recorded: Rp25.000 at [merchant].”
- “You have 2 transactions to review.”
- “Your monthly budget is almost reached.”
- “Your goal is 65% complete.”

Output notifications must be configurable and non-spammy.

Never include sensitive financial information in notification text when device privacy settings make that inappropriate; provide a privacy option for notification detail level.

---

## 21. Permissions

Request permissions contextually, not all at once.

Potential permissions include:

- notification access for automatic wallet detection
- POST_NOTIFICATIONS for ViNote alerts
- camera for receipt scanning
- microphone/audio for voice input

Each permission screen must explain:

- why ViNote needs it
- what feature it enables
- what happens if it is denied
- how to enable it later in Settings

---

## 22. Settings / Me

### Required sections

**Profile**
- name
- account
- sign out

**Money**
- currency
- starting balance/settings
- budget

**Wallets & Integrations**
- e-wallets
- banks
- listener permission status
- connection health

**NoTa**
- AI provider/model configuration where appropriate
- response preferences
- privacy controls

**Notifications**
- transaction alerts
- budget alerts
- goal alerts
- notification detail/privacy

**Data**
- sync status
- export data
- delete local data
- account/data deletion flow if supported

**About**
- app version
- privacy policy
- terms
- diagnostics

---

## 23. Data Model Direction

At minimum, production storage should conceptually contain:

```text
User
WalletAccount
Transaction
TransactionEvent / DetectionEvent
Goal
Budget
Category
AiConversation
AiMessage
SyncMetadata
AppIntegration
```

Important relationships:

```text
User
 ├── WalletAccount
 │     └── Transaction
 ├── Transaction
 ├── Goal
 ├── Budget
 └── AppIntegration

DetectionEvent
 └── may produce Transaction
```

Do not store raw notification text indefinitely unless there is a clear product reason. Prefer normalized/minimized metadata and configurable retention.

---

## 24. Architecture Requirements

Preserve and strengthen the current separation:

```text
UI / Compose
    ↓
ViewModel
    ↓
Domain / Use Cases
    ↓
Repository interfaces
    ↓
Room / Firestore / Network / Android services
```

### Rules

- UI must not directly manipulate Room/Firestore.
- Android services must call domain/application logic through an injected coordinator.
- Financial calculations must be deterministic.
- AI must be an optional capability behind interfaces.
- Repository methods should expose `Flow`/observable state where appropriate.
- Background work should use lifecycle-safe Android mechanisms.
- Avoid singleton/global mutable state for user-specific financial processing.

---

## 25. Background Processing

Automatic wallet detection must work when the main Activity is not open.

Requirements:

- NotificationListenerService receives events.
- Processing is delegated to an application-scoped, dependency-injected coordinator/repository.
- Work is persisted quickly so process death does not lose events.
- Long-running/retryable work should use WorkManager where appropriate.
- Processing must be idempotent.
- Battery impact must be minimized.

The service must not require the user to keep the app open.

---

## 26. Error Handling

Every production flow needs explicit states:

```text
Idle
Loading
Success
Empty
Recoverable error
Permission required
Offline
Sync error
```

Examples:

- OpenRouter unavailable → continue without AI.
- Cloud unavailable → keep local transaction and queue sync.
- Wallet parser fails → create detection error/pending review, never corrupt balance.
- Notification permission disabled → explain how to re-enable.
- Camera unavailable → manual transaction remains available.

Avoid generic “Something went wrong” without a recovery action.

---

## 27. Security & Privacy

Financial data is sensitive.

Requirements:

- never commit API keys
- never log raw financial notification content in production
- minimize notification storage
- use secure local storage for secrets/tokens
- enforce user ownership in cloud rules
- validate all cloud reads/writes against authenticated user identity
- avoid sending raw notification data to AI unless necessary
- redact sensitive values from diagnostics
- provide clear privacy explanation for notification access

The OpenRouter API key must not be hard-coded into source or packaged as a publicly recoverable application secret. Prefer a controlled backend/proxy for production if the architecture requires a shared provider credential.

---

## 28. Performance Requirements

Target behavior:

- Home renders useful local data immediately when cached.
- Notification-to-local-record processing should normally complete within a few seconds.
- UI must remain responsive during parsing, sync, OCR, and AI requests.
- AI/network work must never block the main thread.
- Large transaction histories must be paginated/lazy-loaded.
- Avoid unnecessary recomposition and repeated database queries.
- Background detection must have minimal battery impact.

---

## 29. Accessibility & UX Quality

- Touch targets should be comfortably tappable.
- Support dynamic font scaling.
- Maintain readable contrast.
- Do not communicate transaction type by color alone.
- Provide content descriptions for important icons/images.
- Ensure bottom navigation remains usable with larger text.
- Forms must provide clear validation.

Visual style: **minimal, modern, friendly, clean**. Avoid excessive cards, gradients, shadows, or decorative elements that reduce information density.

---

## 30. Testing Strategy

### Unit tests — mandatory

- DANA parser
- GoPay parser
- wallet identification
- amount extraction
- income/expense classification
- deduplication
- transaction fingerprint generation
- balance calculation
- budget calculation
- goal progress
- AI structured-response parsing

### Integration tests — mandatory

- Notification → adapter → transaction pipeline
- duplicate notification → one transaction
- app restart → data persists
- offline transaction → later cloud sync
- logout/login → correct user data isolation

### UI tests — mandatory for P0

- onboarding/auth
- Home
- manual transaction
- transaction detail/edit/delete
- E-Wallet setup
- pending transaction confirmation
- Goals
- NoTa basic interaction

### Device tests

Test notification listener behavior on real Android devices because OEM background restrictions can differ significantly from emulator behavior.

Test at least:

- Android stock behavior
- aggressive battery-management OEM behavior where available
- notification access disabled/enabled
- app process killed/restarted
- reboot recovery

---

## 31. Acceptance Criteria — Definition of Done

ViNote 2 is considered production-ready for the MVP when all of the following are true:

### Automatic wallet detection

- [ ] User can enable/disable supported wallet apps with toggles.
- [ ] ViNote detects relevant notifications while the Activity is closed.
- [ ] DANA transactions are correctly detected for supported notification formats.
- [ ] GoPay transactions are correctly detected for supported notification formats.
- [ ] Duplicate notifications do not create duplicate transactions.
- [ ] Invalid/irrelevant notifications are ignored.
- [ ] Parser failures do not change balances.
- [ ] High-confidence transactions can be auto-recorded.
- [ ] Ambiguous transactions can be reviewed before affecting the ledger.

### Financial ledger

- [ ] Every transaction has a unique stable ID.
- [ ] Amount calculations are exact and safe.
- [ ] Wallet balances are consistent.
- [ ] Transfers are not counted as spending.
- [ ] Editing/deleting transactions updates aggregates.
- [ ] App restart does not lose transactions.

### Cloud

- [ ] User data is isolated.
- [ ] Local data works without network.
- [ ] Sync retries after network recovery.
- [ ] Duplicate sync events are safe.
- [ ] Logout cannot expose another user's data.

### AI

- [ ] NoTa can answer questions from actual transaction data.
- [ ] AI cannot invent ledger state.
- [ ] AI failures do not break finance features.
- [ ] AI-created transaction data requires validation/confirmation when uncertain.

### UX

- [ ] No major screen relies on fake/mock financial data.
- [ ] Loading, empty, error, offline, and permission states are implemented.
- [ ] Back navigation works consistently.
- [ ] Settings can recover disabled permissions.
- [ ] No critical action silently fails.

### Security

- [ ] No production API keys are committed.
- [ ] Sensitive notification contents are not written to production logs.
- [ ] Cloud access is user-scoped.
- [ ] Sensitive local/session information is protected.

---

## 32. Implementation Phases

### Phase 0 — Stabilize the foundation

- Audit current repository and remove prototype-only paths.
- Establish dependency injection/application scope.
- Establish canonical data models and migrations.
- Make startup/auth state persistent.
- Add structured error/state handling.
- Add baseline unit/integration tests.

**Exit:** App launches reliably and architecture is ready for production features.

### Phase 1 — Production ledger

- Harden Room schema.
- Implement transaction use cases.
- Implement deterministic balance calculations.
- Complete Activity CRUD.
- Complete Home using real repository data.
- Implement wallet/account model.

**Exit:** Manual finance tracking is fully reliable offline.

### Phase 2 — Automatic E-Wallet Engine

- Refactor notification listener lifecycle.
- Remove fragile global processor state.
- Implement injected processor/coordinator.
- Harden DANA/GoPay adapters.
- Add stable event fingerprinting.
- Add deduplication constraints.
- Add confidence states.
- Add pending transaction review.
- Add wallet reconciliation.

**Exit:** Automatic detection can run unattended and cannot double-count transactions.

### Phase 3 — Cloud & identity

- Complete authentication.
- User-scoped Firestore data.
- Offline queue.
- Retry/backoff.
- Conflict handling.
- Logout/session cleanup.

**Exit:** Reinstall/device change can recover cloud-backed user data.

### Phase 4 — NoTa production AI

- Define AI gateway interface.
- Harden OpenRouter client.
- Implement tool-based finance context.
- Add structured transaction extraction.
- Add conversation persistence.
- Add AI fallback/offline behavior.

**Exit:** NoTa is useful, grounded, and never authoritative over ledger arithmetic.

### Phase 5 — Input expansion

- Receipt OCR → transaction draft.
- Voice → transaction draft.
- Better merchant/category inference.
- Goal/budget integrations.

**Exit:** Manual entry becomes extremely low-friction.

### Phase 6 — Production polish

- Permission UX.
- Notifications.
- Privacy controls.
- Accessibility.
- Performance.
- Crash/error diagnostics.
- Device/OEM testing.
- Release signing and production build configuration.

**Exit:** App can be used daily without developer intervention.

---

## 33. Priority Matrix

| Feature | Priority | Release Gate |
|---|---:|---:|
| Real transaction ledger | P0 | Yes |
| Local persistence | P0 | Yes |
| Automatic wallet notification detection | P0 | Yes |
| DANA adapter | P0 | Yes |
| GoPay adapter | P0 | Yes |
| Deduplication | P0 | Yes |
| Correct balance accounting | P0 | Yes |
| Pending transaction review | P0 | Yes |
| Authentication | P0 | Yes |
| Cloud sync | P0 | Yes |
| Home dashboard | P0 | Yes |
| Activity CRUD | P0 | Yes |
| E-wallet settings | P0 | Yes |
| NoTa grounded assistant | P1 | No |
| Receipt scanning | P1 | No |
| Voice input | P1 | No |
| Goals | P1 | No |
| Budgets | P1 | No |
| Additional banks/wallets | P2 | No |
| Advanced personalization | P2 | No |

---

## 34. Developer Instructions

When implementing this PRD against the existing ViNote-2 repository:

1. **Do not rebuild the app from scratch.** Reuse existing screens, models, adapters, repositories, and services where they are sound.
2. **Do not create fake/mock functionality to satisfy a screen.** Every production screen must connect to the actual data layer.
3. **Prioritize correctness over visual polish.** A beautiful incorrect balance is a critical bug.
4. **Do not let AI own financial truth.** AI interprets; deterministic application logic calculates and persists.
5. **Every background event must be idempotent.** Assume notifications can arrive repeatedly.
6. **Every network feature must degrade gracefully offline.**
7. **Every permission-dependent feature needs a recovery path.**
8. **Use migrations instead of destructive database resets.**
9. **Write tests alongside critical financial logic.**
10. **Keep provider-specific wallet parsing isolated behind adapters.**
11. **Do not store or expose secrets in source code.**
12. **After each phase, build and test on a real Android device.**

---

## 35. Final Product Definition

The finished ViNote 2 should pass this simple user test:

> A user installs ViNote, signs in, enables DANA/GoPay notification detection, sets their starting balance and budget, then closes the app. They spend money normally using their wallet. ViNote detects the transaction, records it once, updates the correct financial state, and makes the transaction visible in Activity without requiring manual entry. When the user opens ViNote later, Home immediately shows the correct state. They can ask NoTa why their spending changed, and NoTa answers from the real ledger. If the internet is unavailable, the core experience still works and sync resumes later.

That is the transition from **ViNote-2 prototype** to **usable ViNote product**.
