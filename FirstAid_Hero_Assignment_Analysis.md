# FirstAid Hero — Assignment Analysis

## 1. Executive summary

The assignment asks for **FirstAid Hero**, an Android application that provides rapid first-aid guidance without an internet connection. The app must use **Java and a local SQLite database**, including preloaded emergency cases and ordered first-aid instructions. It also needs emergency contacts, a record of treated incidents, search, severity statistics, and a detailed reflective report with both a developer guide and a user/investor guide.

The most important quality goals are fast retrieval under pressure, reliable offline operation, clear and accessible instructions, correct data relationships, and a stable app that handles invalid input without crashing.

## 2. Assignment facts and constraints

| Topic | Requirement stated in the brief |
|---|---|
| App | FirstAid Hero — an offline emergency first-aid guide |
| Platform / tools | Android Studio, Java, local SQLite; no cloud dependency |
| Starter data | Pre-populated emergency cases and their treatment steps |
| Search | Fast, dynamic search; the brief explicitly mentions SQL `LIKE` and `MATCH` |
| Required data | Emergencies, ordered steps, user-managed emergency contacts, and treated-case records |
| Screens | Dashboard/search, first-aid action steps, emergency contacts/dialer, treated-case entry, and severity statistics/history |
| Report | Reflective report containing a Developer’s Guide and a User/Investor Guide |
| Group size | The cover says 3–5 students; the project description later says 5 students. Confirm the actual team size with the instructor if this affects submission. |
| Due date shown | 5 November 2026 |
| Marking | Code quality 30; CRUD and interfaces 20; developer report 25; user/investor report 15; error handling/stability 10 (100 total) |
| Submission naming example | `BAIT_IWB303_HW_F25_C?_Name_ID`; replace placeholders with the required section, student name, and ID. |

## 3. Functional requirements, organized by feature

### 3.1 Dashboard and emergency search

- Present a simple home screen with a prominent search field and common emergencies for quick access.
- Return relevant cases as the user types; a partial word such as `لدغ` should find the snake/insect bite case.
- Search must work offline and should remain responsive with the app’s local data.
- Selecting a result opens the case’s symptoms and ordered instructions.

### 3.2 Emergency guidance

- Show the emergency title, symptoms, and severity.
- Display instructions in a clearly numbered, ordered list with large, legible text.
- Avoid burying critical actions in long paragraphs. The brief’s snakebite example expects concise ordered steps.
- Present a clear route to the emergency-contact screen or call action.

### 3.3 Emergency contacts

- Store general and user-added emergency contacts locally.
- Allow adding contacts and, to satisfy the CRUD rubric well, editing and deleting them as well.
- Provide a call action for a stored number. A practical implementation is to open the system dialer with the number prefilled; if the team chooses to place calls directly, it must account for Android’s call permission requirements and explain the choice.

### 3.4 Treated-case records

- Let the user enter a description, date, a photo, and the procedures/actions performed.
- Support reviewing records by date and searching for a specific record.
- Validate required fields and handle absent or inaccessible photos gracefully.
- The brief does not explicitly spell out record-editing/deletion behavior, but implementing suitable CRUD operations would strengthen the feature and the rubric alignment.

### 3.5 Statistics and history

- Show counts grouped by severity (at least critical and moderate; include minor if the data uses that level).
- Show treated cases by date and allow searching/filtering for a particular record.
- Keep statistics derived from the local database rather than hard-coded values.

## 4. Suggested local data model

The brief names four logical entities. The following is a proposed schema interpretation, not a schema supplied by the assignment. Use consistent names and document any changes in the final report.

### `emergencies`

| Column | Purpose |
|---|---|
| `case_id` | Primary key |
| `title` | Case title, e.g. burns or choking |
| `danger_level` | Severity category (critical, moderate, minor) |
| `symptoms` | Associated symptoms |
| `search_keywords` | Optional aliases/synonyms to improve offline search |

### `steps`

| Column | Purpose |
|---|---|
| `step_id` | Primary key |
| `case_id` | Foreign key to `emergencies.case_id` |
| `step_number` | Position within the case |
| `instruction_text` | One concise first-aid instruction |

