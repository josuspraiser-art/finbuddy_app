# FinBuddy – Technical Product Requirements Document (PRD)

## 1. Problem Statement

### Background

Many personal finance applications rely on bank integrations and third-party data aggregation, creating privacy concerns and reducing user control over financial information.

Users who prefer full ownership of their financial data need a simple, mobile-first solution that allows them to manually track finances, manage budgets, monitor spending behavior, and generate reports without connecting bank accounts.

### Problem

Users currently lack a lightweight, privacy-focused budgeting application that enables:

* Manual financial account management
* Income and expense tracking
* Budget monitoring
* Spending analysis
* Financial reporting

while maintaining complete control over their financial data.

### Goals

#### Business Goals

* Enable users to manually manage personal finances.
* Provide privacy-first financial tracking.
* Deliver meaningful financial insights through reporting.
* Create a scalable foundation for future financial management features.

#### User Goals

* Track income and expenses easily.
* Manage multiple financial accounts.
* Monitor spending against budgets.
* Understand financial health through reports.
* Export financial data when needed.

#### Non-Goals

* Bank integrations
* Automatic transaction imports
* Investment tracking
* Multi-currency support
* Shared accounts
* Recurring transactions

### Success Criteria

* User completes onboarding within 3 minutes.
* Transaction entry completed within 15 seconds.
* Budget calculations remain accurate.
* Reports generated within 3 seconds.
* PDF exports generated within 5 seconds.

---

# 2. Solution Overview

FinBuddy is a mobile-first personal finance management application that allows users to manually manage financial accounts and transactions while maintaining complete privacy and ownership of their financial information.

### Core Modules

1. Authentication & User Management
2. Financial Accounts
3. Transaction Management
4. Categories & Tags
5. Budget Management
6. Dashboard
7. Reports & Analytics
8. PDF Export

---

# Functional Requirements

## Module 1: Authentication & User Management

### Features

* User Signup
* User Login
* Session Management
* Logout

### Signup Requirements

| Field    | Required | Validation           |
| -------- | -------- | -------------------- |
| Username | Yes      | Unique               |
| Password | Yes      | Minimum 8 characters |

### Business Rules

* Username must be unique.
* Password must be securely hashed using BCrypt.
* User is automatically logged in after successful signup.
* User sessions remain active until logout or token expiration.

### Database Schema

#### users

| Column        | Type         |
| ------------- | ------------ |
| id            | UUID         |
| username      | VARCHAR(100) |
| password_hash | TEXT         |
| created_at    | TIMESTAMP    |
| updated_at    | TIMESTAMP    |

---

## Module 2: Financial Accounts

### Default Account Types

* Credit Card
* Bank Account
* Cash Wallet

### Custom Accounts

Users can create unlimited accounts.

Examples:

* Salary Account
* Savings Account
* Travel Wallet
* Business Account

### Account Fields

| Field           | Required |
| --------------- | -------- |
| Account Name    | Yes      |
| Account Type    | Yes      |
| Opening Balance | Yes      |

### Business Rules

* Account names must be unique per user.
* Opening balance may be positive, negative, or zero.
* Users can maintain unlimited accounts.

### Database Schema

#### accounts

| Column          | Type          |
| --------------- | ------------- |
| id              | UUID          |
| user_id         | UUID          |
| account_name    | VARCHAR(100)  |
| account_type    | VARCHAR(50)   |
| opening_balance | DECIMAL(15,2) |
| created_at      | TIMESTAMP     |
| updated_at      | TIMESTAMP     |

---

## Module 3: Transaction Management

### Supported Transaction Types

* Income
* Expense
* Transfer

### Required Transaction Fields

| Field       | Required |
| ----------- | -------- |
| Amount      | Yes      |
| Date        | Yes      |
| Description | Yes      |
| Account     | Yes      |
| Category    | Yes      |

### Validation Rules

| Validation        | Expected Behavior |
| ----------------- | ----------------- |
| Amount <= 0       | Reject            |
| Future Date       | Reject            |
| Missing Account   | Reject            |
| Missing Category  | Reject            |
| Empty Description | Reject            |

### Income Transaction Logic

Account Balance = Current Balance + Amount

### Expense Transaction Logic

Account Balance = Current Balance - Amount

### Transfer Transaction Logic

Required Fields:

* Source Account
* Destination Account
* Amount
* Date
* Description

Balance Updates:

* Source Account Balance -= Amount
* Destination Account Balance += Amount

### Transfer Business Rules

* Source and destination accounts cannot be identical.
* Transfers are excluded from income calculations.
* Transfers are excluded from expense calculations.

### Edit Transaction

