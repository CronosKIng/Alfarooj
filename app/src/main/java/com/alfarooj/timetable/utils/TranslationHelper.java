package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.TranslateRequest;
import com.alfarooj.timetable.models.TranslateResponse;
import com.google.android.material.navigation.NavigationView;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationHelper {
    private static final String PREF_NAME = "translation_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String CACHE_PREF_NAME = "translation_cache";
    private static String currentLanguage = "en";
    private static Map<String, String> translationCache = new HashMap<>();
    private static SharedPreferences cachePrefs;

    public static void init(Context context) {
        if (cachePrefs == null && context != null) {
            cachePrefs = context.getSharedPreferences(CACHE_PREF_NAME, Context.MODE_PRIVATE);
            loadCachedTranslations();
        }
    }

    private static void loadCachedTranslations() {
        if (cachePrefs != null) {
            Map<String, ?> allEntries = cachePrefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                translationCache.put(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    private static void saveToCache(String key, String value) {
        translationCache.put(key, value);
        if (cachePrefs != null) {
            cachePrefs.edit().putString(key, value).apply();
        }
    }

    public static void clearCache() {
        translationCache.clear();
        if (cachePrefs != null) {
            cachePrefs.edit().clear().apply();
        }
    }

    public static void saveLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
        currentLanguage = langCode;
        init(context);
    }

    public static void loadLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en");
        init(context);
    }

    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static String translateTextDirect(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        translateTextAsync(text, null);
        return text;
    }

    public static void translateText(String text, TranslationCallback callback) {
        translateTextAsync(text, callback);
    }

    private static void translateTextAsync(String text, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) callback.onSuccess(text);
            return;
        }
        if (currentLanguage.equals("en")) {
            if (callback != null) callback.onSuccess(text);
            return;
        }
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            if (callback != null) callback.onSuccess(translationCache.get(cacheKey));
            return;
        }
        ApiClient.getApiService().translateText(new TranslateRequest(text, currentLanguage))
            .enqueue(new Callback<TranslateResponse>() {
                @Override
                public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String translated = response.body().getTranslated();
                        saveToCache(cacheKey, translated);
                        if (callback != null) callback.onSuccess(translated);
                    } else {
                        if (callback != null) callback.onError("Failed");
                    }
                }
                @Override
                public void onFailure(Call<TranslateResponse> call, Throwable t) {
                    if (callback != null) callback.onError(t.getMessage());
                }
            });
    }

    // Tafsiri View yote (recursive)
    public static void translateViewGroup(ViewGroup viewGroup) {
        if (viewGroup == null) return;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                String text = tv.getText().toString();
                String hint = tv.getHint() != null ? tv.getHint().toString() : "";
                if (!TextUtils.isEmpty(text)) {
                    translateText(text, new TranslationCallback() {
                        @Override
                        public void onSuccess(String translated) {
                            tv.setText(translated);
                        }
                        @Override
                        public void onError(String error) {}
                    });
                }
                if (!TextUtils.isEmpty(hint)) {
                    translateText(hint, new TranslationCallback() {
                        @Override
                        public void onSuccess(String translated) {
                            tv.setHint(translated);
                        }
                        @Override
                        public void onError(String error) {}
                    });
                }
            } else if (child instanceof Button) {
                Button btn = (Button) child;
                String text = btn.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    translateText(text, new TranslationCallback() {
                        @Override
                        public void onSuccess(String translated) {
                            btn.setText(translated);
                        }
                        @Override
                        public void onError(String error) {}
                    });
                }
            } else if (child instanceof EditText) {
                EditText et = (EditText) child;
                String hint = et.getHint() != null ? et.getHint().toString() : "";
                if (!TextUtils.isEmpty(hint)) {
                    translateText(hint, new TranslationCallback() {
                        @Override
                        public void onSuccess(String translated) {
                            et.setHint(translated);
                        }
                        @Override
                        public void onError(String error) {}
                    });
                }
            } else if (child instanceof Spinner) {
                // Spinner inashughulikiwa tofauti
            } else if (child instanceof ViewGroup) {
                translateViewGroup((ViewGroup) child);
            }
        }
    }

    // Tafsiri Toolbar title
    public static void translateToolbar(Toolbar toolbar) {
        if (toolbar != null && toolbar.getTitle() != null) {
            String title = toolbar.getTitle().toString();
            translateText(title, new TranslationCallback() {
                @Override
                public void onSuccess(String translated) {
                    toolbar.setTitle(translated);
                }
                @Override
                public void onError(String error) {}
            });
        }
    }

    // Tafsiri NavigationView menu
    public static void translateNavigationView(NavigationView navView) {
        if (navView == null) return;
        android.view.Menu menu = navView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            android.view.MenuItem item = menu.getItem(i);
            String title = item.getTitle().toString();
            translateText(title, new TranslationCallback() {
                @Override
                public void onSuccess(String translated) {
                    item.setTitle(translated);
                }
                @Override
                public void onError(String error) {}
            });
        }
    }

    // Tafsiri Menu ya Options
    public static void translateMenu(android.view.Menu menu) {
        if (menu == null) return;
        for (int i = 0; i < menu.size(); i++) {
            android.view.MenuItem item = menu.getItem(i);
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if (!TextUtils.isEmpty(title)) {
                translateText(title, new TranslationCallback() {
                    @Override
                    public void onSuccess(String translated) {
                        item.setTitle(translated);
                    }
                    @Override
                    public void onError(String error) {}
                });
            }
        }
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }
}
