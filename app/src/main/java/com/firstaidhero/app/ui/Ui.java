package com.firstaidhero.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.R;
import com.firstaidhero.app.ui.screens.ContactsActivity;
import com.firstaidhero.app.ui.screens.MainActivity;
import com.firstaidhero.app.ui.screens.SettingsActivity;
import com.firstaidhero.app.ui.screens.TreatedCasesActivity;

/** Shared design system for the FirstAid Hero screens. */
public final class Ui {
    public static final int RED = Color.rgb(190, 35, 45);
    public static final int RED_DARK = Color.rgb(145, 23, 32);
    public static final int WHITE = Color.WHITE;
    private static final int LIGHT_BG = Color.rgb(247, 248, 250);
    private static final int DARK_BG = Color.rgb(18, 21, 27);
    private static final int LIGHT_SURFACE = Color.WHITE;
    private static final int DARK_SURFACE = Color.rgb(29, 33, 41);
    private static final int LIGHT_TEXT = Color.rgb(32, 38, 47);
    private static final int DARK_TEXT = Color.rgb(241, 243, 247);
    private static final int LIGHT_MUTED = Color.rgb(105, 113, 124);
    private static final int DARK_MUTED = Color.rgb(174, 181, 192);
    private static final int LIGHT_BORDER = Color.rgb(229, 232, 237);
    private static final int DARK_BORDER = Color.rgb(56, 62, 72);

    private Ui() { }
    public static int dp(Context c, int value) { return (int) (value * c.getResources().getDisplayMetrics().density + 0.5f); }
    public static boolean isDark(Context c) { return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES; }
    public static int background(Context c) { return isDark(c) ? DARK_BG : LIGHT_BG; }
    public static int surface(Context c) { return isDark(c) ? DARK_SURFACE : LIGHT_SURFACE; }
    public static int ink(Context c) { return isDark(c) ? DARK_TEXT : LIGHT_TEXT; }
    public static int muted(Context c) { return isDark(c) ? DARK_MUTED : LIGHT_MUTED; }
    public static int border(Context c) { return isDark(c) ? DARK_BORDER : LIGHT_BORDER; }
    public static int softAccent(Context c) { return isDark(c) ? Color.rgb(62, 35, 41) : Color.rgb(255, 239, 240); }