Relationship: **one emergency to many steps**. Sort steps by `step_number` whenever displayed.

### `custom_contacts`

| Column | Purpose |
|---|---|
| `contact_id` | Primary key |
| `contact_name` | Display name / service name |
| `phone_number` | Number to dial |

### `treated_cases`

| Column | Purpose |
|---|---|
| `record_id` | Recommended primary key |
| `description` | Description of the incident |
| `event_date` | Date of the incident |
| `photo_uri` | Optional locally accessible photo reference |
| `procedures` | Actions performed |

The brief calls these last fields “Description, Date, photo, procedures” but does not give the table a formal name or specify a case foreign key. A separate `treated_cases` table is a straightforward interpretation. If records should be associated with a known emergency, add a nullable `case_id` foreign key and explain that design in the ERD.

### Data integrity and offline behavior

- Enable SQLite foreign-key enforcement and add appropriate constraints/indexes.
- Bundle the pre-populated database in `app/src/main/assets/` and copy it to the app’s database location on first launch, or use another clearly documented pre-population method.
- Keep all essential guidance and contact/record data available without network access. The app should not depend on cloud APIs for core behavior.
- If photos are selected through Android’s photo picker or storage APIs, ensure the chosen access strategy works on the supported Android versions and handle missing permissions/URIs without crashing.

## 5. Search and query considerations

A simple initial search can use parameterized SQLite queries with `LIKE`, for example:

```sql
SELECT case_id, title, danger_level, symptoms
FROM emergencies
WHERE title LIKE ? OR symptoms LIKE ? OR search_keywords LIKE ?
ORDER BY CASE danger_level
    WHEN 'critical' THEN 0
    WHEN 'moderate' THEN 1
    ELSE 2
END, title;
```

Bind a pattern such as `%query%` for each placeholder; do not concatenate raw user input into SQL. A `MATCH` query generally requires a full-text-search table (for example SQLite FTS); treat FTS as an optional optimization/design choice and describe its indexing and synchronization approach if implemented. The brief mentions both `LIKE` and `MATCH`, but it does not define which query must be used for which screen. A sensible staged approach is to implement and test `LIKE` first, then add FTS if it is appropriate for the dataset and course requirements.

For severity summaries, a grouped query can provide the counts:

```sql
SELECT danger_level, COUNT(*) AS case_count
FROM emergencies
GROUP BY danger_level;
```

For treated-case history, query by date in a stable sort order and add an index if the dataset warrants it.

## 6. Screen and navigation plan

1. **Dashboard / Search** — prominent search, common cases, fast result selection.
2. **Emergency Details / Action Steps** — title, severity, symptoms, numbered instructions, and access to emergency contacts.
3. **Emergency Contacts** — general and custom contacts, add/edit/delete actions, and dial action.
4. **Treated Case Form** — description, date, photo, and procedures, with validation and save/cancel behavior.
5. **Treated Case History** — date-ordered records, search/filter, and record details.
6. **Statistics** — local counts by severity and, if useful, treated-case summaries.

These may be implemented as activities or fragments. Whichever navigation structure is chosen should be simple to reach quickly and should preserve a clear back path.

## 7. Reflective report checklist

### Part A — Developer’s Guide

- Explain the ERD, each table, primary/foreign keys, and why the relationships were chosen.
- Describe the project architecture and actual classes used (database helper/repository, adapters, activities/fragments, and any view models if used). Do not claim classes or patterns that are not present in the submitted code.
- Document how the bundled database is copied/initialized on first launch, how schema versions are handled, and how offline behavior was verified.
- Include the actual search, statistics, and history SQL queries used.
- Reflect on real development problems, evidence of their causes, the fixes, and what the team learned.
- Include real app screenshots where the assignment requests them; label each screenshot and explain the user flow it demonstrates.

### Part B — User / Investor Guide

