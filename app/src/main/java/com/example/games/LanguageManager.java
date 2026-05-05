package com.example.games;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager {
    private static final String PREF_NAME = "LanguagePref";
    private static final String KEY_LANG = "lang";
    private final Context context;
    private final SharedPreferences prefs;

    public LanguageManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLanguage(String langCode) {
        prefs.edit().putString(KEY_LANG, langCode).apply();
        updateResource(langCode);
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANG, "th"); // Default to Thai
    }

    public void updateResource(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void toggleLanguage() {
        if (getLanguage().equals("th")) {
            setLanguage("en");
        } else {
            setLanguage("th");
        }
    }
}
