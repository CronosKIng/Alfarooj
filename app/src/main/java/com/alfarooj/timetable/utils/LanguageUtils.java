package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class LanguageUtils {
    private static final String PREF_NAME = "language_pref";
    private static final String KEY_LANGUAGE = "selected_language";

    public static void setLocale(Context context, String languageCode) {
        Locale locale;
        if (languageCode.contains("-")) {
            String[] parts = languageCode.split("-");
            locale = new Locale(parts[0], parts[1]);
        } else {
            locale = new Locale(languageCode);
        }
        
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public static void applyLanguage(Context context) {
        String languageCode = getSavedLanguage(context);
        setLocale(context, languageCode);
    }
    
    public static String[] getAllLanguages() {
        return new String[]{
            "English", "Kiswahili", "Arabic", "Hindi", "Chinese", "Spanish", "French", 
            "German", "Italian", "Portuguese", "Russian", "Japanese", "Korean", 
            "Dutch", "Greek", "Turkish", "Urdu", "Bengali", "Tamil", "Telugu"
        };
    }
    
    public static String[] getAllLanguageCodes() {
        return new String[]{
            "en", "sw", "ar", "hi", "zh", "es", "fr", 
            "de", "it", "pt", "ru", "ja", "ko", 
            "nl", "el", "tr", "ur", "bn", "ta", "te"
        };
    }
}
