package com.firstaidhero.app.ui.screens;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.data.DatabaseHelper;
import com.firstaidhero.app.ui.Ui;

public class EmergencyDetailActivity extends BaseActivity {
    private DatabaseHelper db;
    @Override protected void onCreate(Bundle state) { super.onCreate(state); db = new DatabaseHelper(this); render(); }
    private void render() {
        LinearLayout content = Ui.column(this); long id = getIntent().getLongExtra("case_id", -1); Cursor c = db.getEmergency(id); boolean ar = AppPreferences.isArabic(this);
        if (c.moveToFirst()) {
            TextView back = Ui.text(this, (ar ? "‹  " : "‹  ") + I18n.t(this, "common"), 14, Ui.RED, true); content.addView(back); back.setOnClickListener(v -> finish());
            Ui.addLabel(this, content, c.getString(ar ? 2 : 1), 28, Ui.ink(this), true, 18);
            String level = c.getString(3); TextView severity = Ui.text(this, I18n.t(this, "severity") + "  ·  " + severity(level), 13, Ui.RED_DARK, true);
            severity.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8)); severity.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 14, 0));
            LinearLayout.LayoutParams slp = Ui.lp(this, -2, -2); slp.topMargin = Ui.dp(this, 12); content.addView(severity, slp);
            Ui.addLabel(this, content, I18n.t(this, "symptoms"), 17, Ui.ink(this), true, 24);
            TextView symptoms = Ui.card(this, c.getString(ar ? 5 : 4), 15, Ui.ink(this), false); LinearLayout.LayoutParams symp = Ui.lp(this, -1, -2); symp.topMargin = Ui.dp(this, 8); content.addView(symptoms, symp);
            Ui.addLabel(this, content, I18n.t(this, "steps"), 17, Ui.ink(this), true, 22);
            Cursor steps = db.getSteps(id); int n = 1;
            try { while (steps.moveToNext()) {
                LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.TOP); row.setPadding(Ui.dp(this, 14), Ui.dp(this, 13), Ui.dp(this, 14), Ui.dp(this, 13)); row.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 16, 1));
                TextView number = Ui.centered(this, String.valueOf(n), 14, Ui.WHITE, true); number.setBackground(Ui.rounded(this, Ui.RED, Ui.RED, 14, 0)); row.addView(number, Ui.lp(this, 28, 28));
                TextView instruction = Ui.text(this, steps.getString(ar ? 2 : 1), 15, Ui.ink(this), false); LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, -2, 1); ip.leftMargin = Ui.dp(this, 12); row.addView(instruction, ip);
                LinearLayout.LayoutParams rp = Ui.lp(this, -1, -2); rp.topMargin = Ui.dp(this, 8); content.addView(row, rp); n++;
            }} finally { steps.close(); }
            TextView notice = Ui.card(this, I18n.t(this, "medical_notice"), 13, Ui.muted(this), false); LinearLayout.LayoutParams np = Ui.lp(this, -1, -2); np.topMargin = Ui.dp(this, 18); content.addView(notice, np);
            Button contacts = Ui.button(this, I18n.t(this, "open_contacts"), true); LinearLayout.LayoutParams bp = Ui.lp(this, -1, 50); bp.topMargin = Ui.dp(this, 14); content.addView(contacts, bp); contacts.setOnClickListener(v -> startActivity(new Intent(this, ContactsActivity.class)));
        } else Ui.addLabel(this, content, I18n.t(this, "no_results"), 16, Ui.muted(this), false, 0);
        c.close(); AppPreferences.applyDirection(this, content); setContentView(Ui.screen(this, Ui.scroll(this, content), "home"));
    }
    private String severity(String value) { if ("critical".equals(value)) return I18n.t(this,"severity_critical"); if ("moderate".equals(value)) return I18n.t(this,"severity_moderate"); return I18n.t(this,"severity_minor"); }
    @Override protected void onDestroy() { if (db != null) db.close(); super.onDestroy(); }
}
