# SQLite Implementation — FirstAid Hero

This document describes the SQLite layer as it is implemented now (database **version 6**). The Arabic reference with every method is [`CODE_AND_METHODS_AR.md`](CODE_AND_METHODS_AR.md); the all-in-one Arabic guide is [`FULL_PROJECT_GUIDE_AR.md`](FULL_PROJECT_GUIDE_AR.md).

## 1. Location and lifecycle

| Item | Value |
|---|---|
| Database class | `app/src/main/java/com/firstaidhero/app/data/DatabaseHelper.java` (extends `SQLiteOpenHelper`) |
| Database file on the device | `firstaid_hero.db` in the app's private database directory |
| Bundled pre-populated database | `app/src/main/assets/databases/firstaid_hero.db` |
| Generator script | `tools/generate_assets_db.py` (run `python tools/generate_assets_db.py`) |
| Schema version | `6` (`DB_VERSION` in Java, `PRAGMA user_version` in the bundled file) |
| Foreign keys | Enabled in `onConfigure()` on every open |
| Network | None. The manifest declares no `INTERNET` permission |

**First launch.** The `DatabaseHelper` constructor calls `copyDatabaseFromAssets()`. If the database file does not exist yet, it copies the bundled file from `assets/databases/` to the app's database path in 8 KB chunks. `SQLiteOpenHelper` then reads `user_version` = 6 from the copied file, finds it equal to `DB_VERSION`, and calls neither `onCreate()` nor `onUpgrade()`. If the copy fails (asset missing or unreadable), any partial file is deleted and `onCreate()` builds the same database from the in-code seed (`SEED_CASES`, `SEED_STEPS`, `SEED_PRESET_CONTACTS`).

## 2. Schema

### `emergencies`

| Column | Type / rule | Purpose | Brief's name |
|---|---|---|---|
| `case_id` | `INTEGER PRIMARY KEY` | Case identifier | `case_id` |
| `title_en`, `title_ar` | `TEXT NOT NULL` | Case title in English and Arabic | `title` |
| `danger_level` | `TEXT NOT NULL CHECK(danger_level IN ('critical','moderate','minor'))` | Severity | `danger_level` |
| `symptoms_en`, `symptoms_ar` | `TEXT NOT NULL` | Associated symptoms | `symptoms` |

### `steps` (one emergency → many steps)

| Column | Type / rule | Purpose | Brief's name |
|---|---|---|---|
| `step_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Step key | `step_id` |
| `case_id` | `INTEGER NOT NULL`, `FOREIGN KEY → emergencies(case_id) ON DELETE CASCADE` | Owning case | `case_id` |
| `step_number` | `INTEGER NOT NULL` | Order 1, 2, 3 | `step_number` |
| `instruction_en`, `instruction_ar` | `TEXT NOT NULL` | Instruction text | `instruction_text` |
| — | `UNIQUE(case_id, step_number)` | No duplicate positions within a case | — |

### `custom_contacts`

| Column | Type / rule | Purpose |
|---|---|---|
| `contact_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Contact key |
| `contact_name` | `TEXT NOT NULL` | Name (for presets, a translation key such as `ambulance`) |
| `phone_number` | `TEXT NOT NULL` | Number to call |
| `preset` | `INTEGER NOT NULL DEFAULT 0` | `1` = built-in public number (police 112, ambulance 110, fire 113) that cannot be edited or deleted |

### `treated_cases`

| Column | Type / rule | Purpose |
|---|---|---|
| `record_id` | `INTEGER PRIMARY KEY AUTOINCREMENT` | Record key |
| `description` | `TEXT NOT NULL` | Incident description |
| `event_date` | `TEXT NOT NULL` | `YYYY-MM-DD`, so text order is date order |
| `photo_path` | `TEXT` | File name of the photo copied into `files/photos/` by `PhotoStore` |
| `procedures` | `TEXT NOT NULL` | Actions performed |
| `created_at` | `INTEGER NOT NULL DEFAULT (strftime('%s','now'))` | Creation timestamp |
| `case_id` | `INTEGER`, `REFERENCES emergencies(case_id) ON DELETE SET NULL` | Optional link to the emergency that was treated |

