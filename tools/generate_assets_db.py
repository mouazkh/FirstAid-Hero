# سكربت توليد قاعدة البيانات الجاهزة التي تُشحن داخل التطبيق في مجلد assets.
# التشغيل: python tools/generate_assets_db.py
# بعد أي تعديل على الحالات أو الخطوات يجب تعديل SEED_CASES و SEED_STEPS في DatabaseHelper بالشكل نفسه.
import os  # التعامل مع المسارات والملفات
import sqlite3  # مكتبة SQLite المدمجة في Python لإنشاء ملف قاعدة البيانات

# مجلد المشروع، ومسار ملف قاعدة البيانات الناتج داخل assets.
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "databases", "firstaid_hero.db")

# أوامر إنشاء الجداول والفهارس وجدول البحث النصي FTS4 (مطابقة لـ onCreate في DatabaseHelper).
SCHEMA = [
    """CREATE TABLE emergencies (
        case_id INTEGER PRIMARY KEY,
        title_en TEXT NOT NULL,
        title_ar TEXT NOT NULL,
        danger_level TEXT NOT NULL CHECK(danger_level IN ('critical','moderate','minor')),
        symptoms_en TEXT NOT NULL,
        symptoms_ar TEXT NOT NULL)""",
    """CREATE TABLE steps (
        step_id INTEGER PRIMARY KEY AUTOINCREMENT,
        case_id INTEGER NOT NULL,
        step_number INTEGER NOT NULL,
        instruction_en TEXT NOT NULL,
        instruction_ar TEXT NOT NULL,
        FOREIGN KEY(case_id) REFERENCES emergencies(case_id) ON DELETE CASCADE,
        UNIQUE(case_id, step_number))""",
    """CREATE TABLE custom_contacts (
        contact_id INTEGER PRIMARY KEY AUTOINCREMENT,
        contact_name TEXT NOT NULL,
        phone_number TEXT NOT NULL,
        preset INTEGER NOT NULL DEFAULT 0)""",
    """CREATE TABLE treated_cases (
        record_id INTEGER PRIMARY KEY AUTOINCREMENT,
        description TEXT NOT NULL,
        event_date TEXT NOT NULL,
        photo_path TEXT,
        procedures TEXT NOT NULL,
        created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
        case_id INTEGER REFERENCES emergencies(case_id) ON DELETE SET NULL)""",
    "CREATE INDEX idx_emergency_severity ON emergencies(danger_level)",
    "CREATE INDEX idx_treated_date ON treated_cases(event_date)",
    "CREATE INDEX idx_treated_case ON treated_cases(case_id)",
    "CREATE VIRTUAL TABLE emergencies_fts4 USING fts4(title_en, title_ar, symptoms_en, symptoms_ar, tokenize=unicode61)",
]

