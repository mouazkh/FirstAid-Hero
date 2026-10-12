package com.firstaidhero.app.ui.screens;

import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.view.Gravity;  // محاذاة العناصر والنصوص
import android.view.View;  // الفئة الأساسية لكل عناصر الواجهة
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.data.DatabaseHelper;  // قاعدة البيانات وكل الاستعلامات
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)
import java.util.LinkedHashMap;  // خريطة (مفتاح ← قيمة) تحافظ على ترتيب الإدخال
import java.util.Map;  // واجهة الخرائط (مفتاح ← قيمة)

/**
 * شاشة الإحصاءات: تصنيف الحالات حسب الخطورة باستعلامات التجميع (GROUP BY + COUNT)،
 * وعدد الحالات المعالجة حسب الخطورة وحسب الشهر.
 */
public class StatisticsActivity extends BaseActivity {
    /** مستويات الخطورة الثلاثة بالترتيب. */
    private static final String[] LEVELS = {"critical", "moderate", "minor"};
    /** قاعدة البيانات وحاوية بطاقات الإحصاء. */
    private DatabaseHelper db; private LinearLayout sections;

    /** تفتح قاعدة البيانات وتبني الشاشة. */
    @Override protected void onCreate(Bundle state) { super.onCreate(state); db = new DatabaseHelper(this); render(); }
    /** تحديث الأرقام عند العودة إلى الشاشة (بعد إضافة سجل مثلاً). */
    @Override protected void onResume() { super.onResume(); if (db != null && sections != null) load(); }

    /** تبني عنوان الشاشة وحاوية البطاقات. */
    private void render() {
        LinearLayout content = Ui.column(this);
        Ui.addSectionHeader(this, content, I18n.t(this, "stats"), I18n.t(this, "stats_sub"));
        sections = new LinearLayout(this); sections.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2); lp.topMargin = Ui.dp(this, 8); content.addView(sections, lp);
        AppPreferences.applyDirection(this, content);
        setContentView(Ui.screen(this, Ui.scroll(this, content), "stats"));
        load();
    }

    /** تنفذ الاستعلامات التجميعية الثلاثة وتعرض ثلاث بطاقات. */
    private void load() {
        sections.removeAllViews();
        Map<String, Integer> guide = zeroed(LEVELS);
        readCounts(db.countEmergenciesBySeverity(), guide);
        addSection(I18n.t(this, "stats_guide"), labelled(guide));

        Map<String, Integer> treated = zeroed(LEVELS); treated.put("unspecified", 0);
        readCounts(db.countTreatedBySeverity(), treated);
        addSection(I18n.t(this, "stats_treated"), labelled(treated));

        Map<String, Integer> months = new LinkedHashMap<>();
        readCounts(db.countTreatedByMonth(), months);
        addSection(I18n.t(this, "stats_months"), months);
    }

    /** تجهّز كل مستويات الخطورة بقيمة 0 حتى يظهر المستوى ولو لم توجد له حالات. */
    private static Map<String, Integer> zeroed(String[] keys) { Map<String, Integer> m = new LinkedHashMap<>(); for (String k : keys) m.put(k, 0); return m; }

    /** تقرأ نتيجة GROUP BY إلى خريطة (المستوى ← العدد). */
    private static void readCounts(Cursor c, Map<String, Integer> into) {
        try { while (c.moveToNext()) if (!c.isNull(0)) into.put(c.getString(0), c.getInt(1)); } finally { c.close(); }
    }

    /** تترجم مفاتيح الخطورة إلى نصوص للعرض. */
    private Map<String, Integer> labelled(Map<String, Integer> byLevel) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : byLevel.entrySet()) out.put(I18n.t(this, "severity_" + e.getKey()), e.getValue());
        return out;
    }

    /** بطاقة إحصاء: العنوان والمجموع وشريط أفقي لكل صف طوله نسبة إلى أكبر قيمة. */
    private void addSection(String title, Map<String, Integer> rows) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 15), Ui.dp(this, 16), Ui.dp(this, 15));
        card.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 18, 1));
        int total = 0, max = 0;
        for (int v : rows.values()) { total += v; max = Math.max(max, v); }
        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(Ui.text(this, title, 16, Ui.ink(this), true), new LinearLayout.LayoutParams(0, -2, 1));
        head.addView(Ui.text(this, I18n.t(this, "total") + " " + total, 13, Ui.muted(this), true));
        card.addView(head);
        if (total == 0) {
            TextView empty = Ui.text(this, I18n.t(this, "no_data"), 14, Ui.muted(this), false);
            LinearLayout.LayoutParams ep = Ui.lp(this, -1, -2); ep.topMargin = Ui.dp(this, 12); card.addView(empty, ep);
        } else {
            for (Map.Entry<String, Integer> e : rows.entrySet()) { LinearLayout.LayoutParams bp = Ui.lp(this, -1, -2); bp.topMargin = Ui.dp(this, 12); card.addView(barRow(e.getKey(), e.getValue(), max), bp); }
        }
        LinearLayout.LayoutParams cp = Ui.lp(this, -1, -2); cp.topMargin = Ui.dp(this, 12); sections.addView(card, cp);
    }

    /** صف واحد: الاسم، الشريط الأحمر، والعدد، مع وصف لقارئ الشاشة. */
    private View barRow(String label, int value, int max) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setContentDescription(label + ": " + value); row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        TextView name = Ui.text(this, label, 14, Ui.ink(this), false); row.addView(name, Ui.lp(this, 96, -2));
        LinearLayout track = new LinearLayout(this); track.setWeightSum(max);
        track.setBackground(Ui.rounded(this, Ui.border(this), Ui.border(this), 4, 0));
        if (value > 0) {
            View fill = new View(this); fill.setBackground(Ui.rounded(this, Ui.RED, Ui.RED, 4, 0));
            track.addView(fill, new LinearLayout.LayoutParams(0, -1, value));
        }
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, Ui.dp(this, 14), 1); tp.setMarginStart(Ui.dp(this, 8)); row.addView(track, tp);
        TextView count = Ui.text(this, String.valueOf(value), 14, Ui.ink(this), true); count.setGravity(Gravity.CENTER);
        row.addView(count, Ui.lp(this, 36, -2));
        return row;
    }

    /** إغلاق قاعدة البيانات عند إغلاق الشاشة. */
    @Override protected void onDestroy() { if (db != null) db.close(); super.onDestroy(); }
}
