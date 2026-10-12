package com.firstaidhero.app.ui.screens;

import android.app.DatePickerDialog;  // نافذة اختيار التاريخ الجاهزة من النظام
import android.database.Cursor;  // مؤشر لقراءة نتائج الاستعلام صفاً صفاً
import android.graphics.Bitmap;  // صورة محمّلة في الذاكرة
import android.net.Uri;  // عنوان لمورد مثل صورة أو جهة اتصال أو رقم هاتف (tel:)
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.text.InputFilter;  // تقييد المدخلات مثل الحد الأقصى لعدد الأحرف
import android.view.View;  // الفئة الأساسية لكل عناصر الواجهة
import android.widget.Button;  // زر
import android.widget.EditText;  // حقل إدخال نص
import android.widget.ImageView;  // عرض صورة أو أيقونة
import android.widget.LinearLayout;  // حاوية ترتب العناصر عمودياً أو أفقياً
import android.widget.TextView;  // عرض نص
import android.widget.Toast;  // رسالة قصيرة مؤقتة أسفل الشاشة
import androidx.activity.result.ActivityResultLauncher;  // إطلاق طلب (صلاحية أو منتقٍ) واستقبال نتيجته
import androidx.activity.result.PickVisualMediaRequest;  // طلب اختيار صورة من منتقي الصور الحديث
import androidx.activity.result.contract.ActivityResultContracts;  // عقود جاهزة: طلب صلاحية، منتقي صور، فتح شاشة واستقبال نتيجة
import androidx.appcompat.app.AlertDialog;  // نافذة حوار (تأكيد، نموذج، قائمة اختيار)
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.data.DatabaseHelper;  // قاعدة البيانات وكل الاستعلامات
import com.firstaidhero.app.data.PhotoStore;  // حفظ صور الحالات المعالجة وتحميلها
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)
import java.text.ParseException;  // خطأ عند تحليل نص تاريخ غير صحيح
import java.text.SimpleDateFormat;  // تحويل التاريخ من وإلى صيغة yyyy-MM-dd
import java.util.ArrayList;  // قائمة ديناميكية
import java.util.Calendar;  // التعامل مع السنة والشهر واليوم
import java.util.Date;  // تاريخ ووقت
import java.util.List;  // واجهة القوائم
import java.util.Locale;  // اللغة والمنطقة (لتنسيق التاريخ والنصوص)
import java.util.concurrent.ExecutorService;  // تنفيذ مهام في خيط خلفي
import java.util.concurrent.Executors;  // إنشاء خيط خلفي لمعالجة الصور دون تجميد الواجهة

/**
 * شاشة إدخال الحالات المعالجة: إضافة أو تعديل سجل (نوع الحالة، الوصف، التاريخ، الإجراءات، الصورة).
 * تستقبل record_id للتعديل، أو case_id لاختيار نوع الحالة مسبقاً.
 */
public class TreatedCaseFormActivity extends BaseActivity {
    /** الحد الأقصى لطول الوصف والإجراءات، وصيغة التاريخ، وخيط خلفي لمعالجة الصور. */
    private static final int MAX_DESCRIPTION = 200;
    private static final int MAX_PROCEDURES = 2000;
    private final SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    /** قاعدة البيانات، منتقي الصور، رقم السجل (-1 للإضافة)، ونوع الحالة المختار. */
    private DatabaseHelper db;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPicker;
    private long recordId = -1;
    private Long caseId;

    /** الصورة المعروضة حالياً، والصورة المحفوظة في القاعدة، والصور المنسوخة في هذه الجلسة ولم تُحفظ بعد. */
    private String photoName, savedPhotoName;
    private final ArrayList<String> unsavedPhotos = new ArrayList<>();
    private boolean importing;

    /** عناصر النموذج. */
    private EditText description, date, procedures;
    private Button caseButton, photoButton, removePhoto;
    private ImageView preview;
    private TextView photoStatus;

