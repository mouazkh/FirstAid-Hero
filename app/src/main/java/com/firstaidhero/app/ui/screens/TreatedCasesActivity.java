package com.firstaidhero.app.ui.screens;

import android.content.Intent;  // طلب لفتح شاشة أخرى أو تطبيق آخر (الاتصال، منتقي جهات الاتصال)
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.graphics.Bitmap;  // صورة محمّلة في الذاكرة
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.text.Editable;  // النص القابل للتعديل داخل حقل الإدخال
import android.text.TextWatcher;  // مراقبة الكتابة في حقل الإدخال حرفاً بحرف (للبحث الفوري)
import android.view.Gravity;  // محاذاة العناصر والنصوص
import android.widget.Button;  // زر
import android.widget.EditText;  // حقل إدخال نص
import android.widget.ImageView;  // عرض صورة أو أيقونة
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import androidx.appcompat.app.AlertDialog;  // نافذة حوار (تأكيد، نموذج، قائمة اختيار)
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.data.DatabaseHelper;  // قاعدة البيانات وكل الاستعلامات
import com.firstaidhero.app.data.PhotoStore;  // حفظ صور الحالات المعالجة وتحميلها
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)

/**
 * شاشة سجل الحالات المعالجة: السجلات مرتبة حسب التاريخ مع صورة مصغّرة ونوع الحالة،
 * وبحث فوري، وأزرار تعديل وحذف، وزر لتسجيل حالة جديدة.
 */
