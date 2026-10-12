# FirstAid Hero — Problems Encountered and How We Fixed Them

Engineering log of the issues hit while building the app, with the root cause and the fix for each.
Intended as raw material for the reflective report (Developer's Guide). The Arabic version of these problems is in the report and in [`FULL_PROJECT_GUIDE_AR.md`](FULL_PROJECT_GUIDE_AR.md).

---

## 1. Content overlapped the status bar and system navigation bar

**Problem.** The app targets SDK 36, where edge-to-edge rendering is enforced: the window draws behind the system status bar and navigation bar. Our screens ignored window insets, so page content was hidden under the status bar and the app's own bottom navigation sat behind the gesture/navigation bar area.

**Cause.** No inset handling at all: `setDecorFitsSystemWindows` was not called and no `WindowInsets` listener applied padding.

**Fix.** `BaseActivity` now calls `Ui.edgeToEdge()` (transparent bars + correct icon contrast via `WindowInsetsControllerCompat`). `Ui.screen()` wraps every page in a shell that applies the status-bar inset as top padding and the navigation-bar inset under the bottom bar; the onboarding screen uses `Ui.shell()` for the same. The theme sets transparent `statusBarColor`/`navigationBarColor` as fallback.

**Verified.** Emulator: page headers visible below the status bar; bottom navigation fully above the gesture bar in both light and dark themes.

---

## 2. Bottom navigation was stuck on the wrong tab

**Problem.** After Home → Contacts → Back (or switching tabs), the highlighted tab no longer matched the visible screen; tapping the current screen's tab appeared to do nothing.

**Cause.** We decompiled the Material 1.10.0 AAR and found that `NavigationBarView` **inverts** the `OnItemSelectedListener` return value. The internal menu callback returns `!listener.onNavigationItemSelected(item)`, and `NavigationBarMenuView.onClick` runs `item.setChecked(true)` when that callback returns false. Our listener always returned `true`, so every tap checked the tapped item **on the outgoing screen's bar**. Since tabs are activities reused with `FLAG_ACTIVITY_REORDER_TO_FRONT`, the wrong highlight survived and felt like the app was "stuck on Home/Contacts".

**Fix.** Return `true` only when the tapped item is the screen's own tab; return `false` when navigating away, so the outgoing bar never mutates its checked item.

**Verified.** Emulator: highlight stays correct through repeated Home ↔ Contacts ↔ Records round-trips, including back-press returns.

---

## 3. Search crashed on every keystroke: "no such module: fts5"

**Problem.** Typing in the search box closed the app immediately (an "Application Error"/"not responding" dialog on the device).

**Cause.** Crash buffer showed `android.database.sqlite.SQLiteException: no such module: fts5`. Stock Android SQLite ships FTS3/FTS4 but **not FTS5**; our FTS5 search index cannot even be queried on those builds.

**Fix.** Switched the index to FTS4 (`emergencies_fts4`, `tokenize=unicode61`) which exists on all stock Android builds. The search method additionally wraps the FTS query in a try/catch and degrades to a LIKE-only query if any device still lacks the module, so search can never crash from a missing SQLite module.

**Verified.** Emulator (which lacks fts5): typing works, and the query `sna ins` returns "Snake or insect bite" — a hit that only the FTS index can produce (LIKE cannot match non-adjacent words), proving the MATCH path is active.

---

## 4. Arabic search broke twice

### 4a. FTS4 default tokenizer did not prefix-match Arabic

**Problem.** After switching to FTS4, English prefix queries worked but Arabic prefix queries (e.g. `لدغ`) returned nothing from the MATCH path.

**Cause.** FTS4's default `simple` tokenizer does not treat non-Latin text properly for prefix matching.

**Fix.** Created the table with `tokenize=unicode61`, which tokenizes Arabic correctly.

**Verified.** Direct SQL: `MATCH 'لدغ*'` returns the snake/insect-bite case.

### 4b. Arabic content in the bundled database was corrupted (mojibake)

**Problem.** A speed-test run revealed Arabic search returning 0 rows, and the stored Arabic text was mojibake (`ظ†ط²ظٹظپ` instead of `نزيف`).

**Cause.** During a script edit we piped `tools/generate_assets_db.py` through a PowerShell `Get-Content -Raw | -replace | Set-Content` pipeline. PowerShell 5.1 read the UTF-8 file with the system ANSI codepage and rewrote it, corrupting every Arabic string. Every regeneration of `assets/databases/firstaid_hero.db` afterwards shipped corrupted Arabic. The Java fallback seed was unaffected (edited only with UTF-8-safe tools).

**Fix.** Rewrote the generator with the correct Arabic strings, regenerated the database, and bumped the schema to version 4 with a migration that re-seeds the static emergency content (DELETE + reinsert emergencies/steps + rebuild the FTS index) while leaving user data (contacts, treated cases) untouched.

**Verified.** `LIKE '%لدغ%'` and `MATCH 'لدغ*'` both return the snake-bite case in the rebuilt asset; keep-data upgrade (v3→v4) on the emulator repaired the on-device database, and a fresh install ships correct Arabic.

**Lesson.** Never round-trip non-ASCII source files through PowerShell text pipelines; edit them with UTF-8-safe tools only.

---

## 5. Rounded corners were almost square on high-density screens

**Problem.** The UI looked flat/off on a high-DPI device.

**Cause.** The shared `rounded()` helper accepted a `radiusDp` parameter but passed it straight to `setCornerRadius()` without a dp→px conversion, so a "17dp" corner was actually 17 raw pixels.

**Fix.** `rounded()` now takes the Context and converts both radius and stroke width properly; all call sites updated, and small badges/avatars became true circles.

---

## 6. LIKE wildcards leaked user input

**Problem.** Typing `%` or `_` in the search box matched everything (they are LIKE wildcards), even though values were bound as parameters.

**Cause.** Parameter binding protects against SQL injection but not against wildcard semantics.

**Fix.** `likePattern()` escapes `%`, `_`, and `\`, and both search queries use `ESCAPE '\'`.

---

## 7. Pre-populated database requirement (assignment) vs in-code seeding

**Problem.** The brief requires a pre-populated SQLite database shipped in `assets/`; the first implementation only seeded data in `SQLiteOpenHelper.onCreate()`.

**Fix.** The database is generated by `tools/generate_assets_db.py` into `assets/databases/firstaid_hero.db` and copied to the app's database directory on first launch. The in-code seed remains as a fallback if the asset is missing. Migration history: v1 (initial schema) → v2 (preset contacts column + emergency numbers) → v3 (FTS4 index) → v4 (repair of corrupted Arabic content) → v5 (photo stored as a private file, record linked to a case) → v6 (fractures and minor-cuts cases, snake-bite wording; content refreshed with an UPSERT that keeps user links). The schema matches the brief's four tables (see below).

---

## 8. Device-contacts access required a permission flow

**Problem.** Users should pick an emergency contact from the device instead of retyping it.

**Fix.** `READ_CONTACTS` runtime permission with a rationale dialog and an in-app picker list (name + number, sorted). If permission is denied, the app falls back to the system contacts picker (`ACTION_PICK`), which needs no permission — the picked URI grants temporary read access. Picking pre-fills the add form; the user reviews and saves.

---

## 9. Installed the wrong APK while debugging (AGP 9 output paths)

**Problem.** After fixing the search crash, reinstalling "the APK" still ran the old code.

**Cause.** With AGP 9, a stale APK exists under `app/build/intermediates/apk/debug/` while the real build output is `app/build/outputs/apk/debug/app-debug.apk`. We installed the stale one.

**Fix.** Always install from `app/build/outputs/apk/debug/`. Confirmed by checking the dex inside the APK before installing.

---

## 9b. Treated-case photos were saved but never shown, and could disappear

**Problem.** The brief asks for a photo stored with each treated case. The first version saved only the picked image's `content://` URI in `photo_uri`. The form gave no feedback after picking, the records list never displayed the photo, and the photo was lost if the user deleted it from the gallery (or, for cloud-backed pickers, unavailable offline). The add form was an `AlertDialog`, so a validation error or screen rotation closed it and discarded what the user typed.

**Fix.** Added `PhotoStore`: the picked image is decoded with `ImageDecoder` (EXIF rotation applied), downscaled to at most 1280 px, and saved as a JPEG under the app's private `files/photos/`. The database stores only the file name (`photo_path`). The entry form became a dedicated screen (`TreatedCaseFormActivity`) with a photo preview, inline validation that keeps the form open, a date picker, and state saved across rotation. The records list shows a thumbnail that opens the full photo, and deleting a record deletes its photo file. Schema v5 renames `photo_uri` to `photo_path` and adds an optional `case_id` link to `emergencies`.

**Why a file path and not a BLOB.** Android's `CursorWindow` limits a row to about 2 MB, and loading BLOBs slows every list query. Storing the file in app storage and its name in SQLite is the standard approach; the file still lives entirely on the device.

---

## 9c. Contact form lost the user's input on a validation error

**Problem.** The add/edit contact form was an `AlertDialog`. Its Save button always closes the dialog, so entering an invalid phone number showed an error and discarded everything typed.

**Fix.** The Save button's listener is attached after `show()` through `getButton(BUTTON_POSITIVE)`, so the dialog only closes after a successful save. Errors are shown next to the field (`setError`) with a specific message for an invalid phone number.

---

## 9d. Gaps found when re-checking the brief line by line

**Problem.** A second pass over the assignment PDF found: no fracture case although the brief names fractures; no case with the "minor" severity although the brief lists critical/moderate/minor; the snake-bite title and step order differed from the brief's example; the steps screen only linked to the contacts screen instead of offering a quick call button at the bottom; calls only opened the dialer although the brief asks for direct calling; the statistics queries grouped but did not sort.

**Fix.** Database v6 adds "Fractures" (moderate) and "Minor cuts and scrapes" (minor), renames the case to "لدغات الأفاعي والحشرات", and reorders its steps to match the example. Seeding now uses an UPSERT so existing cases are updated in place rather than deleted, which keeps the links from treated cases. The steps screen has a fixed quick-call button that lists the stored numbers. `BaseActivity.call()` places calls directly with `CALL_PHONE` and falls back to the dialer. Statistics queries add `ORDER BY` severity.

---

## 9e. Margins on the wrong side in Arabic

**Problem.** Screenshots in Arabic showed step numbers touching their text and record thumbnails touching their descriptions.

**Cause.** Horizontal gaps used `leftMargin` / `rightMargin`, which are physical sides; in right-to-left layouts the gap ends up on the wrong side.

**Fix.** All 13 places now use `setMarginStart` / `setMarginEnd`, which follow the layout direction.

---

## 10. Schema mapping to the assignment brief

| Brief requirement | Implemented as |
|---|---|
| Emergencies: `case_id` PK, `title`, `danger_level`, `symptoms` | `emergencies(case_id PK, title_en, title_ar, danger_level CHECK(critical/moderate/minor), symptoms_en, symptoms_ar)` — bilingual superset |
| Steps (one-to-many): `step_id` PK, `case_id` FK, `step_number` 1–3, `instruction_text` | `steps(step_id PK, case_id FK ON DELETE CASCADE, step_number, instruction_en, instruction_ar, UNIQUE(case_id, step_number))` |
| Custom contacts: `contact_id`, `contact_name`, `phone_number` | `custom_contacts` as required + `preset` flag so built-in emergency numbers cannot be edited/deleted |
| Treated-case record: Description, Date, photo, procedures | `treated_cases(record_id PK, description, event_date, photo_path, procedures, created_at, case_id FK → emergencies ON DELETE SET NULL)`; the photo is copied into app storage |
| Statistics: cases by severity (aggregate queries) | `GROUP BY danger_level` + `COUNT(*)` on `emergencies` and on `treated_cases LEFT JOIN emergencies`; treated cases per month via `GROUP BY substr(event_date,1,7)` |
| Fast search with LIKE and MATCH | `emergencies_fts4` (FTS4, unicode61) + combined MATCH/LIKE query |

---

## 11. Database speed test

Method: ran the exact production SQL against the bundled database (6 emergency cases, 18 steps at the time) with Python's sqlite3 (3.50.4), 200 warm-up runs then 20,000 timed runs per query, reporting the average per query. A re-run on the v6 database (8 cases) is in section 10 of `CODE_AND_METHODS_AR.md`; all queries stayed under 0.25 ms.

| Query | Mode | Avg time | Rows |
|---|---|---|---|
| "" (all cases) | full scan | 106 µs | 6 |
| "bur" | MATCH + LIKE | 174 µs | 1 |
| "bur" | LIKE-only | 143 µs | 1 |
| "sna ins" | MATCH + LIKE | 182 µs | 1 |
| "sna ins" | LIKE-only | 134 µs | 0 |
| Arabic prefix `لدغ` | MATCH + LIKE | 179 µs | 1 |
| Arabic prefix `لدغ` | LIKE-only | 142 µs | 1 |
| "heart-attack" | LIKE-only | 134 µs | 0 |
| "%" (escaped literal) | LIKE-only | 134 µs | 0 |

Conclusion: sub-millisecond on the current dataset — search is effectively instant and safe to run on every keystroke. Note that `"sna ins"` returns a result only through the MATCH index, confirming the FTS path adds real value over LIKE alone. On-device behavior was additionally verified on the emulator (typing, Arabic content, both migration paths).

---

## 12. Known deviations and notes

- The statistics screen was removed at one point and later restored as `StatisticsActivity` (fifth bottom-navigation tab) to meet section ج of the brief.
- Preset emergency numbers (Police 112, Ambulance 110, Fire 113) appear to match Syria's numbers but should be confirmed before submission.
- Direct calling works for user numbers; Android itself routes official emergency numbers (such as 112) to the dialer to avoid accidental calls.
- Lists are built directly from database cursors instead of RecyclerView adapters / ViewModels, because the data set is small (tens of rows). The brief lists these only as examples of classes to describe.
- Emergency instructions are conservative starter guidance pending local medical review (the app already shows a medical notice).
- Tab navigation is implemented with activities rather than fragments; back-stack order can be unusual (e.g., returning from details). Acceptable for this scope, worth documenting in the report.