    /**
     * تسجّل منتقي الصور (لا يحتاج صلاحية)، ثم تبني النموذج وتملؤه: من الحالة المحفوظة بعد التدوير،
     * أو من القاعدة عند التعديل، أو بتاريخ اليوم عند الإضافة.
     */
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        iso.setLenient(false);
        db = new DatabaseHelper(this);
        photoPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> { if (uri != null) importPhoto(uri); });
        recordId = getIntent().getLongExtra("record_id", -1);
        render();
        if (state != null) restore(state);
        else if (recordId >= 0) loadRecord();
        else {
            long preset = getIntent().getLongExtra("case_id", -1);
            if (preset >= 0) caseId = preset;
            date.setText(iso.format(new Date()));
        }
        refreshCaseButton(); refreshPhoto();
    }

    /** تبني حقول النموذج: نوع الحالة، الوصف، التاريخ، الإجراءات، معاينة الصورة وأزرارها، وزر الحفظ. */
    private void render() {
        LinearLayout content = Ui.column(this);
        TextView back = Ui.text(this, "‹  " + I18n.t(this, "records"), 14, Ui.RED, true); content.addView(back); back.setOnClickListener(v -> finish());
        Ui.addLabel(this, content, I18n.t(this, recordId >= 0 ? "edit_record" : "open_records"), 26, Ui.ink(this), true, 14);

        Ui.addLabel(this, content, I18n.t(this, "emergency_type"), 15, Ui.ink(this), true, 18);
        caseButton = Ui.button(this, "", false); caseButton.setOnClickListener(v -> chooseCase());
        content.addView(caseButton, spaced(-2, 8));

        Ui.addLabel(this, content, I18n.t(this, "description") + " *", 15, Ui.ink(this), true, 16);
        description = Ui.input(this, I18n.t(this, "description"), false);
        description.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION)});
        content.addView(description, spaced(-2, 8));

        Ui.addLabel(this, content, I18n.t(this, "date") + " *", 15, Ui.ink(this), true, 16);
        date = Ui.input(this, "YYYY-MM-DD", false);
        date.setFocusable(false); date.setCursorVisible(false); date.setOnClickListener(v -> pickDate());
        content.addView(date, spaced(-2, 8));

        Ui.addLabel(this, content, I18n.t(this, "procedures") + " *", 15, Ui.ink(this), true, 16);
        procedures = Ui.input(this, I18n.t(this, "procedures"), true);
        procedures.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PROCEDURES)});
        content.addView(procedures, spaced(-2, 8));

        Ui.addLabel(this, content, I18n.t(this, "photo_label"), 15, Ui.ink(this), true, 16);
        preview = new ImageView(this); preview.setScaleType(ImageView.ScaleType.CENTER_CROP); preview.setClipToOutline(true);
        preview.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 16, 1));
        preview.setContentDescription(I18n.t(this, "photo_label"));
        preview.setOnClickListener(v -> showFullPhoto(this, photoName));
        content.addView(preview, spaced(200, 8));
        photoStatus = Ui.text(this, "", 13, Ui.muted(this), false); content.addView(photoStatus, spaced(-2, 6));
        LinearLayout photoRow = new LinearLayout(this);
        photoButton = Ui.button(this, "", false); photoButton.setOnClickListener(v -> photoPicker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        removePhoto = Ui.button(this, I18n.t(this, "remove_photo"), false); removePhoto.setOnClickListener(v -> { photoName = null; refreshPhoto(); });
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        photoRow.addView(photoButton, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1); half2.setMarginStart(Ui.dp(this, 10));
        photoRow.addView(removePhoto, half2);
        content.addView(photoRow, spaced(-2, 8));

        Button save = Ui.button(this, I18n.t(this, "save"), true); save.setOnClickListener(v -> save());
        content.addView(save, spaced(52, 24));
        AppPreferences.applyDirection(this, content);
        setContentView(Ui.screen(this, Ui.scroll(this, content), "records"));
    }

    /** أبعاد عنصر بعرض كامل ومسافة علوية. */
    private LinearLayout.LayoutParams spaced(int heightDp, int topDp) { LinearLayout.LayoutParams p = Ui.lp(this, -1, heightDp); p.topMargin = Ui.dp(this, topDp); return p; }

    /** تقرأ السجل من القاعدة عند التعديل وتملأ الحقول. */
    private void loadRecord() {
        Cursor c = db.getTreatedCase(recordId);
        try {
            if (!c.moveToFirst()) { recordId = -1; date.setText(iso.format(new Date())); return; }
            description.setText(c.getString(1)); date.setText(c.getString(2)); procedures.setText(c.getString(4));
            photoName = savedPhotoName = c.getString(3);
            caseId = c.isNull(5) ? null : c.getLong(5);
        } finally { c.close(); }
    }

    /** قائمة اختيار نوع الحالة من جدول الحالات (أو غير محدد). */
    private void chooseCase() {
        boolean ar = AppPreferences.isArabic(this);
        List<Long> ids = new ArrayList<>(); List<String> labels = new ArrayList<>();
        ids.add(null); labels.add(I18n.t(this, "not_specified"));
        Cursor c = db.searchEmergencies("");
        try { while (c.moveToNext()) { ids.add(c.getLong(0)); labels.add(c.getString(ar ? 2 : 1)); } } finally { c.close(); }
        int checked = Math.max(0, ids.indexOf(caseId));
        new AlertDialog.Builder(this).setTitle(I18n.t(this, "emergency_type"))
                .setSingleChoiceItems(labels.toArray(new String[0]), checked, (d, which) -> { caseId = ids.get(which); refreshCaseButton(); d.dismiss(); })
                .setNegativeButton(I18n.t(this, "cancel"), null).show();
    }

    /** تعرض اسم نوع الحالة المختار على الزر. */
    private void refreshCaseButton() {
        String label = I18n.t(this, "not_specified");
        if (caseId != null) {
            Cursor c = db.getEmergency(caseId);
            try { if (c.moveToFirst()) label = c.getString(AppPreferences.isArabic(this) ? 2 : 1); else caseId = null; } finally { c.close(); }
        }
        caseButton.setText(label + "  ▾");
    }

    /** منتقي تاريخ لا يسمح باختيار تاريخ مستقبلي. */
    private void pickDate() {
        Calendar cal = Calendar.getInstance();
        Date current = parseDate(date.getText().toString());
        if (current != null) cal.setTime(current);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar picked = Calendar.getInstance(); picked.set(y, m, d);
            date.setText(iso.format(picked.getTime())); date.setError(null);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    /** تحليل صارم لتاريخ بصيغة YYYY-MM-DD؛ تعيد null إذا كانت الصيغة أو التاريخ غير صحيحين. */
    private Date parseDate(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) return null;
        try { return iso.parse(value); } catch (ParseException e) { return null; }
    }

    /** تنسخ الصورة المختارة عبر PhotoStore في خيط خلفي حتى لا تتجمد الواجهة، ثم تعرض المعاينة. */
    private void importPhoto(Uri uri) {
        importing = true; refreshPhoto();
        io.execute(() -> {
            String name = null;
            try { name = PhotoStore.importPhoto(getApplicationContext(), uri); } catch (Exception ignored) { }
            String result = name;
            runOnUiThread(() -> {
                importing = false;
                if (isDestroyed()) { PhotoStore.delete(getApplicationContext(), result); return; }
                if (result == null) Toast.makeText(this, I18n.t(this, "photo_error"), Toast.LENGTH_LONG).show();
                else { unsavedPhotos.add(result); photoName = result; }
                refreshPhoto();
            });
        });
    }

    /** تحدّث المعاينة والرسائل والأزرار حسب حالة الصورة. */
    private void refreshPhoto() {
        Bitmap bitmap = importing ? null : PhotoStore.load(this, photoName, Ui.dp(this, 360));
        preview.setImageBitmap(bitmap);
        preview.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
        boolean missing = !importing && photoName != null && bitmap == null;
        photoStatus.setText(importing ? I18n.t(this, "photo_processing") : missing ? I18n.t(this, "photo_missing") : bitmap == null ? I18n.t(this, "no_photo") : "");
        photoStatus.setVisibility(photoStatus.getText().length() > 0 ? View.VISIBLE : View.GONE);
        photoButton.setText(I18n.t(this, photoName == null ? "photo" : "change_photo"));
        photoButton.setEnabled(!importing);
        removePhoto.setVisibility(photoName != null && !importing ? View.VISIBLE : View.GONE);
    }

    /**
     * التحقق: الوصف والإجراءات مطلوبة، والتاريخ صحيح وغير مستقبلي؛ عند الخطأ يُعلَّم الحقل وتبقى الشاشة مفتوحة.
     * عند النجاح يُحفظ السجل وتُحذف الصورة القديمة إن استُبدلت.
     */
    private void save() {
        String ds = description.getText().toString().trim(), dt = date.getText().toString().trim(), pr = procedures.getText().toString().trim();
        boolean valid = true;
        if (ds.isEmpty()) { description.setError(I18n.t(this, "field_required")); valid = false; }
        Date parsed = parseDate(dt);
        if (parsed == null) { date.setError(I18n.t(this, "field_required")); valid = false; }
        else if (parsed.after(new Date())) { date.setError(I18n.t(this, "date_future")); valid = false; }
        if (pr.isEmpty()) { procedures.setError(I18n.t(this, "field_required")); valid = false; }
        if (importing) { Toast.makeText(this, I18n.t(this, "photo_processing"), Toast.LENGTH_SHORT).show(); return; }
        if (!valid) { Toast.makeText(this, I18n.t(this, "required"), Toast.LENGTH_SHORT).show(); return; }
        if (db.saveTreatedCase(recordId, caseId, ds, dt, photoName, pr) < 0) { Toast.makeText(this, I18n.t(this, "save_failed"), Toast.LENGTH_LONG).show(); return; }
        if (savedPhotoName != null && !savedPhotoName.equals(photoName)) PhotoStore.delete(this, savedPhotoName);
        unsavedPhotos.remove(photoName);
        finish();
    }

    /** تعرض الصورة كاملة داخل نافذة (تُستخدم أيضاً من شاشة السجل). */
    static void showFullPhoto(BaseActivity activity, String name) {
        Bitmap full = PhotoStore.load(activity, name, 1600);
        if (full == null) return;
        ImageView image = new ImageView(activity); image.setAdjustViewBounds(true); image.setImageBitmap(full);
        new AlertDialog.Builder(activity).setView(image).setPositiveButton("OK", null).show();
    }

    /** تسترجع كل الحقول والصورة بعد تدوير الشاشة. */
    private void restore(Bundle s) {
        recordId = s.getLong("record_id", recordId);
        caseId = s.containsKey("case_id") ? s.getLong("case_id") : null;
        photoName = s.getString("photo"); savedPhotoName = s.getString("saved_photo");
        ArrayList<String> pending = s.getStringArrayList("unsaved"); if (pending != null) unsavedPhotos.addAll(pending);
        description.setText(s.getString("description")); date.setText(s.getString("date")); procedures.setText(s.getString("procedures"));
    }

    /** تحفظ كل الحقول والصورة قبل تدوير الشاشة. */
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putLong("record_id", recordId);
        if (caseId != null) out.putLong("case_id", caseId);
        out.putString("photo", photoName); out.putString("saved_photo", savedPhotoName);
        out.putStringArrayList("unsaved", unsavedPhotos);
        out.putString("description", description.getText().toString()); out.putString("date", date.getText().toString()); out.putString("procedures", procedures.getText().toString());
    }

    /** عند الخروج دون حفظ تُحذف الصور المنسوخة غير المحفوظة حتى لا تبقى ملفات يتيمة، ثم يُغلق الخيط والقاعدة. */
    @Override protected void onDestroy() {
        if (isFinishing()) for (String name : unsavedPhotos) PhotoStore.delete(this, name);
        io.shutdown();
        if (db != null) db.close();
        super.onDestroy();
    }
}
