package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.TranslateRequest;
import com.alfarooj.timetable.models.TranslateResponse;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationHelper {
    private static final String PREF_NAME = "translation_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static String currentLanguage = "en";
    // Cache ya tafsiri
    private static Map<String, String> translationCache = new HashMap<>();

    public static void saveLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
        currentLanguage = langCode;
    }

    public static void loadLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en");
    }

    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    // ★★★ HII NDIYO METHOD INAYOKOSEKANA ★★★
    // Synchronous translation - inarudisha original text kama hakuna tafsiri
    public static String translateTextDirect(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (currentLanguage.equals("en")) {
            return text;
        }
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        // Kama haipo kwenye cache, rudisha original na uanze kutafsiri background
        translateTextAsync(text, null);
        return text;
    }

    // Tafsiri ya async - inaita callback mara tafsiri ikipatikana
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
        // Pitia API
        ApiClient.getApiService().translateText(new TranslateRequest(text, currentLanguage))
            .enqueue(new Callback<TranslateResponse>() {
                @Override
                public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String translated = response.body().getTranslated();
                        translationCache.put(cacheKey, translated);
                        if (callback != null) callback.onSuccess(translated);
                    } else {
                        if (callback != null) callback.onError("Translation failed");
                    }
                }
                @Override
                public void onFailure(Call<TranslateResponse> call, Throwable t) {
                    if (callback != null) callback.onError(t.getMessage());
                }
            });
    }

    // Public method ya async translation
    public static void translateText(String text, TranslationCallback callback) {
        translateTextAsync(text, callback);
    }

    // Tafsiri ya TextView na kuupdate mara tu tafsiri inapofika
    public static void translateTextView(TextView textView, String originalText) {
        if (textView == null) return;
        textView.setText(originalText);
        translateTextAsync(originalText, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                if (textView != null && !translatedText.equals(originalText)) {
                    textView.setText(translatedText);
                }
            }
            @Override
            public void onError(String error) {
                // Ikiwa API imeshindwa, acha text asili
            }
        });
    }

    public static void translateButtonText(Button button, String originalText) {
        if (button == null) return;
        button.setText(originalText);
        translateTextAsync(originalText, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                if (button != null && !translatedText.equals(originalText)) {
                    button.setText(translatedText);
                }
            }
            @Override
            public void onError(String error) {}
        });
    }

    public static void translateHint(TextView textView, String originalHint) {
        if (textView == null) return;
        textView.setHint(originalHint);
        translateTextAsync(originalHint, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                if (textView != null && !translatedText.equals(originalHint)) {
                    textView.setHint(translatedText);
                }
            }
            @Override
            public void onError(String error) {}
        });
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }
}
