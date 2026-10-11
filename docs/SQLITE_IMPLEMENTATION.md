# SQLite Implementation — FirstAid Hero

**Implementation note — 10 October 2026**

This document describes the SQLite layer currently implemented in the Android Studio project. It is intended to be updated as the database design and first-aid content evolve.

## 1. Location and lifecycle

- Database class: `app/src/main/java/com/firstaidhero/app/data/DatabaseHelper.java`
- Database filename: `firstaid_hero.db`
- Helper: Android `SQLiteOpenHelper`
- Schema version: `1`
- Foreign-key enforcement is enabled in `onConfigure()`.
- The database is created on the device the first time an activity opens `DatabaseHelper`. `onCreate()` creates the tables, indexes, full-text index, and starter records in a transaction managed by SQLiteOpenHelper.
- All core reads/writes are local. The current manifest declares no internet permission.

## 2. Schema implemented

### `emergencies`

| Column | Type / rule | Purpose |
|---|---|---|
| `case_id` | `INTEGER PRIMARY KEY` | Stable case identifier |
| `title_en` | `TEXT NOT NULL` | English case title |
| `title_ar` | `TEXT NOT NULL` | Arabic case title |
| `danger_level` | `TEXT NOT NULL`, checked against `critical`, `moderate`, `minor` | Severity and statistics grouping |
| `symptoms_en` | `TEXT NOT NULL` | English summary of common signs |
| `symptoms_ar` | `TEXT NOT NULL` | Arabic summary of common signs |

An index, `idx_emergency_severity`, supports severity grouping/sorting.

### `steps`

| Column | Type / rule | Purpose |
|---|---|---|
| `step_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Step key |
| `case_id` | `INTEGER NOT NULL` | Foreign key to `emergencies.case_id`; cascade deletes |
| `step_number` | `INTEGER NOT NULL` | Display order |
| `instruction_en` | `TEXT NOT NULL` | English instruction |
| `instruction_ar` | `TEXT NOT NULL` | Arabic instruction |

`UNIQUE(case_id, step_number)` prevents duplicate step positions within one case. Detail screens sort by `step_number`.

### `custom_contacts`

| Column | Type / rule | Purpose |
|---|---|---|
| `contact_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Contact key |
| `contact_name` | `TEXT NOT NULL` | User-provided name |
| `phone_number` | `TEXT NOT NULL` | User-provided number |

### `treated_cases`

| Column | Type / rule | Purpose |
|---|---|---|
| `record_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Record key |
| `description` | `TEXT NOT NULL` | Incident description |
| `event_date` | `TEXT NOT NULL` | Date stored as `YYYY-MM-DD` for sorting |
| `photo_uri` | `TEXT` | Optional persisted content URI for a selected photo |
| `procedures` | `TEXT NOT NULL` | Actions recorded by the user |
| `created_at` | Integer timestamp | Creation time, with SQLite default |

`idx_treated_date` supports date-ordered case history.

### `emergencies_fts`

An SQLite **FTS4** virtual table indexes the English/Arabic titles and symptom summaries for `MATCH` searches. Current emergency rows are static starter data; if case editing is added, index maintenance/triggers must be added too.

## 3. Starter data and important medical-content limitation

Six bilingual emergency categories are inserted from `seed()` when the database is first created: severe bleeding, burns, choking, possible heart attack, drowning/water emergency, and snake/insect bite. The current steps are intentionally **general starter prompts** (prioritize scene safety, contact local emergency services for severe/life-threatening symptoms, and follow the dispatcher). They are **not condition-specific medical instructions**.

The app displays a notice that these starter instructions need local medical review and that professional help must not be delayed. Before real-world use or final academic submission, replace/expand these steps with verified, locally applicable guidance and record the authoritative sources and review date. Do not present the current generic content as a complete treatment protocol.

## 4. Prepopulation approach: current state versus assignment wording

The current implementation populates tables from Java in `SQLiteOpenHelper.onCreate()`. It does **not yet ship a prebuilt `.db` file in `assets/` and copy that file to the app’s database path**. The brief specifically describes a pre-populated database bundled in `assets`; if the instructor expects that exact technique, a later step should export a reviewed database, add it under `app/src/main/assets/`, and implement a one-time safe copy plus version/migration handling. This distinction is recorded rather than claiming the asset-copy requirement is already complete.

## 5. Search and reporting queries

### Dynamic `LIKE` search

`searchEmergencies(query)` checks English and Arabic titles and symptoms. It binds the same `%query%` pattern to placeholders rather than concatenating raw user input:

```sql
SELECT case_id, title_en, title_ar, danger_level, symptoms_en, symptoms_ar
FROM emergencies
WHERE title_en LIKE ? OR title_ar LIKE ?
   OR symptoms_en LIKE ? OR symptoms_ar LIKE ?
ORDER BY CASE danger_level
    WHEN 'critical' THEN 0
    WHEN 'moderate' THEN 1
    ELSE 2
END, title_en;
```

### Full-text `MATCH` search

`searchEmergenciesFullText(query)` queries `emergencies_fts` with FTS4 `MATCH` and joins results back to the emergency rows. The dashboard currently uses `LIKE`, which supports arbitrary substring matching (including a partial middle-of-word match). `MATCH` is available as a separate method for testing/extension; FTS prefix matching has different behavior and is not substituted silently for `LIKE`.

### Severity counts

```sql
SELECT danger_level, COUNT(*) AS case_count
FROM emergencies
GROUP BY danger_level
ORDER BY CASE danger_level
    WHEN 'critical' THEN 0
    WHEN 'moderate' THEN 1
    ELSE 2
END;
```

### Treated-case search/history

The record query filters description, procedures, or date using bound `LIKE` values, and sorts by `event_date DESC, record_id DESC`.

## 6. CRUD and interaction implemented

- **Contacts:** insert and update through `saveContact()`, list through `getContacts()`, delete through `deleteContact()`. The activity validates name/phone fields and opens Android’s dialer with `ACTION_DIAL`; it does not request permission to place calls directly.
- **Treated-case records:** insert description/date/procedures plus an optional photo URI; list/search and delete are implemented. The image is referenced by URI rather than copied into the app database as a BLOB.
- **Emergency catalog:** read/search and ordered steps are implemented; emergency cases are seeded and currently not user-editable.

## 7. Versioning, validation, and next database work

- `DB_VERSION` is `1`. `onUpgrade()` is currently a placeholder; schema changes must use explicit, preferably non-destructive migrations before increasing this value.
- The treated-case form checks that required text is present and that the date matches the `YYYY-MM-DD` shape. Calendar-validity checks and comprehensive automated database tests remain to be added.
- Test database creation on a clean install, app relaunch, search with empty/partial/Arabic text, ordered steps, contact edits/deletes, saved records, severity counts, photo URI persistence, and upgrade behavior.
- A build was attempted with Android Studio's installed Java 21 JBR. Gradle 9.4.1 downloaded successfully, but dependency resolution timed out while downloading Android Gradle Plugin 9.2.1 from Google Maven and the Kotlin Gradle plugin from Maven Central. No debug APK was produced, so compilation and runtime behavior are not yet verified.