# الحالات الإسعافية: (الرقم، العنوان بالإنجليزية، العنوان بالعربية، الخطورة، الأعراض بالإنجليزية، الأعراض بالعربية، [الخطوات]).
CASES = [
    (1, "Severe bleeding", "نزيف شديد", "critical", "Heavy bleeding or a deep wound", "نزيف غزير أو جرح عميق", [
        ("Press firmly on the wound with a clean cloth or dressing.", "اضغط بقوة على الجرح بقطعة قماش أو ضماد نظيف."),
        ("Keep pressure on the wound and raise the injured part if possible.", "استمر بالضغط على الجرح وارفع الجزء المصاب إن أمكن."),
        ("Call the local emergency number if the bleeding is heavy or does not stop.", "اتصل برقم الطوارئ المحلي إذا كان النزيف غزيراً أو لا يتوقف."),
    ]),
    (2, "Burns", "حروق", "moderate", "Pain, redness, blistering, or skin damage", "ألم أو احمرار أو بثور أو تلف في الجلد", [
        ("Move away from the heat source and cool the burn under cool running water for 10 to 20 minutes.", "ابتعد عن مصدر الحرارة وبرّد الحرق تحت ماء جارٍ بارد لمدة 10 إلى 20 دقيقة."),
        ("Remove tight clothing or jewelry near the burn before swelling starts.", "أزل الملابس الضيقة أو الحلي قرب مكان الحرق قبل بدء التورم."),
        ("Cover the burn loosely with a clean, non-stick dressing and seek medical help for large or deep burns.", "غطِّ الحرق بشكل فضفاض بضماد نظيف غير لاصق واطلب المساعدة الطبية للحروق الكبيرة أو العميقة."),
    ]),
    (3, "Choking", "اختناق", "critical", "Difficulty breathing, speaking, or coughing", "صعوبة في التنفس أو الكلام أو السعال", [
        ("If the person cannot cough, speak, or breathe, call the local emergency number now.", "إذا كان الشخص لا يستطيع السعال أو الكلام أو التنفس فاتصل برقم الطوارئ المحلي فوراً."),
        ("Give up to five firm blows between the shoulder blades with the heel of your hand.", "وجّه حتى خمس ضربات قوية بين لوحي الكتف براحة يدك."),
        ("If the blockage continues, perform abdominal thrusts only if you are trained to do so.", "إذا استمر الانسداد فنفّذ الضغطات البطنية فقط إذا كنت مدرّباً عليها."),
    ]),
    (4, "Possible heart attack", "اشتباه نوبة قلبية", "critical", "Chest discomfort, breathlessness, sweating, or nausea", "انزعاج في الصدر أو ضيق نفس أو تعرق أو غثيان", [
        ("Call the local emergency number immediately and say you suspect a heart attack.", "اتصل برقم الطوارئ المحلي فوراً وقل إنك تشتبه بحدوث نوبة قلبية."),
        ("Help the person sit down in a comfortable position and loosen any tight clothing.", "ساعد الشخص على الجلوس بوضعية مريحة وفك أي ملابس ضيقة."),
        ("Stay with the person and follow the dispatcher's instructions until help arrives.", "ابقَ مع الشخص واتبع تعليمات موظف الطوارئ حتى وصول المساعدة."),
    ]),
    (5, "Drowning / water emergency", "غرق أو طارئ مائي", "critical", "A person in or recently removed from water who may be unresponsive", "شخص في الماء أو أُخرج منه وقد يكون فاقداً للاستجابة", [
        ("Get the person out of the water only if you can do so without putting yourself in danger.", "أخرج الشخص من الماء فقط إذا كان بإمكانك ذلك دون تعريض نفسك للخطر."),
        ("Check responsiveness and breathing; start CPR if the person is unresponsive and not breathing normally and you are trained.", "تحقق من استجابة الشخص وتنفسه، وابدأ الإنعاش القلبي الرئوي إذا لم يستجب ولم يتنفس بشكل طبيعي وكنت مدرّباً."),
        ("Call the local emergency number immediately.", "اتصل برقم الطوارئ المحلي فوراً."),
    ]),
    (6, "Snake or insect bite", "لدغات الأفاعي والحشرات", "moderate", "A bite or sting with pain, swelling, or other concerning symptoms", "لدغة أو لسعة مع ألم أو تورم أو أعراض مقلقة", [
        ("Keep the person calm and as still as possible and immobilize the bitten limb.", "حافظ على هدوء المصاب وثباته قدر الإمكان وثبّت الطرف الملدوغ."),
        ("Do not cut the wound or try to suck out the venom.", "لا تجرح مكان اللدغة ولا تحاول مص السم."),
        ("Remove tight clothing or jewelry near the bite and call the local emergency number.", "أزل الملابس أو الحلي الضيقة قرب مكان اللدغة واتصل برقم الطوارئ المحلي."),
    ]),
    (7, "Fractures", "كسور", "moderate", "Pain, swelling, deformity, or inability to move the injured part", "ألم أو تورم أو تشوه أو عدم القدرة على تحريك الجزء المصاب", [
        ("Keep the injured part still and support it in the position you found it; do not try to straighten it.", "أبقِ الجزء المصاب ثابتاً وادعمه بالوضعية التي وجدته عليها، ولا تحاول تقويمه."),
        ("Cover any open wound with a clean dressing and apply a wrapped cold pack to reduce swelling.", "غطِّ أي جرح مفتوح بضماد نظيف وضع كمادة باردة ملفوفة بقماش لتخفيف التورم."),
        ("Call the local emergency number for an open fracture or a suspected neck, back, hip, or thigh fracture; otherwise seek medical care.", "اتصل برقم الطوارئ المحلي عند وجود كسر مفتوح أو اشتباه بكسر في الرقبة أو الظهر أو الحوض أو الفخذ، وإلا فاطلب الرعاية الطبية."),
    ]),
    (8, "Minor cuts and scrapes", "جروح وخدوش بسيطة", "minor", "A small cut or graze with light bleeding", "جرح صغير أو خدش مع نزيف خفيف", [
        ("Wash your hands, then rinse the wound with clean running water.", "اغسل يديك ثم اغسل الجرح بماء جارٍ نظيف."),
        ("Press gently with a clean cloth or gauze until the bleeding stops.", "اضغط برفق بقطعة قماش أو شاش نظيف حتى يتوقف النزيف."),
        ("Cover the wound with a sterile dressing or plaster and seek medical care if signs of infection appear.", "غطِّ الجرح بضماد معقّم أو لاصقة طبية، واطلب الرعاية الطبية إذا ظهرت علامات التهاب."),
    ]),
]

# أرقام الطوارئ العامة الافتراضية (الاسم مفتاح ترجمة داخل التطبيق).
PRESET_CONTACTS = [
    ("police", "112"),
    ("ambulance", "110"),
    ("fire", "113"),
]


def main():
    # تنشئ ملف القاعدة من جديد: الجداول، الحالات، فهرس البحث، الخطوات المرقمة، الأرقام العامة،
    # ثم تضبط رقم الإصدار user_version ليطابق DB_VERSION في التطبيق.
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    if os.path.exists(OUT):
        os.remove(OUT)
    con = sqlite3.connect(OUT)
    cur = con.cursor()
    for stmt in SCHEMA:
        cur.execute(stmt)
    for case_id, title_en, title_ar, level, symptoms_en, symptoms_ar, steps in CASES:
        cur.execute("INSERT INTO emergencies VALUES (?,?,?,?,?,?)", (case_id, title_en, title_ar, level, symptoms_en, symptoms_ar))
        cur.execute("INSERT INTO emergencies_fts4(docid,title_en,title_ar,symptoms_en,symptoms_ar) VALUES (?,?,?,?,?)", (case_id, title_en, title_ar, symptoms_en, symptoms_ar))
        for number, (step_en, step_ar) in enumerate(steps, start=1):
            cur.execute("INSERT INTO steps(case_id,step_number,instruction_en,instruction_ar) VALUES (?,?,?,?)", (case_id, number, step_en, step_ar))
    for name, phone in PRESET_CONTACTS:
        cur.execute("INSERT INTO custom_contacts(contact_name,phone_number,preset) VALUES (?,?,1)", (name, phone))
    cur.execute("PRAGMA user_version = 6")
    con.commit()
    con.close()
    print("Wrote", OUT)


# نقطة التشغيل عند تنفيذ السكربت مباشرة.
if __name__ == "__main__":
    main()