    public static LinearLayout column(Context c) {
        LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(c, 20), dp(c, 16), dp(c, 20), dp(c, 20)); v.setBackgroundColor(background(c));
        AppPreferences.applyDirection(c, v); return v;
    }
    public static ScrollView scroll(Context c, View child) {
        ScrollView s = new ScrollView(c); s.setFillViewport(true); s.setClipToPadding(false); s.setVerticalScrollBarEnabled(false); s.addView(child); return s;
    }
    public static LinearLayout screen(Context c, View content, String selectedTab) {
        LinearLayout shell = new LinearLayout(c); shell.setOrientation(LinearLayout.VERTICAL); shell.setBackgroundColor(background(c));
        LinearLayout navWrap = new LinearLayout(c); navWrap.setOrientation(LinearLayout.VERTICAL); navWrap.setBackgroundColor(surface(c));
        navWrap.addView(bottomNavigation(c, selectedTab), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View divider = new View(c); divider.setBackgroundColor(border(c));
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        shell.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 1)));
        shell.addView(navWrap, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AppPreferences.applyDirection(c, shell);
        ViewCompat.setOnApplyWindowInsetsListener(shell, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            navWrap.setPadding(0, 0, 0, bars.bottom);
            return new WindowInsetsCompat.Builder(windowInsets).setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE).build();
        });
        return shell;
    }
    public static LinearLayout shell(Context c, View content) {
        LinearLayout shell = new LinearLayout(c); shell.setOrientation(LinearLayout.VERTICAL); shell.setBackgroundColor(background(c));
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        AppPreferences.applyDirection(c, shell);
        ViewCompat.setOnApplyWindowInsetsListener(shell, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        return shell;
    }
    public static void edgeToEdge(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        boolean dark = isDark(activity);
        setSystemBarIcons(activity, !dark, !dark);
    }
    public static void setSystemBarIcons(Activity activity, boolean lightStatus, boolean lightNav) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(lightStatus);
        controller.setAppearanceLightNavigationBars(lightNav);
    }
    private static BottomNavigationView bottomNavigation(Context c, String selected) {
        BottomNavigationView nav = new BottomNavigationView(c);
        nav.setBackgroundColor(surface(c));
        nav.setElevation(0);
        nav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        nav.setItemActiveIndicatorEnabled(true);
        nav.setItemActiveIndicatorColor(ColorStateList.valueOf(softAccent(c)));
        ColorStateList tint = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{RED, muted(c)});
        nav.setItemIconTintList(tint);
        nav.setItemTextColor(tint);
        nav.inflateMenu(R.menu.bottom_nav);
        String[] ids = {"home", "records", "contacts", "settings"};
        int[] resIds = {R.id.nav_home, R.id.nav_records, R.id.nav_contacts, R.id.nav_settings};
        Class<?>[] destinations = {MainActivity.class, TreatedCasesActivity.class, ContactsActivity.class, SettingsActivity.class};
        Menu menu = nav.getMenu();
        for (int i = 0; i < ids.length; i++) {
            MenuItem item = menu.findItem(resIds[i]);
            if (item != null) item.setTitle(I18n.t(c, "tab_" + ids[i]));
        }
        nav.setSelectedItemId(resIds[indexOf(ids, selected)]);
        nav.setOnItemSelectedListener(item -> {
            int index = indexOfResId(resIds, item.getItemId());
            if (index < 0) return false;
            String id = ids[index];
            // NavigationBarView negates this return value: returning true checks the
            // tapped item on THIS screen's bar. Only keep it when the tap is on the
            // screen we are already on; otherwise leave the bar untouched so it still
            // highlights this screen when the activity comes back to the front.
            if (id.equals(selected)) return true;
            if (c instanceof Activity) {
                Intent intent = new Intent(c, destinations[index]);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                c.startActivity(intent);
                ((Activity) c).overridePendingTransition(0, 0);
            }
            return false;
        });
        return nav;
    }
    private static int indexOf(String[] items, String value) { for (int i = 0; i < items.length; i++) if (items[i].equals(value)) return i; return 0; }
    private static int indexOfResId(int[] items, int value) { for (int i = 0; i < items.length; i++) if (items[i] == value) return i; return -1; }

    public static TextView text(Context c, String value, int size, int color, boolean bold) {
        TextView t = new TextView(c); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        Typeface face = Typeface.create(AppPreferences.isArabic(c) ? "serif" : "sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL);
        // Android's serif Arabic fallback uses Noto Naskh, while English remains in the clean sans UI family.
        if (AppPreferences.isArabic(c)) { android.os.LocaleList locales = new android.os.LocaleList(new java.util.Locale("ar")); t.setTextLocales(locales); }
        t.setTypeface(face); t.setGravity(AppPreferences.isArabic(c) ? Gravity.RIGHT : Gravity.LEFT); t.setLineSpacing(dp(c, 3), 1.04f); return t;
    }
    public static TextView centered(Context c, String value, int size, int color, boolean bold) { TextView t = text(c, value, size, color, bold); t.setGravity(Gravity.CENTER); return t; }
    public static TextView card(Context c, String value, int size, int color, boolean bold) {
        TextView t = text(c, value, size, color, bold); t.setPadding(dp(c, 16), dp(c, 15), dp(c, 16), dp(c, 15));
        t.setBackground(rounded(c, surface(c), border(c), 18, 1)); return t;
    }
    public static Button button(Context c, String value, boolean primary) {
        Button b = new Button(c); b.setText(value); b.setAllCaps(false); b.setTextSize(14); b.setTypeface(Typeface.create(AppPreferences.isArabic(c) ? "serif" : "sans-serif", Typeface.BOLD));
        b.setTextColor(primary ? WHITE : ink(c)); b.setMinHeight(dp(c, 46)); b.setPadding(dp(c, 14), dp(c, 7), dp(c, 14), dp(c, 7));
        b.setBackground(rounded(c, primary ? RED : surface(c), primary ? RED : border(c), 14, 1)); b.setElevation(0); return b;
    }
    public static GradientDrawable rounded(Context c, int fill, int stroke, int radiusDp, int strokeWidthDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fill);
        bg.setCornerRadius(dp(c, radiusDp));
        if (strokeWidthDp > 0) bg.setStroke(dp(c, strokeWidthDp), stroke);
        return bg;
    }
    public static EditText input(Context c, String hint, boolean multiline) {
        EditText e = new EditText(c); e.setSingleLine(!multiline); e.setTextSize(16); e.setTextColor(ink(c)); e.setHintTextColor(muted(c));
        e.setHint(hint); e.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12));
        if (multiline) { e.setMinLines(3); e.setGravity(Gravity.TOP | (AppPreferences.isArabic(c) ? Gravity.RIGHT : Gravity.LEFT)); }
        e.setBackground(rounded(c, surface(c), border(c), 15, 1));
        if (AppPreferences.isArabic(c)) e.setTextLocales(new android.os.LocaleList(new java.util.Locale("ar")));
        return e;
    }
    public static LinearLayout.LayoutParams lp(Context c, int width, int height) { return new LinearLayout.LayoutParams(width < 0 ? width : dp(c, width), height < 0 ? height : dp(c, height)); }
    public static void gap(Context c, LinearLayout parent, int heightDp) { View gap = new View(c); parent.addView(gap, new LinearLayout.LayoutParams(1, dp(c, heightDp))); }
    public static void addLabel(Context c, LinearLayout parent, String value, int size, int color, boolean bold, int topGap) {
        if (topGap > 0) gap(c, parent, topGap); TextView t = text(c, value, size, color, bold); t.setPadding(0, dp(c, 2), 0, dp(c, 2));
        parent.addView(t, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    public static void addSectionHeader(Context c, LinearLayout parent, String title, String subtitle) {
        TextView t = text(c, title, 27, ink(c), true); parent.addView(t, lp(c, -1, -2));
        if (subtitle != null && !subtitle.isEmpty()) { TextView sub = text(c, subtitle, 14, muted(c), false); LinearLayout.LayoutParams p = lp(c, -1, -2); p.topMargin = dp(c, 5); parent.addView(sub, p); }
    }
}
