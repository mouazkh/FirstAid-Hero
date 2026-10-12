# FirstAid Hero — App Structure

## Technology

- Android Studio project in **Java**, application ID `com.firstaidhero.app`.
- `minSdk 30` (Android 11), `targetSdk 36`, Java 11.
- Local **SQLite** only. No internet permission and no server.
- Libraries: AndroidX AppCompat, AndroidX Activity, Material Components (bottom navigation). ConstraintLayout is declared by the template but not used.
- Every class, method and imported library has an Arabic comment in the source.

## Java packages

```text
com.firstaidhero.app
├── core/
│   ├── AppPreferences.java       # SharedPreferences: onboarding, language, theme
│   └── I18n.java                 # English/Arabic UI strings
├── data/
│   ├── DatabaseHelper.java       # Schema, asset copy, migrations, FTS4 + LIKE search, CRUD, statistics
│   ├── DeviceContacts.java       # Reads device contacts for the picker
│   └── PhotoStore.java           # Copies treated-case photos into app storage, loads thumbnails
└── ui/
    ├── Ui.java                   # Shared design system and bottom navigation
    └── screens/
        ├── BaseActivity.java             # Theme, language refresh, direct calling
        ├── SplashActivity.java
        ├── OnboardingActivity.java
        ├── MainActivity.java             # Dashboard / search
        ├── EmergencyDetailActivity.java  # Action steps + quick-call bar
        ├── ContactsActivity.java         # Emergency dialer
        ├── TreatedCasesActivity.java     # Treated-case history
        ├── TreatedCaseFormActivity.java  # Treated-case entry / edit
        ├── StatisticsActivity.java       # Severity statistics
        └── SettingsActivity.java
```

Screens are built in Java through the shared `Ui` helpers rather than XML layouts. Lists are built directly from database cursors; the data set is small, so the app does not use RecyclerView adapters or ViewModels.

## Launch and first-run behavior

1. `SplashActivity` is the launcher. It shows the brand for 650 ms.
2. On first launch it opens `OnboardingActivity` (three pages, Skip / Next). Finishing or skipping saves `onboarding_done`.
3. Later launches go straight to `MainActivity`.
4. Language (`en`/`ar`) and theme (`system`/`light`/`dark`) are saved separately. The default theme follows the system.

## Screens and navigation

| Screen | What it does |
|---|---|
| **Dashboard** (`MainActivity`) | Large search bar; results update on every keystroke (FTS4 `MATCH` + `LIKE`); all cases listed by severity when the field is empty |
| **Action steps** (`EmergencyDetailActivity`) | Title, severity, symptoms, numbered steps in 19sp text, medical notice, "Log this as a treated case", and a fixed quick-call button at the bottom that lists the stored numbers and calls the chosen one |
| **Emergency dialer** (`ContactsActivity`) | Public numbers (police, ambulance, fire) plus user numbers with add / edit / delete / call; pick from device contacts. Presets cannot be edited or deleted |
| **Records** (`TreatedCasesActivity`) | Treated cases newest first with thumbnail, linked emergency and severity, search, edit, delete |
| **Treated-case form** (`TreatedCaseFormActivity`) | Emergency type, description, date picker, procedures, photo with preview; validation keeps the form open; state survives rotation |
| **Statistics** (`StatisticsActivity`) | Guide cases by severity, treated cases by severity, treated cases per month (`GROUP BY` / `COUNT` / `ORDER BY`) |
| **Settings** (`SettingsActivity`) | Language and appearance drop-downs |

A shared five-tab Material `BottomNavigationView` links **Home**, **Records**, **Stats**, **Contacts** and **Settings**. Tabs are activities reused with `FLAG_ACTIVITY_REORDER_TO_FRONT`.

## Calling

`BaseActivity.call(phone)` places a call directly with `ACTION_CALL` when `CALL_PHONE` is granted, asks for the permission otherwise, and falls back to the dialer (`ACTION_DIAL`) if it is refused. Android itself routes official emergency numbers such as 112 to the dialer.

## Photos

`PhotoStore` copies the picked image (system photo picker, no permission needed) into `files/photos/` as a JPEG of at most 1280 px. The database stores only the file name. Thumbnails are decoded at the size needed. Deleting a record or replacing its photo deletes the file.

## Launcher icon

| File | Role |
|---|---|
| `res/drawable-nodpi/ic_launcher_art.png` | The app icon artwork (1024 px) |
| `res/drawable/ic_launcher_foreground.xml` | Adaptive-icon foreground: the artwork inset by 15dp so the circle and cross stay inside the safe zone of every launcher mask |
| `res/drawable/ic_launcher_background.xml` | Red gradient behind it (#BA202C → #8B191F) |
| `res/drawable/ic_launcher_monochrome.xml` | White cross for Android 13+ themed icons |
| `res/mipmap-anydpi/ic_launcher.xml`, `ic_launcher_round.xml` | The adaptive icon definitions referenced by the manifest |
| `app/src/main/ic_launcher-playstore.png` | 512 px store icon |

minSdk 30 always uses the adaptive icon, so no per-density bitmap icons are needed.

## Permissions

| Permission | Use | If refused |
|---|---|---|
| `READ_CONTACTS` | Pick a number from the device contacts | System contacts picker (no permission needed) |
| `CALL_PHONE` | Direct calling | Dialer with the number filled in |

`android.hardware.telephony` is declared as not required, so tablets can install the app.

## Database

See [`SQLITE_IMPLEMENTATION.md`](SQLITE_IMPLEMENTATION.md). In short: schema version 6, bundled in `assets/databases/firstaid_hero.db` and copied on first launch, with an identical in-code seed as a fallback.

## UI direction

High-contrast emergency red, charcoal text and neutral surfaces; rounded cards; large touch targets. Light and dark palettes. Edge-to-edge layout that respects the status and navigation bars. Arabic text uses the system Arabic serif (Noto Naskh) with Arabic locale shaping and right-to-left layout.

## Validation status

- All Java sources compile with `javac` against the Android 36 SDK and the project's AndroidX libraries.
- The latest changes still need a Gradle build and a run on a device or emulator from Android Studio.
- Emergency instructions are general guidance pending local medical review.
