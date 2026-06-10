# FinBuddy – Technical API Documentation

## Base URL
- Local Development: `http://localhost:8080`

---

## Global Headers
For all endpoints except **Authentication** APIs, the following authorization header is mandatory:
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

---

## 1. Authentication APIs

### A. User Signup & Registration
- **Endpoint:** `POST /api/auth/signup`
- **Authentication Required:** No
- **Request Body (`UserRegisterRequest`):**
  ```json
  {
    "username": "john123",
    "password": "password123"
  }
  ```
- **Validation Rules:**
  - `username` must be unique in the system.
  - `password` must be at least 8 characters long.
- **Success Response (`200 OK`):**
  ```json
  {
    "userId": "d74e0d9b-8f3e-48a5-9bb7-4589255a40b9",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```
- **Error Responses:**
  - `400 Bad Request` (Password too short or fields empty)
  - `409 Conflict` (Username already exists)

### B. User Login
- **Endpoint:** `POST /api/auth/login`
- **Authentication Required:** No
- **Request Body (`UserLoginRequest`):**
  ```json
  {
    "username": "john123",
    "password": "password123"
  }
  ```
- **Success Response (`200 OK`):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```
- **Error Responses:**
  - `400 Bad Request` (Empty fields)
  - `401 Unauthorized` (Invalid username or password)

---

## 2. Financial Account APIs (Secured)

### A. Create Account
- **Endpoint:** `POST /api/accounts`
- **Request Body (`AccountCreateRequest`):**
  ```json
  {
    "accountName": "Salary Account",
    "accountType": "BANK_ACCOUNT", 
    "openingBalance": 15000.00
  }
  ```
  *Note: Valid account types are `BANK_ACCOUNT`, `CASH_WALLET`, `CREDIT_CARD` (case-insensitive).*
- **Success Response (`200 OK`):**
  ```json
  {
    "id": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "accountName": "Salary Account",
    "accountType": "BANK_ACCOUNT",
    "openingBalance": 15000.00
  }
  ```
- **Error Responses:**
  - `400 Bad Request` (Empty account name/type, duplicate name for user)

### B. List Accounts
- **Endpoint:** `GET /api/accounts`
- **Success Response (`200 OK`):**
  ```json
  [
    {
      "id": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
      "accountName": "Salary Account",
      "accountType": "BANK_ACCOUNT",
      "openingBalance": 15000.00
    }
  ]
  ```

### C. Update Account
- **Endpoint:** `PUT /api/accounts/{id}`
- **Request Body (`AccountUpdateRequest`):**
  ```json
  {
    "accountName": "Primary Savings",
    "accountType": "BANK_ACCOUNT",
    "openingBalance": 16000.00
  }
  ```
- **Success Response (`200 OK`):**
  ```json
  {
    "id": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "accountName": "Primary Savings",
    "accountType": "BANK_ACCOUNT",
    "openingBalance": 16000.00
  }
  ```

### D. Delete Account
- **Endpoint:** `DELETE /api/accounts/{id}`
- **Success Response (`200 OK`):** Empty body
- **Validation Rules:**
  - Account cannot be deleted if any transactions or transfers reference it.
- **Error Responses:**
  - `400 Bad Request` (Account has existing transactions/transfers, or account not found)

---

## 3. Category APIs (Secured)

### A. Create Category
- **Endpoint:** `POST /api/categories`
- **Request Body (`CategoryCreateRequest`):**
  ```json
  {
    "categoryName": "Electricity",
    "categoryType": "EXPENSE"
  }
  ```
  *Note: Valid category types are `INCOME` or `EXPENSE`.*
- **Success Response (`200 OK`):**
  ```json
  {
    "id": "fa2c30df-1d65-4f7d-81ee-bb840c265691",
    "categoryName": "Electricity",
    "categoryType": "EXPENSE",
    "isSystem": false
  }
  ```
- **Error Responses:**
  - `400 Bad Request` (Empty name, duplicate custom category name, or clashes with default system names)

### B. List Categories
- **Endpoint:** `GET /api/categories`
- **Success Response (`200 OK`):**
  *Returns seeded system defaults (Grocery, Rent, Petrol, Restaurant, Clothing, Accessories, Home Appliances, Others) plus the user's custom categories.*
  ```json
  [
    {
      "id": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
      "categoryName": "Grocery",
      "categoryType": "EXPENSE",
      "isSystem": true
    },
    {
      "id": "fa2c30df-1d65-4f7d-81ee-bb840c265691",
      "categoryName": "Electricity",
      "categoryType": "EXPENSE",
      "isSystem": false
    }
  ]
  ```

---

## 4. Transaction & Transfer APIs (Secured)

### A. Create Transaction
- **Endpoint:** `POST /api/transactions`
- **Request Body (`TransactionCreateRequest`):**
  ```json
  {
    "type": "EXPENSE",
    "amount": 450.00,
    "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "description": "Weekly Groceries",
    "date": "2026-06-10"
  }
  ```
- **Validation Rules:**
  - `amount` must be greater than zero.
  - `date` must be in `YYYY-MM-DD` format and cannot be in the future.
  - `description` cannot be empty.
- **Success Response (`200 OK`):**
  *Also dynamically updates the associated account balance.*
  ```json
  {
    "id": "ba45f8e0-bb12-4211-ac9f-aa019cc20d91",
    "type": "EXPENSE",
    "amount": 450.00,
    "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "description": "Weekly Groceries",
    "date": "2026-06-10"
  }
  ```

### B. List Transactions (with filtering)
- **Endpoint:** `GET /api/transactions`
- **Query Parameters (All Optional):**
  - `startDate`: filter transactions starting from date (`YYYY-MM-DD`)
  - `endDate`: filter transactions up to date (`YYYY-MM-DD`)
  - `type`: filter by type (`INCOME`/`EXPENSE`)
  - `accountId`: filter by account UUID
  - `categoryId`: filter by category UUID
- **Success Response (`200 OK`):**
  ```json
  [
    {
      "id": "ba45f8e0-bb12-4211-ac9f-aa019cc20d91",
      "type": "EXPENSE",
      "amount": 450.00,
      "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
      "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
      "description": "Weekly Groceries",
      "date": "2026-06-10"
    }
  ]
  ```

### C. Update Transaction
- **Endpoint:** `PUT /api/transactions/{id}`
- **Request Body (`TransactionUpdateRequest`):**
  ```json
  {
    "type": "EXPENSE",
    "amount": 500.00,
    "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "description": "Weekly Groceries (updated)",
    "date": "2026-06-10"
  }
  ```
- **Success Response (`200 OK`):**
  *Reverts old transaction account balance impact and applies updated impact.*
  ```json
  {
    "id": "ba45f8e0-bb12-4211-ac9f-aa019cc20d91",
    "type": "EXPENSE",
    "amount": 500.00,
    "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "description": "Weekly Groceries (updated)",
    "date": "2026-06-10"
  }
  ```

### D. Delete Transaction
- **Endpoint:** `DELETE /api/transactions/{id}`
- **Success Response (`200 OK`):** Empty body
  *Reverts the balance impact on the associated account.*

### E. Create Transfer
- **Endpoint:** `POST /api/transfers`
- **Request Body (`TransferCreateRequest`):**
  ```json
  {
    "sourceAccountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "destinationAccountId": "a15f013d-1a89-4b62-9e90-c1bfa780d192",
    "amount": 1000.00,
    "description": "Bank to Cash Wallet",
    "date": "2026-06-08"
  }
  ```
- **Validation Rules:**
  - Source and destination accounts must not be identical.
  - Amount must be greater than zero.
  - Date must be in `YYYY-MM-DD` format and cannot be in the future.
- **Success Response (`200 OK`):**
  *Deducts amount from source account and adds to destination account.*
  ```json
  {
    "id": "ca238e91-d2bc-44aa-9c02-fe810cc918fa",
    "sourceAccountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
    "destinationAccountId": "a15f013d-1a89-4b62-9e90-c1bfa780d192",
    "amount": 1000.00,
    "description": "Bank to Cash Wallet",
    "date": "2026-06-08"
  }
  ```

---

## 5. Budget Management APIs (Secured)

### A. Create/Setup Budget
- **Endpoint:** `POST /api/budgets`
- **Request Body (`BudgetCreateRequest`):**
  ```json
  {
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "month": 6,
    "year": 2026,
    "budgetAmount": 5000.00
  }
  ```
- **Validation Rules:**
  - `month` must be between 1 and 12.
  - `year` must be >= 2000.
  - `budgetAmount` must be zero or positive.
  - Category must be unique per month/year period config (no duplicates).
- **Success Response (`200 OK`):**
  *Includes dynamically aggregated spends and status alerts.*
  ```json
  {
    "id": "db81a9cd-2ba1-4cfa-9310-44bbfa890e12",
    "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
    "month": 6,
    "year": 2026,
    "budgetAmount": 5000.00,
    "spentAmount": 500.00,
    "remainingAmount": 4500.00,
    "usagePercentage": 10.0,
    "status": "NORMAL"
  }
  ```
  *Note: Status threshold triggers: `NORMAL` (< 80%), `WARNING` (80% - 100%), `EXCEEDED` (> 100%).*

### B. List Budgets
- **Endpoint:** `GET /api/budgets`
- **Success Response (`200 OK`):**
  ```json
  [
    {
      "id": "db81a9cd-2ba1-4cfa-9310-44bbfa890e12",
      "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
      "month": 6,
      "year": 2026,
      "budgetAmount": 5000.00,
      "spentAmount": 500.00,
      "remainingAmount": 4500.00,
      "usagePercentage": 10.0,
      "status": "NORMAL"
    }
  ]
  ```

---

## 6. Dashboard APIs (Secured)

### A. Get Summary Dashboard Data
- **Endpoint:** `GET /api/dashboard`
- **Query Parameters (Optional):**
  - `month`: selected month integer (1-12)
  - `year`: selected year integer (>= 2000)
  *Defaults to the current calendar month and year if parameters are omitted.*
- **Success Response (`200 OK`):**
  ```json
  {
    "totalBalance": 16500.00,
    "monthlyIncome": 3000.00,
    "monthlyExpense": 500.00,
    "netWorth": 15000.00,
    "budgets": [
      {
        "id": "db81a9cd-2ba1-4cfa-9310-44bbfa890e12",
        "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
        "month": 6,
        "year": 2026,
        "budgetAmount": 5000.00,
        "spentAmount": 500.00,
        "remainingAmount": 4500.00,
        "usagePercentage": 10.0,
        "status": "NORMAL"
      }
    ]
  }
  ```
  *Note: Net Worth is calculated as: `(Bank Accounts + Cash Wallets) - (Credit Cards)`.*

---

## 7. Reports & Analytics APIs (Secured)

### A. Get Income vs Expense Report
- **Endpoint:** `GET /api/reports/income-expense`
- **Query Parameters:**
  - `period`: `"weekly"` or `"monthly"` (Required, case-insensitive)
  - `date`: anchor date (`YYYY-MM-DD`, Optional, defaults to today)
- **Success Response (`200 OK`):**
  ```json
  {
    "totalIncome": 3000.00,
    "totalExpense": 500.00,
    "savings": 2500.00,
    "breakdown": [
      {
        "label": "2026-06-08",
        "income": 3000.00,
        "expense": 0.0
      },
      {
        "label": "2026-06-09",
        "income": 0.0,
        "expense": 500.0
      }
    ]
  }
  ```

### B. Get Category Spending Report
- **Endpoint:** `GET /api/reports/category`
- **Query Parameters:**
  - `period`: `"weekly"` or `"monthly"` (Required, case-insensitive)
  - `date`: anchor date (`YYYY-MM-DD`, Optional, defaults to today)
- **Success Response (`200 OK`):**
  ```json
  {
    "totalExpense": 500.00,
    "categories": [
      {
        "categoryId": "3c3d5232-a63e-4682-bbfe-85ffc778fa01",
        "categoryName": "Grocery",
        "spend": 500.00,
        "percentage": 100.0
      }
    ]
  }
  ```

### C. Get Account Activity Report
- **Endpoint:** `GET /api/reports/account`
- **Query Parameters:**
  - `period`: `"weekly"` or `"monthly"` (Required, case-insensitive)
  - `date`: anchor date (`YYYY-MM-DD`, Optional, defaults to today)
- **Success Response (`200 OK`):**
  ```json
  {
    "accounts": [
      {
        "accountId": "e93fca10-09a2-4a00-bb6f-124b89e20a32",
        "accountName": "Primary Savings",
        "transactionCount": 2,
        "totalSpending": 500.00,
        "currentBalance": 16500.00
      }
    ]
  }
  ```

---

## 8. PDF Export APIs (Secured)

### A. Export PDF Reports
- **Endpoint:** `GET /api/reports/export/pdf`
- **Query Parameters:**
  - `reportType`: `"income-expense"`, `"category"`, or `"account"` (Required)
  - `period`: `"weekly"` or `"monthly"` (Required)
  - `date`: anchor date (`YYYY-MM-DD`, Optional)
  - `accountId`: filter report by account UUID (Optional)
  - `categoryId`: filter report by category UUID (Optional)
- **Success Response (`200 OK`):**
  - **Headers:** `Content-Type: application/pdf`, `Content-Disposition: attachment; filename="finbuddy_report.pdf"`
  - **Body:** Raw binary PDF byte stream.
