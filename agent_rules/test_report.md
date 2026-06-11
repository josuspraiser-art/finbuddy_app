# FinBuddy – Test Execution Report

This document records the results and execution status of all unit and instrumented tests in the FinBuddy project. It serves as the single source of truth for test verification, tracking current successes, failures, and updates.

---

## 📊 Summary of Test Results (Current Run)

* **Last Updated:** 2026-06-11T13:10:21+05:30 (Local Time)
* **Build / Test Status:** `SUCCESSFUL`
* **Total Tests Executed:** 26 (Unit Tests)
* **Total Passed:** 26
* **Total Failed:** 0
* **Success Rate:** 100%

| Test Suite / Class | Type | Total Tests | Passed | Failed | Status |
| :--- | :--- | :---: | :---: | :---: | :---: |
| [ExampleUnitTest](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ExampleUnitTest.kt) | Local Unit Test | 1 | 1 | 0 | PASSED |
| [AuthViewModelTest](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/auth/AuthViewModelTest.kt) | Local Unit Test | 8 | 8 | 0 | PASSED |
| [AccountViewModelTest](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/accounts/AccountViewModelTest.kt) | Local Unit Test | 8 | 8 | 0 | PASSED |
| [TransactionViewModelTest](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/transactions/TransactionViewModelTest.kt) | Local Unit Test | 9 | 9 | 0 | PASSED |
| [ExampleInstrumentedTest](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/androidTest/java/com/example/findbuddy/ExampleInstrumentedTest.kt) | Instrumented Test | 1 | - | - | UNRUN (Requires Device/Emulator) |

---

## ⚙️ Environment & Build Configuration

* **OS:** Windows
* **Android Gradle Plugin (AGP):** v9.1.1
* **Kotlin Version:** v2.2.10
* **KSP Version:** v2.2.10-2.0.2
* **Dagger Hilt:** v2.59.2
* **Compile SDK / Target SDK:** 37 / 35
* **Gradle Command Executed:** `.\gradlew.bat test`
* **Experimental Flags Enabled:** `android.disallowKotlinSourceSets=false`

---

## 📝 Detailed Test Execution Logs

### 1. ExampleUnitTest
* **Class Path:** `com.example.findbuddy.ExampleUnitTest`
* **File:** [ExampleUnitTest.kt](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ExampleUnitTest.kt)

| Sr. No. | Test Case Name | Description | Status |
| :---: | :--- | :--- | :---: |
| 1 | `addition_isCorrect` | Validates standard arithmetic addition operation on JVM environment. | PASSED |

### 2. AuthViewModelTest (MVI & State Validation)
* **Class Path:** `com.example.findbuddy.ui.auth.AuthViewModelTest`
* **File:** [AuthViewModelTest.kt](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/auth/AuthViewModelTest.kt)

| Sr. No. | Test Case Name | Description | Status |
| :---: | :--- | :--- | :---: |
| 1 | `initial state is empty` | Validates that `AuthViewModel` state flows initialize with empty fields, null errors, and `isLoading` false. | PASSED |
| 2 | `intent username changed updates state` | Validates that emitting `AuthIntent.UsernameChanged` updates the UI state username property correctly. | PASSED |
| 3 | `intent password changed updates state` | Validates that emitting `AuthIntent.PasswordChanged` updates the UI state password property correctly. | PASSED |
| 4 | `intent confirm password changed updates state` | Validates that emitting `AuthIntent.ConfirmPasswordChanged` updates the UI state confirmPassword property. | PASSED |
| 5 | `login fails when fields are empty` | Validates that submitting login with empty fields updates the state with the validation error message. | PASSED |
| 6 | `signup fails when master keys mismatch` | Validates that submitting signup with mismatched password and confirmPassword triggers the mismatch error. | PASSED |
| 7 | `signup fails when password length is under 8 characters` | Validates that submitting signup with a password length of < 8 characters updates the state with the length validation error. | PASSED |
| 8 | `signup succeeds with matching credentials` | Validates that a successful signup using matching, valid inputs performs repository registration, handles loading state, and successfully updates the JWT session token. | PASSED |

### 3. AccountViewModelTest (MVI & Account Management)
* **Class Path:** `com.example.findbuddy.ui.accounts.AccountViewModelTest`
* **File:** [AccountViewModelTest.kt](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/accounts/AccountViewModelTest.kt)

