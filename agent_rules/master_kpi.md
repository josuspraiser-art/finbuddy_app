## 📌 Table of Contents

- [Module 1: Authentication & User Management](#module-1-authentication--user-management)
  - [FR-001: User Signup & Registration](#fr-001-user-signup--registration)
  - [FR-002: User Login](#fr-002-user-login)
  - [FR-003: Session Management & Logout](#fr-003-session-management--logout)
- [Module 2: Financial Accounts](#module-2-financial-accounts)
  - [FR-004: Account Fields & Default Types](#fr-004-account-fields--default-types)
  - [FR-005: Custom Accounts & Account List](#fr-005-custom-accounts--account-list)
  - [FR-006: Account Deletion Constraints](#fr-006-account-deletion-constraints)
- [Module 3: Transaction Management](#module-3-transaction-management)
  - [FR-007: Income & Expense Transaction Creation](#fr-007-income--expense-transaction-creation)
  - [FR-008: Transfer Transaction Creation](#fr-008-transfer-transaction-creation)
  - [FR-009: Transaction Edit & Deletion](#fr-009-transaction-edit--deletion)
- [Module 4: Categories & Tags](#module-4-categories--tags)
  - [FR-010: Categories Management](#fr-010-categories-management)
- [Module 5: Budget Management](#module-5-budget-management)
  - [FR-011: Budget Creation & Monitoring](#fr-011-budget-creation--monitoring)
- [Module 6: Dashboard](#module-6-dashboard)
  - [FR-012: Dashboard Summary Metrics & Budget Status Widget](#fr-012-dashboard-summary-metrics--budget-status-widget)
  - [FR-013: Dashboard Live Updates & Refresh](#fr-013-dashboard-live-updates--refresh)
- [Module 7: Reports & Analytics](#module-7-reports--analytics)
  - [FR-014: Reports Filter & Visualization](#fr-014-reports-filter--visualization)
- [Module 8: PDF Export](#module-8-pdf-export)
  - [FR-015: PDF Export Structure & Filters](#fr-015-pdf-export-structure--filters)

---

## Module 1: Authentication & User Management

### FR-001: User Signup & Registration

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Signup with valid inputs | Validate user can successfully sign up using a unique username and password |
| 2 | Mandatory fields validation | Validate registration fails if username or password is empty |
| 3 | Password length validation | Validate registration fails if password is less than 8 characters |
| 4 | Duplicate username prevention | Validate registration fails when username already exists in the system |
| 5 | Password hashing validation | Validate that passwords are stored securely as hashes using BCrypt |
| 6 | Auto-login after signup | Validate user is automatically logged in after successful signup |

### FR-002: User Login

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Login with valid credentials | Validate user can successfully login using registered username and password |
| 2 | Mandatory username validation | Validate login fails when username is empty |
| 3 | Mandatory password validation | Validate login fails when password is empty |
| 4 | Invalid username validation | Validate login fails when username does not exist |
| 5 | Invalid password validation | Validate login fails when password is incorrect |
| 6 | Token response validation | Validate JWT token is generated after successful authentication and returned in response |

### FR-003: Session Management & Logout

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | JWT session persistence validation | Validate client maintains active session while JWT token is present and valid |
| 2 | JWT expiration enforcement | Validate session is invalidated when the JWT token expires |
| 3 | User logout | Validate that user can log out, discarding the client-side JWT token |
| 4 | Route authorization validation | Validate that protected API endpoints and screens reject requests without an active session |

---

## Module 2: Financial Accounts

### FR-004: Account Fields & Default Types

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Render default account types | Validate default account types (Credit Card, Bank Account, Cash Wallet) are available |
| 2 | Account fields validation | Validate fields (Account Name, Account Type, Opening Balance) are validated properly |
| 3 | Account name uniqueness | Validate account name must be unique per user |
| 4 | Opening balance validation | Validate opening balance can be positive, negative, or zero |

### FR-005: Custom Accounts & Account List

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Create custom accounts | Validate users can create unlimited accounts (e.g. Salary Account, Savings Account, Travel Wallet, Business Account) |
| 2 | Retrieve account list | Validate application successfully retrieves all accounts for the authenticated user |
| 3 | User-level data isolation | Validate users cannot view or modify accounts of other users |

### FR-006: Account Deletion Constraints

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Prevent deletion if transactions exist | Validate account deletion is rejected if there are transactions associated with it |
| 2 | Delete empty account | Validate account can be deleted if it has no transactions associated with it |

---

## Module 3: Transaction Management

### FR-007: Income & Expense Transaction Creation

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Create transaction with valid inputs | Validate user can create income/expense with amount, date, description, account, and category |
| 2 | Invalid amount validation | Validate transaction creation is rejected if amount is less than or equal to zero |
| 3 | Future date validation | Validate transaction creation is rejected if date is in the future |
| 4 | Mandatory fields validation | Validate transaction creation is rejected if account, category, or empty description is missing |
| 5 | Income transaction balance logic | Validate income transaction increases the associated account balance |
| 6 | Expense transaction balance logic | Validate expense transaction decreases the associated account balance |

### FR-008: Transfer Transaction Creation

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Create transfer with valid inputs | Validate transfer can be created with source account, destination account, amount, date, and description |
| 2 | Transfer account validation | Validate transfer is rejected if source and destination accounts are identical |
| 3 | Transfer balance updates | Validate source account balance decreases and destination account balance increases by transfer amount |
| 4 | Transfer exclusions | Validate transfers are excluded from income and expense calculations |

### FR-009: Transaction Edit & Deletion

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Edit transaction updates | Validate allowed fields (amount, date, description, account, category) can be edited |
| 2 | Recalculate balance on edit | Validate account balances are recalculated correctly after editing a transaction |
| 3 | Delete transaction | Validate transaction can be deleted |
| 4 | Recalculate balance on delete | Validate account balances are recalculated correctly after deleting a transaction |

---

## Module 4: Categories & Tags

### FR-010: Categories Management

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | System default categories | Validate default categories (Grocery, Rent, Petrol, Restaurant, Clothing, Accessories, Home Appliances, Others) are available |
| 2 | Custom categories creation | Validate users can create custom income and expense categories |
| 3 | Duplicate category prevention | Validate category names must be unique per user |
| 4 | Category user isolation | Validate categories are user-specific and isolated |

---

## Module 5: Budget Management

### FR-011: Budget Creation & Monitoring

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Monthly budget setup | Validate users can define a monthly budget per category |
| 2 | Budget amount validation | Validate budget amount can be zero or positive |
| 3 | Budget usage formula | Validate budget usage percentage is calculated correctly (Category Expense / Budget Amount * 100) |
| 4 | Budget status normal threshold | Validate budget status is Normal if usage is less than 80% |
| 5 | Budget status warning threshold | Validate budget status is Warning if usage is between 80% and 100% |
| 6 | Budget status exceeded threshold | Validate budget status is Exceeded if usage is above 100%, and allow save with warning |

---

## Module 6: Dashboard

### FR-012: Dashboard Summary Metrics & Budget Status Widget

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Total Balance calculation | Validate Total Balance displays the sum of all account balances |
| 2 | Monthly Income calculation | Validate Monthly Income displays total income in the current month |
| 3 | Monthly Expense calculation | Validate Monthly Expense displays total expenses in the current month |
| 4 | Net Worth calculation | Validate Net Worth displays (Bank Accounts + Cash Wallets) - (Credit Cards) |
| 5 | Budget Status Widget metrics | Validate widget displays budget amount, amount spent, remaining budget, and progress indicator |

### FR-013: Dashboard Live Updates & Refresh

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Live refresh on transaction changes | Validate dashboard metrics update immediately after creating, updating, or deleting a transaction |
| 2 | Live refresh on transfer changes | Validate dashboard metrics update immediately after creating a transfer |

---

## Module 7: Reports & Analytics

### FR-014: Reports Filter & Visualization

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Supported filters | Validate report filtering by Income vs Expense, Category-wise, and Account-wise |
| 2 | Time periods | Validate report filtering by Weekly or Monthly periods |
| 3 | Visualization types | Validate reports render using Pie Charts or Bar Charts |
| 4 | Income vs Expense Report metrics | Validate report displays Total Income, Total Expense, and Savings (Income - Expense) |
| 5 | Category Report metrics | Validate report displays Category Spend and Category Percentage |
| 6 | Account Report metrics | Validate report displays Transaction Count, Total Spending, and Current Balance |

---

## Module 8: PDF Export

### FR-015: PDF Export Structure & Filters

| Sr. No. | KPI | Validation Function |
| :---: | :--- | :--- |
| 1 | Export filters validation | Validate PDF export respects selected report filters and time periods |
| 2 | PDF Header structure | Validate PDF header contains FinBuddy Report, Generated Date, Selected Filters, and Time Period |
| 3 | PDF Body structure | Validate PDF body contains charts, summary metrics, and transaction data |
| 4 | PDF Footer structure | Validate PDF footer contains page numbers |
| 5 | PDF export empty data | Validate PDF export generates an empty report when no data exists |
