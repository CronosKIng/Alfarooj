package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
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
    private static Map<String, String> translationCache = new HashMap<>();
    private static String currentLanguage = "en";
    private static Map<String, String> pendingTranslations = new HashMap<>();

    public static void saveLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
        currentLanguage = langCode;
        // Clear cache when language changes
        translationCache.clear();
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

    // DIRECT TRANSLATION - inarudisha String moja kwa moja
    public static String translateTextDirect(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        // Kama iko kwenye pending, rudisha text asili kwa sasa
        if (pendingTranslations.containsKey(cacheKey)) {
            return text;
        }
        
        // Tumia API kwa translation
        pendingTranslations.put(cacheKey, text);
        
        ApiClient.getApiService().translateText(new TranslateRequest(text, currentLanguage))
            .enqueue(new Callback<TranslateResponse>() {
                @Override
                public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String translated = response.body().getTranslated();
                        translationCache.put(cacheKey, translated);
                        pendingTranslations.remove(cacheKey);
                    }
                }
                
                @Override
                public void onFailure(Call<TranslateResponse> call, Throwable t) {
                    pendingTranslations.remove(cacheKey);
                }
            });
        
        return text;
    }

    public static void translateTextView(TextView textView, String originalText) {
        if (textView == null) return;
        String translated = translateTextDirect(originalText);
        textView.setText(translated);
    }

    public static void translateButtonText(android.widget.Button button, String originalText) {
        if (button == null) return;
        String translated = translateTextDirect(originalText);
        button.setText(translated);
    }

    public static void translateHint(TextView textView, String originalHint) {
        if (textView == null) return;
        String translated = translateTextDirect(originalHint);
        textView.setHint(translated);
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }
}
