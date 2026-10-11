"""Builds the pre-populated SQLite database bundled in app/src/main/assets/databases/.

Run with:  python tools/generate_assets_db.py
"""
import os
import sqlite3

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "databases", "firstaid_hero.db")

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
        photo_uri TEXT,
        procedures TEXT NOT NULL,
        created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')))""",
    "CREATE INDEX idx_emergency_severity ON emergencies(danger_level)",
    "CREATE INDEX idx_treated_date ON treated_cases(event_date)",
    "CREATE VIRTUAL TABLE emergencies_fts4 USING fts4(title_en, title_ar, symptoms_en, symptoms_ar, tokenize=unicode61)",
]

CASES = [
    (1, "Severe bleeding", "ظ†ط²ظٹظپ ط´ط¯ظٹط¯", "critical", "Heavy bleeding or a deep wound", "ظ†ط²ظٹظپ ط؛ط²ظٹط± ط£ظˆ ط¬ط±ط­ ط¹ظ…ظٹظ‚", [
        ("Press firmly on the wound with a clean cloth or dressing.", "ط§ط¶ط؛ط· ط¨ظ‚ظˆط© ط¹ظ„ظ‰ ط§ظ„ط¬ط±ط­ ط¨ظ‚ط·ط¹ط© ظ‚ظ…ط§ط´ ط£ظˆ ط¶ظ…ط§ط¯ ظ†ط¸ظٹظپ."),
        ("Keep pressure on the wound and raise the injured part if possible.", "ط§ط³طھظ…ط± ط¨ط§ظ„ط¶ط؛ط· ط¹ظ„ظ‰ ط§ظ„ط¬ط±ط­ ظˆط§ط±ظپط¹ ط§ظ„ط¬ط²ط، ط§ظ„ظ…طµط§ط¨ ط¥ظ† ط£ظ…ظƒظ†."),
        ("Call the local emergency number if the bleeding is heavy or does not stop.", "ط§طھطµظ„ ط¨ط±ظ‚ظ… ط§ظ„ط·ظˆط§ط±ط¦ ط§ظ„ظ…ط­ظ„ظٹ ط¥ط°ط§ ظƒط§ظ† ط§ظ„ظ†ط²ظٹظپ ط؛ط²ظٹط±ط§ظ‹ ط£ظˆ ظ„ط§ ظٹطھظˆظ‚ظپ."),
    ]),
    (2, "Burns", "ط­ط±ظˆظ‚", "moderate", "Pain, redness, blistering, or skin damage", "ط£ظ„ظ… ط£ظˆ ط§ط­ظ…ط±ط§ط± ط£ظˆ ط¨ط«ظˆط± ط£ظˆ طھظ„ظپ ظپظٹ ط§ظ„ط¬ظ„ط¯", [
        ("Move away from the heat source and cool the burn under cool running water for 10 to 20 minutes.", "ط§ط¨طھط¹ط¯ ط¹ظ† ظ…طµط¯ط± ط§ظ„ط­ط±ط§ط±ط© ظˆط¨ط±ظ‘ط¯ ط§ظ„ط­ط±ظ‚ طھط­طھ ظ…ط§ط، ط¬ط§ط±ظچ ط¨ط§ط±ط¯ ظ„ظ…ط¯ط© 10 ط¥ظ„ظ‰ 20 ط¯ظ‚ظٹظ‚ط©."),
        ("Remove tight clothing or jewelry near the burn before swelling starts.", "ط£ط²ظ„ ط§ظ„ظ…ظ„ط§ط¨ط³ ط§ظ„ط¶ظٹظ‚ط© ط£ظˆ ط§ظ„ط­ظ„ظٹ ظ‚ط±ط¨ ظ…ظƒط§ظ† ط§ظ„ط­ط±ظ‚ ظ‚ط¨ظ„ ط¨ط¯ط، ط§ظ„طھظˆط±ظ…."),
        ("Cover the burn loosely with a clean, non-stick dressing and seek medical help for large or deep burns.", "ط؛ط·ظگظ‘ ط§ظ„ط­ط±ظ‚ ط¨ط´ظƒظ„ ظپط¶ظپط§ط¶ ط¨ط¶ظ…ط§ط¯ ظ†ط¸ظٹظپ ط؛ظٹط± ظ„ط§طµظ‚ ظˆط§ط·ظ„ط¨ ط§ظ„ظ…ط³ط§ط¹ط¯ط© ط§ظ„ط·ط¨ظٹط© ظ„ظ„ط­ط±ظˆظ‚ ط§ظ„ظƒط¨ظٹط±ط© ط£ظˆ ط§ظ„ط¹ظ…ظٹظ‚ط©."),
    ]),
    (3, "Choking", "ط§ط®طھظ†ط§ظ‚", "critical", "Difficulty breathing, speaking, or coughing", "طµط¹ظˆط¨ط© ظپظٹ ط§ظ„طھظ†ظپط³ ط£ظˆ ط§ظ„ظƒظ„ط§ظ… ط£ظˆ ط§ظ„ط³ط¹ط§ظ„", [
        ("If the person cannot cough, speak, or breathe, call the local emergency number now.", "ط¥ط°ط§ ظƒط§ظ† ط§ظ„ط´ط®طµ ظ„ط§ ظٹط³طھط·ظٹط¹ ط§ظ„ط³ط¹ط§ظ„ ط£ظˆ ط§ظ„ظƒظ„ط§ظ… ط£ظˆ ط§ظ„طھظ†ظپط³ ظپط§طھطµظ„ ط¨ط±ظ‚ظ… ط§ظ„ط·ظˆط§ط±ط¦ ط§ظ„ظ…ط­ظ„ظٹ ظپظˆط±ط§ظ‹."),
        ("Give up to five firm blows between the shoulder blades with the heel of your hand.", "ظˆط¬ظ‘ظ‡ ط­طھظ‰ ط®ظ…ط³ ط¶ط±ط¨ط§طھ ظ‚ظˆظٹط© ط¨ظٹظ† ظ„ظˆط­ظٹ ط§ظ„ظƒطھظپ ط¨ط±ط§ط­ط© ظٹط¯ظƒ."),
        ("If the blockage continues, perform abdominal thrusts only if you are trained to do so.", "ط¥ط°ط§ ط§ط³طھظ…ط± ط§ظ„ط§ظ†ط³ط¯ط§ط¯ ظپظ†ظپظ‘ط° ط§ظ„ط¶ط؛ط·ط§طھ ط§ظ„ط¨ط·ظ†ظٹط© ظپظ‚ط· ط¥ط°ط§ ظƒظ†طھ ظ…ط¯ط±ظ‘ط¨ط§ظ‹ ط¹ظ„ظٹظ‡ط§."),
    ]),
    (4, "Possible heart attack", "ط§ط´طھط¨ط§ظ‡ ظ†ظˆط¨ط© ظ‚ظ„ط¨ظٹط©", "critical", "Chest discomfort, breathlessness, sweating, or nausea", "ط§ظ†ط²ط¹ط§ط¬ ظپظٹ ط§ظ„طµط¯ط± ط£ظˆ ط¶ظٹظ‚ ظ†ظپط³ ط£ظˆ طھط¹ط±ظ‚ ط£ظˆ ط؛ط«ظٹط§ظ†", [
        ("Call the local emergency number immediately and say you suspect a heart attack.", "ط§طھطµظ„ ط¨ط±ظ‚ظ… ط§ظ„ط·ظˆط§ط±ط¦ ط§ظ„ظ…ط­ظ„ظٹ ظپظˆط±ط§ظ‹ ظˆظ‚ظ„ ط¥ظ†ظƒ طھط´طھط¨ظ‡ ط¨ط­ط¯ظˆط« ظ†ظˆط¨ط© ظ‚ظ„ط¨ظٹط©."),
        ("Help the person sit down in a comfortable position and loosen any tight clothing.", "ط³ط§ط¹ط¯ ط§ظ„ط´ط®طµ ط¹ظ„ظ‰ ط§ظ„ط¬ظ„ظˆط³ ط¨ظˆط¶ط¹ظٹط© ظ…ط±ظٹط­ط© ظˆظپظƒ ط£ظٹ ظ…ظ„ط§ط¨ط³ ط¶ظٹظ‚ط©."),
        ("Stay with the person and follow the dispatcher's instructions until help arrives.", "ط§ط¨ظ‚ظژ ظ…ط¹ ط§ظ„ط´ط®طµ ظˆط§طھط¨ط¹ طھط¹ظ„ظٹظ…ط§طھ ظ…ظˆط¸ظپ ط§ظ„ط·ظˆط§ط±ط¦ ط­طھظ‰ ظˆطµظˆظ„ ط§ظ„ظ…ط³ط§ط¹ط¯ط©."),
    ]),
    (5, "Drowning / water emergency", "ط؛ط±ظ‚ ط£ظˆ ط·ط§ط±ط¦ ظ…ط§ط¦ظٹ", "critical", "A person in or recently removed from water who may be unresponsive", "ط´ط®طµ ظپظٹ ط§ظ„ظ…ط§ط، ط£ظˆ ط£ظڈط®ط±ط¬ ظ…ظ†ظ‡ ظˆظ‚ط¯ ظٹظƒظˆظ† ظپط§ظ‚ط¯ط§ظ‹ ظ„ظ„ط§ط³طھط¬ط§ط¨ط©", [
        ("Get the person out of the water only if you can do so without putting yourself in danger.", "ط£ط®ط±ط¬ ط§ظ„ط´ط®طµ ظ…ظ† ط§ظ„ظ…ط§ط، ظپظ‚ط· ط¥ط°ط§ ظƒط§ظ† ط¨ط¥ظ…ظƒط§ظ†ظƒ ط°ظ„ظƒ ط¯ظˆظ† طھط¹ط±ظٹط¶ ظ†ظپط³ظƒ ظ„ظ„ط®ط·ط±."),
        ("Check responsiveness and breathing; start CPR if the person is unresponsive and not breathing normally and you are trained.", "طھط­ظ‚ظ‚ ظ…ظ† ط§ط³طھط¬ط§ط¨ط© ط§ظ„ط´ط®طµ ظˆطھظ†ظپط³ظ‡طŒ ظˆط§ط¨ط¯ط£ ط§ظ„ط¥ظ†ط¹ط§ط´ ط§ظ„ظ‚ظ„ط¨ظٹ ط§ظ„ط±ط¦ظˆظٹ ط¥ط°ط§ ظ„ظ… ظٹط³طھط¬ط¨ ظˆظ„ظ… ظٹطھظ†ظپط³ ط¨ط´ظƒظ„ ط·ط¨ظٹط¹ظٹ ظˆظƒظ†طھ ظ…ط¯ط±ظ‘ط¨ط§ظ‹."),
        ("Call the local emergency number immediately.", "ط§طھطµظ„ ط¨ط±ظ‚ظ… ط§ظ„ط·ظˆط§ط±ط¦ ط§ظ„ظ…ط­ظ„ظٹ ظپظˆط±ط§ظ‹."),
    ]),
    (6, "Snake or insect bite", "ظ„ط¯ط؛ط§طھ ط§ظ„ط£ظپط§ط¹ظٹ ط£ظˆ ط§ظ„ط­ط´ط±ط§طھ", "moderate", "A bite or sting with pain, swelling, or other concerning symptoms", "ظ„ط¯ط؛ط© ط£ظˆ ظ„ط³ط¹ط© ظ…ط¹ ط£ظ„ظ… ط£ظˆ طھظˆط±ظ… ط£ظˆ ط£ط¹ط±ط§ط¶ ظ…ظ‚ظ„ظ‚ط©", [
        ("Keep the person calm and as still as possible and immobilize the bitten limb.", "ط­ط§ظپط¸ ط¹ظ„ظ‰ ظ‡ط¯ظˆط، ط§ظ„ظ…طµط§ط¨ ظˆط«ط¨ط§طھظ‡ ظ‚ط¯ط± ط§ظ„ط¥ظ…ظƒط§ظ† ظˆط«ط¨ظ‘طھ ط§ظ„ط·ط±ظپ ط§ظ„ظ…ظ„ط¯ظˆط؛."),
        ("Remove tight clothing or jewelry near the bite before swelling starts.", "ط£ط²ظ„ ط§ظ„ظ…ظ„ط§ط¨ط³ ط£ظˆ ط§ظ„ط­ظ„ظٹ ط§ظ„ط¶ظٹظ‚ط© ظ‚ط±ط¨ ظ…ظƒط§ظ† ط§ظ„ظ„ط¯ط؛ط© ظ‚ط¨ظ„ ط¨ط¯ط، ط§ظ„طھظˆط±ظ…."),
        ("Do not cut the wound or try to suck out the venom; call the local emergency number.", "ظ„ط§ طھط¬ط±ط­ ظ…ظƒط§ظ† ط§ظ„ظ„ط¯ط؛ط© ظˆظ„ط§ طھط­ط§ظˆظ„ ط§ظ…طھطµط§طµ ط§ظ„ط³ظ…طŒ ظˆط§طھطµظ„ ط¨ط±ظ‚ظ… ط§ظ„ط·ظˆط§ط±ط¦ ط§ظ„ظ…ط­ظ„ظٹ."),
    ]),
]

