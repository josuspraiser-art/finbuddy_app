# FinBuddy Project – User Prompt Log

This document tracks all user prompts submitted during the development of the FinBuddy application.

---

## Prompt 1
* **Local Timestamp:** 2026-06-10T16:13:36+05:30
* **Goal:** Create `master_prd.md`
* **Content:**
```text
create a master_prd.md file inside @directory:agent_rules with this content - 
# FinBuddy – Technical Product Requirements Document (PRD)

## 1. Problem Statement

### Background

Many personal finance applications rely on bank integrations and third-party data aggregation, creating privacy concerns and reducing user control over financial information.

Users who prefer full ownership of their financial data need a simple, mobile-first solution that allows them to manually track finances, manage budgets, monitor spending behavior, and generate reports without connecting bank accounts.

### Problem

... [Full PRD Content]
```

---

## Prompt 2
* **Local Timestamp:** 2026-06-10T16:16:03+05:30
* **Goal:** Create `master_kpi.md`
* **Content:**
```text
create a master_kpi.md file inside @[agent_rules] , consider this @[agent_rules/master_prd.md] to create this kpi document.
The document should follow this format strcitly , do not add anyhintg on own.
## 📌 Table of Contents

- [Module 1: Authentication & Session Management](#module-1-authentication--session-management)
... [Kanban Template Content]
```

---

## Prompt 3
* **Local Timestamp:** 2026-06-10T16:19:54+05:30
* **Goal:** Create `android_persona.md`
* **Content:**
```text
create an android_persona.md file inside @[agent_rules] , the file should have 5+ years of android development experience in android kotlin jetpak compose mvi architexture. consider @[agent_rules/master_prd.md] @[agent_rules/master_kpi.md] , the doucment should not be more than 60 lines
```

---

## Prompt 4
* **Local Timestamp:** 2026-06-10T16:21:51+05:30
* **Goal:** Create `project_scope.md`
* **Content:**
```text
create a project_scope.md files inside @[agent_rules] , consider @[agent_rules/master_prd.md] @[agent_rules/master_kpi.md] , this document should have tech stack, and overall scope of the project.
```

---

## Prompt 5
* **Local Timestamp:** 2026-06-10T16:24:54+05:30
* **Goal:** Create `prompt_log.md`
* **Content:**
```text
create a prompt_log.md file inside @[agent_rules] , the document should have all the prompts i have written till now , and add all prompts from now which i write
```

---

## Prompt 6
* **Local Timestamp:** 2026-06-10T17:00:58+05:30
* **Goal:** Create `project_boundaries.md`
* **Content:**
```text
use @[agent_rules/android_persona.md] @[agent_rules/master_prd.md] @[agent_rules/master_kpi.md] @[agent_rules/save_token.md] and create project_boundaries.md file inside @[agent_rules] , keep it precise and concise, do not add similar content to project scope
```

---

## Prompt 7
* **Local Timestamp:** 2026-06-11T12:27:11+05:30
* **Goal:** Create `test_report.md` in `agent_rules`
* **Content:**
```text
create a test_report.md file inside @[agent_rules] , this file should containt all the test results with its status till now, and also add from now for all the test cases.
```

---

## Prompt 8
* **Local Timestamp:** 2026-06-11T12:30:56+05:30
* **Goal:** Create implementation plan for Module 2
* **Content:**
```text
use @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\android_persona.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_prd.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_kpi.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_scope.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_boundaries.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\api_doc.md]  and create implementation plan for module 2

For ui design strictly follow this - 
@mcp:StitchMCP:get_project:4639296201462069600

Do not add any screen on own strictly, do not add any own idea.
```

---

## Prompt 9
* **Local Timestamp:** 2026-06-11T13:00:06+05:30
* **Goal:** Create implementation plan for Module 3
* **Content:**
```text
use @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\android_persona.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_prd.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_kpi.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_scope.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_boundaries.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\api_doc.md]  and create implementation plan for module 3

For ui design strictly follow this - 
@mcp:StitchMCP:get_project:4639296201462069600

Do not add any screen on own strictly, do not add any own idea.
```

---

## Prompt 10
* **Local Timestamp:** 2026-06-11T14:56:07+05:30
* **Goal:** Create implementation plan for Module 4
* **Content:**
```text
use @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\android_persona.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_prd.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_kpi.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_scope.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_boundaries.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\api_doc.md]  and create implementation plan for module 4

For ui design strictly follow this - 
@mcp:StitchMCP:get_project:4639296201462069600

Do not add any screen on own strictly, do not add any own idea.
```

---

## Prompt 11
* **Local Timestamp:** 2026-06-11T15:07:26+05:30
* **Goal:** Create implementation plan for Module 5
* **Content:**
```text
use @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\android_persona.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_prd.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\master_kpi.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_scope.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\project_boundaries.md] @[c:\Users\user\Josus\finalPOC\finBuddyApp\agent_rules\api_doc.md]  and create implementation plan for module 5

For ui design strictly follow this - 
@mcp:StitchMCP:get_project:4639296201462069600

Do not add any screen on own strictly, do not add any own idea.
```



