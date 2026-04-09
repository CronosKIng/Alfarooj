package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

public class LanguageUtils {
    private static final String PREF_NAME = "multilang_pref";
    private static final String KEY_LANG = "app_language";

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, languageCode).apply();
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "en");
    }

    public static void applyLanguage(Context context) {
        String code = getSavedLanguage(context);
        setLocale(context, code);
    }
}