Allowed Updates:

* Amount
* Date
* Description
* Account
* Category

System must recalculate balances after update.

### Delete Transaction

System must recalculate balances after deletion.

### Database Schema

#### transactions

| Column           | Type          |
| ---------------- | ------------- |
| id               | UUID          |
| user_id          | UUID          |
| transaction_type | ENUM          |
| account_id       | UUID          |
| category_id      | UUID          |
| amount           | DECIMAL(15,2) |
| transaction_date | DATE          |
| description      | TEXT          |
| created_at       | TIMESTAMP     |
| updated_at       | TIMESTAMP     |

#### transfers

| Column                 | Type          |
| ---------------------- | ------------- |
| id                     | UUID          |
| user_id                | UUID          |
| source_account_id      | UUID          |
| destination_account_id | UUID          |
| amount                 | DECIMAL(15,2) |
| transfer_date          | DATE          |
| description            | TEXT          |
| created_at             | TIMESTAMP     |

---

## Module 4: Categories & Tags

### System Categories

#### Expense Categories

* Grocery
* Rent
* Petrol
* Restaurant
* Clothing
* Accessories
* Home Appliances
* Others

### Custom Categories

Users can create:

* Income Categories
* Expense Categories

### Business Rules

* Categories are user-specific.
* Category names must be unique per user.

### Database Schema

#### categories

| Column        | Type         |
| ------------- | ------------ |
| id            | UUID         |
| user_id       | UUID         |
| category_name | VARCHAR(100) |
| category_type | ENUM         |
| is_system     | BOOLEAN      |

---

## Module 5: Budget Management

### Monthly Budgets

Users can define monthly budgets per category.

Example:

| Category | Monthly Budget |
| -------- | -------------- |
| Grocery  | ₹10,000        |
| Rent     | ₹20,000        |

### Budget Status Thresholds

| Usage         | Status   |
| ------------- | -------- |
| Less than 80% | Normal   |
| 80% - 100%    | Warning  |
| Above 100%    | Exceeded |

### Budget Formula

Budget Usage (%) = Category Expense / Budget Amount × 100

### Database Schema

#### budgets

| Column        | Type          |
| ------------- | ------------- |
| id            | UUID          |
| user_id       | UUID          |
| category_id   | UUID          |
| month         | INTEGER       |
| year          | INTEGER       |
| budget_amount | DECIMAL(15,2) |

---

## Module 6: Dashboard

### Dashboard Components

#### Total Balance

Sum of all account balances.

#### Monthly Income

Total income recorded in the current month.

#### Monthly Expense

Total expenses recorded in the current month.

#### Net Worth

Net Worth = Assets - Liabilities

Asset Accounts:

* Bank Accounts
* Cash Wallets

Liability Accounts:

* Credit Cards

#### Budget Status Widget

Display:

* Budget Amount
* Amount Spent
* Remaining Budget
* Progress Indicator

### Refresh Behavior

Dashboard updates immediately after:

* Transaction creation
* Transaction update
* Transaction deletion
* Transfer creation

---

## Module 7: Reports & Analytics

### Supported Filters

1. Income vs Expense
2. Category-wise
3. Account-wise

### Time Periods

* Weekly
* Monthly

### Visualization Types

* Pie Chart
* Bar Chart

### Income vs Expense Report

Metrics:

* Total Income
* Total Expense
* Savings

Savings = Income - Expense

### Category Report

Metrics:

* Category Spend
* Category Percentage

### Account Report

Metrics:

* Transaction Count
* Total Spending
* Current Balance

---

## Module 8: PDF Export

### Supported Export Types

* Income Report
* Expense Report
* Category Report
* Account Report

### Export Filters

All report filters must be respected during export.

### PDF Structure

#### Header

* FinBuddy Report
* Generated Date
* Selected Filters
* Selected Time Period

#### Body

* Charts
* Summary Metrics
* Transaction Data

#### Footer

* Page Number

---

# 3. User Flow

## User Registration

Launch App

→ Signup

→ Enter Username

→ Enter Password

→ Account Created

→ Dashboard

---

## Add Expense Flow

Dashboard

→ Tap Floating Action Button (+)

→ Select Expense

→ Enter Amount

→ Select Account

→ Select Category

→ Enter Description

→ Select Date

→ Save

→ Dashboard Updated

### Mobile UX Requirements

* Floating Action Button always visible.
* Amount field focused by default.
* Last used account automatically selected.
* Searchable category selection.
* Single-screen transaction creation.

Target Interaction Count:

Maximum 5 interactions.

---

## Add Income Flow

Dashboard

→ Add Transaction

→ Income

