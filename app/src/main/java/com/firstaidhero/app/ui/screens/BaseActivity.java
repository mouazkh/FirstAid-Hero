package com.firstaidhero.app.ui.screens;

import android.Manifest;  // أسماء صلاحيات أندرويد مثل READ_CONTACTS و CALL_PHONE
import android.content.ActivityNotFoundException;  // خطأ يحدث عند عدم وجود تطبيق يستقبل الطلب (مثل تطبيق الاتصال)
import android.content.Intent;  // طلب لفتح شاشة أخرى أو تطبيق آخر (الاتصال، منتقي جهات الاتصال)
import android.content.pm.PackageManager;  // للتحقق من منح الصلاحيات
import android.net.Uri;  // عنوان لمورد مثل صورة أو جهة اتصال أو رقم هاتف (tel:)
import android.os.Bundle;  // حاوية لحفظ حالة الشاشة وتمرير البيانات بين الشاشات
import android.widget.Toast;  // رسالة قصيرة مؤقتة أسفل الشاشة
import androidx.activity.result.ActivityResultLauncher;  // إطلاق طلب (صلاحية أو منتقٍ) واستقبال نتيجته
import androidx.activity.result.contract.ActivityResultContracts;  // عقود جاهزة: طلب صلاحية، منتقي صور، فتح شاشة واستقبال نتيجة
import androidx.appcompat.app.AppCompatActivity;  // شاشة متوافقة مع الإصدارات المختلفة وتدعم الوضع الليلي
import androidx.appcompat.app.AppCompatDelegate;  // تطبيق الوضع الفاتح أو الداكن على التطبيق
import androidx.core.content.ContextCompat;  // التحقق من منح صلاحية بطريقة متوافقة
import com.firstaidhero.app.core.AppPreferences;  // إعدادات التطبيق المحفوظة (اللغة، المظهر)
import com.firstaidhero.app.core.I18n;  // نصوص الواجهة بالعربية والإنجليزية
import com.firstaidhero.app.ui.Ui;  // نظام التصميم المشترك (ألوان، نصوص، أزرار)

/**
 * الشاشة الأب لكل شاشات التطبيق: تطبّق المظهر المحفوظ، وتعيد بناء الشاشة عند تغيّر اللغة،
 * وتوفّر طريقة الاتصال المباشر call() لكل الشاشات.
 */
public abstract class BaseActivity extends AppCompatActivity {
    /** اللغة عند إنشاء الشاشة، لمعرفة إن تغيّرت أثناء وجودها في الخلفية. */
    private boolean languageAtCreation;
    private boolean created;
    /** الرقم المنتظر الاتصال به ريثما يرد المستخدم على طلب صلاحية الاتصال. */
    private String pendingCall;
    /** طلب صلاحية CALL_PHONE: عند الموافقة اتصال مباشر، وعند الرفض فتح لوحة الاتصال. */
    private final ActivityResultLauncher<String> callPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (pendingCall != null) startCall(pendingCall, granted);
        pendingCall = null;
    });

    /** تطبيق وضع المظهر قبل رسم الشاشة، وتفعيل الرسم خلف أشرطة النظام، وحفظ اللغة الحالية. */
    @Override protected void onCreate(Bundle state) {
        AppCompatDelegate.setDefaultNightMode(AppPreferences.toNightMode(AppPreferences.getThemeMode(this)));
        super.onCreate(state);
        Ui.edgeToEdge(this);
        languageAtCreation = AppPreferences.isArabic(this);
        created = true;
    }
    /** إذا تغيّرت اللغة من الإعدادات يُعاد بناء الشاشة باللغة الجديدة. */
    @Override protected void onResume() {
        super.onResume();
        if (created && languageAtCreation != AppPreferences.isArabic(this)) {
            languageAtCreation = AppPreferences.isArabic(this);
            recreate();
        }
    }

    /** الاتصال المباشر بالرقم: إذا كانت صلاحية CALL_PHONE ممنوحة يتم الاتصال فوراً، وإلا تُطلب الصلاحية. */
    protected void call(String phone) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) startCall(phone, true);
        else { pendingCall = phone; callPermission.launch(Manifest.permission.CALL_PHONE); }
    }

    /**
     * تنفيذ الاتصال: ACTION_CALL للاتصال المباشر، أو ACTION_DIAL لفتح لوحة الاتصال مع الرقم.
     * إذا لم يوجد تطبيق اتصال تظهر رسالة بدل الانهيار.
     */
    private void startCall(String phone, boolean direct) {
        Uri uri = Uri.parse("tel:" + Uri.encode(phone));
        try {
            startActivity(new Intent(direct ? Intent.ACTION_CALL : Intent.ACTION_DIAL, uri));
        } catch (ActivityNotFoundException | SecurityException e) {
            try { startActivity(new Intent(Intent.ACTION_DIAL, uri)); }
            catch (ActivityNotFoundException none) { Toast.makeText(this, I18n.t(this, "no_dialer"), Toast.LENGTH_LONG).show(); }
        }
    }
}
