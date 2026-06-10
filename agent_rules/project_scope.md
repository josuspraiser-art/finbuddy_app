# FinBuddy – Project Scope & Tech Stack Document

## 1. Project Overview
FinBuddy is a privacy-first, offline-focused personal finance and budgeting Android application. It allows users to manually track accounts, transactions (income, expense, transfer), manage budgets, and generate visual analytics and PDF reports without any bank integrations or third-party data sharing.

---

## 2. Technical Stack

The project will be built natively for the Android platform, adhering to the following stack:

* **Language:** Kotlin (1.9+)
* **UI Framework:** Jetpack Compose (Declarative UI with Material 3 styling)
* **Architecture:** Model-View-Intent (MVI) using Uni-directional Data Flow (UDF) paired with Clean Architecture (Presentation, Domain, Data layers)
* **Local Persistence:** Room Database (SQLite abstraction) for user-level data isolation
* **Concurrency:** Kotlin Coroutines & Flows (StateFlow for UI state representation, SharedFlow for one-time side-effects)
* **Dependency Injection:** Dagger Hilt
* **PDF Export:** Native Android `PdfDocument` API
* **Security & Authentication:** 
  * Local session management & JWT validation simulations
  * Password hashing via BCrypt (client-side)
  * Data isolation per authenticated user ID

---

## 3. Project Scope

### In-Scope Features (By Module)

#### Module 1: Authentication & User Management
* User registration (username, password >= 8 characters) with BCrypt hashing.
* Secure login, JWT-based authentication validation, and active session management.
* User logout and session redirection for unauthorized users.

#### Module 2: Financial Accounts
* Support for three default account types: Credit Card, Bank Account, Cash Wallet.
* Creation of unlimited custom accounts with unique names per user and opening balances.
* Prevention of deleting accounts that have active transactions associated with them.

#### Module 3: Transaction Management
* Supported types: Income, Expense, and Transfer.
* Enforced validations: Amount > 0, no future-dated transactions, no duplicate transfer accounts.
* Balance update logic:
  * Income: Balance increase.
  * Expense: Balance decrease.
  * Transfer: Debit source account, credit destination account (excluded from income/expense metrics).
* Balance auto-recalculation upon editing or deleting historical transactions.

#### Module 4: Categories & Tags
* Default categories: Grocery, Rent, Petrol, Restaurant, Clothing, Accessories, Home Appliances, and Others.
* Custom user-specific category creation (Income and Expense).
* User-level data isolation and unique category name constraints.

#### Module 5: Budget Management
* Define monthly budget amounts per category.
* Calculate budget usage percentage: `(Category Expense / Budget Amount) * 100`.
* Budget thresholds with status alerts:
  * `< 80%`: Normal
  * `80% - 100%`: Warning
  * `> 100%`: Exceeded (with warnings during transaction entry).

#### Module 6: Dashboard
* Live widgets displaying: Total Balance, Monthly Income, Monthly Expense, and Net Worth (`Assets - Liabilities`).
* Live Budget Status Widget showing remaining budgets and progress indicators.
* Real-time dashboard refreshes on any transaction or transfer changes.

#### Module 7: Reports & Analytics
* Category-wise, account-wise, and income vs. expense filter categories.
* Weekly and monthly time-period filters.
* Graphical rendering using Pie Charts and Bar Charts.

#### Module 8: PDF Export
* Export formatted PDF files with Header (Title, generated date, active filters), Body (visualization charts, summary metrics, transactions list), and Footer (page numbers).
* Return empty PDF reports if no transaction data matches the active filters.

---

## 4. Out-of-Scope (Excluded Features)
* **Bank Integrations:** No direct banking API access or third-party aggregators (Plaid, Yodlee, etc.).
* **Auto Import:** No automated transaction imports or SMS parsing.
* **Investment Tracking:** Excludes stocks, bonds, crypto, or mutual fund portfolio tracking.
* **Multi-Currency:** Only one base currency (e.g., INR / ₹) is supported.
* **Shared Accounts:** No multi-user shared budgets, collaboration accounts, or family grouping.
* **Recurring Transactions:** No automated recurring scheduler for bills or transactions.
* **Receipt Capture:** No attachments, scanning, or OCR parsing of physical receipts.
