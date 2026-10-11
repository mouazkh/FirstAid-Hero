package com.firstaidhero.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * All app data stays in this on-device SQLite database.
 * The database ships pre-populated in {@code assets/databases/} and is copied
 * on first launch, so guidance works fully offline.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "firstaid_hero.db";
    private static final String ASSET_PATH = "databases/firstaid_hero.db";
    private static final int DB_VERSION = 3;

    private static final String[][] SEED_CASES = {
        {"1", "Severe bleeding", "نزيف شديد", "critical", "Heavy bleeding or a deep wound", "نزيف غزير أو جرح عميق"},
        {"2", "Burns", "حروق", "moderate", "Pain, redness, blistering, or skin damage", "ألم أو احمرار أو بثور أو تلف في الجلد"},
        {"3", "Choking", "اختناق", "critical", "Difficulty breathing, speaking, or coughing", "صعوبة في التنفس أو الكلام أو السعال"},
        {"4", "Possible heart attack", "اشتباه نوبة قلبية", "critical", "Chest discomfort, breathlessness, sweating, or nausea", "انزعاج في الصدر أو ضيق نفس أو تعرق أو غثيان"},
        {"5", "Drowning / water emergency", "غرق أو طارئ مائي", "critical", "A person in or recently removed from water who may be unresponsive", "شخص في الماء أو أُخرج منه وقد يكون فاقداً للاستجابة"},
        {"6", "Snake or insect bite", "لدغات الأفاعي أو الحشرات", "moderate", "A bite or sting with pain, swelling, or other concerning symptoms", "لدغة أو لسعة مع ألم أو تورم أو أعراض مقلقة"}
    };

    private static final String[][][] SEED_STEPS = {
        {
            {"Press firmly on the wound with a clean cloth or dressing.", "اضغط بقوة على الجرح بقطعة قماش أو ضماد نظيف."},
            {"Keep pressure on the wound and raise the injured part if possible.", "استمر بالضغط على الجرح وارفع الجزء المصاب إن أمكن."},
            {"Call the local emergency number if the bleeding is heavy or does not stop.", "اتصل برقم الطوارئ المحلي إذا كان النزيف غزيراً أو لا يتوقف."}
        },
        {
            {"Move away from the heat source and cool the burn under cool running water for 10 to 20 minutes.", "ابتعد عن مصدر الحرارة وبرّد الحرق تحت ماء جارٍ بارد لمدة 10 إلى 20 دقيقة."},
            {"Remove tight clothing or jewelry near the burn before swelling starts.", "أزل الملابس الضيقة أو الحلي قرب مكان الحرق قبل بدء التورم."},
            {"Cover the burn loosely with a clean, non-stick dressing and seek medical help for large or deep burns.", "غطِّ الحرق بشكل فضفاض بضماد نظيف غير لاصق واطلب المساعدة الطبية للحروق الكبيرة أو العميقة."}
        },
        {
            {"If the person cannot cough, speak, or breathe, call the local emergency number now.", "إذا كان الشخص لا يستطيع السعال أو الكلام أو التنفس فاتصل برقم الطوارئ المحلي فوراً."},
            {"Give up to five firm blows between the shoulder blades with the heel of your hand.", "وجّه حتى خمس ضربات قوية بين لوحي الكتف براحة يدك."},
            {"If the blockage continues, perform abdominal thrusts only if you are trained to do so.", "إذا استمر الانسداد فنفّذ الضغطات البطنية فقط إذا كنت مدرّباً عليها."}
        },
        {
            {"Call the local emergency number immediately and say you suspect a heart attack.", "اتصل برقم الطوارئ المحلي فوراً وقل إنك تشتبه بحدوث نوبة قلبية."},
            {"Help the person sit down in a comfortable position and loosen any tight clothing.", "ساعد الشخص على الجلوس بوضعية مريحة وفك أي ملابس ضيقة."},
            {"Stay with the person and follow the dispatcher's instructions until help arrives.", "ابقَ مع الشخص واتبع تعليمات موظف الطوارئ حتى وصول المساعدة."}
        },
        {
            {"Get the person out of the water only if you can do so without putting yourself in danger.", "أخرج الشخص من الماء فقط إذا كان بإمكانك ذلك دون تعريض نفسك للخطر."},
            {"Check responsiveness and breathing; start CPR if the person is unresponsive and not breathing normally and you are trained.", "تحقق من استجابة الشخص وتنفسه، وابدأ الإنعاش القلبي الرئوي إذا لم يستجب ولم يتنفس بشكل طبيعي وكنت مدرّباً."},
            {"Call the local emergency number immediately.", "اتصل برقم الطوارئ المحلي فوراً."}
        },
        {
            {"Keep the person calm and as still as possible and immobilize the bitten limb.", "حافظ على هدوء المصاب وثباته قدر الإمكان وثبّت الطرف الملدوغ."},
            {"Remove tight clothing or jewelry near the bite before swelling starts.", "أزل الملابس أو الحلي الضيقة قرب مكان اللدغة قبل بدء التورم."},
            {"Do not cut the wound or try to suck out the venom; call the local emergency number.", "لا تجرح مكان اللدغة ولا تحاول امتصاص السم، واتصل برقم الطوارئ المحلي."}
        }
    };

    private static final String[][] SEED_PRESET_CONTACTS = {
        {"police", "112"},
        {"ambulance", "110"},
        {"fire", "113"}
    };

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        copyDatabaseFromAssets(context);
    }

    /** Copies the pre-populated database from assets/ on first launch. */
    private void copyDatabaseFromAssets(Context context) {
        File target = context.getDatabasePath(DB_NAME);
        if (target.exists()) return;
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        try (InputStream in = context.getAssets().open(ASSET_PATH);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            out.flush();
        } catch (IOException e) {
            // Asset missing or unreadable: fall back to the in-code seed in onCreate().
            if (target.exists()) target.delete();
        }
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE emergencies (case_id INTEGER PRIMARY KEY, title_en TEXT NOT NULL, title_ar TEXT NOT NULL, danger_level TEXT NOT NULL CHECK(danger_level IN ('critical','moderate','minor')), symptoms_en TEXT NOT NULL, symptoms_ar TEXT NOT NULL)");
        db.execSQL("CREATE TABLE steps (step_id INTEGER PRIMARY KEY AUTOINCREMENT, case_id INTEGER NOT NULL, step_number INTEGER NOT NULL, instruction_en TEXT NOT NULL, instruction_ar TEXT NOT NULL, FOREIGN KEY(case_id) REFERENCES emergencies(case_id) ON DELETE CASCADE, UNIQUE(case_id, step_number))");
        db.execSQL("CREATE TABLE custom_contacts (contact_id INTEGER PRIMARY KEY AUTOINCREMENT, contact_name TEXT NOT NULL, phone_number TEXT NOT NULL, preset INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE treated_cases (record_id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT NOT NULL, event_date TEXT NOT NULL, photo_uri TEXT, procedures TEXT NOT NULL, created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')))");
        db.execSQL("CREATE INDEX idx_emergency_severity ON emergencies(danger_level)");
        db.execSQL("CREATE INDEX idx_treated_date ON treated_cases(event_date)");
        rebuildFts(db);
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE custom_contacts ADD COLUMN preset INTEGER NOT NULL DEFAULT 0"); } catch (SQLException ignored) { }
            seedPresetContacts(db);
        }
        if (oldVersion < 3) rebuildFts(db);
    }

    /** (Re)creates the FTS4 index over bilingual titles and symptoms. No-op-safe when the device SQLite lacks FTS4. */
    private void rebuildFts(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS emergencies_fts4");
            db.execSQL("CREATE VIRTUAL TABLE emergencies_fts4 USING fts4(title_en, title_ar, symptoms_en, symptoms_ar, tokenize=unicode61)");
            db.execSQL("INSERT INTO emergencies_fts4(docid, title_en, title_ar, symptoms_en, symptoms_ar) SELECT case_id, title_en, title_ar, symptoms_en, symptoms_ar FROM emergencies");
        } catch (SQLiteException e) {
            // FTS4 unavailable on this device: search() falls back to LIKE-only.
        }
    }

    private void seed(SQLiteDatabase db) {
        for (int i = 0; i < SEED_CASES.length; i++) {
            String[] c = SEED_CASES[i];
            ContentValues v = new ContentValues();
            v.put("case_id", Long.parseLong(c[0])); v.put("title_en", c[1]); v.put("title_ar", c[2]);
            v.put("danger_level", c[3]); v.put("symptoms_en", c[4]); v.put("symptoms_ar", c[5]);
            db.insertOrThrow("emergencies", null, v);
            try {
                ContentValues fts = new ContentValues();
                fts.put("title_en", c[1]); fts.put("title_ar", c[2]); fts.put("symptoms_en", c[4]); fts.put("symptoms_ar", c[5]);
                db.insert("emergencies_fts4", null, fts);
            } catch (SQLiteException e) {
                // Device SQLite without FTS4: search falls back to LIKE-only.
            }
            String[][] steps = SEED_STEPS[i];
            for (int s = 0; s < steps.length; s++) {
                ContentValues step = new ContentValues(); step.put("case_id", Long.parseLong(c[0]));
                step.put("step_number", s + 1); step.put("instruction_en", steps[s][0]); step.put("instruction_ar", steps[s][1]);
                db.insertOrThrow("steps", null, step);
            }
        }
        seedPresetContacts(db);
    }

    private void seedPresetContacts(SQLiteDatabase db) {
        for (String[] p : SEED_PRESET_CONTACTS) {
            ContentValues v = new ContentValues();
            v.put("contact_name", p[0]); v.put("phone_number", p[1]); v.put("preset", 1);
            db.insertOrThrow("custom_contacts", null, v);
        }
    }

    /** Searches with an FTS MATCH prefix query plus LIKE fallbacks for partial, offline-friendly results. */
    public Cursor searchEmergencies(String query) {
        SQLiteDatabase db = getReadableDatabase();
        String q = query == null ? "" : query.trim();
        String order = " ORDER BY CASE danger_level WHEN 'critical' THEN 0 WHEN 'moderate' THEN 1 ELSE 2 END, title_en";
        String base = "SELECT case_id,title_en,title_ar,danger_level,symptoms_en,symptoms_ar FROM emergencies";
        if (q.isEmpty()) return db.rawQuery(base + order, null);
        String pattern = likePattern(q);
        String match = buildMatchExpression(q);
        String likeClause = "title_en LIKE ? ESCAPE '\\' OR title_ar LIKE ? ESCAPE '\\' OR symptoms_en LIKE ? ESCAPE '\\' OR symptoms_ar LIKE ? ESCAPE '\\'";
        try {
            String sql = base + " WHERE " +
                    (match != null ? "case_id IN (SELECT docid FROM emergencies_fts4 WHERE emergencies_fts4 MATCH ?) OR " : "") +
                    likeClause + order;
            if (match != null) return db.rawQuery(sql, new String[]{match, pattern, pattern, pattern, pattern});
            return db.rawQuery(sql, new String[]{pattern, pattern, pattern, pattern});
        } catch (SQLiteException e) {
            // Device SQLite without FTS (or an un-migrated database): LIKE-only search.
            return db.rawQuery(base + " WHERE " + likeClause + order, new String[]{pattern, pattern, pattern, pattern});
        }
    }

    /** Builds an FTS prefix expression like "snake* bite*"; returns null when the query is not FTS-safe. */
    private String buildMatchExpression(String q) {
        if (!q.matches("[\\p{L}\\p{N}][\\p{L}\\p{N}\\s]*")) return null;
        String[] tokens = q.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(tokens[i]).append('*');
        }
        return sb.toString();
    }

    /** Wraps a query in LIKE wildcards, escaping %, _ and \ so they match literally. */
    private static String likePattern(String q) {
        return "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    public Cursor getEmergency(long id) {
        return getReadableDatabase().rawQuery("SELECT case_id,title_en,title_ar,danger_level,symptoms_en,symptoms_ar FROM emergencies WHERE case_id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getSteps(long caseId) {
        return getReadableDatabase().rawQuery("SELECT step_number,instruction_en,instruction_ar FROM steps WHERE case_id=? ORDER BY step_number", new String[]{String.valueOf(caseId)});
    }

    public long saveContact(long id, String name, String phone) {
        ContentValues v = new ContentValues(); v.put("contact_name", name.trim()); v.put("phone_number", phone.trim());
        SQLiteDatabase db = getWritableDatabase();
        if (id < 0) return db.insert("custom_contacts", null, v);
        return db.update("custom_contacts", v, "contact_id=? AND preset=0", new String[]{String.valueOf(id)}) > 0 ? id : -1;
    }

    public Cursor getContacts() { return getReadableDatabase().rawQuery("SELECT contact_id,contact_name,phone_number,preset FROM custom_contacts ORDER BY preset DESC, contact_name COLLATE NOCASE", null); }
    public int deleteContact(long id) { return getWritableDatabase().delete("custom_contacts", "contact_id=? AND preset=0", new String[]{String.valueOf(id)}); }

    public long addTreatedCase(String description, String date, String photoUri, String procedures) {
        ContentValues v = new ContentValues(); v.put("description", description.trim()); v.put("event_date", date.trim());
        v.put("photo_uri", photoUri); v.put("procedures", procedures.trim());
        return getWritableDatabase().insert("treated_cases", null, v);
    }

    public Cursor getTreatedCases(String query) {
        String q = query == null ? "" : query.trim();
        String sql = "SELECT record_id,description,event_date,photo_uri,procedures FROM treated_cases";
        if (!q.isEmpty()) sql += " WHERE description LIKE ? ESCAPE '\\' OR procedures LIKE ? ESCAPE '\\' OR event_date LIKE ? ESCAPE '\\'";
        sql += " ORDER BY event_date DESC, record_id DESC";
        if (q.isEmpty()) return getReadableDatabase().rawQuery(sql, null);
        String pattern = likePattern(q);
        return getReadableDatabase().rawQuery(sql, new String[]{pattern, pattern, pattern});
    }

    public int deleteTreatedCase(long id) { return getWritableDatabase().delete("treated_cases", "record_id=?", new String[]{String.valueOf(id)}); }
}