### `emergencies_fts4`

FTS4 virtual table with the `unicode61` tokenizer (which handles Arabic) over `title_en`, `title_ar`, `symptoms_en`, `symptoms_ar`. Its `docid` equals `case_id`. It is rebuilt from `emergencies` by `rebuildFts()`. FTS4 was chosen because stock Android SQLite does not include FTS5.

### Indexes

| Index | Purpose |
|---|---|
| `idx_emergency_severity` on `emergencies(danger_level)` | Severity grouping and sorting |
| `idx_treated_date` on `treated_cases(event_date)` | Date-ordered history |
| `idx_treated_case` on `treated_cases(case_id)` | Join with `emergencies` for the records list and statistics |

### Relationships

- `emergencies` 1 → N `steps` (required, cascade delete).
- `emergencies` 1 → N `treated_cases` (optional, set null on delete) so treated cases can be counted by severity.
- `custom_contacts` is independent.

## 3. Content

Eight bilingual cases, three ordered steps each (24 steps), covering all three severity levels:

| # | Case | Severity |
|---|---|---|
| 1 | Severe bleeding — نزيف شديد | critical |
| 2 | Burns — حروق | moderate |
| 3 | Choking — اختناق | critical |
| 4 | Possible heart attack — اشتباه نوبة قلبية | critical |
| 5 | Drowning / water emergency — غرق أو طارئ مائي | critical |
| 6 | Snake or insect bite — لدغات الأفاعي والحشرات | moderate |
| 7 | Fractures — كسور | moderate |
| 8 | Minor cuts and scrapes — جروح وخدوش بسيطة | minor |

The snake-bite steps follow the brief's example order (immobilize the limb, do not suck the venom, then call). The instructions are conservative general guidance; the app shows a medical notice, and the content should be reviewed against local guidance before real-world use.

## 4. Versions and migrations

`onUpgrade()` migrates older installs step by step without deleting user data:

| Version | Change | Migration step |
|---|---|---|
| 1 | Initial schema | — |
| 2 | `preset` column for built-in numbers | `ALTER TABLE custom_contacts ADD COLUMN preset ...` + insert presets |
| 3 | FTS4 index | Now rebuilt by the v6 step |
| 4 | Repair of corrupted Arabic text in the bundled file | Now repaired by the v6 UPSERT |
| 5 | Photo stored as a private file; record linked to a case | `RENAME COLUMN photo_uri TO photo_path`, `ADD COLUMN case_id ...`, create `idx_treated_case` |
| 6 | Fractures + minor cuts added; snake-bite title and steps updated | `seedEmergencyContent()` (UPSERT cases, REPLACE steps) then `rebuildFts()` |

`seedEmergencyContent()` uses `INSERT ... ON CONFLICT(case_id) DO UPDATE` so existing cases are updated in place instead of being deleted. Deleting them would set `treated_cases.case_id` to NULL and lose the links. Steps use `CONFLICT_REPLACE` on `UNIQUE(case_id, step_number)`.

## 5. Queries

### Instant search (`searchEmergencies`)

```sql
SELECT case_id, title_en, title_ar, danger_level, symptoms_en, symptoms_ar
FROM emergencies
WHERE case_id IN (SELECT docid FROM emergencies_fts4 WHERE emergencies_fts4 MATCH ?)
   OR title_en LIKE ? ESCAPE '\' OR title_ar LIKE ? ESCAPE '\'
   OR symptoms_en LIKE ? ESCAPE '\' OR symptoms_ar LIKE ? ESCAPE '\'
ORDER BY CASE danger_level WHEN 'critical' THEN 0 WHEN 'moderate' THEN 1 WHEN 'minor' THEN 2 ELSE 3 END, title_en;
```