PRESET_CONTACTS = [
    ("police", "112"),
    ("ambulance", "110"),
    ("fire", "113"),
]


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    if os.path.exists(OUT):
        os.remove(OUT)
    con = sqlite3.connect(OUT)
    cur = con.cursor()
    for stmt in SCHEMA:
        cur.execute(stmt)
    for case_id, title_en, title_ar, level, symptoms_en, symptoms_ar, steps in CASES:
        cur.execute("INSERT INTO emergencies VALUES (?,?,?,?,?,?)", (case_id, title_en, title_ar, level, symptoms_en, symptoms_ar))
        cur.execute("INSERT INTO emergencies_fts4(title_en,title_ar,symptoms_en,symptoms_ar) VALUES (?,?,?,?)", (title_en, title_ar, symptoms_en, symptoms_ar))
        for number, (step_en, step_ar) in enumerate(steps, start=1):
            cur.execute("INSERT INTO steps(case_id,step_number,instruction_en,instruction_ar) VALUES (?,?,?,?)", (case_id, number, step_en, step_ar))
    for name, phone in PRESET_CONTACTS:
        cur.execute("INSERT INTO custom_contacts(contact_name,phone_number,preset) VALUES (?,?,1)", (name, phone))
    cur.execute("PRAGMA user_version = 3")
    con.commit()
    con.close()
    print("Wrote", OUT)


if __name__ == "__main__":
    main()

