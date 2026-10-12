package com.firstaidhero.app.ui.screens;

import android.content.Intent;  // طلب لفتح شاشة أخرى أو تطبيق آخر (الاتصال، منتقي جهات الاتصال)
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.graphics.drawable.Drawable;  // عنصر رسومي مثل الأيقونات
import android.graphics.drawable.GradientDrawable;  // خلفية بلون أو تدرج مع زوايا دائرية وإطار
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.text.Editable;  // النص القابل للتعديل داخل حقل الإدخال
import android.text.TextWatcher;  // مراقبة الكتابة في حقل الإدخال حرفاً بحرف (للبحث الفوري)
import android.view.Gravity;  // محاذاة العناصر والنصوص
import android.widget.EditText;  // حقل إدخال نص
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.R;  // معرّفات موارد المشروع (أيقونات، قوائم)
import com.firstaidhero.app.data.DatabaseHelper;  // قاعدة البيانات وكل الاستعلامات
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)

/** الشاشة الرئيسية (Dashboard): شريط بحث كبير وقائمة الحالات الإسعافية مع بحث فوري أثناء الكتابة. */
public class MainActivity extends BaseActivity {
    /** قاعدة البيانات، وحاوية نتائج البحث، وحقل البحث. */
    private DatabaseHelper db;
    private LinearLayout results;
    private EditText search;

    /** تفتح قاعدة البيانات وتبني الشاشة. */
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); db = new DatabaseHelper(this); render();
    }
    /** تبني الشعار والبطاقة الحمراء وشريط البحث وقائمة النتائج، وتربط البحث بكل حرف يُكتب. */
    private void render() {
        LinearLayout content = Ui.column(this);
        LinearLayout brand = new LinearLayout(this); brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = Ui.centered(this, "✚", 18, Ui.RED, true); mark.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 19, 0));
        brand.addView(mark, Ui.lp(this, 38, 38));
        TextView title = Ui.text(this, "FirstAid Hero", 17, Ui.ink(this), true); LinearLayout.LayoutParams titleLp = Ui.lp(this, -2, -2); titleLp.setMarginStart(Ui.dp(this, 10)); brand.addView(title, titleLp);
        content.addView(brand);
        LinearLayout hero = new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        GradientDrawable heroBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Ui.RED, Ui.RED_DARK}); heroBg.setCornerRadius(Ui.dp(this, 22)); hero.setBackground(heroBg);
        TextView eyebrow = Ui.text(this, I18n.t(this, "offline").toUpperCase(AppPreferences.isArabic(this) ? new java.util.Locale("ar") : java.util.Locale.ENGLISH), 11, 0xFFFFE7E8, true); hero.addView(eyebrow);
        TextView heroTitle = Ui.text(this, I18n.t(this, "home"), 27, Ui.WHITE, true); LinearLayout.LayoutParams htp = Ui.lp(this, -1, -2); htp.topMargin = Ui.dp(this, 9); hero.addView(heroTitle, htp);
        TextView subtitle = Ui.text(this, I18n.t(this, "home_sub"), 14, 0xFFFFE7E8, false); LinearLayout.LayoutParams stp = Ui.lp(this, -1, -2); stp.topMargin = Ui.dp(this, 5); hero.addView(subtitle, stp);
        LinearLayout.LayoutParams heroLp = Ui.lp(this, -1, -2); heroLp.topMargin = Ui.dp(this, 20); content.addView(hero, heroLp);
        Ui.addLabel(this, content, I18n.t(this, "common"), 19, Ui.ink(this), true, 22);
        search = Ui.input(this, I18n.t(this, "search_hint"), false);
        search.setCompoundDrawablePadding(Ui.dp(this, 10));
        search.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
        Drawable searchIcon = search.getCompoundDrawablesRelative()[0];
        if (searchIcon != null) searchIcon.setTint(Ui.muted(this));
        LinearLayout.LayoutParams searchLp = Ui.lp(this, -1, 54); searchLp.topMargin = Ui.dp(this, 12); content.addView(search, searchLp);
        results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams resultsLp = Ui.lp(this, -1, -2); resultsLp.topMargin = Ui.dp(this, 7); content.addView(results, resultsLp);
        search.addTextChangedListener(new TextWatcher() {
            /** قبل تغيّر النص: لا نحتاجها. */
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            /** مع كل حرف يُكتب أو يُحذف: تنفيذ البحث فوراً. */
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadCases(s.toString()); }
            /** بعد تغيّر النص: لا نحتاجها. */
            @Override public void afterTextChanged(Editable e) { }
        });
        AppPreferences.applyDirection(this, content);
        setContentView(Ui.screen(this, Ui.scroll(this, content), "home")); loadCases("");
    }
    /** تنفذ البحث وتعرض كل حالة كبطاقة (الاسم، الخطورة، الأعراض)؛ الضغط يفتح شاشة الخطوات، وعند عدم وجود نتائج تظهر رسالة. */
    private void loadCases(String query) {
        if (results == null) return; results.removeAllViews(); boolean ar = AppPreferences.isArabic(this); Cursor c = db.searchEmergencies(query); int count = 0;
        try { while (c.moveToNext()) {
            long id = c.getLong(0); String name = c.getString(ar ? 2 : 1); String level = c.getString(3); String symptoms = c.getString(ar ? 5 : 4);
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12)); card.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 17, 1));
            TextView marker = Ui.centered(this, "!", 17, Ui.RED, true); marker.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 18, 0)); card.addView(marker, Ui.lp(this, 36, 36));
            LinearLayout details = new LinearLayout(this); details.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(0, -2, 1); detailsLp.setMarginStart(Ui.dp(this, 12)); detailsLp.setMarginEnd(Ui.dp(this, 8)); card.addView(details, detailsLp);
            details.addView(Ui.text(this, name, 15, Ui.ink(this), true));
            TextView meta = Ui.text(this, severityLabel(level) + "  ·  " + symptoms, 12, Ui.muted(this), false); LinearLayout.LayoutParams mlp = Ui.lp(this, -1, -2); mlp.topMargin = Ui.dp(this, 4); details.addView(meta, mlp);
            TextView arrow = Ui.centered(this, AppPreferences.isArabic(this) ? "‹" : "›", 24, Ui.muted(this), false); card.addView(arrow, Ui.lp(this, 26, 36));
            LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2); lp.topMargin = Ui.dp(this, 9); results.addView(card, lp);
            card.setClickable(true); card.setFocusable(true); card.setForeground(getDrawable(android.R.drawable.list_selector_background));
            card.setOnClickListener(v -> { Intent i = new Intent(this, EmergencyDetailActivity.class); i.putExtra("case_id", id); startActivity(i); }); count++;
        }} finally { c.close(); }
        if (count == 0) { TextView empty = Ui.card(this, I18n.t(this, "no_results"), 14, Ui.muted(this), false); LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2); lp.topMargin = Ui.dp(this, 10); results.addView(empty, lp); }
    }
    /** ترجمة مستوى الخطورة إلى نص (حرج، متوسط، طفيف). */
    private String severityLabel(String level) { if ("critical".equals(level)) return I18n.t(this, "severity_critical"); if ("moderate".equals(level)) return I18n.t(this, "severity_moderate"); return I18n.t(this, "severity_minor"); }
    /** إغلاق قاعدة البيانات عند إغلاق الشاشة. */
    @Override protected void onDestroy() { if (db != null) db.close(); super.onDestroy(); }
}