| Sr. No. | Test Case Name | Description | Status |
| :---: | :--- | :--- | :---: |
| 1 | `initial state has empty list and is not loading` | Validates that the accounts screen initial state lists empty accounts and sets loading states appropriately during dispatcher scheduling. | PASSED |
| 2 | `intent load accounts fetches list and triggers sync` | Validates that accounts are successfully loaded from local repository and remote API synchronization is triggered. | PASSED |
| 3 | `create account fails when name is empty` | Validates that trying to create an account with an empty name displays the validation error. | PASSED |
| 4 | `create account fails when name is duplicate` | Validates that trying to create an account with a duplicate name is rejected with a unique name validation error. | PASSED |
| 5 | `create account succeeds with valid params` | Validates that creating an account with valid name, type, and balance succeeds and updates the states. | PASSED |
| 6 | `update account succeeds with valid params` | Validates that editing an account's properties correctly modifies it. | PASSED |
| 7 | `delete account succeeds` | Validates that a successful account deletion removes it from the list. | PASSED |
| 8 | `delete account fails when repository throws constraint error` | Validates that if delete constraints fail (e.g. transactions exist), deletion is prevented and an error is set in UI state. | PASSED |

### 4. TransactionViewModelTest (MVI & Transaction/Transfer Validation)
* **Class Path:** `com.example.findbuddy.ui.transactions.TransactionViewModelTest`
* **File:** [TransactionViewModelTest.kt](file:///c:/Users/user/Josus/finalPOC/finBuddyApp/app/src/test/java/com/example/findbuddy/ui/transactions/TransactionViewModelTest.kt)

| Sr. No. | Test Case Name | Description | Status |
| :---: | :--- | :--- | :---: |
| 1 | `initial state sets default date and is loading` | Validates that default dates are set and fields initialize empty. | PASSED |
| 2 | `intent load initial data fetches accounts and categories` | Validates that accounts and categories are fetched from local and remote API. | PASSED |
| 3 | `intent change type and inputs updates state correctly` | Validates standard flow state updates. | PASSED |
| 4 | `save fails when amount is negative or zero` | Validates transaction rejects invalid amount values. | PASSED |
| 5 | `save fails when description is empty` | Validates transaction rejects blank description fields. | PASSED |
| 6 | `save standard transaction succeeds` | Validates saving standard transaction reduces state and closes screen. | PASSED |
| 7 | `save transfer fails when accounts are identical` | Validates transfer rejects identical source and destination accounts. | PASSED |
| 8 | `save transfer succeeds` | Validates saving a valid transfer triggers correct state updates. | PASSED |
| 9 | `delete transaction succeeds` | Validates transaction deletion triggers successfully. | PASSED |

---

## ⏭️ Instructions for Logging Future Test Cases

All developers and AI agents must update this document whenever new tests are added, modified, or executed. 

### Step-by-Step Update Protocol:
1. **Run Test Suites:** Execute unit tests using `.\gradlew.bat test` or the appropriate module task.
2. **Update Execution Statistics:** Modify the **Summary of Test Results** table at the top of the file with the latest counts and local timestamp.
3. **Log New Test Cases:**
   - If adding tests to existing classes, append rows to their respective tables.
   - If creating new test classes, create a new section under **Detailed Test Execution Logs** following the table format.
4. **Append to History Log:** Add an entry in the **Test Run Changelog / History** below.

### Test Run Changelog / History

| Timestamp (ISO-8601) | Trigger/Reason | Executed Commands | Run Result | Verified By |
| :--- | :--- | :--- | :--- | :--- |
| 2026-06-11T12:27:11+05:30 | Initial Verification of Module 1 (Auth MVI) | `.\gradlew.bat test` | `BUILD SUCCESSFUL` (9 Passed, 0 Failed) | Antigravity AI |
| 2026-06-11T12:33:02+05:30 | Implementation of Module 2 (Accounts DB/MVI) | `.\gradlew.bat test` | `BUILD SUCCESSFUL` (17 Passed, 0 Failed) | Antigravity AI |
| 2026-06-11T13:10:21+05:30 | Implementation of Module 3 (Transaction MVI) | `.\gradlew.bat test` | `BUILD SUCCESSFUL` (26 Passed, 0 Failed) | Antigravity AI |