public class TreatedCasesActivity extends BaseActivity {
    /** قاعدة البيانات، وحاوية السجلات، وحقل البحث. */
    private DatabaseHelper db; private LinearLayout list; private EditText search;
    /** تفتح قاعدة البيانات وتبني الشاشة. */
    @Override protected void onCreate(Bundle state) { super.onCreate(state); db = new DatabaseHelper(this); render(); }
    /** تحديث القائمة عند العودة من شاشة الإدخال. */
    @Override protected void onResume() { super.onResume(); if (db != null && list != null) load(search == null ? "" : search.getText().toString()); }
    /** تبني العنوان وحقل البحث (مع مراقبة الكتابة) وزر التسجيل وحاوية السجلات. */
    private void render() {
        LinearLayout content = Ui.column(this); Ui.addSectionHeader(this, content, I18n.t(this,"records"), I18n.t(this,"open_records"));
        search = Ui.input(this, I18n.t(this,"search_records"), false); LinearLayout.LayoutParams sp = Ui.lp(this, -1, 52); sp.topMargin = Ui.dp(this, 16); content.addView(search, sp);
        Button add = Ui.button(this, "+   " + I18n.t(this,"open_records"), true); LinearLayout.LayoutParams ap = Ui.lp(this, -1, 50); ap.topMargin = Ui.dp(this, 10); content.addView(add, ap); add.setOnClickListener(v -> startActivity(new Intent(this, TreatedCaseFormActivity.class)));
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams lp = Ui.lp(this, -1, -2); lp.topMargin = Ui.dp(this, 12); content.addView(list, lp);
        search.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){ load(s.toString()); } public void afterTextChanged(Editable e){} });
        AppPreferences.applyDirection(this, content); setContentView(Ui.screen(this, Ui.scroll(this, content), "records")); load("");
    }
    /**
     * تعرض السجلات المطابقة للبحث: صورة مصغّرة (الضغط يعرضها كاملة)، الوصف، التاريخ، نوع الحالة وخطورتها، الإجراءات.
     * الحذف يحذف السجل وملف صورته معاً.
     */
    private void load(String query) {
        if (list == null) return; list.removeAllViews(); boolean ar = AppPreferences.isArabic(this); Cursor c = db.getTreatedCases(query); int found = 0;
        try { while (c.moveToNext()) {
            long id = c.getLong(0); String desc = c.getString(1), date = c.getString(2), photo = c.getString(3), procedures = c.getString(4);
            String caseTitle = c.isNull(5) ? null : c.getString(ar ? 7 : 6), level = c.getString(8);
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(Ui.dp(this, 15), Ui.dp(this, 14), Ui.dp(this, 15), Ui.dp(this, 14)); card.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 18, 1));
            LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
            Bitmap thumb = PhotoStore.load(this, photo, Ui.dp(this, 64));
            if (thumb != null) {
                ImageView image = new ImageView(this); image.setImageBitmap(thumb); image.setScaleType(ImageView.ScaleType.CENTER_CROP); image.setClipToOutline(true);
                image.setBackground(Ui.rounded(this, Ui.softAccent(this), Ui.softAccent(this), 12, 0)); image.setContentDescription(I18n.t(this, "photo_label"));
                image.setOnClickListener(v -> TreatedCaseFormActivity.showFullPhoto(this, photo)); header.addView(image, Ui.lp(this, 64, 64));
            } else { TextView icon = Ui.centered(this,"▤",17,Ui.RED,true); icon.setBackground(Ui.rounded(this,Ui.softAccent(this),Ui.softAccent(this),19,0)); header.addView(icon,Ui.lp(this,38,38)); }
            LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0,-2,1); llp.setMarginStart(Ui.dp(this,11)); header.addView(labels,llp); labels.addView(Ui.text(this,desc,15,Ui.ink(this),true)); TextView d=Ui.text(this,date,12,Ui.RED, true); LinearLayout.LayoutParams dlp=Ui.lp(this,-1,-2);dlp.topMargin=Ui.dp(this,3);labels.addView(d,dlp);card.addView(header);
            if (caseTitle != null) { TextView chip = Ui.text(this, caseTitle + "  ·  " + severityLabel(level), 12, Ui.RED_DARK, true); chip.setPadding(Ui.dp(this,10),Ui.dp(this,5),Ui.dp(this,10),Ui.dp(this,5)); chip.setBackground(Ui.rounded(this,Ui.softAccent(this),Ui.softAccent(this),12,0)); LinearLayout.LayoutParams clp=Ui.lp(this,-2,-2); clp.topMargin=Ui.dp(this,10); card.addView(chip,clp); }
            TextView note=Ui.text(this,procedures,14,Ui.muted(this),false); LinearLayout.LayoutParams nlp=Ui.lp(this,-1,-2);nlp.topMargin=Ui.dp(this,11);card.addView(note,nlp);
            LinearLayout actions = new LinearLayout(this); LinearLayout.LayoutParams alp = Ui.lp(this,-1,-2); alp.topMargin=Ui.dp(this,11); card.addView(actions, alp);
            Button edit=Ui.button(this,I18n.t(this,"edit"),false); actions.addView(edit,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1)); edit.setOnClickListener(v->{ Intent i = new Intent(this, TreatedCaseFormActivity.class); i.putExtra("record_id", id); startActivity(i); });
            Button del=Ui.button(this,I18n.t(this,"delete"),false); LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(0,Ui.dp(this,42),1); blp.setMarginStart(Ui.dp(this,10)); actions.addView(del,blp);
            del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage(I18n.t(this,"delete")+"?").setNegativeButton(I18n.t(this,"cancel"),null).setPositiveButton(I18n.t(this,"delete"),(x,w)->{ if (db.deleteTreatedCase(id) > 0) PhotoStore.delete(this, photo); load(search.getText().toString()); }).show());
            LinearLayout.LayoutParams cp=Ui.lp(this,-1,-2);cp.topMargin=Ui.dp(this,9);list.addView(card,cp);found++;
        }} finally { c.close(); }
        if(found==0){TextView empty=Ui.card(this,"▤\n\n"+I18n.t(this,"empty_records"),14,Ui.muted(this),false);empty.setGravity(Gravity.CENTER);list.addView(empty,Ui.lp(this,-1,135));}
    }
    /** ترجمة مستوى الخطورة إلى نص. */
    private String severityLabel(String level) { if ("critical".equals(level)) return I18n.t(this, "severity_critical"); if ("moderate".equals(level)) return I18n.t(this, "severity_moderate"); return I18n.t(this, "severity_minor"); }
    /** إغلاق قاعدة البيانات عند إغلاق الشاشة. */
    @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