- The MATCH argument is built by `buildMatchExpression()`: one prefix term per word (`sna ins` → `sna* ins*`). It is skipped when the input contains characters that are not safe in an FTS expression.
- The LIKE argument is built by `likePattern()`: `%query%` with `%`, `_` and `\` escaped.
- Empty input returns every case in severity order. If the FTS query throws (device without FTS4), the same search runs with LIKE only.
- All values are bound with `?` placeholders.

### Statistics

```sql
SELECT danger_level, COUNT(*) FROM emergencies
GROUP BY danger_level
ORDER BY CASE danger_level WHEN 'critical' THEN 0 WHEN 'moderate' THEN 1 WHEN 'minor' THEN 2 ELSE 3 END;

SELECT COALESCE(e.danger_level, 'unspecified') AS danger_level, COUNT(*)
FROM treated_cases t LEFT JOIN emergencies e ON e.case_id = t.case_id
GROUP BY 1
ORDER BY CASE danger_level WHEN 'critical' THEN 0 WHEN 'moderate' THEN 1 WHEN 'minor' THEN 2 ELSE 3 END;

SELECT substr(event_date, 1, 7) AS month, COUNT(*) FROM treated_cases
GROUP BY month ORDER BY month DESC LIMIT 6;
```

### Treated-case history and search

```sql
SELECT t.record_id, t.description, t.event_date, t.photo_path, t.procedures, t.case_id,
       e.title_en, e.title_ar, e.danger_level
FROM treated_cases t LEFT JOIN emergencies e ON e.case_id = t.case_id
WHERE t.description LIKE ? ESCAPE '\' OR t.procedures LIKE ? ESCAPE '\' OR t.event_date LIKE ? ESCAPE '\'
   OR e.title_en LIKE ? ESCAPE '\' OR e.title_ar LIKE ? ESCAPE '\'
ORDER BY t.event_date DESC, t.record_id DESC;
```

## 6. CRUD

| Data | Create / update | Read | Delete |
|---|---|---|---|
| Emergencies and steps | Shipped content (read-only in the app) | `searchEmergencies`, `getEmergency`, `getSteps` | — |
| Contacts | `saveContact(id, name, phone)` (insert when `id < 0`; update only where `preset = 0`) | `getContacts()` (presets first, then by name) | `deleteContact(id)` (only `preset = 0`) |
| Treated cases | `saveTreatedCase(id, caseId, description, date, photoPath, procedures)` | `getTreatedCase(id)`, `getTreatedCases(query)` | `deleteTreatedCase(id)` (the screen also deletes the photo file) |

## 7. Validation and integrity

- Database: `NOT NULL`, `CHECK` on severity, `UNIQUE(case_id, step_number)`, and enforced foreign keys.
- Contact form: name required; phone limited to digits, `+ ( ) -` and spaces with at least 3 digits; the dialog stays open on error.
- Treated-case form: description and procedures required; strict `YYYY-MM-DD` parsing with no future dates; length limits (200 / 2000 characters).
- Search: escaped LIKE wildcards, bound parameters, LIKE-only fallback.

## 8. Performance

Production queries on the v6 database (8 cases), 200 warm-up runs and 20,000 timed runs each, SQLite 3.53 on the development PC:

| Query | Mode | Average | Rows |
|---|---|---|---|
| Empty search (all cases) | Full scan | 130 µs | 8 |
| `bur` | MATCH + LIKE | 194 µs | 1 |
| `sna ins` | MATCH + LIKE | 205 µs | 1 |
| `لدغ` | MATCH + LIKE | 200 µs | 1 |
| `كسو` | MATCH + LIKE | 206 µs | 1 |
| Guide cases by severity | GROUP BY + COUNT | 116 µs | 3 |

Every query finishes in under 0.25 ms, so search runs on every keystroke without delay.

## 9. Changing the content

1. Edit `CASES` in `tools/generate_assets_db.py` and the matching `SEED_CASES` / `SEED_STEPS` in `DatabaseHelper`.
2. Run `python tools/generate_assets_db.py`.
3. Increase `DB_VERSION` and the script's `user_version` together, and add an `onUpgrade()` step (normally `seedEmergencyContent(db); rebuildFts(db);`).
