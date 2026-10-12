package com.firstaidhero.app.data;

import android.content.ContentValues;  // حاوية (اسم العمود ← القيمة) تُستخدم في الإدخال والتعديل على قاعدة البيانات
import android.content.Context;  // سياق التطبيق للوصول إلى الموارد والملفات وقواعد البيانات
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.database.SQLException;  // خطأ عام في تنفيذ أوامر SQL
import android.database.sqlite.SQLiteDatabase;  // كائن قاعدة البيانات لتنفيذ الاستعلامات والأوامر
import android.database.sqlite.SQLiteException;  // خطأ خاص بـ SQLite (مثل عدم دعم FTS4 على الجهاز)
import android.database.sqlite.SQLiteOpenHelper;  // الفئة الأب لإدارة إنشاء قاعدة البيانات وترقية إصداراتها
import java.io.File;  // التعامل مع الملفات والمجلدات
import java.io.FileOutputStream;  // الكتابة إلى ملف
import java.io.IOException;  // خطأ في القراءة أو الكتابة
import java.io.InputStream;  // القراءة من مصدر بيانات (ملف assets)
import java.io.OutputStream;  // الكتابة إلى مصدر بيانات

/**
 * مدير قاعدة البيانات المحلية SQLite (يرث SQLiteOpenHelper):
 * - ينسخ قاعدة البيانات الجاهزة من مجلد assets عند أول تشغيل.
 * - ينشئ الجداول ويرحّل الإصدارات القديمة دون حذف بيانات المستخدم.
 * - يحتوي كل استعلامات البحث (LIKE و MATCH) وعمليات CRUD والإحصاءات.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /** اسم ملف القاعدة على الجهاز، ومسارها داخل assets، ورقم إصدار المخطط الحالي. */
    private static final String DB_NAME = "firstaid_hero.db";
    private static final String ASSET_PATH = "databases/firstaid_hero.db";
    private static final int DB_VERSION = 6;

    /**
     * نسخة احتياطية من الحالات الإسعافية: الرقم، العنوان (إنجليزي/عربي)، الخطورة، الأعراض (إنجليزي/عربي).
     * تُستخدم إذا تعذّر نسخ ملف assets، وفي ترحيل الإصدارات.
     */
    private static final String[][] SEED_CASES = {
        {"1", "Severe bleeding", "نزيف شديد", "critical", "Heavy bleeding or a deep wound", "نزيف غزير أو جرح عميق"},
        {"2", "Burns", "حروق", "moderate", "Pain, redness, blistering, or skin damage", "ألم أو احمرار أو بثور أو تلف في الجلد"},
        {"3", "Choking", "اختناق", "critical", "Difficulty breathing, speaking, or coughing", "صعوبة في التنفس أو الكلام أو السعال"},
        {"4", "Possible heart attack", "اشتباه نوبة قلبية", "critical", "Chest discomfort, breathlessness, sweating, or nausea", "انزعاج في الصدر أو ضيق نفس أو تعرق أو غثيان"},
        {"5", "Drowning / water emergency", "غرق أو طارئ مائي", "critical", "A person in or recently removed from water who may be unresponsive", "شخص في الماء أو أُخرج منه وقد يكون فاقداً للاستجابة"},
        {"6", "Snake or insect bite", "لدغات الأفاعي والحشرات", "moderate", "A bite or sting with pain, swelling, or other concerning symptoms", "لدغة أو لسعة مع ألم أو تورم أو أعراض مقلقة"},
        {"7", "Fractures", "كسور", "moderate", "Pain, swelling, deformity, or inability to move the injured part", "ألم أو تورم أو تشوه أو عدم القدرة على تحريك الجزء المصاب"},
        {"8", "Minor cuts and scrapes", "جروح وخدوش بسيطة", "minor", "A small cut or graze with light bleeding", "جرح صغير أو خدش مع نزيف خفيف"}
    };

    /** خطوات الإسعاف لكل حالة بالترتيب نفسه لمصفوفة SEED_CASES؛ لكل خطوة نص إنجليزي وعربي. */
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
            {"Do not cut the wound or try to suck out the venom.", "لا تجرح مكان اللدغة ولا تحاول مص السم."},
            {"Remove tight clothing or jewelry near the bite and call the local emergency number.", "أزل الملابس أو الحلي الضيقة قرب مكان اللدغة واتصل برقم الطوارئ المحلي."}
        },
        {
            {"Keep the injured part still and support it in the position you found it; do not try to straighten it.", "أبقِ الجزء المصاب ثابتاً وادعمه بالوضعية التي وجدته عليها، ولا تحاول تقويمه."},
            {"Cover any open wound with a clean dressing and apply a wrapped cold pack to reduce swelling.", "غطِّ أي جرح مفتوح بضماد نظيف وضع كمادة باردة ملفوفة بقماش لتخفيف التورم."},
            {"Call the local emergency number for an open fracture or a suspected neck, back, hip, or thigh fracture; otherwise seek medical care.", "اتصل برقم الطوارئ المحلي عند وجود كسر مفتوح أو اشتباه بكسر في الرقبة أو الظهر أو الحوض أو الفخذ، وإلا فاطلب الرعاية الطبية."}
        },
        {
            {"Wash your hands, then rinse the wound with clean running water.", "اغسل يديك ثم اغسل الجرح بماء جارٍ نظيف."},
            {"Press gently with a clean cloth or gauze until the bleeding stops.", "اضغط برفق بقطعة قماش أو شاش نظيف حتى يتوقف النزيف."},
            {"Cover the wound with a sterile dressing or plaster and seek medical care if signs of infection appear.", "غطِّ الجرح بضماد معقّم أو لاصقة طبية، واطلب الرعاية الطبية إذا ظهرت علامات التهاب."}
        }
    };

    /** تعبير ترتيب حسب الخطورة: حرج ثم متوسط ثم طفيف ثم غير محدد. يُستخدم في البحث والإحصاءات. */
    private static final String SEVERITY_ORDER = "CASE danger_level WHEN 'critical' THEN 0 WHEN 'moderate' THEN 1 WHEN 'minor' THEN 2 ELSE 3 END";

    /** أرقام الطوارئ العامة الافتراضية (الاسم مفتاح ترجمة)، ولا يمكن تعديلها أو حذفها. */
    private static final String[][] SEED_PRESET_CONTACTS = {
        {"police", "112"},
        {"ambulance", "110"},
        {"fire", "113"}
    };

    /** المُنشئ: يحدد اسم القاعدة وإصدارها، ثم ينسخ القاعدة الجاهزة من assets إن لم تكن موجودة. */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        copyDatabaseFromAssets(context);
    }

    /**
     * تنسخ القاعدة الجاهزة من assets/databases إلى مسار قواعد بيانات التطبيق عند أول تشغيل فقط.
     * عند الفشل تحذف أي ملف ناقص، فيُنشئ onCreate القاعدة من البيانات الاحتياطية.
     */
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
            if (target.exists()) target.delete();
        }
    }

    /** تُستدعى عند كل فتح للقاعدة: تفعّل قيود المفاتيح الأجنبية (FOREIGN KEY). */
    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** تُستدعى فقط إذا لم توجد قاعدة: تنشئ الجداول الأربعة والفهارس، تملأ البيانات، ثم تبني فهرس البحث. */
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE emergencies (case_id INTEGER PRIMARY KEY, title_en TEXT NOT NULL, title_ar TEXT NOT NULL, danger_level TEXT NOT NULL CHECK(danger_level IN ('critical','moderate','minor')), symptoms_en TEXT NOT NULL, symptoms_ar TEXT NOT NULL)");
        db.execSQL("CREATE TABLE steps (step_id INTEGER PRIMARY KEY AUTOINCREMENT, case_id INTEGER NOT NULL, step_number INTEGER NOT NULL, instruction_en TEXT NOT NULL, instruction_ar TEXT NOT NULL, FOREIGN KEY(case_id) REFERENCES emergencies(case_id) ON DELETE CASCADE, UNIQUE(case_id, step_number))");
        db.execSQL("CREATE TABLE custom_contacts (contact_id INTEGER PRIMARY KEY AUTOINCREMENT, contact_name TEXT NOT NULL, phone_number TEXT NOT NULL, preset INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE treated_cases (record_id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT NOT NULL, event_date TEXT NOT NULL, photo_path TEXT, procedures TEXT NOT NULL, created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')), case_id INTEGER REFERENCES emergencies(case_id) ON DELETE SET NULL)");
        db.execSQL("CREATE INDEX idx_emergency_severity ON emergencies(danger_level)");
        db.execSQL("CREATE INDEX idx_treated_date ON treated_cases(event_date)");
        db.execSQL("CREATE INDEX idx_treated_case ON treated_cases(case_id)");
        seed(db);
        rebuildFts(db);
    }

    /**
     * ترحّل قاعدة إصدار قديم إلى الإصدار الحالي خطوة بخطوة دون حذف بيانات المستخدم:
     * v2 عمود preset، v5 حفظ الصورة كملف وربط السجل بنوع الحالة، v6 تحديث المحتوى وإعادة بناء فهرس البحث.
     */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE custom_contacts ADD COLUMN preset INTEGER NOT NULL DEFAULT 0"); } catch (SQLException ignored) { }
            seedPresetContacts(db);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE treated_cases RENAME COLUMN photo_uri TO photo_path");
            db.execSQL("ALTER TABLE treated_cases ADD COLUMN case_id INTEGER REFERENCES emergencies(case_id) ON DELETE SET NULL");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_treated_case ON treated_cases(case_id)");
        }
        if (oldVersion < 6) {
            seedEmergencyContent(db);
            rebuildFts(db);
        }
    }

    /**
     * تحذف وتعيد إنشاء جدول البحث النصي FTS4 وتملؤه من جدول الحالات.
     * إذا لم يدعم الجهاز FTS4 يُتجاهل الخطأ ويعمل البحث بـ LIKE فقط.
     */
    private void rebuildFts(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS emergencies_fts4");
            db.execSQL("CREATE VIRTUAL TABLE emergencies_fts4 USING fts4(title_en, title_ar, symptoms_en, symptoms_ar, tokenize=unicode61)");
            db.execSQL("INSERT INTO emergencies_fts4(docid, title_en, title_ar, symptoms_en, symptoms_ar) SELECT case_id, title_en, title_ar, symptoms_en, symptoms_ar FROM emergencies");
        } catch (SQLiteException e) {
        }
    }

    /** تملأ القاعدة الجديدة بالحالات وخطواتها وبأرقام الطوارئ العامة. */
    private void seed(SQLiteDatabase db) {
        seedEmergencyContent(db);
        seedPresetContacts(db);
    }

    /**
     * تُدخل الحالات بأمر UPSERT: الحالة الجديدة تُضاف، والموجودة يُحدَّث نصها دون حذفها فلا تنقطع روابط السجلات بها.
     * الخطوات تُستبدل حسب المفتاح الفريد (case_id, step_number).
     */
    private void seedEmergencyContent(SQLiteDatabase db) {
        for (int i = 0; i < SEED_CASES.length; i++) {
            String[] c = SEED_CASES[i];
            db.execSQL("INSERT INTO emergencies(case_id,title_en,title_ar,danger_level,symptoms_en,symptoms_ar) VALUES (?,?,?,?,?,?) "
                    + "ON CONFLICT(case_id) DO UPDATE SET title_en=excluded.title_en, title_ar=excluded.title_ar, danger_level=excluded.danger_level, symptoms_en=excluded.symptoms_en, symptoms_ar=excluded.symptoms_ar",
                    new Object[]{Long.parseLong(c[0]), c[1], c[2], c[3], c[4], c[5]});
            String[][] steps = SEED_STEPS[i];
            for (int s = 0; s < steps.length; s++) {
                ContentValues step = new ContentValues(); step.put("case_id", Long.parseLong(c[0]));
                step.put("step_number", s + 1); step.put("instruction_en", steps[s][0]); step.put("instruction_ar", steps[s][1]);
                db.insertWithOnConflict("steps", null, step, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /** تضيف أرقام الطوارئ العامة مع preset = 1. */
    private void seedPresetContacts(SQLiteDatabase db) {
        for (String[] p : SEED_PRESET_CONTACTS) {
            ContentValues v = new ContentValues();
            v.put("contact_name", p[0]); v.put("phone_number", p[1]); v.put("preset", 1);
            db.insertOrThrow("custom_contacts", null, v);
        }
    }

    /**
     * البحث الفوري: النص الفارغ يعيد كل الحالات، وإلا يُجمع MATCH (فهرس FTS4 لبدايات الكلمات)
     * مع LIKE (أي جزء من الكلمة) على العناوين والأعراض باللغتين، والنتائج مرتبة حسب الخطورة.
     * إذا فشل FTS على الجهاز يُعاد البحث بـ LIKE فقط حتى لا ينهار التطبيق.
     */
    public Cursor searchEmergencies(String query) {
        SQLiteDatabase db = getReadableDatabase();
        String q = query == null ? "" : query.trim();
        String order = " ORDER BY " + SEVERITY_ORDER + ", title_en";
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
            return db.rawQuery(base + " WHERE " + likeClause + order, new String[]{pattern, pattern, pattern, pattern});
        }
    }

    /**
     * تحوّل نص البحث إلى تعبير FTS ببادئة لكل كلمة (sna ins ← sna* ins*).
     * تعيد null إذا احتوى النص رموزاً غير آمنة لـ FTS.
     */
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

    /** تحيط النص بـ %...% وتهرّب المحارف % و _ و \ حتى تُعامل كنص عادي وليس كرموز بحث. */
    private static String likePattern(String q) {
        return "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    /** تجلب حالة واحدة برقمها: العنوان والخطورة والأعراض باللغتين. */
    public Cursor getEmergency(long id) {
        return getReadableDatabase().rawQuery("SELECT case_id,title_en,title_ar,danger_level,symptoms_en,symptoms_ar FROM emergencies WHERE case_id=?", new String[]{String.valueOf(id)});
    }

    /** تجلب خطوات الحالة مرتبة حسب رقم الخطوة. */
    public Cursor getSteps(long caseId) {
        return getReadableDatabase().rawQuery("SELECT step_number,instruction_en,instruction_ar FROM steps WHERE case_id=? ORDER BY step_number", new String[]{String.valueOf(caseId)});
    }

    /**
     * تضيف رقماً جديداً إذا كان id سالباً، وإلا تعدّله. الأرقام العامة محمية بالشرط preset = 0.
     * تعيد رقم السجل، أو -1 عند الفشل.
     */
    public long saveContact(long id, String name, String phone) {
        ContentValues v = new ContentValues(); v.put("contact_name", name.trim()); v.put("phone_number", phone.trim());
        SQLiteDatabase db = getWritableDatabase();
        if (id < 0) return db.insert("custom_contacts", null, v);
        return db.update("custom_contacts", v, "contact_id=? AND preset=0", new String[]{String.valueOf(id)}) > 0 ? id : -1;
    }

    /** تجلب كل الأرقام: العامة أولاً ثم أرقام المستخدم أبجدياً. */
    public Cursor getContacts() { return getReadableDatabase().rawQuery("SELECT contact_id,contact_name,phone_number,preset FROM custom_contacts ORDER BY preset DESC, contact_name COLLATE NOCASE", null); }
    /** تحذف رقماً أضافه المستخدم (الأرقام العامة لا تُحذف). */
    public int deleteContact(long id) { return getWritableDatabase().delete("custom_contacts", "contact_id=? AND preset=0", new String[]{String.valueOf(id)}); }

    /**
     * تضيف سجل حالة معالجة (id سالب) أو تعدّله.
     * caseId نوع الحالة الإسعافية وقد يكون null، و photoPath اسم ملف الصورة داخل التطبيق.
     */
    public long saveTreatedCase(long id, Long caseId, String description, String date, String photoPath, String procedures) {
        ContentValues v = new ContentValues(); v.put("description", description.trim()); v.put("event_date", date.trim());
        v.put("photo_path", photoPath); v.put("procedures", procedures.trim());
        if (caseId == null) v.putNull("case_id"); else v.put("case_id", caseId);
        SQLiteDatabase db = getWritableDatabase();
        if (id < 0) return db.insert("treated_cases", null, v);
        return db.update("treated_cases", v, "record_id=?", new String[]{String.valueOf(id)}) > 0 ? id : -1;
    }

    /** بداية استعلام السجل مع LEFT JOIN على جدول الحالات لجلب عنوان الحالة المرتبطة وخطورتها. */
    private static final String TREATED_COLUMNS = "SELECT t.record_id,t.description,t.event_date,t.photo_path,t.procedures,t.case_id,e.title_en,e.title_ar,e.danger_level FROM treated_cases t LEFT JOIN emergencies e ON e.case_id=t.case_id";

    /** تجلب سجل حالة معالجة واحداً مع بيانات الحالة المرتبطة. */
    public Cursor getTreatedCase(long id) {
        return getReadableDatabase().rawQuery(TREATED_COLUMNS + " WHERE t.record_id=?", new String[]{String.valueOf(id)});
    }

    /** تجلب السجلات من الأحدث للأقدم، مع بحث في الوصف والإجراءات والتاريخ وعنوان الحالة. */
    public Cursor getTreatedCases(String query) {
        String q = query == null ? "" : query.trim();
        String sql = TREATED_COLUMNS;
        if (!q.isEmpty()) sql += " WHERE t.description LIKE ? ESCAPE '\\' OR t.procedures LIKE ? ESCAPE '\\' OR t.event_date LIKE ? ESCAPE '\\' OR e.title_en LIKE ? ESCAPE '\\' OR e.title_ar LIKE ? ESCAPE '\\'";
        sql += " ORDER BY t.event_date DESC, t.record_id DESC";
        if (q.isEmpty()) return getReadableDatabase().rawQuery(sql, null);
        String pattern = likePattern(q);
        return getReadableDatabase().rawQuery(sql, new String[]{pattern, pattern, pattern, pattern, pattern});
    }

    /** تحذف سجل حالة معالجة. */
    public int deleteTreatedCase(long id) { return getWritableDatabase().delete("treated_cases", "record_id=?", new String[]{String.valueOf(id)}); }

    /** إحصاء: عدد حالات الدليل لكل مستوى خطورة (GROUP BY + COUNT + ORDER BY). */
    public Cursor countEmergenciesBySeverity() {
        return getReadableDatabase().rawQuery("SELECT danger_level, COUNT(*) FROM emergencies GROUP BY danger_level ORDER BY " + SEVERITY_ORDER, null);
    }

    /** إحصاء: عدد الحالات المعالجة لكل مستوى خطورة؛ السجل غير المرتبط بحالة يُحسب unspecified. */
    public Cursor countTreatedBySeverity() {
        return getReadableDatabase().rawQuery("SELECT COALESCE(e.danger_level,'unspecified') AS danger_level, COUNT(*) FROM treated_cases t LEFT JOIN emergencies e ON e.case_id=t.case_id GROUP BY 1 ORDER BY " + SEVERITY_ORDER, null);
    }

    /** إحصاء: عدد الحالات المعالجة في كل شهر (آخر ستة أشهر فيها سجلات). */
    public Cursor countTreatedByMonth() {
        return getReadableDatabase().rawQuery("SELECT substr(event_date,1,7) AS month, COUNT(*) FROM treated_cases GROUP BY month ORDER BY month DESC LIMIT 6", null);
    }
}
