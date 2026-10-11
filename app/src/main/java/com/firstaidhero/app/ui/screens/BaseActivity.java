package com.firstaidhero.app.ui.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.firstaidhero.app.core.AppPreferences;
import com.firstaidhero.app.ui.Ui;

/** Applies persisted appearance settings consistently across app screens. */
public abstract class BaseActivity extends AppCompatActivity {
    private boolean languageAtCreation;
    private boolean created;
    @Override protected void onCreate(Bundle state) {
        AppCompatDelegate.setDefaultNightMode(AppPreferences.toNightMode(AppPreferences.getThemeMode(this)));
        super.onCreate(state);
        Ui.edgeToEdge(this);
        languageAtCreation = AppPreferences.isArabic(this);
        created = true;
    }
    @Override protected void onResume() {
        super.onResume();
        if (created && languageAtCreation != AppPreferences.isArabic(this)) {
            languageAtCreation = AppPreferences.isArabic(this);
            recreate();
        }
    }
}
