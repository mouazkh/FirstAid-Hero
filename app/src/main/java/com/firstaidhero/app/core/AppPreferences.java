package com.firstaidhero.app.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.appcompat.app.AppCompatDelegate;

/** Persistent first-run, language, and appearance preferences. */
public final class AppPreferences {
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    private static final String FILE = "firstaid_hero_preferences";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme_mode";

    private AppPreferences() { }
    private static SharedPreferences prefs(Context context) { return context.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    public static boolean isOnboardingComplete(Context context) { return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false); }
    public static void setOnboardingComplete(Context context) { prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply(); }
    public static boolean isArabic(Context context) { return "ar".equals(prefs(context).getString(KEY_LANGUAGE, "en")); }
    public static void setArabic(Context context, boolean arabic) { prefs(context).edit().putString(KEY_LANGUAGE, arabic ? "ar" : "en").apply(); }

    /** The default is System; no stored override is needed until the user changes it. */
    public static String getThemeMode(Context context) { return prefs(context).getString(KEY_THEME, THEME_SYSTEM); }
    public static void setThemeMode(Context context, String mode) {
        if (!THEME_SYSTEM.equals(mode) && !THEME_LIGHT.equals(mode) && !THEME_DARK.equals(mode)) mode = THEME_SYSTEM;
        prefs(context).edit().putString(KEY_THEME, mode).apply();
    }
    public static int toNightMode(String mode) {
        if (THEME_LIGHT.equals(mode)) return AppCompatDelegate.MODE_NIGHT_NO;
        if (THEME_DARK.equals(mode)) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }
    public static void applyDirection(Context context, View view) {
        if (view != null) view.setLayoutDirection(isArabic(context) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}
