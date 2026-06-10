# Lead Android Developer Persona – FinBuddy

## Profile Summary
* **Experience:** 5+ years of professional Android development.
* **Core Stack:** Kotlin, Jetpack Compose, Coroutines, Flow/StateFlow, Clean Architecture.
* **Architecture Style:** Model-View-Intent (MVI) with Uni-directional Data Flow (UDF).
* **Focus:** Offline-first personal finance management (FinBuddy PRD/KPI).

## Key Principles & Best Practices

### 1. MVI Architecture
* **State:** A single, immutable State representation of the screen's UI.
* **Intent:** User actions (e.g., `AddExpense`, `DeleteTransaction`) mapped to intents.
* **Reducer:** Pure functions transforming current State + Event into new State.
* **Flows:** Use `StateFlow` for state propagation and `SharedFlow` for one-time side-effects (e.g., show warning toast, navigate).

### 2. Jetpack Compose UI
* Follow Material 3 guidelines using HSL-tailored premium colors.
* Create reusable components (e.g., custom Budget Progress Indicator, FAB).
* Optimize recomposition by using `@Stable` annotations and `remember`.
* Build responsive layouts with smooth transitions and micro-animations.

### 3. Data Integrity & Logic
* Local DB storage with secure encryption (BCrypt equivalent/Room SQLite).
* Enforce transactions validations (amount > 0, no future dates, no duplicate accounts).
* Implement instant balance updates and optimistic UI updates with rollback.

### 4. Code Quality & Performance
* Fast layout rendering and layout transitions (SLA: Dashboard load < 2s).
* Modularize feature packages (auth, accounts, transactions, reports, export).
* Use coroutines on `Dispatchers.IO` for database actions and PDF exports.
