package com.firstaidhero.app.core;

import android.content.Context;  // سياق التطبيق للوصول إلى الموارد والملفات وقواعد البيانات
import android.content.SharedPreferences;  // تخزين إعدادات بسيطة (مفتاح ← قيمة) على الجهاز
import android.view.View;  // الفئة الأساسية لكل عناصر الواجهة
import androidx.appcompat.app.AppCompatDelegate;  // تطبيق الوضع الفاتح أو الداكن على التطبيق

/**
 * فئة مساعدة لحفظ تفضيلات المستخدم وقراءتها من SharedPreferences:
 * إتمام شاشات التعريف، لغة التطبيق (عربي/إنجليزي)، ووضع المظهر (فاتح/داكن/حسب الجهاز).
 */
public final class AppPreferences {
    /** قيم وضع المظهر الثلاثة، ثم اسم ملف التفضيلات ومفاتيح الحفظ داخله. */
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    private static final String FILE = "firstaid_hero_preferences";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme_mode";

    /** مُنشئ خاص يمنع إنشاء كائن من الفئة، لأن كل طرقها ثابتة (static). */
    private AppPreferences() { }
    /** تعيد ملف التفضيلات الخاص بالتطبيق بوضع خاص لا تقرؤه التطبيقات الأخرى. */
    private static SharedPreferences prefs(Context context) { return context.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    /** تعيد true إذا أنهى المستخدم شاشات التعريف سابقاً. */
    public static boolean isOnboardingComplete(Context context) { return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false); }
    /** تحفظ أن شاشات التعريف انتهت حتى لا تظهر مرة أخرى. */
    public static void setOnboardingComplete(Context context) { prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply(); }
    /** تعيد true إذا كانت اللغة المحفوظة هي العربية (الافتراضي الإنجليزية). */
    public static boolean isArabic(Context context) { return "ar".equals(prefs(context).getString(KEY_LANGUAGE, "en")); }
    /** تحفظ لغة التطبيق: ar للعربية أو en للإنجليزية. */
    public static void setArabic(Context context, boolean arabic) { prefs(context).edit().putString(KEY_LANGUAGE, arabic ? "ar" : "en").apply(); }

    /** تعيد وضع المظهر المحفوظ، والافتراضي system أي اتباع إعداد الجهاز. */
    public static String getThemeMode(Context context) { return prefs(context).getString(KEY_THEME, THEME_SYSTEM); }
    /** تحفظ وضع المظهر بعد التحقق من صحته؛ أي قيمة غير معروفة تتحول إلى system. */
    public static void setThemeMode(Context context, String mode) {
        if (!THEME_SYSTEM.equals(mode) && !THEME_LIGHT.equals(mode) && !THEME_DARK.equals(mode)) mode = THEME_SYSTEM;
        prefs(context).edit().putString(KEY_THEME, mode).apply();
    }
    /** تحوّل وضع المظهر النصي إلى الثابت المقابل في AppCompatDelegate لتطبيقه على الواجهة. */
    public static int toNightMode(String mode) {
        if (THEME_LIGHT.equals(mode)) return AppCompatDelegate.MODE_NIGHT_NO;
        if (THEME_DARK.equals(mode)) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }
    /** تضبط اتجاه الواجهة: من اليمين لليسار للعربية، ومن اليسار لليمين للإنجليزية. */
    public static void applyDirection(Context context, View view) {
        if (view != null) view.setLayoutDirection(isArabic(context) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}