→ Enter Details

→ Save

---

## Transfer Flow

Dashboard

→ Transfer

→ Source Account

→ Destination Account

→ Amount

→ Save

---

## Budget Setup Flow

Budget Screen

→ Select Category

→ Enter Budget Amount

→ Save

---

## Report Generation Flow

Reports

→ Select Filter

→ Select Weekly or Monthly

→ View Report

→ Export PDF

---

# 4. API Design

## Authentication APIs

### POST /api/auth/signup

Request

{
"username": "john123",
"password": "password123"
}

Response

{
"userId": "uuid",
"token": "jwt-token"
}

---

### POST /api/auth/login

Request

{
"username": "john123",
"password": "password123"
}

Response

{
"token": "jwt-token"
}

---

## Account APIs

### POST /api/accounts

{
"accountName": "Salary Account",
"accountType": "BANK_ACCOUNT",
"openingBalance": 50000
}

### GET /api/accounts

Returns all accounts.

### PUT /api/accounts/{id}

Update account.

### DELETE /api/accounts/{id}

Delete account.

Validation:

Prevent deletion if transactions exist.

---

## Transaction APIs

### POST /api/transactions

{
"type": "EXPENSE",
"amount": 1000,
"accountId": "uuid",
"categoryId": "uuid",
"description": "Groceries",
"date": "2026-06-10"
}

### GET /api/transactions

Filters:

* startDate
* endDate
* type
* accountId
* categoryId

### PUT /api/transactions/{id}

Update transaction.

### DELETE /api/transactions/{id}

Delete transaction.

---

## Transfer APIs

### POST /api/transfers

{
"sourceAccountId": "uuid",
"destinationAccountId": "uuid",
"amount": 5000,
"description": "Savings Transfer",
"date": "2026-06-10"
}

---

## Budget APIs

### POST /api/budgets

{
"categoryId": "uuid",
"month": 6,
"year": 2026,
"budgetAmount": 10000
}

### GET /api/budgets

Returns all budgets.

---

## Report APIs

### GET /api/reports/income-expense

Query Parameters:

period=weekly|monthly

### GET /api/reports/category

Query Parameters:

period=weekly|monthly

### GET /api/reports/account

Query Parameters:

period=weekly|monthly

### GET /api/reports/export/pdf

Query Parameters:

* reportType
* period
* filters

Response Content-Type:

application/pdf

---

# 5. Edge Cases

| Scenario                           | Expected Behavior       |
| ---------------------------------- | ----------------------- |
| Future transaction date            | Reject save             |
| Amount equals zero                 | Reject                  |
| Negative amount                    | Reject                  |
| Empty description                  | Reject                  |
| Duplicate category name            | Reject                  |
| Duplicate account name             | Reject                  |
| Transfer to same account           | Reject                  |
| Budget amount equals zero          | Allowed                 |
| Delete account with transactions   | Prevent deletion        |
| Edit historical transaction        | Recalculate balances    |
| Expense exceeds budget             | Allow save with warning |
| User creates 100+ accounts         | Supported               |
| User creates 100,000+ transactions | Support pagination      |
| PDF export with no data            | Generate empty report   |

---

# 6. Limitations

| Limitation                            | Reason                 |
| ------------------------------------- | ---------------------- |
| No bank integrations                  | Privacy-first approach |
| No automatic transaction imports      | Manual tracking only   |
| No recurring transactions             | Out of scope           |
| No future-dated transactions          | Product requirement    |
| No multi-currency support             | Out of scope           |
| No shared accounts                    | Out of scope           |
| No investment tracking                | Out of scope           |
| No receipt attachments                | Out of scope           |
| No notifications/reminders            | Out of scope           |
| Weekly and monthly reports only       | Defined scope          |
| Username/password authentication only | Defined scope          |

---

# Technical Considerations

## Database Indexing

CREATE INDEX idx_transactions_user_date
ON transactions(user_id, transaction_date);

CREATE INDEX idx_transactions_account
ON transactions(account_id);

CREATE INDEX idx_transactions_category
ON transactions(category_id);

CREATE INDEX idx_budgets_user_month
ON budgets(user_id, month, year);

## Security Requirements

* BCrypt password hashing
* JWT authentication
* HTTPS enforcement
* Parameterized SQL queries
* Server-side validation
* User-level data isolation

## Performance Targets

| Operation         | SLA         |
| ----------------- | ----------- |
| Login             | < 2 seconds |
| Add Transaction   | < 1 second  |
| Dashboard Load    | < 2 seconds |
| Report Generation | < 3 seconds |
| PDF Export        | < 5 seconds |

---

**End of Document**
