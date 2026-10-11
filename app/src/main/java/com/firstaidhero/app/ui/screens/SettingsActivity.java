package com.firstaidhero.app.ui.screens;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.core.I18n;
import com.firstaidhero.app.ui.Ui;

public class SettingsActivity extends BaseActivity {
    private boolean ready;
    @Override protected void onCreate(Bundle state) { super.onCreate(state); render(); }
    private void render() {
        ready = false;
        LinearLayout content = Ui.column(this); Ui.addSectionHeader(this, content, I18n.t(this, "settings"), I18n.t(this, "settings_hint"));
        LinearLayout settingsCard = new LinearLayout(this); settingsCard.setOrientation(LinearLayout.VERTICAL); settingsCard.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 10));
        settingsCard.setBackground(Ui.rounded(this, Ui.surface(this), Ui.border(this), 20, 1));
        addDropdownRow(settingsCard, "language", new String[]{I18n.t(this,"language_en"), I18n.t(this,"language_ar")}, AppPreferences.isArabic(this) ? 1 : 0, position -> {
            boolean arabic = position == 1;
            if (arabic != AppPreferences.isArabic(this)) { AppPreferences.setArabic(this, arabic); recreate(); }
        });
        View divider = new View(this); divider.setBackgroundColor(Ui.border(this)); settingsCard.addView(divider, new LinearLayout.LayoutParams(-1, Ui.dp(this, 1)));
        String[] themes = {I18n.t(this,"theme_system"), I18n.t(this,"theme_light"), I18n.t(this,"theme_dark")};
        String current = AppPreferences.getThemeMode(this); int selected = AppPreferences.THEME_LIGHT.equals(current) ? 1 : AppPreferences.THEME_DARK.equals(current) ? 2 : 0;
        addDropdownRow(settingsCard, "appearance", themes, selected, position -> {
            String mode = position == 1 ? AppPreferences.THEME_LIGHT : position == 2 ? AppPreferences.THEME_DARK : AppPreferences.THEME_SYSTEM;
            if (!mode.equals(AppPreferences.getThemeMode(this))) {
                AppPreferences.setThemeMode(this, mode);
                AppCompatDelegate.setDefaultNightMode(AppPreferences.toNightMode(mode));
            }
        });
        LinearLayout.LayoutParams cardLp = Ui.lp(this, -1, -2); cardLp.topMargin = Ui.dp(this, 22); content.addView(settingsCard, cardLp);
        TextView note = Ui.text(this, I18n.t(this, "saved_on_device"), 13, Ui.muted(this), false); LinearLayout.LayoutParams noteLp = Ui.lp(this, -1, -2); noteLp.topMargin = Ui.dp(this, 12); content.addView(note, noteLp);
        AppPreferences.applyDirection(this, content); setContentView(Ui.screen(this, Ui.scroll(this, content), "settings"));
        content.post(() -> ready = true);
    }
    private void addDropdownRow(LinearLayout parent, String titleKey, String[] options, int selected, SelectionListener listener) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
        TextView title = Ui.text(this, I18n.t(this, titleKey), 15, Ui.ink(this), true); row.addView(title);
        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN); ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinner.setAdapter(adapter); spinner.setSelection(selected, false);
        spinner.setBackground(Ui.rounded(this, Ui.background(this), Ui.border(this), 13, 1));
        LinearLayout.LayoutParams sp = Ui.lp(this, -1, 48); sp.topMargin = Ui.dp(this, 8); row.addView(spinner, sp);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) { if (ready) listener.onSelected(position); }
            @Override public void onNothingSelected(AdapterView<?> parentView) { }
        });
        parent.addView(row, Ui.lp(this, -1, -2));
    }
    private interface SelectionListener { void onSelected(int position); }
}