- Provide concise run/setup instructions and a user manual for searching, opening guidance, managing contacts, and recording a treated case.
- Justify visual and interaction choices for readability and use under stress. The brief gives red/gray as an example palette and large text as an example, not as a required fixed theme.
- Explain the practical value of a locally usable safety reference and the absence of required server operating costs, without making unsupported claims that the app replaces professional care or guarantees outcomes.
- Include screenshots that reflect the real implementation, not mockups presented as completed features.

### Report-format note

The instructions say the report should be sober and free of decoration and specifically say not to use colors or backgrounds. This appears to refer to the submitted report’s presentation/cover; it should not be confused with the app’s own UI. Keep the report formal and restrained, while making the app interface legible and high-contrast as appropriate.

## 8. Rubric-driven implementation priorities

| Priority | What to implement / prove | Rubric connection |
|---|---|---|
| 1 | Reliable pre-populated SQLite database, schema integrity, ordered steps, fast search | Code quality (30) |
| 2 | Working search UI, contact CRUD, coherent emergency screens, treated-case workflow | CRUD and interfaces (20) |
| 3 | Accurate ERD, architecture explanation, real SQL, honest reflection on issues and fixes | Developer report (25) |
| 4 | Usable manual, real screenshots, reasoned UX and value proposition | User/investor report (15) |
| 5 | Input validation, empty/error states, offline tests, no-crash verification | Stability/error handling (10) |

## 9. Current project status — 12 October 2026

Every requirement in sections 1–5 of the brief is implemented. The requirement-by-requirement mapping is in [`docs/CHECKLIST_AR.md`](docs/CHECKLIST_AR.md).

| Area | Status |
|---|---|
| Pre-populated database | `assets/databases/firstaid_hero.db` (schema v6) generated by `tools/generate_assets_db.py`, copied on first launch, with an in-code fallback seed and non-destructive migrations v1→v6 |
| Content | 8 bilingual cases × 3 ordered steps, covering critical, moderate and minor; includes burns, fractures, drowning and heart attack as named in the brief |
| Search | FTS4 `MATCH` (prefix per word, `unicode61` tokenizer) combined with escaped `LIKE`, updated on every keystroke; "لدغ" finds "لدغات الأفاعي والحشرات" |
| Screens | Dashboard/search, action steps with a fixed quick-call button, emergency dialer with add/edit/delete and direct calling, treated-case entry form with photo, records history, statistics, settings |
| Treated cases | Description, date, photo (copied into app storage), procedures, optional link to the emergency type; full CRUD and search |
| Statistics | `GROUP BY` / `COUNT` / `ORDER BY` by severity for guide cases and treated cases, plus treated cases per month |
| Validation | Forms stay open on errors with messages next to the field; LIKE escaping; FTS fallback; missing photo and missing dialer handled |
| Code documentation | Arabic comment on every class, method and imported library |
| Documentation | [`docs/FULL_PROJECT_GUIDE_AR.md`](docs/FULL_PROJECT_GUIDE_AR.md) (all-in-one Arabic guide), [`docs/CODE_AND_METHODS_AR.md`](docs/CODE_AND_METHODS_AR.md), [`docs/SQLITE_IMPLEMENTATION.md`](docs/SQLITE_IMPLEMENTATION.md), [`docs/APP_STRUCTURE.md`](docs/APP_STRUCTURE.md), [`docs/PROBLEMS_ENCOUNTERED.md`](docs/PROBLEMS_ENCOUNTERED.md) |
| Report | Arabic Word report in [`report/`](report/) with code images, ERD and screenshot slots |

## 10. Remaining before submission

1. Build and run the latest code in Android Studio, then capture the screenshots for the User Guide.
2. Fill in the cover page (names, IDs, section) and rename the report to `BAIT_IWB303_HW_F25_C?_Name_ID`.
3. Confirm the local emergency numbers (112 / 110 / 113) and the team members.

## 11. Medical-content and project-scope caution

The assignment is an educational software project. First-aid instructions and emergency numbers should be checked against reputable, locally applicable medical/emergency guidance before inclusion. The app presents itself as a quick reference, not as a substitute for professional emergency services or medical judgment.
