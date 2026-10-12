package com.firstaidhero.app.ui.screens;

import android.content.Intent;  // طلب لفتح شاشة أخرى أو تطبيق آخر (الاتصال، منتقي جهات الاتصال)
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.view.Gravity;  // محاذاة العناصر والنصوص
import android.widget.Button;  // زر
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import androidx.appcompat.app.AlertDialog;  // نافذة حوار (تأكيد، نموذج، قائمة اختيار)
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.data.DatabaseHelper;  // قاعدة البيانات وكل الاستعلامات
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)
import java.util.ArrayList;  // قائمة ديناميكية
import java.util.List;  // واجهة القوائم

/**
 * شاشة خطوات الإسعاف (Action Steps): العنوان، الخطورة، الأعراض، الخطوات المرقمة بخط كبير،
 * وزر ثابت أسفل الشاشة للاتصال السريع بأرقام الطوارئ المخزنة محلياً.
 */
public class EmergencyDetailActivity extends BaseActivity {
    /** قاعدة البيانات. */
    private DatabaseHelper db;
    /** تفتح قاعدة البيانات وتبني الشاشة. */
    @Override protected void onCreate(Bundle state) { super.onCreate(state); db = new DatabaseHelper(this); render(); }
    /** تقرأ الحالة وخطواتها من القاعدة وتعرضها، وتضيف زر تسجيل الحالة كمعالجة، وزر الاتصال الثابت أسفل الشاشة. */
    private void render() {
        LinearLayout content = Ui.column(this); Button quickCall = null; long id = getIntent().getLongExtra("case_id", -1); Cursor c = db.getEmergency(id); boolean ar = AppPreferences.isArabic(this);
        if (c.moveToFirst()) {
            TextView back = Ui.text(this, (ar ? "‹  " : "‹  ") + I18n.t(this, "common"), 14, Ui.RED, true); content.addView(back); back.setOnClickListener(v -> finish());
            Ui.addLabel(this, content, c.getString(ar ? 2 : 1), 28, Ui.ink(this), true, 18);
            String level = c.getString(3); TextView severity = Ui.text(this, I18n.t(this, "severity") + "  ·  " + severity(level), 13, Ui.RED_DARK, true);
            severity.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8)); severity.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 14, 0));
            LinearLayout.LayoutParams slp = Ui.lp(this, -2, -2); slp.topMargin = Ui.dp(this, 12); content.addView(severity, slp);
            Ui.addLabel(this, content, I18n.t(this, "symptoms"), 17, Ui.ink(this), true, 24);
            TextView symptoms = Ui.card(this, c.getString(ar ? 5 : 4), 17, Ui.ink(this), false); LinearLayout.LayoutParams symp = Ui.lp(this, -1, -2); symp.topMargin = Ui.dp(this, 8); content.addView(symptoms, symp);
            Ui.addLabel(this, content, I18n.t(this, "steps"), 17, Ui.ink(this), true, 22);
            Cursor steps = db.getSteps(id); int n = 1;
            try { while (steps.moveToNext()) {
                LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.TOP); row.setPadding(Ui.dp(this, 14), Ui.dp(this, 13), Ui.dp(this, 14), Ui.dp(this, 13)); row.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 16, 1));
                TextView number = Ui.centered(this, String.valueOf(n), 17, Ui.WHITE, true); number.setBackground(Ui.rounded(this, Ui.RED, Ui.RED, 17, 0)); row.addView(number, Ui.lp(this, 34, 34));
                TextView instruction = Ui.text(this, steps.getString(ar ? 2 : 1), 19, Ui.ink(this), false); LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, -2, 1); ip.setMarginStart(Ui.dp(this, 12)); row.addView(instruction, ip);
                LinearLayout.LayoutParams rp = Ui.lp(this, -1, -2); rp.topMargin = Ui.dp(this, 8); content.addView(row, rp); n++;
            }} finally { steps.close(); }
            TextView notice = Ui.card(this, I18n.t(this, "medical_notice"), 13, Ui.muted(this), false); LinearLayout.LayoutParams np = Ui.lp(this, -1, -2); np.topMargin = Ui.dp(this, 18); content.addView(notice, np);
            Button log = Ui.button(this, I18n.t(this, "log_case"), false); LinearLayout.LayoutParams lgp = Ui.lp(this, -1, 50); lgp.topMargin = Ui.dp(this, 14); content.addView(log, lgp);
            log.setOnClickListener(v -> { Intent i = new Intent(this, TreatedCaseFormActivity.class); i.putExtra("case_id", id); startActivity(i); });
            quickCall = Ui.button(this, "☎   " + I18n.t(this, "call_emergency"), true); quickCall.setTextSize(16); quickCall.setOnClickListener(v -> chooseNumberToCall());
        } else Ui.addLabel(this, content, I18n.t(this, "no_results"), 16, Ui.muted(this), false, 0);
        c.close(); AppPreferences.applyDirection(this, content);
        LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        body.addView(Ui.scroll(this, content), new LinearLayout.LayoutParams(-1, 0, 1));
        if (quickCall != null) {
            LinearLayout bar = new LinearLayout(this); bar.setBackgroundColor(Ui.background(this)); bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));
            bar.addView(quickCall, Ui.lp(this, -1, 54)); body.addView(bar, Ui.lp(this, -1, -2));
        }
        setContentView(Ui.screen(this, body, "home"));
    }
    /**
     * تعرض قائمة الأرقام المخزنة محلياً (العامة والخاصة)؛ الضغط على رقم يتصل به مباشرة،
     * وزر إدارة الأرقام يفتح شاشة أرقام الطوارئ.
     */
    private void chooseNumberToCall() {
        List<String> labels = new ArrayList<>(), numbers = new ArrayList<>();
        Cursor c = db.getContacts();
        try { while (c.moveToNext()) { labels.add(I18n.t(this, c.getString(1)) + "  ·  " + c.getString(2)); numbers.add(c.getString(2)); } } finally { c.close(); }
        new AlertDialog.Builder(this).setTitle(I18n.t(this, "call_emergency"))
                .setItems(labels.toArray(new String[0]), (d, which) -> call(numbers.get(which)))
                .setNeutralButton(I18n.t(this, "manage_contacts"), (d, w) -> startActivity(new Intent(this, ContactsActivity.class)))
                .setNegativeButton(I18n.t(this, "cancel"), null).show();
    }
    /** ترجمة مستوى الخطورة إلى نص. */
    private String severity(String value) { if ("critical".equals(value)) return I18n.t(this,"severity_critical"); if ("moderate".equals(value)) return I18n.t(this,"severity_moderate"); return I18n.t(this,"severity_minor"); }
    /** إغلاق قاعدة البيانات عند إغلاق الشاشة. */
    @Override protected void onDestroy() { if (db != null) db.close(); super.onDestroy(); }
}
