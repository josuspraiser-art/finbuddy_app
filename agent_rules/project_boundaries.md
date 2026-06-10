# FinBuddy – Project Boundaries

This document defines the architectural guardrails, implementation limits, and coding constraints for FinBuddy, ensuring strict separation of concerns and operational boundaries.

---

## 1. Architectural Guardrails
* **Presentation-Domain Isolation:** Composable screens must only interact with ViewModels via `StateFlow` (read-only states) and `SharedFlow` (one-time events). Direct database calls from the UI layer are strictly prohibited.
* **Domain Layer Purity:** The domain layer must be pure Kotlin. It must have zero dependencies on Android framework components, Jetpack Compose, or Room database classes.
* **No Shared State Across Users:** All local database queries must filter by the active authenticated `user_id`. There must be no global leaks or shared data buffers.

---

## 2. Operational & Data Boundaries
* **Strict Offline Operation:** All financial data (transactions, accounts, budgets, categories) must be persisted locally using Room DB. No external cloud sync, telemetry, or remote telemetry backups.
* **Constraint Boundaries:** 
  * Accounts with transactions cannot be deleted.
  * Transaction amounts must be strictly positive (`amount > 0`). Future-dated transactions are rejected.
  * Category names and account names must be unique per user.
* **Transfer Transactions:** Transfers must debit the source and credit the destination in a single database transaction boundary to guarantee atomicity. Transfers are excluded from total income and expense calculations.

---

## 3. Session & Security Boundaries
* **Password Hashing:** Passwords must be hashed using BCrypt. Plain-text passwords must never be stored, logged, or printed.
* **Route Protection:** Screens outside the login/signup flow must check session validity. If the session/JWT is invalid or expired, the UI must immediately pop backstacks and redirect the user to the Login screen.

---

## 4. Context & Development Constraints
* **Save Token Guidelines:** In accordance with [save_token.md](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/agent_rules/save_token.md), code modifications must use targeted edits (`replace_file_content`) to prevent context bloat. Boilerplate logs during build/run tasks should be filtered to show summary status only.
